package com.exlibris.dps.submissionvl.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.ConfigProperties;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifTool.Tag;

/**
 * Class model that handles check and manipulation of exif information
 * in supplied tif files
 * 
 * 
 * @author Lars Haendler
 *
 */

public class ExifManipulator
{
	private final Logger logger = Logger.getLogger(this.getClass());
	private ExifTool tool;
	private static ConfigProperties config;
	
	
	/**
	 * Constructor that instantiates Exif with stayopen option
	 */
	public ExifManipulator(ConfigProperties conf)
	{
		tool = new ExifTool();
		config = conf;
	}
	
	
	/**
	 * Checks whether or not the ModificationDate in the exif infos of the current file
	 * has ":" at position 4 and 7
	 * 
	 * @param f
	 * @return boolean if date ModifyDate is wrong
	 */
	public boolean hasWrongModifyDateFormat(File f)
	{
		String modifyDate = null;
		
		if(f.exists())
		{
			try
			{
				modifyDate = tool.getImageMeta(f, Tag.MODIFY_DATE).get(Tag.MODIFY_DATE);
			}
			catch (IllegalArgumentException | SecurityException | IOException e)
			{
				logger.error(e.getMessage());
				System.exit(2);
			}
			
			if(modifyDate==null)
			{
				logger.warn(f.getName() + " has no exif ModifyDate");
			}
			else
			{
				if(modifyDate.charAt(config.getExifDateFirstWrongCharPos()) == config.getExifDateFirstWrongChar() 
						&& modifyDate.charAt(config.getExifDateSecondWrongCharPos()) == config.getExifDateSecondWrongChar())
				{
					return true;
				}
			}
		}
		else
		{
			logger.warn(f.getName() + " does not exist");
		}
		
		return false;
	}
	
	
	/**
	 * exif tag pair will be read out, modified and written anews
	 * implementation is only for ModifyDate at the moment
	 * (extending this method can easily be done by working with more map pair)
	 * finally any copy from the original file that exiftool may have created is removed
	 * 
	 * @param f
	 */
	public void makeExifCorrections(File f)
	{
		Map<Tag, String> exifPairs;
		
		if(f.exists())
		{
			try
			{
				//for all exif values
				//exifPairs = tool.getImageMeta(f, Tag.values());	
				exifPairs = tool.getImageMeta(f, Tag.MODIFY_DATE);
				exifPairs.put(Tag.MODIFY_DATE, correctModifyDate(exifPairs.get(Tag.MODIFY_DATE)));
				tool.setImageMeta(f, exifPairs);				
			}
			catch (IllegalArgumentException | SecurityException | IOException e)
			{
				logger.error(e.getMessage());
				System.exit(2);
			}
		}
		else
		{
			logger.warn(f.getName() + " at " + f.getAbsolutePath() + "does not exist");
		}
		
		deleteOriginalFile(f);
	}
	
	
	/**
	 * Remove any (redundant) original files that may have been created by exiftool
	 * those files are usually created by appending _original to the file name
	 * 
	 * @param f
	 */
	private void deleteOriginalFile(File f)
	{
		File orgFile = new File(f.getAbsoluteFile() + "_original");
		if(orgFile.exists())
		{
			orgFile.delete();
		}
	}
	
	
	/**
	 * Makes correction to a wrong date string
	 * Looks only for ":" at position 4 and 7 and replaces them with "."
	 * 
	 * @param date
	 * @return String
	 */
	private String correctModifyDate(String date)
	{
		String corrected = "";
		//2011.03.22 07:41:50 
		if(date.charAt(config.getExifDateFirstWrongCharPos())==config.getExifDateFirstWrongChar() 
				&& date.charAt(config.getExifDateSecondWrongCharPos())==config.getExifDateSecondWrongChar())
		{
			StringBuilder dateSb = new StringBuilder(date);
			dateSb.setCharAt(config.getExifDateFirstWrongCharPos(), config.getExifDateFirstWrongCharFix());
			dateSb.setCharAt(config.getExifDateSecondWrongCharPos(), config.getExifDateSecondWrongCharFix());
			
			corrected = dateSb.toString();
		}
		else
		{
			corrected = date;
		}
		
		return corrected;
	}	
	
	
	/**
	 * Closes com.thebuzzmedia.exiftool.ExifTool instance
	 * If not properly closed perl instances of exif will 
	 * stay alive indefinitely
	 * 
	 */
	public void close()
	{
		tool.close();
	}
	
	
}
