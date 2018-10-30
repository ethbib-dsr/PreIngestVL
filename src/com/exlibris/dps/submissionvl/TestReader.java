package com.exlibris.dps.submissionvl;

import java.io.File;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.xml.MetsReader;

public class TestReader
{

	private static ConfigProperties config;
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final String TESTFOLDER = "/home/hlars/Downloads/testmetsreader/";

	
	public TestReader(String configFilePath)
	{
		config = new ConfigProperties(configFilePath);
	}
	
	
	public void init()
	{
		logger.debug("start test run");
		
		String test1 = "000310679_20180223T010423_master_ver1.zip/";
		//mets file location
		//folder name
		if(new File(TESTFOLDER + test1).exists())
		{
			readerController(new File(TESTFOLDER + test1));
		}
		
		
		logger.debug("end test run");
	}
	
	
	public void readerController(File folder)
	{
		logger.debug("controller started");
		
		String metsFilePath = folder.getAbsolutePath()+"/000310679/"+config.getMetsFileName();
		
		//logger.debug("mets.xml: " + folder.getAbsolutePath()+"/000310679/"+config.getMetsFileName());
		
		MetsReader mr = new MetsReader(metsFilePath, config);
		mr.findSectionInDom();
		
		logger.debug("controller ended");
	}
	

}
