package com.exlibris.dps.submissionvl.util;

import com.exlibris.dps.submissionvl.ConfigProperties;


public class SourceSip implements Comparable<SourceSip>
{

	public enum SipTypeEnum {
		MASTER, GEN, UNKNOWN
	};	
	
	public enum CapsuleTypeEnum {
		ALEPH, DOI, UNKNOWN
	}
	
	private String fileName;
	private String capsuleID;
	private String doi;
	private VisualLibraryTimestamp timestamp;
	private SipTypeEnum sipType;
	private CapsuleTypeEnum capsuleType;
	private int genVersion;
	private String sourcePath;
	private String targetPath;
	private long fileSize;
	private String fileExtension;
	private int position;
	
	static final String SPLITTER_FILENAME = "_";
	
	
	/**
	 * Default constructor
	 * that also fills all variables from supplied data
	 * 
	 * @param fileName
	 * @param sourcePath
	 * @param targetPath
	 * @param fileSize
	 */
	public SourceSip(String fileName, String sourcePath, String targetPath, long fileSize, int position)
	{
		super();
		this.fileName = fileName;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.fileSize = fileSize;
		this.position = position;
		fillSourceSip();
	}


	/**
	 * fills capsuleID (alephID or DOI), timestamp, sipType, capsuleType, genVersion for current instance
	 * done by splitting file name into array
	 * each element will than be handled separately
	 * Since Version 1.7.3 capsules can either be of type alephID or DOI
	 * e.g. alephID = 006484184_20110727T230147_master_ver1.zip
	 *      DOI     = 10_3931_e-rara-9083_20201009T032247_gen7_ver1.zip
	 * 
	 */
	private void fillSourceSip()
	{
		String fileNameArray[] = getFileName().split(SPLITTER_FILENAME);
		
		if (fileNameArray.length > 4) {
			// 10_3931_e-rara-9083 --> 10.3931/e-rara-9083
			setDOI(fileNameArray[0]+"."+fileNameArray[1]+"/"+fileNameArray[2]);
			setCapsuleID(fileNameArray[0]+"_"+fileNameArray[1]+"_"+fileNameArray[2]);
			setTimestamp(fileNameArray[3]);
			setSipType(extractSipType(fileNameArray[4]));
			setGenVersion(extractVersionNumber(fileNameArray[4]));
			setCapsuleType(CapsuleTypeEnum.DOI);
		} else {
			setCapsuleID(fileNameArray[0]);
			setTimestamp(fileNameArray[1]);
			setSipType(extractSipType(fileNameArray[2]));
			setGenVersion(extractVersionNumber(fileNameArray[2]));
			setCapsuleType(CapsuleTypeEnum.ALEPH);
		}
		
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
	 * capsuleID getter
	 * 
	 * @return
	 */
	public String getCapsuleID()
	{
		return capsuleID;
	}


	/**
	 * alephID setter
	 * 
	 * @param capsuleID
	 */
	public void setCapsuleID(String capsuleID)
	{
		this.capsuleID = capsuleID;
	}
	
	/**
	 * DOI getter
	 * 
	 * @return
	 */
	public String getDOI()
	{
		return doi;
	}


	/**
	 * DOI setter
	 * 
	 * @param alephID
	 */
	public void setDOI(String doi)
	{
		this.doi = doi;
	}

	/**
	 * timestamp getter
	 * 
	 * @return
	 */
	public VisualLibraryTimestamp getTimestamp()
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
		this.timestamp = new VisualLibraryTimestamp(timestamp);
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
	 * sipType getter
	 * 
	 * @return SipTypeEnum
	 */
	public SipTypeEnum getSipType()
	{
		return sipType;
	}


	/**
	 * capsuleType setter
	 * 
	 * @param capsuleType
	 */
	public void setCapsuleType(CapsuleTypeEnum capsuleType)
	{
		this.capsuleType = capsuleType;
	}
	
	/**
	 * capsuleType getter
	 * 
	 * @return CapsuleTypeEnum
	 */
	public CapsuleTypeEnum getCapsuleType()
	{
		return capsuleType;
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
		return getCapsuleID() + SPLITTER_FILENAME + getTimestamp().getSourceVisualLibraryTimestamp();
	}
	
	
	/**
	 * position getter
	 * 
	 * @return 
	 */
	public int getPosition() {
		return position;
	}
	

	/**
	 * compares depending on getPosition()
	 * 
	 */
	@Override
	public int compareTo(SourceSip otherSourceSip)
	{	   
	   // DDE-800, SourceSip compares to its size 
	   // but on 11.11.2020, we decided, that the position in the listfile
	   // is more important
		
	   if (this.getPosition() == otherSourceSip.getPosition()) {
		   return 0;
	   } else if (this.getPosition() > otherSourceSip.getPosition()) {
		   return 1;
	   } else {
		   return -1;
	   }   
	}
	
	
	/**
	 * redefining equal 
	 * 
	 */
	public boolean equals(Object o)
	{
		if (o == this) {
			return true;
		}
		
		if (! (o instanceof SourceSip))	{
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
	 * make capsuleID comparable
	 * 
	 * @param o Object
	 * @return boolean
	 */
	public boolean sameCapsuleID(SourceSip s)
	{
		return s.getCapsuleID().equals(getCapsuleID());
	}
	
	/**
	 * make DOI comparable
	 * 
	 * @param o Object
	 * @return boolean
	 */
	public boolean sameDOI(SourceSip s)
	{
		return s.getDOI().equals(getDOI());
	}
	

	/**
	 * toString implementation
	 */
	@Override
	public String toString()
	{
		return "SourceSip [fileName=" + fileName + ", capsuleID=" + capsuleID + ", DOI=" + doi + ", timestamp="
				+ timestamp + ", sipType=" + sipType + ", genVersion=" + genVersion + ", sourcePath="
				+ sourcePath + ", fileSize=" + fileSize + "]";

	}

}
