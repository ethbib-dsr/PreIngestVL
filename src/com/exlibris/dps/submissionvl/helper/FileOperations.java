package com.exlibris.dps.submissionvl.helper;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;

public class FileOperations
{

	private final Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * Defeat instantiation
	 */
	private FileOperations()
	{
		
	}

	
	/**
	 * Returns array with all subdirectory names
	 * in given directory
	 * 
	 * @param directory
	 * @return String[]
	 */
	public static String[] getSubdirectories(File directory)
	{
		String subDirectories[] = null;
		
		if(directory.isDirectory())
		{
			subDirectories = directory.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name){
					return new File(current, name).isDirectory();
				}
			});
		}
		
		return subDirectories;
	}
	
	
	/**
	 * Returns array with all file file name 
	 * in given directory
	 * 
	 * @param directory
	 * @return String[]
	 */
	public static String[] getFilenamesFromDirectory(File directory)
	{
		String fileNames[] = null;
		
		if(directory.isDirectory())
		{
			fileNames = directory.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name)
				{
					return new File(current, name).isFile();
				}
			});
		}
		
		return fileNames;
	}
	
	
	public static String removeSlashes(String content)
	{
		return content.replace("/", "").replace("\\", "");
	}
	
}
