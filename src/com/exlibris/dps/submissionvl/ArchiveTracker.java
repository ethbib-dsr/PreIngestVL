package com.exlibris.dps.submissionvl;

public class ArchiveTracker
{

	private String archiveStatus;	
	private String archiveFileName;

	
	public ArchiveTracker(String fileName)
	{
		setArchiveFileName(fileName);
	}
	
	
	public String getArchiveFileName()
	{
		return archiveFileName;
	}

	private void setArchiveFileName(String archiveFileName)
	{
		this.archiveFileName = archiveFileName;
	}

	public String getArchiveStatus()
	{
		return archiveStatus;
	}

	public void setArchiveStatus(String archiveStatus)
	{
		this.archiveStatus = archiveStatus;
	}

}
