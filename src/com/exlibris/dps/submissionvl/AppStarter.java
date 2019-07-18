package com.exlibris.dps.submissionvl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


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

	private static final String CONF_DIR = "conf";

	private static final int DEFAULT_PORT = 7777;
	private static final String APP_NAME = "submissionvl";
	private static final String VERSION_FILE = "version/version.txt";

	private static final String CONF_EXTENSION = ".properties";

	private static String VERSION = "unknown";
	private static String BUILD = "unknown";

	private static Logger logger = Logger.getLogger(AppStarter.class);


	/**
	 * main method to start the application
	 *
	 * @param String[] holding all parameters passed to the application
	 */
	public static void main(String[] args) {

		//check number of arguments
		if(args.length < 2)
		{
			System.out.println("Two arguments are required.");
			System.exit(1);
		}
		else
		{
			File configFile = new File(System.getProperty("user.dir") + File.separator + CONF_DIR + File.separator + args[0]);
			File log4jFile = new File(System.getProperty("user.dir") + File.separator + CONF_DIR + File.separator + args[1]);

			//if a third value exists, use it as port
			//if not use the default port value
			int port = (args.length == 3) ? Integer.valueOf(args[2]) : DEFAULT_PORT;

			//check if config file really exists
			if(!configFile.exists())
			{
				System.out.println("config file '" + configFile.getAbsolutePath() + "' does not exist.");
				System.exit(1);
			}

			//check if log4j file really exists
			if(!log4jFile.exists())
			{
				System.out.println("log4j file '" + log4jFile.getAbsolutePath() + "' does not exist.");
				System.exit(1);
			}

			//extract version and build number
			getVersionAndBuild();

			//start actual initialsation
			init(configFile, log4jFile, port);
		}
	}


	/**
	 * Initializer for AppStarter
	 *
	 * @param configRelativePath
	 * @param log4jRelativePath
	 * @param port
	 */
	public static void init(File configFile, File log4jFile, int port)
	{

		String configRelativePath = CONF_DIR + File.separator + configFile.getName();
		String log4jRelativePath = CONF_DIR + File.separator + log4jFile.getName();
		Path lockFilePath = Paths.get(System.getProperty("user.dir") + File.separator
									+ configFile.getName().replaceAll(CONF_EXTENSION, "") + ".lock");

		//get the correct log4j config file
		PropertyConfigurator.configure(log4jRelativePath);

		//version debug out
		logger.debug(APP_NAME + " v" + VERSION);
		logger.debug("config: " + configRelativePath + ", log4j: " + log4jRelativePath + ", port: " + port);

		//Start application
		logger.debug("Started");

		//check for existing lock
		checkLock(lockFilePath);

		//check that not two instance of the application run at once
		//create a pseudo server app
		try
		{
			s = new ServerSocket(port, 10, InetAddress.getLocalHost());
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

		//Start actual application
		final SubmissionSingleton subApp = SubmissionSingleton.getInstance(configRelativePath);
		subApp.init();
		//lock file removed
		removeLock(lockFilePath);
		logger.debug("Finished -");
	}


	/**
	 * Extract version and build number from version.txt
	 *
	 */
	private static void getVersionAndBuild()
	{
		//file content has to look like this:
		//  version=1.4
		//  build.date=2018-09-27
		try(Scanner fileReader = new Scanner(new File(VERSION_FILE))) {
			VERSION = fileReader.nextLine().split("=")[1];
			BUILD = fileReader.nextLine().split("=")[1];
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a lock file with given path
	 *
	 * @param lockFilePath
	 */
	private static void createLock(Path lockFilePath)
	{
=======


		//close pseudo server app
>>>>>>> master
		try
		{
			Files.createFile(lockFilePath);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}

		//lock file removed
		removeLock(lockFilePath);
		logger.debug("Finished");
	}

	/**
	 * Extract version and build number from version.txt
	 *
	 */
	public static void getVersionAndBuild()
	{
		//file content has to look like this:
		//  version=1.4
		//  build.date=2018-09-27
		try(Scanner fileReader = new Scanner(new File(VERSION_FILE))) {
			VERSION = fileReader.nextLine().split("=")[1];
			BUILD = fileReader.nextLine().split("=")[1];
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * check if lock exists and stop application
	 *
	 * @param lockFilePath
	 */
	private static void checkLock(Path lockFilePath)
	{
		File lockFile = lockFilePath.toFile();
		if(lockFile.exists())
		{
			logger.error("Last run did not stop properly.");
			logger.error("Lock file found '" +  lockFilePath.toString() + "'");
			System.exit(1);
		}
		else
		{
			createLock(lockFilePath);
		}
	}


	/**
	 * Creates a lock file with given path
	 *
	 * @param lockFilePath
	 */
	private static void createLock(Path lockFilePath)
	{
		try
		{
			Files.createFile(lockFilePath);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
	}

	/**
	 * Removes a lock file with a given path
	 *
	 * @param lockFilePath
	 */
	private static void removeLock(Path lockFilePath)
	{
		try
		{
			Files.delete(lockFilePath);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
	}

}
