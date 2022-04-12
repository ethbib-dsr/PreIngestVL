package com.exlibris.dps.submissionvl.xml;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.exlibris.digitool.common.dnx.DnxDocument;
import com.exlibris.digitool.common.dnx.DnxDocumentFactory;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.CMS;
import com.exlibris.digitool.common.dnx.DnxDocumentHelper.GeneralIECharacteristics;

import com.exlibris.core.sdk.consts.Enum;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.parser.IEParserException;

import com.exlibris.dps.sdk.deposit.IEParser;
import com.exlibris.dps.sdk.deposit.IEParserFactory;
import com.exlibris.dps.submissionvl.ConfigProperties;
import com.exlibris.dps.submissionvl.util.FileHandler;
import com.exlibris.dps.submissionvl.util.SourceSip;
import com.exlibris.dps.submissionvl.util.SourceSip.CapsuleTypeEnum;

import gov.loc.mets.FileType;
import gov.loc.mets.MetsType.FileSec.FileGrp;

/**
 * IEBuilder Object
 * 
 * contains all behaviour to construct the content for IE.xml
 * based on data from SIP and export_mets.xml
 * 
 * @author Lars Haendler
 * 
 */
public class IEBuilder
{
	IEParser ie = null;
	Mets mets;
	FileHandler fileHandler;
	ConfigProperties config;
	SourceSip currentSip;
	boolean hasImageFiles;
	boolean hasTextFiles;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * constructor
	 * 
	 * @param Mets mets
	 * @param FileHandler fileHandler
	 * @param ConfigProperties config
	 */
	public IEBuilder(Mets mets, FileHandler fileHandler, ConfigProperties config, SourceSip currentSip, boolean hasImageFiles, boolean hasTextFiles)
	{
		this.mets = mets;
		this.fileHandler = fileHandler;
		this.config = config;
		this.hasImageFiles = hasImageFiles;
		this.hasTextFiles = hasTextFiles;
		this.currentSip = currentSip;
	}
	
	
	/**
	 * main method the creates the ie.xml and returns the xml as String
	 * 
	 * @return
	 */
	public String runBuilder()
	{
		String xml = "";
		
		try
		{
			//builder for ie files ... very long  :-(
			//but there seems to be no other using the classes EL provided
			
			//deactivate logging
			org.apache.log4j.helpers.LogLog.setQuietMode(true);
	
			
			//set up IE
			ie = IEParserFactory.create();

			//add DC data
			sneakInLog("generate DC section");
			ie.setIEDublinCore(getDcForIe());
			
			//add CMS dnx data
			sneakInLog("generate CMS DNX section with CMS and generalIEChar subsections");
			ie.setIeDnx(getCmsDnxDocument());
			
			//current file group
			@SuppressWarnings("deprecation")
			FileGrp fGrp = ie.addNewFileGrp(Enum.UsageType.VIEW, Enum.PreservationType.PRESERVATION_MASTER);
			
			//DNX helper
			DnxDocument dnxDocument = ie.getFileGrpDnx(fGrp.getID());
			DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument);
			
			//DNX generalRepCharacteristics secion
			documentHelper.getGeneralRepCharacteristics().setRevisionNumber(config.getXmlGeneralRevNo());
			documentHelper.getGeneralRepCharacteristics().setDigitalOriginal(config.getXmlGeneralDigOrig());
			
			//add all current DNX sections to IE
			ie.setFileDnx(documentHelper.getDocument(), fGrp.getID());

			//fixity list, has to be kept separately
			List<DnxDocumentHelper.FileFixity> fixityList = new ArrayList<DnxDocumentHelper.FileFixity>();			

			
			//run only if image files exist in SIP
			if(hasImageFiles)
			{
				List<MetsFileList> metsFiles = mets.getFileList();
				
				Map<String, String> imageNameHashPair = fileHandler.getImageHashPairMap();
				
				//iterate over all files in mets
				for(MetsFileList metsFileList : metsFiles)
				{
					//if file in mets does exist in file system from SIP
					if(imageNameHashPair.containsKey(metsFileList.getFilename()))
					{
						//add new file third paramter is used in mets:FLocat as value for xlin:href
						FileType ft = ie.addNewFile(fGrp, metsFileList.getMimetype(), metsFileList.getFilepath(), metsFileList.getFilename());
						
						documentHelper.getGeneralRepCharacteristics().setLabel(metsFileList.getFilename());
						
						//DNX genereal file characteristics per file
						DnxDocument fileDnx = ie.getFileDnx(ft.getID());
						DnxDocumentHelper fileDnxHelper = getDnxHelperForImage(fileDnx, metsFileList);
						
						//DNX MD5-fixity per file
						DnxDocumentHelper.FileFixity fFixityMD5 = fileDnxHelper.new FileFixity("", "", config.getXmlFixityAlgorithMd5(), imageNameHashPair.get(metsFileList.getFilename()));
						fixityList.add(fFixityMD5);
						
						if (metsFileList.getChecksumtype().equals(config.getXmlFixityAlgorithSha1()) )
							{
							//DNX SHA-1-fixity per file
							DnxDocumentHelper.FileFixity fFixitySHA1 = fileDnxHelper.new FileFixity("REG_SA_JAVA5_FIXITY", "", metsFileList.getChecksumtype(), metsFileList.getChecksum());
							fixityList.add(fFixitySHA1);
							logger.debug("Checksum read from source " + metsFileList.getFilename()+": " + metsFileList.getChecksumtype() + " = " + metsFileList.getChecksum());
							}
						//DnxDocumentHelper.FileFixity fFixity2 = fileDnxHelper.new FileFixity("REG_SA_JAVA5_FIXITY", "", "[algorithm] 2", "[checksum] 2");
						//fixityList.add(fFixity2);
						
						ie.setFileDnx(fileDnxHelper.getDocument(), ft.getID());
					}
				}				
			}

			//run only if fulltext files exist in SIP
			if(hasTextFiles)
			{				
				List<MetsFileList> metsFiles = mets.getFileList();
				
				Map<String, String> textNameHashPair = fileHandler.getTextHashPairMap();
				
				//iterate over all files in mets
				for(MetsFileList metsFileList : metsFiles)
				{
					if (metsFileList.filepathIsContained(config.getSipTextDirectories()))
					{
						//if file in mets does exist in file system from SIP
						if(textNameHashPair.containsKey(metsFileList.getFilename()))
						{
							//add new file third paramter is used in mets:FLocat as value for xlin:href
							FileType ft = ie.addNewFile(fGrp, metsFileList.getMimetype(), metsFileList.getFilepath(), metsFileList.getFilename());
							
							documentHelper.getGeneralRepCharacteristics().setLabel(metsFileList.getFilename());
							
							//DNX genereal file characteristics per file
							DnxDocument fileDnx = ie.getFileDnx(ft.getID());
							DnxDocumentHelper fileDnxHelper = getDnxHelperForImage(fileDnx, metsFileList);
							
							//DNX MD5-fixity per file
							DnxDocumentHelper.FileFixity fFixityMD5 = fileDnxHelper.new FileFixity("", "", config.getXmlFixityAlgorithMd5(), textNameHashPair.get(metsFileList.getFilename()));
							fixityList.add(fFixityMD5);
							//DnxDocumentHelper.FileFixity fFixity2 = fileDnxHelper.new FileFixity("REG_SA_JAVA5_FIXITY", "", "[algorithm] 2", "[checksum] 2");
							//fixityList.add(fFixity2);
							
							ie.setFileDnx(fileDnxHelper.getDocument(), ft.getID());
						}
					}
				}
			}
		
			//Builder for mets file entry
			FileType ft = ie.addNewFile(fGrp, config.getXmlMetsFileType(), currentSip.getCapsuleID() + ConfigProperties.getFileSeparator() + config.getMetsFileName(), config.getMetsFileName());
			
			//DNX genereal file characteristics per file
			DnxDocument fileDnx = ie.getFileDnx(ft.getID());
			DnxDocumentHelper fileDnxHelper = getDnxHelperForMets(fileDnx);
			
			DnxDocumentHelper.FileFixity fFixity = fileDnxHelper.new FileFixity("", "", config.getXmlFixityAlgorithMd5(), fileHandler.getMd5Hash(fileHandler.getMetsFile().getAbsolutePath()));
			fixityList.add(fFixity);
			
			ie.setFileDnx(fileDnxHelper.getDocument(), ft.getID());
			ie.generateStructMap(null, null, "Table of Contents");
			
			xml = ie.toXML();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			System.exit(0);
		}

		return xml;
	}
	
	
	/**
	 * Helper method to add log messages of type Debug
	 * 
	 * background: log messages have to be suppressed while creating IE
	 * because Ex Libris implementation is full of error and warning 
	 * messages
	 * 
	 * @param message
	 */
	private void sneakInLog(String message)
	{
		sneakInLog(message, "DEBUG");
	}
	
	
	/**
	 * Activates log mode, adds message with correct log level
	 * and deactivates log mode again
	 * Needed to suppress all other errors and warning due to 
	 * bad implementation of IEParserFactory 
	 * 
	 * @param message
	 * @param level
	 */
	private void sneakInLog(String message, String level)
	{
		org.apache.log4j.helpers.LogLog.setQuietMode(false);
		
		switch(level) 
		{
			case "INFO" : 
				logger.info(message);
				break;
			case "WARN" :
				logger.warn(message);
				break;
			case "ERROR" :
				logger.error(message);
				break;
			default :
				logger.debug(message);
				break;
		}
		
		org.apache.log4j.helpers.LogLog.setQuietMode(true);
	}

		
	/**
	 * returns a DnxDocument object filled with record id and system of CMS secion
	 * and entitytype of generalIECharacteristics section
	 * 
	 * @return DnxDocument
	 */
	private DnxDocument getCmsDnxDocument()
	{
		DnxDocument ieDnx = DnxDocumentFactory.getInstance().createDnxDocument();
		DnxDocumentHelper ieDnxHelper = new DnxDocumentHelper(ieDnx);


		// DDE-796. DOIs capsules will no longer have an aleph-id, but an alma-id, therefore
		// the CMS section makes no sense and is no longer part in ie.xml
		/*
		if (currentSip.getCapsuleType() == CapsuleTypeEnum.ALEPH) {
			//CMS secion
			CMS cms = ieDnxHelper.new CMS();
			cms.setSystem(config.getXmlCmsSystem());			
			cms.setRecordId(mets.getRecordIdentifier());
		}*/
		
		//using the setCMS method will create all CMS nodes, those not filled will created empty
		//ieDnxHelper.setCMS(cms);
		
		//generalIECharacteristics section
		GeneralIECharacteristics generalIEChar = ieDnxHelper.new GeneralIECharacteristics();
		generalIEChar.setIEEntityType(config.getXmlGeneralIeEntitytype());
		//using the setGeneralIECharacteristics method will create all generalIECharacteristics nodes, 
		//those not filled will created empty
		//ieDnxHelper.setGeneralIECharacteristics(generalIEChar);
		
		//create return object
		DnxDocument dnxDoc = ieDnxHelper.getDocument();		
		
		return dnxDoc;
	}
	
	
	/**
	 * returns a DnxDocumentHelper filled with data for one file from a MetsFileList
	 * 
	 * @param dnxDoc helper to construct DnxDocumentHelper
	 * @param metsFileList object holding all data for one file
	 * @return DnxDocumentHelper for one file
	 */
	private DnxDocumentHelper getDnxHelperForImage(DnxDocument dnxDoc, MetsFileList metsFileList)
	{
		DnxDocumentHelper fileDnxHelper = new DnxDocumentHelper(dnxDoc);
		
		fileDnxHelper.getGeneralFileCharacteristics().setLabel(metsFileList.getFilename());
		fileDnxHelper.getGeneralFileCharacteristics().setFileOriginalName(metsFileList.getFilename());
		fileDnxHelper.getGeneralFileCharacteristics().setFileOriginalPath(metsFileList.getFilepath());
		
		return fileDnxHelper;
	}
	
	
	/**
	 * returns a DnxDocumentHelper filled with data for mets_export.xml
	 * 
	 * @param dnxDoc helper to construct DnxDocumentHelper
	 * @return DnxDocumentHelper for mets_export.xml
	 */
	private DnxDocumentHelper getDnxHelperForMets(DnxDocument dnxDoc)
	{
		DnxDocumentHelper metsDnxHelper = new DnxDocumentHelper(dnxDoc);

		metsDnxHelper.getGeneralFileCharacteristics().setLabel(config.getMetsFileName());
		metsDnxHelper.getGeneralFileCharacteristics().setFileOriginalName(config.getMetsFileName());
		metsDnxHelper.getGeneralFileCharacteristics().setFileOriginalPath(currentSip.getCapsuleID() + ConfigProperties.getFileSeparator() + config.getMetsFileName());		
		
		return metsDnxHelper;
	}
	
	
	/**
	 * returns DublinCore object filled with correct data for SIP
	 * 
	 * @return DublinCore
	 * @throws IEParserException
	 */
	private DublinCore getDcForIe() throws IEParserException
	{
		DublinCore dc = null;
		
		dc = ie.getDublinCoreParser();
		dc.addElement(config.getXmlDcTitle(), fileHandler.getCurrentFileName());
		dc.addElement(config.getXmlDcTermsIsPart(), isPartOfBuilder());		
		dc.addElement(config.getXmlDcIdentifier(), mets.getRecordIdentifier());
		dc.addElement(config.getXmlDcRelation(), mets.getDoi());
		dc.addElement(config.getXmlDcDate(), fileHandler.getFormattedDate(config.getXmlDcDateFormatting()));
		dc.addElement(config.getXmlDcTermsAlternative(), mets.getAlternativeTitle());
		dc.addElement(config.getXmlDcSource(), mets.getLocation());
	
		return dc;
	}
	
	
	/**
	 * return String with content for "dcterms:isPartOf"
	 * 
	 * @return String
	 */
	private String isPartOfBuilder()
	{
		return config.getXmlDomainRoot()
					+ config.getXmlPathDivider()
					+ config.getXmlDcInstitute()
					+ config.getXmlPathDivider()					
					+ mets.getRecordIdentifier();
	}

}
