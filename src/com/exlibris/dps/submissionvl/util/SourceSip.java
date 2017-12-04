package com.exlibris.dps.submissionvl.util;

import com.exlibris.dps.submissionvl.ConfigProperties;


public class SourceSip implements Comparable<SourceSip>
{

	public enum SipTypeEnum {
		MASTER, GEN, UNKNOWN
	};	
	
	private String fileName;
	private String alephID;
	private AlephTimestamp timestamp;
	private SipTypeEnum sipType;
	private int genVersion;
	private String sourcePath;
	private String targetPath;
	private String institute;
	private long fileSize;
	private String fileExtension;
	
	private String SPLITTER_FILENAME = "_";
	
	
	/**
	 * Default constructor
	 * that also fills all variables from supplied data
	 * 
	 * @param fileName
	 * @param sourcePath
	 * @param targetPath
	 * @param fileSize
	 * @param institute
	 */
	public SourceSip(String fileName, String sourcePath, String targetPath, long fileSize, String institute)
	{
		super();
		this.fileName = fileName;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.fileSize = fileSize;
		this.institute = institute;
		fillSourceSip();
	}


	/**
	 * fills alephID, timestamp, sipType, genVersion for current instance
	 * done by splitting file name into array
	 * each element will than be handled separately
	 * 
	 */
	private void fillSourceSip()
	{
		String fileNameArray[] = getFileName().split(SPLITTER_FILENAME);

		setAlephID(fileNameArray[0]);
		setTimestamp(fileNameArray[1]);
		setSipType(extractSipType(fileNameArray[2]));
		setGenVersion(extractVersionNumber(fileNameArray[2]));
		setFileExtension(extractFileExtensionFromName());
	}


	/**
	 * extracts file extension from current file name
	 * 
	 * @return String
	 */
	private String extractFileExtensionFromName()
	{
		String extension = "";

		if (getFileName().lastIndexOf('.') > 0) {
		    extension = getFileName().substring(getFileName().lastIndexOf('.')+1);
		}
		
		return extension;
	}
	
	
	/**
	 * Setter for file extension
	 * 
	 * @param extension
	 */
	private void setFileExtension(String extension)
	{
		this.fileExtension = extension;
	}
	
	
	/**
	 * Return the file extension the SIP file
	 * 
	 * @return
	 */
	public String getFileExtension()
	{
		return this.fileExtension;
	}
	
	
	/**
	 * extracts version number from a string that looks like "gen1", "gen12", etc
	 * a default version is set if SipType is master or unknown
	 * only gen can have a version
	 * 
	 * @param versionString
	 * @return
	 */
	private int extractVersionNumber(String versionString)
	{
		if(getSipType() == SipTypeEnum.GEN)
		{
			return Integer.parseInt(versionString.substring(3));
		}
		else if(getSipType() == SipTypeEnum.MASTER)
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
	
	
	/**
	 * extracts SipTypeEnum depending on substring in supplied data
	 * 
	 * @param typeString
	 * @return
	 */
	private SipTypeEnum extractSipType(String typeString)
	{
		if(typeString.startsWith("master"))
		{
			return SipTypeEnum.MASTER;
		}
		else if(typeString.startsWith("gen"))
		{
			return SipTypeEnum.GEN;
		}
		else
		{
			return SipTypeEnum.UNKNOWN;
		}
	}
	
	/**
	 * filename getter
	 * 
	 * @return
	 */
	public String getFileName()
	{
		return fileName;
	}


	/**
	 * filename setter
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}


	/**
	 * alephID getter
	 * 
	 * @return
	 */
	public String getAlephID()
	{
		return alephID;
	}


	/**
	 * alephID setter
	 * 
	 * @param alephID
	 */
	public void setAlephID(String alephID)
	{
		this.alephID = alephID;
	}


	/**
	 * timestamp getter
	 * 
	 * @return
	 */
	public AlephTimestamp getTimestamp()
	{
		return timestamp;
	}


	/**
	 * timestamp setter
	 * 
	 * @param timestamp
	 */
	public void setTimestamp(String timestamp)
	{
		this.timestamp = new AlephTimestamp(timestamp);
	}

	
	/**
	 * sourcePath getter
	 * 
	 * @return
	 */
	public String getSourcePath()
	{
		return sourcePath;
	}

	/**
	 * sourcePath setter
	 * 
	 * @param sourcePath
	 */
	public void setSourcePath(String sourcePath)
	{
		this.sourcePath = sourcePath;
	}
	
	
	/**
	 * fileSize getter
	 * 
	 * @return long
	 */
	public long getFileSize()
	{
		return fileSize;
	}

	
	public long getFileSizeInMb()
	{
		return (long) (getFileSize()/ConfigProperties.FILESIZ_MB);
		
	}
	

	/**
	 * fileSize setter
	 * 
	 * @param fileSize
	 */
	public void setFileSize(long fileSize)
	{
		this.fileSize = fileSize;
	}	
	
	
	/**
	 * institute getter
	 * 
	 * @return
	 */
	public String getInstitute()
	{
		return institute;
	}
	

	/**
	 * institute setter
	 * 
	 * @param institute
	 */
	public void setInstitute(String institute)
	{
		this.institute = institute;
	}


	/**
	 * sipType getter
	 * 
	 * @return SipTypeEnum
	 */
	public SipTypeEnum getSipType()
	{
		return sipType;
	}


	/**
	 * sipType setter
	 * 
	 * @param sipType
	 */
	public void setSipType(SipTypeEnum sipType)
	{
		this.sipType = sipType;
	}

	
	/**
	 * genVersion getter
	 * 
	 * @return
	 */
	public int getGenVersion()
	{
		return genVersion;
	}


	/**
	 * genVersion setter
	 * 
	 * @param genVersion
	 */
	public void setGenVersion(int genVersion)
	{
		this.genVersion = genVersion;
	}

	
	/**
	 * targetPath getter
	 * 
	 * @return
	 */
	public String getTargetPath()
	{
		return targetPath;
	}


	/**
	 * targetPath setter
	 * 
	 * @param targetPath
	 */
	public void setTargetPath(String targetPath)
	{
		this.targetPath = targetPath;
	}
	
	
	public String getAmdIdFromFilename()
	{
		return getAlephID() + SPLITTER_FILENAME + getTimestamp().getSourceAlephTimestamp();
	}
	

	/**
	 * compares depending on timestamp
	 * 
	 */
	@Override
	public int compareTo(SourceSip otherSourceSip)
	{
		return fileName.compareTo(otherSourceSip.fileName); 
	}
	
	
	/**
	 * redefining equal 
	 * 
	 */
	public boolean equals(Object o)
	{
		if(! (o instanceof SourceSip))
		{
			return false;
		}
		SourceSip s = (SourceSip)o;
		
		return s.fileName.equals(fileName);
	}
	
	
	/**
	 * redefining hashCode 
	 * 
	 */
	public int hashCode()
	{
		return fileName.hashCode();
	}


	/**
	 * make aleph id comparable
	 * 
	 * @param o Object
	 * @return boolean
	 */
	public boolean sameAlephId(SourceSip s)
	{
		return s.getAlephID().equals(getAlephID());
	}
	

	/**
	 * toString implementation
	 */
	@Override
	public String toString()
	{
		return "SourceSip [fileName=" + fileName + ", alephID=" + alephID + ", timestamp="
				+ timestamp + ", sipType=" + sipType + ", genVersion=" + genVersion + ", sourcePath="
				+ sourcePath + ", fileSize=" + fileSize + ", institute=" + institute + "]";

	}

}
