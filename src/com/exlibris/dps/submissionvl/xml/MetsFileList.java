package com.exlibris.dps.submissionvl.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mets file list Object
 * 
 * getter and setter to fill the mets file list and functionality to put
 * everything in a HashMap to be used as an object in the Mets object
 * 
 * @author Lars Haendler
 * 
 */

public class MetsFileList
{

	private String id;
	private String mimetype;
	private String created;
	private String checksum;
	private String checksumtype;
	private String size;
	private String filename;
	private String filepath;
	private String filelabel;


	/**
	 * constructor without initialisation
	 */
	public MetsFileList()
	{
	}


	/**
	 * creates a HashMap of all data in MetsFileList 
	 * 
	 * @return Map<String, String> containing all MetsFileList data
	 */
	public Map<String, String> getFileListMap()
	{
		Map<String, String> metsFileListMap = new HashMap<String, String>();

		metsFileListMap.put(Mets.ID, getId());
		metsFileListMap.put(Mets.MIMETYPE, getMimetype());
		metsFileListMap.put(Mets.CREATED, getCreated());
		metsFileListMap.put(Mets.CHECKSUM, getChecksum());
		metsFileListMap.put(Mets.CHECKSUMTYPE, getChecksumtype());
		metsFileListMap.put(Mets.SIZE, getSize());
		metsFileListMap.put(Mets.FILENAME, getFilename());
		metsFileListMap.put(Mets.FILEPATH, getFilepath());
		metsFileListMap.put(Mets.FILELABEL, getFilelabel());

		return metsFileListMap;
	}


	/**,
	 * Getter for id
	 * 
	 * @return String id
	 */
	public String getId()
	{
		return id;
	}


	/**
	 * Setter for id
	 * 
	 * @param id
	 */
	public void setId(String id)
	{
		this.id = id;
	}


	/**
	 * Getter for mimetype
	 * 
	 * @return String mimetype
	 */
	public String getMimetype()
	{
		return mimetype;
	}


	/**
	 * Setter for mimetype 
	 * 
	 * @param mimetype
	 */
	public void setMimetype(String mimetype)
	{
		this.mimetype = mimetype;
	}


	/**
	 * Getter for created 
	 * 
	 * @return String created
	 */
	public String getCreated()
	{
		return created;
	}

	/**
	 * Setter for created
	 * 
	 * @param created
	 */
	public void setCreated(String created)
	{
		this.created = created;
	}

	/**
	 * Getter for checksum
	 * 
	 * @return String checksum
	 */
	public String getChecksum()
	{
		return checksum;
	}

	/**
	 * Setter for checksum
	 * 
	 * @param checksum
	 */
	public void setChecksum(String checksum)
	{
		this.checksum = checksum;
	}

	/**
	 * Getter for checksumtype
	 * 
	 * @return String checksumtype
	 */
	public String getChecksumtype()
	{
		return checksumtype;
	}

	/**
	 * Setter for checksumtype
	 * 
	 * @param checksumtype
	 */
	public void setChecksumtype(String checksumtype)
	{
		this.checksumtype = checksumtype;
	}

	
	public String getFilelabel()
	{
		return filelabel;
	}	
	
	
	/**
	 * Getter for size
	 * 
	 * @return String size
	 */
	public String getSize()
	{
		return size;
	}

	
	/**
	 * Setter for size
	 * 
	 * @param size
	 */
	public void setSize(String size)
	{
		this.size = size;
	}


	/**
	 * Getter for filename
	 * 
	 * @return String filename
	 */
	public String getFilename()
	{
		return filename;
	}


	/**
	 * Setter for filename
	 * 
	 * @param filename
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}


	/**
	 * Getter for filepath
	 * 
	 * @return String filepath
	 */
	public String getFilepath()
	{
		return filepath;
	}

	/**
	 * Setter for filepath
	 * 
	 * @param filepath
	 */
	public void setFilepath(String filepath)
	{
		this.filepath = filepath;
	}
	
	/**
	 * Getter for filepathIsContained
	 * 
	 * @return boolean true/false
	 */
	public boolean filepathIsContained(List<String> textFileDirectories)
	{
		for (String singleDir : textFileDirectories) {
			if (filepath.contains(singleDir)) {
				return true;
			}
		}
		return false;
	}	

	
	public void setFilelabel(String filelabel)
	{
		this.filelabel = filelabel;
	}
	
	public void setFilelabelFromFilename()
	{
		this.filelabel = getFilename().substring(0, getFilename().lastIndexOf('.'));
	}	
	

	@Override
	public String toString()
	{
		return "MetsFileList [id=" + id + ", mimetype=" + mimetype + ", created=" + created
				+ ", checksum=" + checksum + ", checksumtype=" + checksumtype + ", size=" + size
				+ ", filename=" + filename + ", filepath=" + filepath + ", filelabel=" + filelabel +"]\n";
	}


}
