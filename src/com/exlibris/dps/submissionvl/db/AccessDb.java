package com.exlibris.dps.submissionvl.db;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.ConfigProperties;
import com.exlibris.dps.submissionvl.util.SourceSip;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AccessDb
{

	private final Logger logger = Logger.getLogger(this.getClass());
	
	private ConfigProperties config;
	private Connection conn = null;

	private final String COMMA = ",";
	private final char ORACLE_QUOTES = '\'';
	private final String TOTAL_PARAM = "total";
	private final String FROM = "from";
	private final String AND = "and";
	private final String SELECT = "select";
	private final String WHERE = "where";
	private final char SPACE = ' ';
	
	private final String COUNT_AMDID = "COUNT_AMDID";
	private final String COUNT_ALEPHID = "COUNT_ALEPHID";
	private final String COUNT_ALEPHID_FINISHED = "COUNT_ALEPHID_FINISHED";

	
	/**
	 * default constructor
	 * 
	 * @param config
	 */
	public AccessDb(ConfigProperties config)
	{
		this.config = config;
	}
	
	
	/**
	 * Returns number of AlephId occurences in table
	 * 
	 * @param alephId
	 * @return
	 */
	public int countRecordsWithAlephID(String alephId)
	{
		setupConnection();
		int total = countOccurencesAlephId(alephId);
		closeConnection();
		
		return total;
	}
	
	
	/**
	 * Returns number of AlephId occurences that also 
	 * have SIP_STATUS='FINISHED'
	 * 
	 * @param alephId
	 * @return
	 */
	public int countRecordsWithAlephIdAndFinished(String alephId)
	{
		setupConnection();
		int total = countOccurencesAlephIdAndFinished(alephId);
		closeConnection();
		
		return total;
	}
	
	
	/**
	 * Returns number of AMD_ID occurences in DB
	 * bigger than 1 means not unique
	 * 
	 * @param amdID
	 * @return
	 */
	public int countRecordsWithAmdId(String amdID)
	{
		setupConnection();
		int total = countOccurencesAmdId(amdID);
		closeConnection();
		
		return total;
	}
	
	
	public void updateStatusFromAmdId(String amdID, String status)
	{
		setupConnection();
		executeUpdateStatus(amdID, status);	
		closeConnection();
	}
	
	
	public void insertSipIntoDB(SourceSip s)
	{
		setupConnection();
		executeInsertSipIntoDB(s);
		closeConnection();
	}
	
	
	
	
	private void executeInsertSipIntoDB(SourceSip s)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(buildInsertStatement(s));
			stmt.executeUpdate();	
		}
		catch (SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		
	}
	
	
	
	/**
	 * Return a List of maps with AMD_ID, TIMESTAMP, SIP_STATUS key-value pair
	 * from DB table that have the supplied Aleph ID as part of AMD_ID
	 * 
	 * @param alephId
	 * @return
	 */
	public List<Map<String, String>> getRecordsWithAlephID(String alephId)
	{
		setupConnection();
		List<Map<String, String>> records = getAlephIdSelectRecords(alephId);
		closeConnection();
		
		return records;
	}
	

	
	
	private void executeUpdateStatus(String amdID, String status)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(buildStatusUpdateStatement());
			stmt.setString(1, status);
			stmt.setString(2, amdID);
			stmt.executeUpdate();	
		}
		catch (SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}		
		
	}
	
	
	private String buildStatusUpdateStatement()
	{
		StringBuilder sql = new StringBuilder(200);
		
		sql.append(addRightSpace("update"));
		sql.append(addRightSpace(config.getDbEraraTable()));
		sql.append(addRightSpace("set"));
		sql.append(addRightSpace(config.getDbRowSipStatus()));
		sql.append(addRightSpace("=?" + addRightSpace(COMMA)));
		sql.append(config.getDbRowUpdateDate() + "=");
		sql.append(getOracleCurrentDateValue());
		sql.append(addLeftAndRightSpace(WHERE));
		sql.append(addRightSpace(config.getDbRowAmd()));
		sql.append("=?");
		
		return sql.toString();
	}
	
	
	
	/**
	 * Build insert statement for a supplied SourceSip
	 * 
	 * @param s
	 * @return
	 */
	private String buildInsertStatement(SourceSip s)
	{
		StringBuilder sql = new StringBuilder(1000);
		
		sql.append("insert into ");
		sql.append(config.getDbEraraTable());
		sql.append(addLeftSpace("("));
		sql.append(config.getDbRowWorkflow() + addRightSpace(COMMA));
		sql.append(config.getDbRowWorkspace() + addRightSpace(COMMA));
		sql.append(config.getDbRowAmd() + addRightSpace(COMMA));
		sql.append(config.getDbRowSubmitTs() + addRightSpace(COMMA));
		sql.append(config.getDbRowSourcePath() + addRightSpace(COMMA));
		sql.append(config.getDbRowSipType() + addRightSpace(COMMA));
		sql.append(config.getDbRowSipStatus() + addRightSpace(COMMA));
		sql.append(config.getDbRowUpdateDate() + addRightSpace(COMMA));
		sql.append(config.getDbRowSipPath() + addRightSpace(COMMA));
		sql.append(config.getDbRowSipName());
		sql.append(addRightSpace(")"));
		sql.append("values (");
		sql.append(encapsulateInSingleQuotes(config.getDbInsertWorkspaceId()+"_"+config.getCurrentTime()));
		sql.append(encapsulateInSingleQuotes(config.getDbInsertWorkspaceId())); 
		sql.append(encapsulateInSingleQuotes(s.getAlephID()+"_"+s.getTimestamp().getSourceAlephTimestamp()));
		sql.append(getOracleCurrentDateValue() + COMMA);
		sql.append(encapsulateInSingleQuotes(s.getSourcePath()));
		sql.append(encapsulateInSingleQuotes(config.getDbInsertSipType()));
		sql.append(encapsulateInSingleQuotes(config.getDbStatusInitialized()));
		sql.append(getOracleCurrentDateValue() + COMMA);
		sql.append(encapsulateInSingleQuotes(s.getTargetPath()));
		sql.append(encapsulateInSingleQuotes(s.getFileName(),false));
		sql.append(addRightSpace(")"));
		
		return sql.toString();
	}
	
	
	
	/**
	 * return current date/time enclosed in correct Oracle statements
	 * 
	 * @return
	 */
	private StringBuilder getOracleCurrentDateValue()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("to_date");
		sb.append("(");
		sb.append(encapsulateInSingleQuotes(config.getCurrentTime()));
		sb.append(encapsulateInSingleQuotes(config.getDbCurrentTimeOracleFormat(), false));
		sb.append(")");
		
		return sb;
		
	}
	
	
	/**
	 * Helper that returns any supplied string enclosed in single quotes
	 * additionally a comma at the end can be attached
	 * 
	 * @param s
	 * @param comma
	 * @return
	 */
	private StringBuilder encapsulateInSingleQuotes(String s, Boolean comma)
	{
		StringBuilder sb = new StringBuilder(s.length()+2);
		sb.append(ORACLE_QUOTES).append(s).append(ORACLE_QUOTES);
		if(comma)
		{
			sb.append(addRightSpace(COMMA));
		}
		
		return sb;
	}
	
	
	/**
	 * Helper that returns any supplied with spaces to the left and/or the right
	 * 
	 * @param s
	 * @param left
	 * @param right
	 * @return
	 */
	private StringBuilder encapsulateInSpaces(String s, Boolean left, Boolean right)
	{
		StringBuilder sb = new StringBuilder(s.length()+2);
		
		if(left)
		{
			sb.append(SPACE);
		}
		
		sb.append(s);
		
		if(right)
		{
			sb.append(SPACE);
		}
		
		return sb;
	}
	
	
	/**
	 * Helper that returns any supplied with space to the left
	 * makes uses of abstract encapsulateInSpaces()
	 * 
	 * @param s
	 * @return
	 */
	private StringBuilder addLeftSpace(String s)
	{
		return encapsulateInSpaces(s, true, false);
	}
	
	
	/**
	 * Helper that returns any supplied with space to the right
	 * makes use of abstract encapsulateInSpaces()
	 * 
	 * @param s
	 * @return
	 */
	private StringBuilder addRightSpace(String s)
	{
		return encapsulateInSpaces(s, false, true);
	}
	
	
	/**
	 * Helper that returns any supplied with space to the left and the right
	 * makes use of abstract encapsulateInSpaces()
	 * 
	 * @param s
	 * @return
	 */
	private StringBuilder addLeftAndRightSpace(String s)
	{
		return encapsulateInSpaces(s, true, true);
	}
	
	
	/**
	 * Helper that returns any supplied string enclosed in single quotes
	 * additionally a comma at the end will ALWAYS be attached
	 * makes uses of abstract encapsulateInSingleQuotes()
	 * 
	 * @param s
	 * @return
	 */
	private StringBuilder encapsulateInSingleQuotes(String s)
	{
		return encapsulateInSingleQuotes(s, true);
	}	
	
	
	/**
	 * executes query that retrieves count from supplied AlephId
	 * 
	 * @param alephID
	 * @return
	 */
	private int countOccurences(String alephID, String statementChooser)
	{
		int total = -1;
		String statement;
		
		switch(statementChooser) 
		{
			case COUNT_ALEPHID :
				statement = buildAlephIdCountStatement(alephID);
				break;
			case COUNT_ALEPHID_FINISHED :
				statement = buildFinishedAlephIdCountStatement(alephID);
				break;
			case COUNT_AMDID :
				statement = buildCountAmdIdStatement(alephID);
				break;
			default :
				statement = "";
				break;
		}
	
		
		try
		{
			PreparedStatement stmt = conn.prepareStatement(statement);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			total = rs.getInt(TOTAL_PARAM);
		}
		catch (SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		
		return total;
	}
	
	
	/**
	 * calls countOccurences and returns count in table from supplied AlephId
	 * 
	 * @param alephID
	 * @return
	 */
	private int countOccurencesAlephId(String alephID)
	{
		return countOccurences(alephID, COUNT_ALEPHID);
	}
	
	
	/**
	 * calls countOccurences and returns count in table from supplied AlephId 
	 * and that have SIP_STATUS = 'FINISHE'
	 * 
	 * @param alephID
	 * @return
	 */
	private int countOccurencesAlephIdAndFinished(String alephID)
	{
		return countOccurences(alephID, COUNT_ALEPHID_FINISHED);
	}
	
	
	/**
	 * Calls countOccurences and return count in of supplied complete amdID
	 * makes sure that that amdID is unique
	 * 
	 * @param amdID
	 * @return
	 */
	private int countOccurencesAmdId(String amdID)
	{
		return countOccurences(amdID, COUNT_AMDID);
	}
	
		
	/**
	 * builds sql query that counts all occurences of AlephId in AMD_ID
	 * 
	 * @param alephId
	 * @return
	 */
	private String buildAlephIdCountStatement(String alephId)
	{
		StringBuilder sql = new StringBuilder(200);
		
		sql.append(addRightSpace(config.getDbSelectCountAmdId()));
		sql.append(TOTAL_PARAM);
		sql.append(addLeftAndRightSpace(FROM));
		sql.append(config.getDbEraraTable());
		sql.append(addLeftAndRightSpace(config.getDbSelectCountWhere()));
		sql.append(encapsulateInSingleQuotes(alephId, false));
		
		return sql.toString();
	}	
	
	
	/**
	 * builds sql query that counts all occurences of AlephId that have
	 * status finished
	 * 
	 * @param alephId
	 * @return
	 */
	private String buildFinishedAlephIdCountStatement(String alephId)
	{
		StringBuilder sql = new StringBuilder(200);
		
		sql.append(addRightSpace(config.getDbSelectCountAmdId()));
		sql.append(addRightSpace(TOTAL_PARAM));
		sql.append(addRightSpace(FROM));
		sql.append(addRightSpace(config.getDbEraraTable()));
		sql.append(config.getDbSelectCountWhere());
		sql.append(encapsulateInSingleQuotes(alephId, false));
		sql.append(addLeftAndRightSpace(AND));
		sql.append(config.getDbRowSipStatus());
		sql.append(" IN" + addLeftAndRightSpace(buildSQLinValueGroup(config.getDbRowStatusFinished())));
		
		
		return sql.toString();
	}
	
	
	/**
	 * build sql query for IN value group from a CSV group
	 * 
	 * @param commaValueGroup
	 * @return String
	 */
	private String buildSQLinValueGroup(String commaValueGroup)
	{
		StringBuilder sql = new StringBuilder(commaValueGroup.length() + 2);
		
		//opening brackets for IN values
		sql.append("(");
		
		String[] commaValueArray = commaValueGroup.split(",");

		//iterate over each element in the CSV group
		for(int i=0; i<commaValueArray.length; i++)
		{
			if((i+1)<commaValueArray.length)
			{
				sql.append(encapsulateInSingleQuotes(commaValueArray[i].trim(), true));
			}
			//last element in the IN value group is not followed by a comma
			else
			{
				sql.append(encapsulateInSingleQuotes(commaValueArray[i].trim(), false));
			}
		}
	
		//closing brackets for IN values
		sql.append(")");
		
		return sql.toString();
	}
	
	
	
	private String buildCountAmdIdStatement(String amdID)
	{
		StringBuilder sql = new StringBuilder(200);
		
		sql.append(addRightSpace(config.getDbSelectCountAmdId()));
		sql.append(addRightSpace(TOTAL_PARAM));
		sql.append(addRightSpace(FROM));
		sql.append(addRightSpace(config.getDbEraraTable()));
		sql.append(WHERE);
		sql.append(addLeftSpace(config.getDbRowAmd()));
		sql.append("=" + encapsulateInSingleQuotes(amdID, false));

		
		return sql.toString();
		
	}
	
	
	
	/**
	 * retrieves all rows that have supplied Aleph ID
	 * columns: AMD_ID, TIMESTAMP, SIP_STATUS
	 * 
	 * @param alephId
	 * @return
	 */
	private List<Map<String, String>> getAlephIdSelectRecords(String alephId)
	{
		List<Map<String, String>> records = new ArrayList<Map<String, String>>();

		try
		{
			PreparedStatement stmt = conn.prepareStatement(buildAlephIdSelectStatement(alephId));
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next())
			{
				Map<String, String> row = new HashMap<String, String>();
				row.put(config.getDbRowAmd(), rs.getString(config.getDbRowAmd()));
				row.put(config.getDbRowAliasTimestamp(), rs.getString(config.getDbRowAliasTimestamp()));
				row.put(config.getDbRowSipStatus(), rs.getString(config.getDbRowSipStatus()));
				row.put(config.getDbRowSipName(), rs.getString(config.getDbRowSipName()));
				records.add(row);
			}
			
		}
		catch (SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		
		
		return records;
	}
	
	
	/**
	 * build sql query to extract AMD_ID, TIMESTAMP and SIP_STATUS
	 * 
	 * @param alephId
	 * @return
	 */
	private String buildAlephIdSelectStatement(String alephId)
	{
		StringBuilder sql = new StringBuilder(200);
		
		sql.append(addRightSpace(SELECT));
		sql.append(config.getDbRowAmd() + addRightSpace(COMMA));
		sql.append(addRightSpace(config.getDbSelectAlephIdWhere()));
		sql.append(config.getDbRowAliasTimestamp() + addRightSpace(COMMA));
		sql.append(config.getDbRowSipStatus() + addRightSpace(COMMA));
		sql.append(config.getDbRowSipName());
		sql.append(addLeftAndRightSpace(FROM));
		sql.append(config.getDbEraraTable());
		sql.append(addLeftAndRightSpace(config.getDbSelectCountWhere()));
		sql.append(encapsulateInSingleQuotes(alephId, false));
		sql.append(addLeftSpace(config.getDbSelectAlephIdOrder()));
		
		return sql.toString();
	}	
	
	
	/**
	 * Opens JDBC Oracle connection
	 * 
	 */
	private void setupConnection()
	{
		try
		{
        	Class.forName(config.getDbDriverName());			
			conn = DriverManager.getConnection(config.getDbConnectionUrl(), config.getDbUsername(), config.getDbPassword());
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		catch(SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}
	}	
	
	
	/**
	 * Closes JDBC Oracle connection
	 * 
	 */
	private void closeConnection()
	{
		try
		{
			conn.close();
		}
		catch (SQLException e)
		{
			logger.error(e.getMessage());
		}
	}

}
