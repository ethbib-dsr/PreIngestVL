package com.exlibris.dps.submissionvl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.ConfigProperties;



/**
 * Class Model for file / directory handling
 * 
 * versatile class usable for files and directories
 * 
 * @author Lars Haendler
 *
 */
public class FileHandler
{
	private File workingPath = null;
	private ConfigProperties config = null;

	private final Logger logger = Logger.getLogger(this.getClass());
	

	/**
	 * constructor for String inputs
	 * 
	 * @param path
	 * @param config
	 */
	public FileHandler(String path, ConfigProperties config)
	{
		setWorkingPathFromString(path);
		setConfig(config);
	}

	/**
	 * constructor for File inputs
	 * 
	 * @param file
	 * @param config
	 */
	public FileHandler(File file, ConfigProperties config)
	{
		setWorkingPath(file);
		setConfig(config);
	}
	
	
	/**
	 * returns File object working path
	 * 
	 * @return File workingPath
	 */
	public File getWorkingPath()
	{
		return workingPath;
	}
	
	
	/**
	 * sets workingPath form File path
	 * 
	 * @param File path
	 */
	public void setWorkingPath(File path)
	{
		workingPath = path;
	}

	/**
	 * sets workingPath from String pathName
	 * creating a new File instance from pathName
	 * 
	 * @param String pathName
	 */
	private void setWorkingPathFromString(String pathName)
	{
		workingPath = new File(pathName);
	}

	
	/**
	 * moves file from current location to target location
	 * 
	 * @param targetPath file's new location
	 */
	public void moveFileTo(String targetPath)
	{
		File targetFile = new File(targetPath, getWorkingPath().getName());
		copyWithStreams(getWorkingPath(), targetFile, true);
		deleteFile();
	}
	
	
	/**
	 * copies file from current location to target location
	 * 
	 * @param targetPath location file is copied to
	 */
	public void copyFileTo(String targetPath)
	{
		File targetFile = new File(targetPath, getWorkingPath().getName());
		
		copyWithStreams(getWorkingPath(), targetFile, true);
	}
	
	
	/**
	 * removes file completely from its current location
	 * 
	 * @return boolean for success/failure of deletion
	 */
	public boolean deleteFile()
	{
		boolean success = false;
		
		if(getWorkingPath().isFile())
		{
			getWorkingPath().delete();
			success = true;
		}
		
		return success;
	}

	
	/**
	 * unzips file to target location
	 * 
	 * @param targetPath unzip location
	 */
	public void unzipFileTo(String targetPath)
	{
		unZipIt(getWorkingPath().toString(), targetPath);
	}
	
	
	/**
	 * copy process using byte stream
	 * 
	 * @param aSourceFile source File object
	 * @param aTargetFile target File object
	 * @param aAppend 
	 */
	private void copyWithStreams(File aSourceFile, File aTargetFile, boolean aAppend)
	{
		
		InputStream inStream = null;
		OutputStream outStream = null;
		try
		{
			try
			{
				byte[] bucket = new byte[32 * 1024];
				inStream = new BufferedInputStream(new FileInputStream(aSourceFile));
				outStream = new BufferedOutputStream(new FileOutputStream(aTargetFile, aAppend));
				int bytesRead = 0;
				while (bytesRead != -1)
				{
					bytesRead = inStream.read(bucket); // -1, 0, or more
					if (bytesRead > 0)
					{
						outStream.write(bucket, 0, bytesRead);
					}
				}
			}
			finally
			{
				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
			}
		}
		catch (FileNotFoundException e)
		{
			logger.error(e.getMessage());
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
	}

	


	/**
	 * Unzip it
	 * Source: http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
	 * 
	 * @param zipFile
	 *           input zip file
	 * @param output
	 *           zip file output directory
	 */
	private void unZipIt(String zipFile, String outputDirectory)
	{
		byte[] buffer = new byte[1024];

		try
		{
			// create output directory is not exists
			File directory = new File(outputDirectory);
			if (!directory.exists())
			{
				directory.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			logger.debug("unzip " +  zipFile);
			
			while (ze != null)
			{
				String fileName = ze.getName();
				
				File newFile = new File(outputDirectory + File.separator + fileName);
				
				if (fileName.endsWith("/")) 
				{
					newFile.mkdirs();
					ze = zis.getNextEntry();
					continue;
				}				
				
				logger.debug("extract: " + fileName);
				
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0)
				{
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
				
			}

			zis.closeEntry();
			zis.close();
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
		}
	}
	

	/**
	 * extracts 9 digit Aleph ID from file name of current file
	 * 
	 * @return 9 digit String
	 */
	public String getAlephID()
	{
		String result = "";
		String fileName = getWorkingPath().getName();
		String regexString = "";
		
		regexString = config.getRegexAlephId();
		Pattern regexPattern = Pattern.compile(regexString);
		//Pattern regexPattern = Pattern.compile("(^\\d{9})");
		Matcher match = regexPattern.matcher(fileName);
		
		if(match.find())
		{
			result = match.group(0);
		}
		
		return result;
	}
	
	
	/**
	 * return extract path
	 * 
	 * @return String extract path
	 */
	public String getExtractAlephIDPath()
	{
		return config.getExtractPath() + getWorkingPath().getName() + config.getSipDataPath()
				+ getAlephID() + ConfigProperties.getFileSeparator();
	}
	
	
	/**
	 * get the String file name from the current File 
	 * 
	 * @return String file name
	 */
	public String getCurrentFileName()
	{
		return getWorkingPath().getName();
	}
	
	
	
	
	public String getFormattedDate(String datePattern)
	{
		return new SimpleDateFormat(datePattern).format(getDateFromFileName());
	}
	
	
	private Date getDateFromFileName()
	{
		String dateString = "";
		Date date = null;
		DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
		
		Pattern regexPattern = Pattern.compile("_(\\d{8})T");
		Matcher match = regexPattern.matcher(getCurrentFileName());
		
		if(match.find())
		{
			dateString = match.group(1);
		}		
		

		try
		{
			date = format.parse(dateString);
		}
		catch (ParseException e)
		{
			logger.error(e.getMessage());
		}
		
		return date;
	}
	
	
	
	
	/**
	 * returns String array of all files in current folder
	 * 
	 * @return String[]
	 */
	public String[] getFileListArrayFromCurrentDirectory()
	{
		File dir = getWorkingPath();
		String[] fileList = dir.list();
		
		return fileList;
	}

	
	/**
	 * returns Map of name and hash of all files in SIP/zip
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getImageHashPairMap()
	{
		Map<String, String> imageHashPair = new HashMap<String, String>();
		
		for(String imageName : getImageFilesArray())
		{
			String imageFilePath = getImageFilePath() + imageName;
			imageHashPair.put(imageName, getMd5Hash(imageFilePath));
		}
		
		return imageHashPair;
	}
	
	
	/**
	 * returns Map of name and hash of all fulltext xml files in SIP/zip
	 * 
	 * @return Map<String, String>
	 */
	public Map<String, String> getFulltextHashPairMap()
	{
		Map<String, String> fulltextHashPair = new HashMap<String, String>();
		
		for(String fulltextName : getFulltextFilesArray())
		{
			String fulltextFilePath = getFulltextFilePath() + fulltextName;
			fulltextHashPair.put(fulltextName, getMd5Hash(fulltextFilePath));
		}
		
		return fulltextHashPair;
	}
	
	
	/**
	 * returns md5 hash from current file
	 * 
	 * @return md5 hash
	 */
	public String getMd5Hash(String absolutePathToFile)
	{
		String hash = "";
		
		try
		{
			hash = HashGenerator.generateMD5(new File(absolutePathToFile));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		
		return hash;
	}
	
	
	/**
	 * returns path to image files 
	 * 
	 * @param config
	 * @return
	 */
	public String getImageFilePath()
	{
		String path = "";
		
		path = getExtractAlephIDPath().subSequence(0, getExtractAlephIDPath().length() - 1) + config.getSipImageDirectory();
		
		return path;
	}
	
	
	/**
	 * returns path to fulltext files 
	 * 
	 * @param config
	 * @return
	 */
	public String getFulltextFilePath()
	{
		String path = "";
		
		path = getExtractAlephIDPath().subSequence(0, getExtractAlephIDPath().length() - 1) + config.getSipFulltextDirectory();
		
		return path;
	}
	
	
	
	/**
	 * Helper to extract the extension of a file name supplied as argument
	 * returns extensions with a "." or an empty String
	 * 
	 * @param fileName
	 * @return String extension
	 */
	public static String getFileExtension(String fileName)
	{
		String extension = "";
		
		int i = fileName.lastIndexOf('.');
		
		if (i > 0) {
		    extension = fileName.substring(i+1);
		}
		
		return extension;
	}
	
	
	/**
	 * Helper method
	 * return an array of String containing all images files of the SIP file supplied
	 * 
	 * @return String[]
	 */
	public String[] getImageFilesArray()
	{
		String imagePath = getExtractAlephIDPath() + config.getSipImageDirectory();

		FileHandler imageFolder = new FileHandler(imagePath, config);

		String[] imageFiles = imageFolder.getFileListArrayFromCurrentDirectory();

		return imageFiles;
	}
	

	/**
	 * Helper method
	 * return an array of String containing all fulltext xml files of the SIP file supplied
	 * 
	 * @return String[]
	 */
	public String[] getFulltextFilesArray()
	{
		String fulltextPath = getExtractAlephIDPath() + config.getSipFulltextDirectory();

		FileHandler fulltextFolder = new FileHandler(fulltextPath, config);

		String[] fulltextFiles = fulltextFolder.getFileListArrayFromCurrentDirectory();

		return fulltextFiles;
	}
	

	/**
	 * Helper method
	 * returns a File object for the mets file of the SIP supplied
	 * 
	 * @param File sipFile
	 * @param ConfigProperties config
	 * @return File
	 */
	public File getMetsFile()
	{
		String metsPath = getExtractAlephIDPath() + config.getMetsFileName();

		File metsFile = new File(metsPath);

		return metsFile;
	}
	
	
	/**
	 * returns File with correct DC file path 
	 * 
	 * @return File
	 */
	public File getDcExtractFile()
	{
		return new File(config.getExtractPath() + getWorkingPath().getName() + config.getSipXmlPath() + config.getDcFileName());
	}
	
	
	/**
	 * returns File with correct IE file path 
	 * 
	 * @return
	 */
	public File getIeExtractFile()
	{
		return new File(config.getExtractPath() + getWorkingPath().getName() + config.getSipXmlPath() + config.getIeFileName());
	}
	

	/**
	 * sets object variable ConfigProperties config
	 * 
	 * @param ConfigProperties
	 */
	private void setConfig(ConfigProperties config)
	{
		this.config = config;
	}
	
	
	/**
	 * returns supplied File.length (byte) as MB
	 * 
	 * @param fileLength
	 * @return integer
	 */
	public static String getSize(long fileLength)
	{
		String returnSize = ""; 
		int calculatedsize = 0;
		
		if(fileLength < ConfigProperties.FILESIZ_MB)
		{
			calculatedsize = (int) (fileLength / ConfigProperties.FILESIZ_KB);
			returnSize = calculatedsize + "kb";
		}
		else
		{
			calculatedsize = (int) (fileLength / ConfigProperties.FILESIZ_MB);
			returnSize = calculatedsize + "MB";
		}
				
		return returnSize;
		
	}

}
