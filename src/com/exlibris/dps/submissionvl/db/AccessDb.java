package com.exlibris.dps.submissionvl.db;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.ConfigProperties;
import com.exlibris.dps.submissionvl.util.SourceSip;
import com.exlibris.dps.submissionvl.util.SourceSip.CapsuleTypeEnum;

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
	private final String WHERE = "where";
	private final char SPACE = ' ';

	private final String COUNT_AMDID = "COUNT_AMDID";
	private final String COUNT_ALEPHID_OR_DOI = "COUNT_ALEPHID_OR_DOI";
	private final String COUNT_ALEPHID_OR_DOI_FINISHED = "COUNT_ALEPHID_OR_DOI_FINISHED";

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
	 * Returns number of capsule occurences in table
	 * 
	 * @param 
	 * @return
	 */
	public int countRecordsWithAlephOrDOI(SourceSip sourceSip)
	{
		setupConnection();
		int total = countOccurences(sourceSip, COUNT_ALEPHID_OR_DOI);
		closeConnection();

		return total;
	}

	/**
	 * Returns number of capsule occurences that also have SIP_STATUS='FINISHED'
	 * 
	 * @param sourceSip
	 * @return
	 */
	public int countRecordsWithAlephIdOrDoiAndFinished(SourceSip sourceSip)
	{
		setupConnection();
		int total = countOccurences(sourceSip, COUNT_ALEPHID_OR_DOI_FINISHED);
		closeConnection();

		return total;
	}

	/**
	 * Returns number of AMD_ID occurences in DB bigger than 1 means not unique
	 * 
	 * @param sourceSip
	 * @return
	 */
	public int countRecordsWithAmdId(SourceSip sourceSip)
	{
		setupConnection();
		int total = countOccurences(sourceSip, COUNT_AMDID);
		closeConnection();

		return total;
	}

	/**
	 * Updates SIP_STATUS directly using AMD_ID of itme
	 * 
	 * @param amdID
	 * @param status
	 */
	public void updateStatusFromAmdId(String amdID, String status)
	{
		// if status is too long just cut it down due to DB constraints
		status = (status.length() > 100) ? status.substring(0, 100) : status;

		setupConnection();
		executeUpdateStatus(amdID, status);
		closeConnection();
	}
	
	/**
	 * Updates SIP_STATUS and TRACKING_DOI directly using AMD_ID of item
	 * 
	 * @param amdID
	 * @param status
	 */
	public void updateStatusFromAmdId(String amdID, String status, String doi)
	{
		// if status is too long just cut it down due to DB constraints
		status = (status.length() > 100) ? status.substring(0, 100) : status;

		setupConnection();
		executeUpdateStatus(amdID, status, doi);
		closeConnection();
	}	

	/**
	 * Insert a complete SIP into Db using the SourceSip object provided
	 * 
	 * @param SourceSip
	 *           s
	 */
	public void insertSipIntoDB(SourceSip s)
	{
		setupConnection();
		executeInsertSipIntoDB(s);
		closeConnection();
	}

	/**
	 * Execution of a complete SIP insert into DB using the SourceSip object
	 * provided
	 * 
	 * @param s
	 */
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
	 * Executes the update statement for SIP_STATUS changes
	 * 
	 * @param amdID
	 * @param status
	 */
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
	
	/**
	 * Executes the update statement for SIP_STATUS and TRACKING_DOI changes
	 * 
	 * @param amdID
	 * @param status
	 */
	private void executeUpdateStatus(String amdID, String status, String doi)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(buildStatusUpdateStatementWithDOI());
			stmt.setString(1, status);
			stmt.setString(2, doi);			
			stmt.setString(3, amdID);
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}

	}	

	/**
	 * Build SQL query for SIP_STATUS update
	 * 
	 * @return String
	 */
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
	 * Build SQL query for SIP_STATUS and TRACKING_DOI update
	 * 
	 * @return String
	 */
	private String buildStatusUpdateStatementWithDOI()
	{
		StringBuilder sql = new StringBuilder(200);

		sql.append(addRightSpace("update"));
		sql.append(addRightSpace(config.getDbEraraTable()));
		sql.append(addRightSpace("set"));
		sql.append(addRightSpace(config.getDbRowSipStatus()));
		sql.append(addRightSpace("=?" + addRightSpace(COMMA)));
		sql.append(addRightSpace(config.getDbRowTrackingDOI()));
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
		sql.append(encapsulateInSingleQuotes(config.getDbInsertWorkspaceId() + "_" + config.getCurrentTime()));
		sql.append(encapsulateInSingleQuotes(config.getDbInsertWorkspaceId()));
		sql.append(encapsulateInSingleQuotes(s.getCapsuleID() + "_" + s.getTimestamp().getSourceVisualLibraryTimestamp()));
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
		StringBuilder sb = new StringBuilder(s.length() + 2);
		sb.append(ORACLE_QUOTES).append(s).append(ORACLE_QUOTES);
		if (comma)
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
		StringBuilder sb = new StringBuilder(s.length() + 2);

		if (left)
		{
			sb.append(SPACE);
		}

		sb.append(s);

		if (right)
		{
			sb.append(SPACE);
		}

		return sb;
	}

	/**
	 * Helper that returns any supplied with space to the left makes uses of
	 * abstract encapsulateInSpaces()
	 * 
	 * @param s
	 * @return
	 */
	private StringBuilder addLeftSpace(String s)
	{
		return encapsulateInSpaces(s, true, false);
	}

	/**
	 * Helper that returns any supplied with space to the right makes use of
	 * abstract encapsulateInSpaces()
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
	 * additionally a comma at the end will ALWAYS be attached makes uses of
	 * abstract encapsulateInSingleQuotes()
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
	 * @param sourceSip
	 * @return
	 */
	private int countOccurences(SourceSip sourceSip, String statementChooser)
	{
		int total = -1;
		String statement;

		switch (statementChooser)
		{
		case COUNT_ALEPHID_OR_DOI:
			statement = buildCapsuleCountStatement(sourceSip);
			break;
		case COUNT_ALEPHID_OR_DOI_FINISHED:
			statement = buildFinishedCapsuleCountStatement(sourceSip);
			break;
		case COUNT_AMDID:
			statement = buildCountAmdIdStatement(sourceSip.getAmdIdFromFilename());
			break;
		default:
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
	 * builds sql query that counts all occurences of alephID in AMD_ID
	 * 
	 * @param sourceSip
	 * @return
	 */
	private String buildCapsuleCountStatement(SourceSip sourceSip)
	{
		StringBuilder sql = new StringBuilder(200);

		sql.append(addRightSpace(config.getDbSelectCountAmdId()));
		sql.append(TOTAL_PARAM);
		sql.append(addLeftAndRightSpace(FROM));
		sql.append(addRightSpace(config.getDbEraraTable()));
		if (sourceSip.getCapsuleType() == CapsuleTypeEnum.DOI) {
			sql.append(WHERE);
			sql.append(addLeftSpace(config.getDbRowTrackingDOI()));
			sql.append("=" + encapsulateInSingleQuotes(sourceSip.getDOI(), false));
		} else {
			sql.append(config.getDbSelectCountWhere());
			sql.append(encapsulateInSingleQuotes(sourceSip.getCapsuleID(), false));			
		}

		return sql.toString();
	}

	/**
	 * builds sql query that counts all occurences of AlephId or DOI that have status
	 * finished
	 * 
	 * @param sourceSip
	 * @return
	 */
	private String buildFinishedCapsuleCountStatement(SourceSip sourceSip)
	{
		StringBuilder sql = new StringBuilder(200);

		sql.append(addRightSpace(config.getDbSelectCountAmdId()));
		sql.append(addRightSpace(TOTAL_PARAM));
		sql.append(addRightSpace(FROM));
		sql.append(addRightSpace(config.getDbEraraTable()));
		if (sourceSip.getCapsuleType() == CapsuleTypeEnum.DOI) {
			sql.append(WHERE);
			sql.append(addLeftSpace(config.getDbRowTrackingDOI()));
			sql.append("=" + encapsulateInSingleQuotes(sourceSip.getDOI(), false));		
			
		} else {
			sql.append(config.getDbSelectCountWhere());
			sql.append(encapsulateInSingleQuotes(sourceSip.getCapsuleID(), false));			
		}
		sql.append(addLeftAndRightSpace(AND));
		sql.append(config.getDbRowSipStatus());
		sql.append(" IN" + addLeftAndRightSpace(buildSQLinValueGroup(config.getDbRowStatusFinished())));

		return sql.toString();
	}
	
	/**
	 * builds sql query that counts all occurences of AMD_ID
	 * 
	 * @param amdID
	 * @return
	 */	
	
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
	 * build sql query for IN value group from a CSV group
	 * 
	 * @param commaValueGroup
	 * @return String
	 */
	private String buildSQLinValueGroup(String commaValueGroup)
	{
		StringBuilder sql = new StringBuilder(commaValueGroup.length() + 2);

		// opening brackets for IN values
		sql.append("(");

		String[] commaValueArray = commaValueGroup.split(",");

		// iterate over each element in the CSV group
		for (int i = 0; i < commaValueArray.length; i++)
		{
			if ((i + 1) < commaValueArray.length)
			{
				sql.append(encapsulateInSingleQuotes(commaValueArray[i].trim(), true));
			}
			// last element in the IN value group is not followed by a comma
			else
			{
				sql.append(encapsulateInSingleQuotes(commaValueArray[i].trim(), false));
			}
		}

		// closing brackets for IN values
		sql.append(")");

		return sql.toString();
	}

	
	/**
	 * Return a List of maps with WORKSPACE_ID, AMD_ID, SOURCE_PATH, SIP_STATUS and UPDATE_DT, SIP_STATUS_FS, SIP_STATUS_FS_DT, FORCE_DELETE, DELETE_PATH key-value pair
	 * from DB table  
	 * 
	 * @param ageInDays
	 * @param maxRows
	 * @param workSpaceID
	 * @return
	 */
	public List<Map<String, String>> getRecordsStatusFinishedAndOlderThan(int ageInDays, int maxRows, String workSpaceID)
	{
		setupConnection();
		List<Map<String, String>> records = getFinishedAndOlderThanSelectRecords(ageInDays, maxRows, workSpaceID, "");
		closeConnection();

		return records;
	}
	
	public List<Map<String, String>> getRecordsStatusFinishedAndOlderThan(int ageInDays, int maxRows, String workSpaceID, String pathFragment)
	{
		setupConnection();
		List<Map<String, String>> records = getFinishedAndOlderThanSelectRecords(ageInDays, maxRows, workSpaceID, pathFragment);
		closeConnection();

		return records;
	}
	
	/**
	 * retrieves all rows that have SIP_STATUS = FINISHED and UPDATE_DT < CURRENT_DATE - ageInDays
	 * SIP_STATUS
	 * 
	 * @param ageInDays
	 * @param maxRows
	 * @return
	 */
	private List<Map<String, String>> getFinishedAndOlderThanSelectRecords(int ageInDays, int maxRows, String workSpaceID, String pathFragment)
	{
		List<Map<String, String>> records = new ArrayList<Map<String, String>>();

		try
		{
			PreparedStatement stmt = conn.prepareStatement(buildFinishedAndOlderThanSelectStatement(ageInDays, maxRows, workSpaceID, pathFragment));
			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				Map<String, String> row = new HashMap<String, String>();
				row.put(config.getDbRowWorkspace(), rs.getString(config.getDbRowWorkspace()));
				row.put(config.getDbRowAmd(), rs.getString(config.getDbRowAmd()));
				row.put(config.getDbRowSourcePath(), rs.getString(config.getDbRowSourcePath()));
				row.put(config.getDbRowSipName(), rs.getString(config.getDbRowSipName()));
				row.put(config.getDbRowSipStatus(), rs.getString(config.getDbRowSipStatus()));
				row.put(config.getDbRowUpdateDate(), rs.getString(config.getDbRowUpdateDate()));
				row.put(config.getDbRowDeletePath(), rs.getString(config.getDbRowDeletePath()));
				// null values are possible, so we write an empty string just for convenience
				if (rs.getString(config.getDbRowSipStatusFilesystem()) == null) {
					row.put(config.getDbRowSipStatusFilesystem(), "");
				} else {
					row.put(config.getDbRowSipStatusFilesystem(), rs.getString(config.getDbRowSipStatusFilesystem()));
				}
				if (rs.getString(config.getDbRowForceSourceDelete()) == null) {
					row.put(config.getDbRowForceSourceDelete(), "");
				} else {
					row.put(config.getDbRowForceSourceDelete(), rs.getString(config.getDbRowForceSourceDelete()));
				}
				
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
	 * build sql query to select FINISHED items that could be deleted
	 * 
	 * @param ageInDays
	 * @param maxRows
	 * @return
	 */
	private String buildFinishedAndOlderThanSelectStatement(int ageInDays, int maxRows, String workSpaceID, String pathFragment)
	{
		StringBuilder sql = new StringBuilder(200);

		sql.append(addRightSpace("SELECT *"));
		sql.append(addLeftAndRightSpace(FROM));
		sql.append(config.getCleanUpFilesystemView());		
		// WHERE ((SIP_STATUS = 'FINISHED' AND UPDATE_DT < CURRENT_DATE - 30 AND (SIP_STATUS_FS IS NULL)) OR FORCE_SOURCE_DELETE = 1)		
		sql.append(addLeftAndRightSpace("WHERE (("+config.getDbRowSipStatus()+" ="));
		sql.append(encapsulateInSingleQuotes(config.getCleanUpRowStatusFinished(), false));
		sql.append(addLeftAndRightSpace("AND "+config.getDbRowUpdateDate()+" < CURRENT_DATE - "+Integer.toString(ageInDays)));
		sql.append(addLeftAndRightSpace("AND ("+config.getDbRowSipStatusFilesystem()+" IS NULL")+"))");
		sql.append(addLeftAndRightSpace("OR "+config.getDbRowForceSourceDelete()+" = 1)"));		
		sql.append(addLeftAndRightSpace("AND "+config.getDbRowWorkspace()+" = "));
		sql.append(encapsulateInSingleQuotes(workSpaceID, false));
		if (pathFragment.trim().length() > 0) {
			sql.append(addLeftAndRightSpace("AND "+config.getDbRowSourcePath()+" like "));
			sql.append(encapsulateInSingleQuotes("%/"+pathFragment+"/%", false));
		}
		sql.append(addLeftSpace("ORDER BY UPDATE_DT"));
		sql.append(addLeftSpace("fetch first "+Integer.toString(maxRows)+" rows only"));

		logger.debug(sql.toString());
		return sql.toString();
	}
	
	/**
	 * Updates SIP_STATUS directly using AMD_ID of item
	 * 
	 * @param amdID
	 * @param status
	 */
	public void updateSourceFileDeletedStatus(String workspaceID,String amdID, String status, String deletePath)
	{
		setupConnection();
		executeSourceFileDeletedStatus(workspaceID, amdID, status, deletePath);
		closeConnection();
	}	
	
	/**
	 * Executes the update statement for SIP_STATUS_FS changes
	 * 
	 * @param amdID
	 * @param status
	 */
	private void executeSourceFileDeletedStatus(String workspaceID, String amdID, String status, String deletePath)
	{
		try
		{
			PreparedStatement stmt = conn.prepareStatement(buildSourceFileStatusUpdateStatement(workspaceID, amdID, status, deletePath));		
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}

	}	
	
	/**
	 * Build SQL query for SIP_STATUS_FS update
	 * 
	 * @return String
	 */
	private String buildSourceFileStatusUpdateStatement(String workspaceID, String amdID, String status, String deletePath)
	{
		StringBuilder sql = new StringBuilder(200);
		
		//MERGE INTO SUBMISSION_LIFECYCLE d
		//USING (SELECT 'zb-e-rara' workspace,'source_clean_test08' amd_id, 'SOURCE_DELETED' status from dual) s
		//ON (d.WORKSPACE_ID = s.workspace and d.AMD_id = s.amd_id)
		//WHEN MATCHED THEN UPDATE SET d.SIP_STATUS_FS = s.status, d.UPDATE_DT = SYSDATE
		//WHEN NOT MATCHED THEN INSERT (WORKSPACE_ID, AMD_ID, SIP_STATUS_FS, FORCE_SOURCE_DELETE, UPDATE_DT) VALUES (s.workspace, s.amd_id, s.status, 0, SYSDATE);
		
		sql.append(addRightSpace("MERGE INTO "+config.getSubmissionFilesystemTable()+" d"));
		sql.append(addRightSpace("USING (SELECT"+encapsulateInSingleQuotes(workspaceID, false)+" workspace,"));
		sql.append(addRightSpace(encapsulateInSingleQuotes(amdID, false)+" amd_id,"));
		sql.append(addRightSpace(encapsulateInSingleQuotes(deletePath, false)+" delete_path,"));
		sql.append(addRightSpace(encapsulateInSingleQuotes(status, false)+" status from dual) s"));
		sql.append(addRightSpace("ON (d.WORKSPACE_ID = s.workspace and d.AMD_id = s.amd_id)"));
		sql.append(addRightSpace("WHEN MATCHED THEN UPDATE SET d.SIP_STATUS_FS = s.status, d.UPDATE_DT = SYSDATE, d.FORCE_SOURCE_DELETE = 0"));
		sql.append(addRightSpace("WHEN NOT MATCHED THEN INSERT (WORKSPACE_ID, AMD_ID, SIP_STATUS_FS, FORCE_SOURCE_DELETE, UPDATE_DT, DELETE_PATH) VALUES (s.workspace, s.amd_id, s.status, 0, SYSDATE, s.delete_path)"));		

		
		//logger.info(sql.toString());

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
			conn = DriverManager.getConnection(config.getDbConnectionUrl(), config.getDbUsername(),
					config.getDbPassword());
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		catch (SQLException e)
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
