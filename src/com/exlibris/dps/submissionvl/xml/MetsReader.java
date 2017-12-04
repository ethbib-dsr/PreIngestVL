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
		domParsing();
	}
	
	
	/**
	 * DOM parsing and extracting data into Mets object
	 * 
	 */
	private void domParsing()
	{
		Document dom = createDOM();

		if(dom != null)
		{
			getMets().setAlephid(extractAlephid(dom));
			getMets().setDoi(extractDoi(dom));
			getMets().setAlternativeTitle(extractAlternativeTitle(dom));
			getMets().setLocation(extractLocation(dom));
			addFilelistToMets(dom.getElementsByTagName(config.getFileNodeName()));
		}
	}
	
	
	/**
	 * Extract doi from DOM
	 * 
	 * @param Document dom
	 * @return String doi
	 */
	private String extractDoi(Document dom)
	{
		return getTextContentFromXPath(dom, config.getXpathDOI());
	}
	
	
	/**
	 * Extract AlephID from DOM
	 * 
	 * @param Document dom
	 * @return String Aleph ID
	 */
	private String extractAlephid(Document dom)
	{
		return getTextContentFromXPath(dom, config.getXpathAlephID());
	}
	
	
	
	/**
	 * Extract alternative title from DOM
	 * 
	 * @param Document dom
	 * @return String alternative title
	 */
	private String extractAlternativeTitle(Document dom)
	{
		return getTextContentFromXPath(dom, config.getXpathAlternativeTitle());
	}
	
	
	/**
	 * Extract location from DOM
	 * 
	 * @param Document dom
	 * @return String location
	 */
	private String extractLocation(Document dom)
	{
		return getTextContentFromXPath(dom, config.getXpathLocation());
	}
	
	
	/**
	 * Helper method to get a single value by running an xpath query on xml dom
	 * 
	 * @param Document dom
	 * @param String xpath_value
	 * @return String
	 */
	@SuppressWarnings("finally")
	private String getTextContentFromXPath(Document dom, String xpath_value)
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
