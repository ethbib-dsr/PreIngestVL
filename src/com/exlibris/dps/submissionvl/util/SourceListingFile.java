package com.exlibris.dps.submissionvl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Class model for the source listing file
 * It handles creation and fills it with content
 * 
 * Some parts of how the listing files are filled with content
 * are hardcoded because underlying Linux commands are quite
 * complex and hard to debug / should only be touched by a 
 * professional
 * 
 * @author Lars Haendler
 *
 */
public class SourceListingFile
{

	// hard-coded shell commands
	// we do not want anyone to mess around with this in the config
	private static final String SHELL_PATH = "/bin/sh";
	private static final String SHELL_PARAM = "-c";
	// lists all files with file name, comma, file size in byte
	private static final String SHELL_COMMAND = "ls [sourcepath] -l | tail -n +2 | grep -v '^d' | awk -v OFS=',' '{print $9, $5}'";
	private static final String SHELL_COMMAND_REPLACER = "[sourcepath]";
	private static final String SHELL_OUTPUT = " > ";
	private static final String LS_DIVIDER = ",";

	// initialized logger
	private final Logger logger = Logger.getLogger(this.getClass());

	// path to readout for ls
	private String sourcePathToCrawl;
	// file object
	private File file;
	// file path of file object
	private Path filePath;
	// attributes of file
	private BasicFileAttributes fileAttributes;
	

	/**
	 * Constructor
	 * 
	 * @param sourceListingFile File holding all content from source path parsing
	 * @param sourcePath String source directory path
	 */
	public SourceListingFile(File sourceListingFile, String sourcePath)
	{
		setSourcePathToCrawl(sourcePath);
		initializeFile(sourceListingFile);
	}

	/**
	 * Return the age of the file in relation to the current date
	 * Age is caluclated as floor, 
	 * e.g. 1.7 days will return 1, 2.1 days will return 2
	 * 
	 * @return int floor of file age in days
	 */
	public int getAgeInDays()
	{
		double days = getAgeInSeconds() / 60 / 60 / 24;
		int returnDays = (int) Math.floor(days);

		return returnDays;
	}

	/**
	 * Starts a system process that runs a complex 'ls'
	 * command. Methods runs for as long as the ls command
	 * runs by blocking the current thread
	 * 
	 * Use with caution because the can take a while
	 * 
	 */
	public void generateListing()
	{
		String completeCommand = buildLsCommand();

		ProcessBuilder b = new ProcessBuilder(SHELL_PATH, SHELL_PARAM, completeCommand);
		try
		{
			Process p = b.start();
			logger.debug("started: '" + completeCommand + "'");
			//wait until process is finished
			p.waitFor();
			logger.debug("finished listing");
		}
		catch (IOException | InterruptedException e)
		{
			logger.error("Process could not be executed: " + e.getMessage());
			System.exit(0);
		}

	}

	/**
	 * Return the content of the listing file in form of 
	 * a fast HashMap. keys of the map are file names and 
	 * values are size of the file in bytes
	 * 
	 * @return HashMap<String, Long> file name and file size
	 */
	public Map<String, Long> getListingFileContent()
	{
		// AB 11.11.2020 - to preserve the order of the source list, we need a
		// LinkedHashMap.
		Map<String, Long> fileContent = new LinkedHashMap<String, Long>();
		BufferedReader reader = null;
		
		//start readout with tons of exception handling
		try
		{
			reader = new BufferedReader(new FileReader(getSourceListingFile()));
			String line = null;
			
			//readout line by line
			while((line = reader.readLine()) != null)
			{
				//split csv into array and fill Map accordingly
				String[] lineValue = line.split(LS_DIVIDER);
				fileContent.put(lineValue[0], Long.parseLong(lineValue[1]));
			}
			
		}
		catch (FileNotFoundException e)
		{
			logger.warn("listing file " + getSourceListingFile().getAbsolutePath() + " could not be found");
		}
		catch (IOException e)
		{
			logger.warn("listing file " + getSourceListingFile().getAbsolutePath() + " could not be read out");
		}
		finally
		{
			//always clean up after you played
			if(reader!=null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					logger.warn("file " + getSourceListingFile().getName() + " could not be closed after readout: " + e.getMessage());
				}
			}
			
		}
	
		return fileContent;
	}

	/**
	 * Returns build ls command. The command is build from
	 * multiple parts as well as some replacing for the folder
	 * that should be listed
	 * 
	 * @return String complete ls command 
	 */
	private String buildLsCommand()
	{
		// http://stackoverflow.com/questions/3776195/using-java-processbuilder-to-execute-a-piped-command
		String command = SHELL_COMMAND.replace(SHELL_COMMAND_REPLACER, getSourcePathToCrawl()) 
								+ SHELL_OUTPUT 
								+ getSourceListingFile().getAbsolutePath();
				
		return command;
	}
	
	/**
	 * Initializes listing file by checking whether or not
	 * it exists, extracting the file path elements from 
	 * file and extracting the file attributes
	 * 
	 * @param sourceListingFile 
	 */
	private void initializeFile(File sourceListingFile)
	{
		setSourceListingFile(sourceListingFile);

		// make sure that the file really exists
		if (!getSourceListingFile().exists())
		{
			logger.debug("Listing file '" + getSourceListingFile().getName() + "' did not exist and was created.");

			try
			{
				getSourceListingFile().createNewFile();
			}
			catch (IOException e)
			{
				logger.error("Could not create new listing file '" + getSourceListingFile().getName() + "'");
				System.exit(0);
			}
		}

		// set file path
		setListingFilePath(Paths.get(getSourceListingFile().getPath()));

		// set file attributes
		try
		{
			setFileAttributes(Files.readAttributes(getListingFilePath(), BasicFileAttributes.class));
		}
		catch (IOException e)
		{
			logger.error("Could not read attributes of listing file '" + getSourceListingFile().getName() + "'");
			System.exit(0);
		}
	}
	
	/**
	 * Return the age of the file in second in 
	 * relation to the current date
	 * 
	 * @return long file age in seconds
	 */
	private long getAgeInSeconds()
	{
		FileTime modifiedTime = getFileAttributes().lastModifiedTime();
		
		long secBetweenDates = (System.currentTimeMillis() - modifiedTime.toMillis()) / 1000L;
		logger.debug("The fileage is " + secBetweenDates/60 + " minutes / " + secBetweenDates/60/60/24 + " days.");
		return secBetweenDates;
	}
	
	/**
	 * Getter for file variable
	 * 
	 * @return File file
	 */
	private File getSourceListingFile()
	{
		return file;
	}

	/**
	 * Setter for file variable
	 *  
	 */
	private void setSourceListingFile(File sourceListingFile)
	{
		this.file = sourceListingFile;
	}

	/**
	 * Getter for filePath variable
	 * 
	 * @return Path filePath
	 */
	private Path getListingFilePath()
	{
		return filePath;
	}

	/**
	 * Setter for filePath variable
	 * 
	 * @param Path listingFilePath
	 */
	private void setListingFilePath(Path listingFilePath)
	{
		this.filePath = listingFilePath;
	}

	/**
	 * Getter for variable fileAttributes
	 * 
	 * @return BasicFileAttributes fileAttributes
	 */
	private BasicFileAttributes getFileAttributes()
	{
		return fileAttributes;
	}

	/**
	 * Setter for variable fileAttributes
	 * 
	 * @param BasicFileAttributes
	 */
	private void setFileAttributes(BasicFileAttributes fileAttributes)
	{
		this.fileAttributes = fileAttributes;
	}

	/**
	 * Getter for variable sourcePathToCrawl
	 * 
	 * @return String sourcePathToCrawl
	 */
	private String getSourcePathToCrawl()
	{
		return sourcePathToCrawl;
	}

	/**
	 * Setter for variable sourcePathToCrawl
	 * 
	 * @param String sourcePathToCrawl
	 */
	private void setSourcePathToCrawl(String sourcePathToCrawl)
	{
		this.sourcePathToCrawl = sourcePathToCrawl;
	}

}
