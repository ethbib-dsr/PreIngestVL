package com.exlibris.dps.submissionvl;

import java.io.File;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.util.FileHandler;
import com.exlibris.dps.submissionvl.xml.MetsReader;

public class TestReader
{

	private static ConfigProperties config;
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final String TESTFOLDER = "/home/hlars/Downloads/testmetsreader/";
	private static final String TESTITEM = "000310679_20180223T010423_master_ver1.zip";


	public TestReader(String configFilePath)
	{
		config = new ConfigProperties(configFilePath);
	}


	public void init()
	{
		logger.debug("start test run");

		File item = new File(TESTFOLDER + TESTITEM);

		/*
		logger.debug(item.getName());
		logger.debug(item.isFile());
		logger.debug(item.isDirectory());

		FileHandler testFh = new FileHandler(item, config);
		logger.debug(testFh.getAlephID());
		*/

		if(item.exists())
		{
			readerController(item);
		}


		logger.debug("end test run");
	}


	public void readerController(File item)
	{
		FileHandler itemFh = new FileHandler(item, config);

		logger.debug("controller started");

		String metsFilePath = item.getAbsolutePath() +
									File.separator +  itemFh.getAlephID() + File.separator +
									config.getMetsFileName();

		logger.debug(metsFilePath);

		MetsReader mr = new MetsReader(metsFilePath, config);


		if(mr.hasIdSection(itemFh.getAlephID()))
		{
			mr.initDomParsing(itemFh.getAlephID());
			logger.debug(mr.getMets().toString());
		}
		else
		{
			logger.warn("es gibt keine Sektion mit Aleph Id");
			//NEXT one
			//oder Exit
		}


		logger.debug("controller ended");
	}


}
