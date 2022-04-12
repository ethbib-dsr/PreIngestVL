package com.exlibris.dps.submissionvl.helper;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.ConfigProperties;
import com.exlibris.dps.submissionvl.util.FileHandler;
import com.exlibris.dps.submissionvl.util.SourceSip;
import com.exlibris.dps.submissionvl.xml.MetsReader;


/**
 * Class model for source file integrity checking
 * runs all necessary checks
 * 
 * @author Lars Haendler
 *
 */
public class SourceFileIntegrityChecker
{
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private boolean integrity = true;
	private ConfigProperties config;
	private File currentSipFile;
	private SourceSip currentSingleSip;
	private FileHandler fh;
	private File extractedSip;
	private Set<String> integrityErrors;
	
	/**
	 * Constructor that also kicks off the integrity checks
	 * 
	 * @param sipFile
	 * @param singleSip
	 */
	public SourceFileIntegrityChecker(File sipFile, SourceSip singleSip, ConfigProperties conf)
	{
		currentSipFile = sipFile;
		currentSingleSip = singleSip;
		config = conf;
		fh = new FileHandler(currentSipFile, config);
		extractedSip = new File(fh.getExtractSystemIDPath());
		integrityErrors = new HashSet<String>();
	}
	
	
	/**
	 * Orchestrates individual checks
	 * 
	 */
	public void runIntegrityCheck()
	{
		checkDirectoryIntegrity();
		checkRootFileIntegrity(); 
		checkMetsExists();
		checkImagesIntegrity();
		checkIdSectionExists();
	}

	
	/**
	 * Check if sub directory structure contains only those folders
	 * specified in config.properties.
	 * 
	 */
	private void checkDirectoryIntegrity()
	{
		//set of all sub directores 
		Set<String> subDirectorySet = new HashSet<String>(Arrays.asList(FileOperations.getSubdirectories(extractedSip)));
		
		//remove all sub directories that are alloed
		subDirectorySet.remove(FileOperations.removeSlashes(config.getSipImageDirectory()));		
		// DDE-540
		List<String> textFileDirectories = config.getSipTextDirectories();
		for (String textDir : textFileDirectories) {
			subDirectorySet.remove(FileOperations.removeSlashes(textDir));
		}
		
		//if still sub directories left they are not allowed
		if(subDirectorySet.size()>0)
		{
			setIntegrity(false);
			setDbStatusNotice(config.getDbStatusIntegrityWrongStructure());
			logger.warn("Unknown directories found: " + subDirectorySet.toString()); //put into config
		}		
	}
	
	
	/**
	 * Check if sub directory structure contains only those files
	 * specified in config.properties
	 * 
	 */
	private void checkRootFileIntegrity()
	{
		Set<String> filesDirectorySet = new HashSet<String>(Arrays.asList(FileOperations.getFilenamesFromDirectory(extractedSip)));
		filesDirectorySet.remove(config.getMetsFileName());		
		
		if(filesDirectorySet.size()>0)
		{
			setIntegrity(false);
			setDbStatusNotice(config.getDbStatusIntegrityWrongStructure());
			logger.warn("Unknown files found: " + filesDirectorySet.toString()); //put into config
		}
		
	}
	
	
	/**
	 * Handle if export_mets.xml does not exist
	 * 
	 */
	private void checkMetsExists()
	{
		if(!metsExists())
		{
			setIntegrity(false);
			setDbStatusNotice(config.getDbStatusIntegrityMissingMets());
			logger.warn(config.getIntegrityMissingMets());
		}
	}

	
	/**
	 * Check whether or not all files have the correct file extension
	 * 
	 */
	private void checkImagesIntegrity()
	{
		List<String> allowedImageFileEndings = config.getAllowedImageFileEndings();
		String[] imageNameArray = fh.getImageFilesArray();
		
		if(imageNameArray != null)
		{
			for (String singleFileName : imageNameArray)
			{
				if (!allowedImageFileEndings.contains(FileHandler.getFileExtension(singleFileName)))
				{
					setIntegrity(false);
					setDbStatusNotice(config.getDbStatusIntegrityWrongFiles());
					logger.warn(config.getIntegrityWrongFiles() + " " + singleFileName);
				}
			}			
		}			
	}
	
	
	/**
	 * Check whether or not a xml section with the current ID exists 
	 * 
	 */
	private void checkIdSectionExists()
	{
		//only check section if file containing section exists
		if(metsExists())
		{
			MetsReader mr = new MetsReader(fh.getMetsFile().getAbsolutePath(), config);
			
			if(!mr.hasIdSection(currentSingleSip))
			{
				setIntegrity(false);
				setDbStatusNotice(config.getDbStatusIdNotinMets());
				logger.warn(config.getIntegrityInvalidId() + " " + currentSingleSip.getFileName());
			} 
		}
	}
	
	
	/**
	 * Check if export_mets.xml exists 
	 * 
	 * @return
	 */
	private boolean metsExists()
	{
		Set<String> filesDirectorySet = new HashSet<String>(Arrays.asList(FileOperations.getFilenamesFromDirectory(extractedSip)));
		
		return filesDirectorySet.contains(config.getMetsFileName());
	}	
	
	
	/**
	 * Setter / appender for dbStatusNotice
	 * If is already has something in it
	 * a comma is prepended
	 * 
	 * @param notice
	 */
	private void setDbStatusNotice(String notice)
	{
		integrityErrors.add(notice);
	}
	
	
	/**
	 * Setter for integrity 
	 * 
	 * @param status
	 */
	private void setIntegrity(boolean status)
	{
		integrity = status;
	}
	
	
	/**
	 * Getter for instance integrity variable
	 * 
	 * @return
	 */
	public boolean getIntegrity()
	{
		return integrity;
	}
	
	
	/**
	 * Getter for instance dbStatusNotice variable
	 * 
	 * @return
	 */
	public String getDbStatusNotice()
	{
		return integrityErrors.toString().replace("[", "").replace("]", "");
	}

}
