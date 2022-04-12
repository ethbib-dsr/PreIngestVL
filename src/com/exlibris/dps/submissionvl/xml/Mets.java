package com.exlibris.dps.submissionvl.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Mets Object
 * 
 * contain behaviour and data for one Mets object that has been created from a
 * export_mets.xml file
 * 
 * @author Lars Haendler
 * 
 */
public class Mets
{
	static final String ID = "id";
	static final String MIMETYPE = "mimetype";
	static final String CREATED = "created";
	static final String CHECKSUM = "checksum";
	static final String CHECKSUMTYPE = "checksumtype";
	static final String SIZE = "size";
	static final String FILENAME = "filename";
	static final String FILEPATH = "filepath";
	static final String FILELABEL = "filelabel";

	static final String DOI = "doi";
	static final String SYSTEMID = "systemid";
	static final String ALT_TITLE = "altTitle";
	static final String LOCATION = "location";

	private String recordIdentifier;
	private String doi;
	private String altTitle;
	private String location;

	private List<MetsFileList> fileList;


	/**
	 * Constructor
	 * 
	 */
	public Mets()
	{
		fileList = new ArrayList<MetsFileList>();
	}


	/**
	 * Add an MetsFileList object to fileList ArrayList
	 * 
	 * @param metsFileList
	 */
	public void addFileList(MetsFileList metsFileList)
	{
		fileList.add(metsFileList);
	}

	
	/**
	 * Getter for fileList
	 * 
	 * @return
	 */
	public List<MetsFileList> getFileList()
	{
		return fileList;
	}
	

	/**
	 * Getter for recordIdentifier
	 * 
	 * @return String recordIdentifier
	 */
	public String getRecordIdentifier()
	{
		return recordIdentifier;
	}


	/**
	 * Setter for recordIdentifier
	 * 
	 * @param recordIdentifier
	 */
	public void setRecordIdentifier(String recordIdentifier)
	{
		this.recordIdentifier = recordIdentifier;
	}


	/**
	 * Getter for doi
	 * 
	 * @return String doi
	 */
	public String getDoi()
	{
		return doi;
	}


	/**
	 * Setter for doi
	 * 
	 * @param doi
	 */
	public void setDoi(String doi)
	{
		this.doi = doi;
	}


	/**
	 * Getter for AlternativeTitle
	 * 
	 * @return String altTitle
	 */
	public String getAlternativeTitle()
	{
		return altTitle;
	}


	/**
	 * Setter for AlternativeTitle
	 * 
	 * @param altTitle
	 */
	public void setAlternativeTitle(String altTitle)
	{
		this.altTitle = altTitle;
	}


	/**
	 * Getter for Location
	 * 
	 * @return String location
	 */
	public String getLocation()
	{
		return location;
	}


	/**
	 * Setter for Location
	 * 
	 * @param location
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}


	@Override
	public String toString()
	{
		return "Mets [systemid=" + recordIdentifier + ", doi=" + doi + ", fileList=" + fileList + "]";
	}

}
