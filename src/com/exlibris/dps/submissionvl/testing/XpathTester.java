package com.exlibris.dps.submissionvl.testing;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Class is only used for xpath testing purposes 
 * No additional Javadoc is supplied because this class is just
 * a playground for X-Path behaviour in Java
 * 
 * @author Lars Haendler
 *
 */
public class XpathTester {

	private final static String PATH_XML = "/home/hlars/workspace/submissionvl/testdata/donotdelete/xmlfiles/export_mets.xml";
	private final static String XPATH_ALEPHID = "/mets/dmdSec[contains(@ID,\"md\")][last()]/mdWrap[last()]/xmlData[last()]/mods/recordInfo/recordIdentifier";
	private final static String XPATH_ALLALEPHID = "/mets/dmdSec/mdWrap/xmlData/mods/recordInfo/recordIdentifier";
	private final static String XPATH_DOI = "/mets/dmdSec[contains(@ID,\"md\")][last()]//mdWrap[last()]/xmlData[last()]/mods/identifier[@type=\"doi\"]";
	private final static String XPATH_ALLDOI = "/mets/dmdSec/mdWrap/xmlData/mods/identifier[@type=\"doi\"]";
	
	
	public XpathTester ()
	{
		log("XpathTester start");

		Document dom = createDOM(PATH_XML);
		
		if(dom!=null)
		{
			//getAllAlephID(dom);
			getAlephID(dom);
			//getAllDoi(dom);
			getDoi(dom);
		}

		log("XpathTester end");
	}
	
	
	private void getAllDoi(Document dom)
	{
		getAllXPathItems(dom, XPATH_ALLDOI);
	}
	
	
	private void getDoi(Document dom)
	{
		getSingleXpathItem(dom, XPATH_DOI);
	}
	
	
	
	
	private void getAllAlephID(Document dom)
	{
		getAllXPathItems(dom, XPATH_ALLALEPHID);
	}
	
	
	private void getAlephID(Document dom)
	{
		getSingleXpathItem(dom, XPATH_ALEPHID);
	}
	
	
	private void getAllXPathItems(Document dom, String xnXpath)
	{
		String returnVal = "";		
		
		try
		{
			XPath xpath  = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(xnXpath);
			Object result = expr.evaluate(dom, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			
			log("number of nodes: " + Integer.toString(nodes.getLength()));
			
			for(int i = 0; i<nodes.getLength(); i++)
			{
				log("node [" + i + "]: " + nodes.item(i).getTextContent());
			}

		}
		catch (XPathExpressionException e)
		{
			log("XPathExpressionException: " + e.getMessage());
		}
	}
	
	
	private void getSingleXpathItem(Document dom, String anXpath)
	{
		String returnVal = "";		
		
		try
		{
			XPath xpath  = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile(anXpath);
			Object result = expr.evaluate(dom, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			
			//log(Integer.toString(nodes.getLength()));
			
			//has to be modified
			returnVal = nodes.item(0).getTextContent();

		}
		catch (XPathExpressionException e)
		{
			log("XPathExpressionException: " + e.getMessage());
		}

		log("last node value:" + returnVal);
	}
	
	
	
	private Document createDOM(String pathToFile)
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
			log(e.getMessage());
		}
		

		try
		{
			document = builder.parse(pathToFile);
		}
		catch(Exception e)
		{
			log(e.getMessage());
		}
		
		return document;		
	}
	
	
	private void log (String toLog)
	{
		System.out.println(toLog);
	}
}