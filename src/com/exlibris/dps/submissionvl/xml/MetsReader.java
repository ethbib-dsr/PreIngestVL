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
	private final Logger logger = Logger.getLogger(this.getClass());

	private ConfigProperties config;
	private String metsFilePath;
	private Mets mets;
	private Document dom;
<<<<<<< HEAD


=======
	
	
>>>>>>> master
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
<<<<<<< HEAD
	}

	/**
	 * Check whether or not the mets contains a section that
	 * has the supplied record identifier
	 *
	 * @param recordIdentifier
	 * @return boolean
	 */
	public boolean hasIdSection(String recordIdentifier)
	{
		String xPath = config.getXpathSearchId();
		NodeList nodes = null;
		boolean returnVal = false;

		//find all nodes corresponding with the xpath 'xpath-search-id'
		try
		{
			XPath xpath  = XPathFactory.newInstance().newXPath();

			XPathExpression expr = xpath.compile(xPath);
			Object result = expr.evaluate(dom, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (XPathExpressionException e)
		{
			logger.error("XPathExpressionException: " + e.getMessage());
		}

		logger.debug("mets id sections: " + nodes.getLength());

		//check if nodes contains recordIdentifier (alephid)
		for(int i=0; i<nodes.getLength(); i++)
		{
			Node node = nodes.item(i);

			if(node.getTextContent().equals(recordIdentifier))
			{
				returnVal = true;
				break;
			}
		}

		return returnVal;
	}


	/**
	 * Returns mets section id that contains recordIdentifier
	 * If it cannot be found "0" is returned
	 *
	 * @param recordIdentifier
	 * @return integer
	 */
	private int findSectionId(String recordIdentifier)
	{
		String xPath = config.getXpathSearchId();
		int returnInt = 0;
		NodeList nodes = null;

=======
	}
	
	/**
	 * Check whether or not the mets contains a section that 
	 * has the supplied record identifier
	 * 
	 * @param recordIdentifier
	 * @return boolean
	 */
	public boolean hasIdSection(String recordIdentifier)
	{
		String xPath = config.getXpathSearchId();
		NodeList nodes = null;
		boolean returnVal = false;
		
>>>>>>> master
		//find all nodes corresponding with the xpath 'xpath-search-id'
		try
		{
			XPath xpath  = XPathFactory.newInstance().newXPath();
<<<<<<< HEAD

=======
			
>>>>>>> master
			XPathExpression expr = xpath.compile(xPath);
			Object result = expr.evaluate(dom, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (XPathExpressionException e)
		{
			logger.error("XPathExpressionException: " + e.getMessage());
<<<<<<< HEAD
		}

		//check if nodes contain recordIdentifier (alephid)
		for(int i=0; i<nodes.getLength(); i++)
		{
			Node node = nodes.item(i);

			if(node.getTextContent().equals(recordIdentifier))
			{
				returnInt = i+1; //nodes are arrays starting at 0 BUT xpath start at 1
				break;
			}
		}

		return returnInt;
	}


	/**
	 * DOM parsing and extracting data into Mets object
	 *
	 */
	public void initDomParsing(String recordIdentifier)
	{
		String metsPostion;
=======
		}		
		
		logger.debug("mets id sections: " + nodes.getLength());

		//check if nodes contains recordIdentifier (alephid)
		for(int i=0; i<nodes.getLength(); i++) 
		{
			Node node = nodes.item(i);
			
			if(node.getTextContent().equals(recordIdentifier))
			{
				returnVal = true;
				break;
			}
		}	
		
		return returnVal;
	}
	

	/**
	 * Returns mets section id that contains recordIdentifier
	 * If it cannot be found "0" is returned
	 * 
	 * @param recordIdentifier
	 * @return integer
	 */
	private int findSectionId(String recordIdentifier)
	{
		String xPath = config.getXpathSearchId();
		int returnInt = 0;
		NodeList nodes = null;
		
		//find all nodes corresponding with the xpath 'xpath-search-id'
		try
		{
			XPath xpath  = XPathFactory.newInstance().newXPath();
			
			XPathExpression expr = xpath.compile(xPath);
			Object result = expr.evaluate(dom, XPathConstants.NODESET);
			nodes = (NodeList) result;
		}
		catch (XPathExpressionException e)
		{
			logger.error("XPathExpressionException: " + e.getMessage());
		}
>>>>>>> master

		//check if nodes contain recordIdentifier (alephid)
		for(int i=0; i<nodes.getLength(); i++) 
		{
			Node node = nodes.item(i);
			
			if(node.getTextContent().equals(recordIdentifier))
			{
				returnInt = i+1; //nodes are arrays starting at 0 BUT xpath start at 1
				break;
			}
		}
		
		return returnInt;
	}
	
	
	/**
	 * DOM parsing and extracting data into Mets object
	 * 
	 */
	public void initDomParsing(String recordIdentifier)
	{
		String metsPostion;
		
		if(dom != null)
		{
			metsPostion = Integer.toString(findSectionId(recordIdentifier));

<<<<<<< HEAD
			logger.debug(config.getXpathDOI().replace(config.getXpathReplaceSection(), metsPostion));
			logger.debug(extractAlephid(metsPostion));
			logger.debug(config.getXpathAlephID().replace(config.getXpathReplaceSection(), metsPostion));
=======
			logger.debug(config.getXpathAlephID().replace(config.getXpathReplaceSection(), metsPostion));
			logger.debug(extractAlephid(metsPostion));
			logger.debug(config.getXpathDOI().replace(config.getXpathReplaceSection(), metsPostion));
>>>>>>> master
			logger.debug(extractDoi(metsPostion));
			logger.debug(config.getXpathAlternativeTitle().replace(config.getXpathReplaceSection(), metsPostion));
			logger.debug(extractAlternativeTitle(metsPostion));
			logger.debug(config.getXpathLocation().replace(config.getXpathReplaceSection(), metsPostion));
			logger.debug(extractLocation(metsPostion));
<<<<<<< HEAD

=======
			
>>>>>>> master
			getMets().setAlephid(extractAlephid(metsPostion));
			getMets().setDoi(extractDoi(metsPostion));
			getMets().setAlternativeTitle(extractAlternativeTitle(metsPostion));
			getMets().setLocation(extractLocation(metsPostion));
			addFilelistToMets(dom.getElementsByTagName(config.getFileNodeName()));
		}
	}


	/**
	 * Extract doi from DOM
<<<<<<< HEAD
	 *
=======
	 * 
>>>>>>> master
	 * @param String sectionId
	 * @return String doi
	 */
	private String extractDoi(String sectionId)
	{
		return getTextContentFromXPath(config.getXpathDOI().replace(
					config.getXpathReplaceSection(), sectionId));
	}


	/**
	 * Extract AlephID from DOM
<<<<<<< HEAD
	 *
=======
	 * 
>>>>>>> master
	 * @param String sectionId
	 * @return String Aleph ID
	 */
	private String extractAlephid(String sectionId)
	{
		return getTextContentFromXPath(config.getXpathAlephID().replace(
						config.getXpathReplaceSection(), sectionId));
	}



	/**
	 * Extract alternative title from DOM
<<<<<<< HEAD
	 *
=======
	 * 
>>>>>>> master
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
<<<<<<< HEAD
	 *
=======
	 * 
>>>>>>> master
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
	private void addFilelistToMets(NodeList nl)
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

				metsFileList.setFilepath(getMets().getAlephid() + ConfigProperties.getFileSeparator() + subElm.getAttribute(config.getFilenameAttributeName()));
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
