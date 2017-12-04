package com.exlibris.dps.submissionvl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


/**
 * Submission App for Visual Library
 * 
 * Main Class for starting and controlling the application
 * 
 * Repo: https://bitbucket.org/ethbib_bit/submissionvl/
 * 
 * @author Lars Haendler
 * 
 */
public class AppStarter {

	private static ServerSocket s;
	private static final int PORT = 7777;
	private static final Logger logger = Logger.getLogger(AppStarter.class);
	
	/**
	 * main method to start the application
	 * 
	 * @param String[] holding all parameters passed to the application
	 */
	public static void main(String[] args) {

		logger.debug("AppStarter initialized");
		
		//check that not two instance of the application run at once
		try
		{
			s = new ServerSocket(PORT, 10, InetAddress.getLocalHost());
		}
		catch (UnknownHostException e)
		{
			logger.error("Application already running");
			logger.error(e.getMessage());
			System.exit(1);
		}
		catch (IOException e)
		{
			logger.error("Unexpected error: " + e.getMessage());
			System.exit(2);
		}

		
		//get relative path to config.properties from App paramter
		if(args.length == 0)
		{
			logger.error("No path to config.properties supplied. Relative path has to be supplied as an argument when running AppStarter.");
		}
		else
		{
			if(propertiesExists(args[0]))
			{
				final SubmissionSingleton subApp = SubmissionSingleton.getInstance(args[0]);
				logger.debug("SubmissionSingleton created");
				subApp.init();
			}
			else
			{
				logger.error("No working path to config.properties found.");
			}			
		}
		
		try
		{
			s.close();
		}
		catch (IOException e)
		{
			logger.error("Unexpected error: " + e.getMessage());
			System.exit(2);
		}
		
		logger.debug("AppStarter finished");
	}
	
	
	/**
	 * Checks whether or not the supplied path for config.properties
	 * exists
	 * 
	 * @param String 
	 * @return boolean if path exists
	 */
	private static boolean propertiesExists(String pathToProperties)
	{
		File f = new File(System.getProperty("user.dir") + pathToProperties);
		
		return f.exists();
	}

}
