package com.exlibris.dps.submissionvl.util;

public class AlephTimestamp implements Comparable<AlephTimestamp>
{
	private String sourceAlephTimestamp;
	private int intDate;
	private int intTime;
	private final String SPLITTER = "T"; 
	

	public AlephTimestamp(String alephTimestamp)
	{
		this.sourceAlephTimestamp = alephTimestamp;
		splitSourceTimestamp();
	}

	private void splitSourceTimestamp()
	{
		String[] timeArray = getSourceAlephTimestamp().split(SPLITTER,2);
		setIntDate(Integer.parseInt(timeArray[0]));
		setIntTime(Integer.parseInt(timeArray[1]));
	}
	
	public String getSourceAlephTimestamp()
	{
		return sourceAlephTimestamp;
	}

	public void setSourceAlephTimestamp(String sourceAlephTimestamp)
	{
		this.sourceAlephTimestamp = sourceAlephTimestamp;
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
	public int compareTo(AlephTimestamp ts)
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
		return "AlephTimestamp [sourceAlephTimestamp=" + sourceAlephTimestamp + ", intDate="
				+ intDate + ", intTime=" + intTime + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof AlephTimestamp)) return false;
		
		AlephTimestamp ts = (AlephTimestamp) obj;
		
		return ts.getIntDate()==this.getIntDate() && ts.getIntTime()==this.getIntTime();
	}

	@Override 
	public int hashCode() 
	{
		return getSourceAlephTimestamp().hashCode();
	}
	

}
