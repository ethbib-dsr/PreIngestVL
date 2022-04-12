package com.exlibris.dps.submissionvl;


import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.db.AccessDb;

/**
 * Cleanup class - Responsible for cleanup of source path
 *
 * SourceCleanUp will delete processed (SIP_STATUS = FINISHED) and old (now + 30days + config.properties.SubmissionAge) source files
 *
 * @author Andreas Bilski
 *
 */
public class SourceCleanUp {
	
	private ConfigProperties config;
	private int maxNumberOfFilesToDelete = 0;
	private int daysSinceFinished = 30;	
	private String workSpaceID = "";	
	private AccessDb db;	

	private final Logger logger = Logger.getLogger(this.getClass());	

	/**
	 * default constructor
	 * 
	 * @param config
	 */
	public SourceCleanUp(ConfigProperties config)
	{
		this.config = config;
		db = new AccessDb(config);
	}
	
	/**
	 * default main method to start a cleanup process for submissionvl submissions
	 * 
	 */
	public void cleanupSourcePath() {		
		if (checkAndSetConfiguration()) {			
			logger.info("Source path cleanup starting with");
			logger.info("SOURCE-PATH: "+config.getSourcePath());
			logger.info("Number of files: "+Integer.toString(maxNumberOfFilesToDelete));
			logger.info("Days since finished: "+Integer.toString(daysSinceFinished));
			if (config.getCleanupSimulationMode()) {
				logger.info("Simulation mode, no physical delete");
			}
			
			startCleanUp();
		}		
	}
	
	/**
	 * check the configuration file for minimal configuration
	 * 
	 */
	private boolean checkAndSetConfiguration() {
		if (config.getCleanupActive()) {
			try {
				maxNumberOfFilesToDelete = config.getCleanupNumberOfFiles();
				daysSinceFinished = Math.max(daysSinceFinished, 30 + config.getCleanupFinishedAgeDelta());
				workSpaceID = config.getDbInsertWorkspaceId();
				// I just call the getters, to check if all properties are set correctly. If not, an exception is raised
				config.getCleanUpFilesystemView();
				config.getSubmissionFilesystemTable();
				config.getCleanUpRowStatusFinished();
				config.getDbRowSipStatusFilesystem();
				config.getDbRowForceSourceDelete();
				config.getCleanupUsePathFragment();
				return true;
			} catch(Exception e) {
				logger.warn("SourceCleanUp not applicable due to configuration erros: "+e.getMessage());
			}			
		} else {
			logger.info("SourceCleanUp not active");
		}
		
		return false;		
	}

	/**
	 * start actual cleanup
	 * 
	 */
	private void startCleanUp() {
		int count = 0;
		List<Map<String, String>> dbRecords;
		
		if (config.getCleanupUsePathFragment()) { 
			dbRecords = db.getRecordsStatusFinishedAndOlderThan(daysSinceFinished, maxNumberOfFilesToDelete, workSpaceID, config.getXmlDcInstitute());
		} else {
			dbRecords = db.getRecordsStatusFinishedAndOlderThan(daysSinceFinished, maxNumberOfFilesToDelete, workSpaceID);
		}
		
		Iterator<Map<String, String>> selectedRecords = dbRecords.iterator();
		
		if (!selectedRecords.hasNext()) {
			logger.info("SourceCleanUp: nothing to delete!");
			return;
		}
		
		logger.info("SourceCleanUp: Number of entries found: "+Integer.toString(dbRecords.size()));
		
		while (selectedRecords.hasNext()) {			
			if (deleteEntry(selectedRecords.next())) {
				count++;
			}
		}	
		
		logger.info("SourceCleanUp: Number of entries deleted: "+Integer.toString(count));
	}
	
	/**
	 * Delete single entry
	 * 
	 * @param entry
	 */
	private boolean deleteEntry(Map<String, String> entry) {
		
		String logPrefix = "";
		String logSuffix = "";
		String deletePath = "";
		
		if (config.getSourcePath().endsWith("/")) {
			deletePath = config.getSourcePath() + entry.get(config.getDbRowSipName()); 
		} else {
			deletePath = config.getSourcePath() + "/" + entry.get(config.getDbRowSipName());
		}
		
		if (config.getCleanupSimulationMode()) {
			logPrefix = "Simulation: ";
			logSuffix = "To physically delete, use rm "+deletePath;			
		} else {
			File file = new File(deletePath);
			try {
			  if (file.delete()) {				  
				  db.updateSourceFileDeletedStatus(entry.get(config.getDbRowWorkspace()), 
						                           entry.get(config.getDbRowAmd()), 
						                           config.getDbUpdateSipStatusFileSystemFinal(),
						                           deletePath);
			  } else {
				  logger.warn(logPrefix+"Source file "+deletePath+" not found.");
				  // in case, the file was no longer in the filesystem, but was ready for deletion, we gonna add the
				  // entry in SUBMISSION_LIFECYCLE, otherwise the cleanup job will try to delete the file every run
				  // and the warning message keeps popping up
				  db.updateSourceFileDeletedStatus(entry.get(config.getDbRowWorkspace()), 
                          						   entry.get(config.getDbRowAmd()), 
                          						   config.getDbUpdateSipStatusFileSystemNotFound(),
                          						   deletePath);
				  return false;
			  }
			} catch (Exception e) {
				logger.error("Could not delete source file "+deletePath+". Reason: "+e.getMessage());
				return false;
			}				 
		}
		
		if (entry.get(config.getDbRowForceSourceDelete()).equals("1")) {
			logger.info(logPrefix+deletePath+" "+config.getCleanUpInfoTextForcedDelete()+logSuffix);
		} else {
			logger.info(logPrefix+"Source file "+deletePath+" deleted."+logSuffix);
		}
		
		return true;
	}
	
	
}
