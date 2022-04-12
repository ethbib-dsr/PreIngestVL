package com.exlibris.dps.submissionvl.xml;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exlibris.dps.submissionvl.ConfigProperties;
import com.exlibris.dps.submissionvl.util.SourceSip;
import com.exlibris.dps.submissionvl.util.SourceSip.CapsuleTypeEnum;

/**
 * MetsReader Object
 *
 * Responsible for extracting information from a export_mets.xml file
 * and to provide a usable Mets object
 *
 * @author Lars Haendler
 *
 */
public class MetsReader
{
	// DDE-796, analysing and parsing the mets is done with a different approach
	// these constants are arguments for finding the elements in dom
	final static String STRUCT_LINK            = "mets:structLink";
	final static String STRUCT_LINK_TO         = "xlink:to";
	final static String STRUCT_LINK_FROM       = "xlink:from";
	final static String STRUCT_LINK_PHYSROOT   = "physroot";
	final static String STRUCT_MAP             = "mets:structMap";
	final static String STRUCT_MAP_SRCH_ATTR   = "TYPE";
	final static String STRUCT_MAP_SRCH_VALUE  = "LOGICAL";
	final static String STRUCT_MAP_KEY_ATTR    = "ID";
	final static String STRUCT_MAP_RETURN_ATTR = "DMDID";
	final static String DMD_SECTION            = "mets:dmdSec";
	final static String DMD_SECTION_ID_ATTR    = "ID";
	
	private final Logger logger = Logger.getLogger(this.getClass());

	private ConfigProperties config;
	private String metsFilePath;
	private Mets mets;
	private Document dom;


	/**
	 * Constructor that needs the path to the mets file as initial argument
	 *
	 * @param String filePath
	 */
	public MetsReader(String filePath, ConfigProperties config)
	{
		this.config = config;
		setMetsFilePath(filePath);
		mets = new Mets();
		dom = createDOM();
	}

	/**
	 * Check whether or not the mets contains a section that
	 * has the supplied record identifier
	 *
	 * @param recordIdentifier
	 * @return boolean
	 * @throws Exception 
	 */
	public boolean hasIdSection(SourceSip singleSip) 
	{
		boolean returnVal = false;
		
		String sectionID;
		
		try {
			//sectionID = getDMSecWithItemInfo();
			// DDE-881: to iterate over all dmdSec nodes and find the right one with the metadata we need, we have to pass singleSip
			sectionID = getDMSecWithItemInfo(singleSip);
			
			if (singleSip.getCapsuleType() == CapsuleTypeEnum.ALEPH) {
				returnVal = singleSip.getCapsuleID().equals(extractRecordIdentifier(sectionID));				
			} else if (singleSip.getCapsuleType() == CapsuleTypeEnum.DOI) {
				returnVal = singleSip.getDOI().equals(extractDoi(sectionID));				
			} else {
				logger.warn("Could not check for id section. Unknown capsule type for "+singleSip.getFileName());
			}
		} catch (Exception e) {
			logger.warn("Exception occured when querying section id: "+e.getMessage());
		}


		return returnVal;
	}

	
	//DDE-881: new version, where all dmdSec are parsed for the correct alephID or DOI	
	private String getDMSecWithItemInfo(SourceSip singleSip) throws Exception {
	
		NodeList dmdSections = null;	
		String dmdSecID = "";
		
		dmdSections = dom.getElementsByTagName(DMD_SECTION);
		
		for(int i=0; i<dmdSections.getLength(); i++)
		{
			Node node = dmdSections.item(i);
			String sectionID = node.getAttributes().getNamedItem(DMD_SECTION_ID_ATTR).getTextContent();
			
			if (singleSip.getCapsuleType() == CapsuleTypeEnum.ALEPH) {
				if (singleSip.getCapsuleID().equals(extractRecordIdentifier(sectionID))) {
					dmdSecID = sectionID;
					break;
				}
			} else if (singleSip.getCapsuleType() == CapsuleTypeEnum.DOI) {
				if (singleSip.getDOI().equals(extractDoi(sectionID))) {
					dmdSecID = sectionID;
					break;
				}
			} else {
				throw new Exception("MetsReader.getDMSecWithItemInfo: Capsule type not valid");
			}
		}	
		
		if (dmdSecID.isEmpty()) {
			throw new Exception("MetsReader.getDMSecWithItemInfo: No dmdSec ID found.");
		}
		
		return dmdSecID;
	}	
	
	// start: new way of parsing mets
	
	/*private String getDMSecWithItemInfo() throws Exception 
	{		
		return getDMSecIDFromLogicalStructMap(getStructMapIDFromStructLink());		
	}
	
	private String getDMSecIDFromLogicalStructMap(String idValue) throws Exception {
		
		String dmdSecID = "";
		
		Node logicalStructMap = getLogicalStructMap();
		
		
		if (logicalStructMap == null) {
			throw new Exception("MetsReader: Logical struct map not found!");
		} else {		
			if (idValue != null) {
				dmdSecID = getStructMapChildWithID(logicalStructMap, idValue);
				if (dmdSecID == null) {
					throw new Exception("MetsReader: No dmdSec ID found.");
				}
			} else {
				NodeList childs = logicalStructMap.getChildNodes();
				
				if (childs.getLength() > 0) {
					// without a structLink node, the first child in structMap does contain the physical root item
					dmdSecID = childs.item(0).getAttributes().getNamedItem(STRUCT_MAP_RETURN_ATTR).getTextContent();					
				} else {
					throw new Exception("MetsReader: Logical struct map has no children nodes!");
				}
			}
		}
		
		return dmdSecID;
	}
	
	private String getStructMapChildWithID(Node logicalStructMapNode, String idValue) {
		
		String dmdID = null;
		
		NodeList childs = logicalStructMapNode.getChildNodes();		

		for(int i=0; i<childs.getLength(); i++)
		{
			Node node = childs.item(i);

			if(node.getAttributes().getNamedItem(STRUCT_MAP_KEY_ATTR).getTextContent().equals(idValue))
			{
				dmdID = node.getAttributes().getNamedItem(STRUCT_MAP_RETURN_ATTR).getTextContent();
				break;
			}
			
			dmdID = getStructMapChildWithID(node, idValue);
			
			if (dmdID != null) {
				break;
			}
		}		
		
		return dmdID; 
	}
	
	private Node getLogicalStructMap() {
		NodeList structMaps = null;
		Node logicalStructMap = null;
		
		structMaps = dom.getElementsByTagName(STRUCT_MAP);
		
		for(int i=0; i<structMaps.getLength(); i++)
		{
			Node node = structMaps.item(i);

			if(node.getAttributes().getNamedItem(STRUCT_MAP_SRCH_ATTR).getTextContent().equals(STRUCT_MAP_SRCH_VALUE))
			{
				logicalStructMap = node;
				break;
			}
		}		
		
		return logicalStructMap;
	}	
	
	private String getStructMapIDFromStructLink() {
		String structMapID = null;
		
		NodeList structLinkItems = null;
		NodeList structLinks = null;		
		
		structLinks = dom.getElementsByTagName(STRUCT_LINK);
		
		// we assume, that if a structLink element is provided, that there is only 1
		if (structLinks.getLength() > 0) {
			structLinkItems = structLinks.item(0).getChildNodes();
			
			for(int i=0; i<structLinkItems.getLength(); i++)
			{
				Node node = structLinkItems.item(i);

				if(node.getAttributes().getNamedItem(STRUCT_LINK_TO).getTextContent().equals(STRUCT_LINK_PHYSROOT))
				{
					structMapID = node.getAttributes().getNamedItem(STRUCT_LINK_FROM).getTextContent();
					break;
				}
			}
			
		}		
		
		return structMapID;		
	}*/
	
	// end: new way of parsing mets

	/**
	 * DOM parsing and extracting data into Mets object
	 * @throws Exception 
	 *
	 */
	public void initDomParsing(SourceSip singleSip) throws Exception
	{
		String metsSectionID;

		if(dom != null)
		{
			//metsSectionID = getDMSecWithItemInfo(singleSip);
			//DDE-881: to iterate over all dmdSec nodes and find the right one with the metadata we need, we have to pass singleSip
			metsSectionID = getDMSecWithItemInfo(singleSip);

			logger.debug(config.getXpathSystemID().replace(config.getXpathReplaceSection(), metsSectionID));
			logger.debug(extractRecordIdentifier(metsSectionID));
			logger.debug(config.getXpathDOI().replace(config.getXpathReplaceSection(), metsSectionID));
			String extractedDOI = extractDoi(metsSectionID);
			logger.debug(extractedDOI);
			// DDE-795
			if (extractedDOI.trim().length() == 0) {
				logger.warn("No DOI found in mets.");
			}			
			logger.debug(config.getXpathAlternativeTitle().replace(config.getXpathReplaceSection(), metsSectionID));
			logger.debug(extractAlternativeTitle(metsSectionID));
			logger.debug(config.getXpathLocation().replace(config.getXpathReplaceSection(), metsSectionID));
			logger.debug(extractLocation(metsSectionID));

			getMets().setRecordIdentifier(extractRecordIdentifier(metsSectionID));
			getMets().setDoi(extractedDOI);
			getMets().setAlternativeTitle(extractAlternativeTitle(metsSectionID));
			getMets().setLocation(extractLocation(metsSectionID));
			addFilelistToMets(dom.getElementsByTagName(config.getFileNodeName()), singleSip);
		}
	}


	/**
	 * Extract doi from DOM
	 *
	 * @param String sectionId
	 * @return String doi
	 */
	private String extractDoi(String sectionId)
	{
		return getTextContentFromXPath(config.getXpathDOI().replace(
					config.getXpathReplaceSection(), sectionId));
	}


	/**
	 * Extract recordIdentifier from DOM
	 *
	 * @param String sectionId
	 * @return String recordIdentifier
	 */
	private String extractRecordIdentifier(String sectionId)
	{
		return getTextContentFromXPath(config.getXpathSystemID().replace(
						config.getXpathReplaceSection(), sectionId));
	}


	/**
	 * Extract alternative title from DOM
	 *
	 * @param String sectionId
	 * @return String alternative title
	 */
	private String extractAlternativeTitle(String sectionId)
	{
		return getTextContentFromXPath(config.getXpathAlternativeTitle().replace(
					config.getXpathReplaceSection(), sectionId));
	}


	/**
	 * Extract location from DOM
	 *
	 * @param Document sectionId
	 * @return String location
	 */
	private String extractLocation(String sectionId)
	{
		return getTextContentFromXPath(config.getXpathLocation().replace(
					config.getXpathReplaceSection(), sectionId));
	}


	/**
	 * Helper method to get a single value by running an xpath query on xml dom
	 *
	 * @param Document dom
	 * @param String xpath_value
	 * @return String
	 */
	@SuppressWarnings("finally")
	private String getTextContentFromXPath(String xpath_value)
	{
		String returnVal = "";

		try
		{
			XPath xpath  = XPathFactory.newInstance().newXPath();
			//xpath modified after problems with initial export_mets.xml
			XPathExpression expr = xpath.compile(xpath_value);
			Object result = expr.evaluate(dom, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			returnVal = nodes.item(0).getTextContent();

		}
		catch (XPathExpressionException e)
		{
			logger.error("XPathExpressionException: " + e.getMessage());
		}
		finally
		{
			return returnVal;
		}
	}



	/**
	 * iterates over supplied NodeList and extracts mets files node by node
	 * each file is put into a MetsFileList and
	 * each MetsFileList is added to the Mets object's fileMap
	 *
	 * @param NodeList nl
	 */
	private void addFilelistToMets(NodeList nl, SourceSip singleSip)
	{
		NodeList nodeList = nl;

		for(int nodeNumber = 0; nodeNumber < nodeList.getLength(); nodeNumber++)
		{
			MetsFileList metsFileList = new MetsFileList();

			Node node = nodeList.item(nodeNumber);

			if(node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element elm = (Element) node;

				metsFileList.setId(elm.getAttribute(config.getFileAttributeId()));
				metsFileList.setMimetype(elm.getAttribute(config.getFileAttributeMimetype()));
				metsFileList.setCreated(elm.getAttribute(config.getFileAttributeCreated()));
				metsFileList.setChecksum(elm.getAttribute(config.getFileAttributeChecksum()));
				metsFileList.setChecksumtype(elm.getAttribute(config.getFileAttributeChecksumtype()));
				metsFileList.setSize(elm.getAttribute(config.getFileAttributeSize()));

				Element subElm = (Element) elm.getElementsByTagName(config.getFilenameNodeName()).item(0);

				metsFileList.setFilepath(singleSip.getCapsuleID() + ConfigProperties.getFileSeparator() + subElm.getAttribute(config.getFilenameAttributeName()));
				metsFileList.setFilename(extractFileNameFromXlink(subElm.getAttribute(config.getFilenameAttributeName())));
				metsFileList.setFilelabelFromFilename();

				getMets().addFileList(metsFileList);
			}
		}
	}


	/**
	 * Extracts the file name from given xlink using regex
	 * if regex does not lead to a result the xlink will be returned
	 *
	 * @param String xlink
	 * @return file name or xlink
	 */
	private String extractFileNameFromXlink(String xlink)
	{
		Pattern regex = Pattern.compile("/(.+$)");
		Matcher matcher = regex.matcher(xlink);


		if(matcher.find())
		{
			return matcher.group(1);
		}
		else
		{
			return xlink;
		}
	}


	/**
	 * Creates XML DOM that can be used for parsing
	 *
	 * @return Document
	 */
	private Document createDOM()
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(false);
		DocumentBuilder builder = null;
		Document document = null;

		try
		{
			builder = builderFactory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e)
		{
			logger.error(e.getMessage());
		}

		try
		{
			document = builder.parse(getMetsFilePath());
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}

		return document;

	}


	/**
	 * returns filled Mets object
	 *
	 * @return Mets
	 */
	public Mets getMets()
	{
		return mets;
	}

	/**
	 * Getter for mets file path
	 *
	 * @return
	 */
	public String getMetsFilePath()
	{
		return metsFilePath;
	}


	/**
	 * Setter for mets file path
	 *
	 * @param metsFilePath
	 */
	public void setMetsFilePath(String metsFilePath)
	{
		this.metsFilePath = metsFilePath;
	}

}
