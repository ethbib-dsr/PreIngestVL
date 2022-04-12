package com.exlibris.dps.submissionvl.util;

public class VisualLibraryTimestamp implements Comparable<VisualLibraryTimestamp>
{
	private String visualLibraryTimestamp;
	private int intDate;
	private int intTime;
	private final String SPLITTER = "T"; 
	

	public VisualLibraryTimestamp(String visualLibraryTimestamp)
	{
		this.visualLibraryTimestamp = visualLibraryTimestamp;
		splitSourceTimestamp();
	}

	private void splitSourceTimestamp()
	{
		String[] timeArray = getSourceVisualLibraryTimestamp().split(SPLITTER,2);
		setIntDate(Integer.parseInt(timeArray[0]));
		setIntTime(Integer.parseInt(timeArray[1]));
	}
	
	public String getSourceVisualLibraryTimestamp()
	{
		return visualLibraryTimestamp;
	}

	public void setSourceVisualLibraryTimestamp(String sourceVisualLibraryTimestamp)
	{
		this.visualLibraryTimestamp = sourceVisualLibraryTimestamp;
	}

	public int getIntDate()
	{
		return intDate;
	}

	public void setIntDate(int intDate)
	{
		this.intDate = intDate;
	}

	public int getIntTime()
	{
		return intTime;
	}

	public void setIntTime(int intTime)
	{
		this.intTime = intTime;
	}

	@Override
	public int compareTo(VisualLibraryTimestamp ts)
	{
		if(this.getIntDate() > ts.getIntDate())
		{
			return 1;
		}
		
		if(this.getIntDate() < ts.getIntDate() )
		{
			return -1;
		}
		
		if(this.getIntDate() == ts.getIntDate())
		{
			if(this.getIntTime() > ts.getIntTime())
			{
				return 1;
			}
			if(this.getIntTime() < ts.getIntTime())
			{
				return -1;
			}
		}

		return 0;
	}

	@Override
	public String toString()
	{
		return "VisualLibraryTimestamp [sourceVisualLibraryTimestamp=" + visualLibraryTimestamp + ", intDate="
				+ intDate + ", intTime=" + intTime + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof VisualLibraryTimestamp)) return false;
		
		VisualLibraryTimestamp ts = (VisualLibraryTimestamp) obj;
		
		return ts.getIntDate()==this.getIntDate() && ts.getIntTime()==this.getIntTime();
	}

	@Override 
	public int hashCode() 
	{
		return getSourceVisualLibraryTimestamp().hashCode();
	}
	

}
