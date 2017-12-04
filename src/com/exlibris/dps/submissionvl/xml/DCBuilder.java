package com.exlibris.dps.submissionvl.xml;

import org.apache.log4j.Logger;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.formatting.DublinCoreFactory;

public class DCBuilder
{

	final static String DC_TITLE = "dc:title";
	
	public String title;
	private final DublinCore dc;

	
	private final Logger logger = Logger.getLogger(this.getClass());
	

	public DCBuilder()
	{
		dc = DublinCoreFactory.getInstance().createDocument();
	}


	public DCBuilder(String title)
	{
		dc = DublinCoreFactory.getInstance().createDocument();
		setTitle(title);
	}


	public String getTitle()
	{
		return title;
	}


	public void setTitle(String title)
	{
		this.title = title;
	}


	public String runBuilder()
	{
		String returnXml = "";

		try
		{
			dc.addElement(DC_TITLE, getTitle());
			returnXml = dc.toXml();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return returnXml;
	}

}
