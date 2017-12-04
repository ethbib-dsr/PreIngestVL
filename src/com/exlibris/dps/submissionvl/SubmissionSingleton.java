package com.exlibris.dps.submissionvl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.db.AccessDb;
import com.exlibris.dps.submissionvl.util.AlephTimestamp;
import com.exlibris.dps.submissionvl.util.ExifManipulator;
import com.exlibris.dps.submissionvl.util.FileHandler;
import com.exlibris.dps.submissionvl.util.SourceListingFile;
import com.exlibris.dps.submissionvl.util.SourceSip;
import com.exlibris.dps.submissionvl.util.SourceSip.SipTypeEnum;
import com.exlibris.dps.submissionvl.util.TreeOperations;
import com.exlibris.dps.submissionvl.xml.DCBuilder;
import com.exlibris.dps.submissionvl.xml.IEBuilder;
import com.exlibris.dps.submissionvl.xml.Mets;
import com.exlibris.dps.submissionvl.xml.MetsReader;


/**
 * Singleton Object controlling application
 * 
 * Singleton object that controls the flow of the application cannot be
 * instantiated
 * 
 * @author Lars Haendler
 * 
 */
public class SubmissionSingleton
{
	private static SubmissionSingleton instance = null;
	private static ConfigProperties config;

	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 * disabled constructor
	 * 
	 */
	private SubmissionSingleton()
	{
		// defeat instantiation
	}


	/**
	 * Singleton instance starter including disabling threading
	 * 
	 * @param configString
	 *           config file name
	 * @return SubmissionSingleton
	 */
	public static SubmissionSingleton getInstance(String configString)
	{
		if (instance == null)
		{
			synchronized (SubmissionSingleton.class)
			{
				if (instance == null)
				{
					instance = new SubmissionSingleton();
					config = new ConfigProperties(configString);
				}
			}
		}

		return instance;
	}


	/**
	 * Starting application
	 * 
	 */
	public synchronized void init()
	{
		logger.info("Submission App started");

		checkFileSystem();
		
		//get institutes
		List<String> institutesList = config.getSourceTargetInstitutes();
		
		for(String instituteName : institutesList)
		{
			//test new sip file extraction
			SortedSet<SourceSip> allSips = getAllSips(instituteName);
			
			//extract usable list taking into account all constraints
			Set<SourceSip> currentRunSips = getFilesForCurrentRun(allSips, instituteName);
			
			//handle extracted list
			handleSIPs(currentRunSips);
		}
		
		logger.info("Submission App finished");
	}


	/**
	 * Extract a complete and up to date (in the defined
	 * boundaries) list of all source files and store them
	 * as SourceSip objects in a SortedSet for further
	 * use
	 * 
	 * @param String institute name
	 * @return SortedSet<SourceSip> all file in sources
	 */
	private SortedSet<SourceSip> getAllSips(String instituteName)
	{
		//empty sorted set to be filled with files
		SortedSet<SourceSip> allSips = new TreeSet<SourceSip>();
		//SourceListingFile that gets generated if needed
		SourceListingFile sl = prepareSourceListingFile(instituteName);
		
		//HashMap of file names and size in bytes
		Map<String, Long> fileMap = sl.getListingFileContent();
		//Iterator for fileMap to allow iterating
		Iterator<Entry<String, Long>> it = fileMap.entrySet().iterator();
		
		//iterate over each file fileMap
		while(it.hasNext())
		{
			//get next entry of fileMap from iterator
			Map.Entry<String,Long> pair = (Map.Entry<String, Long>) it.next();
			//creade a new SourceSip object for each file found in fileMap
			//only allow specified file extension
			if(pair.getKey().endsWith(config.getAllowedArchiveType()))
			{
				SourceSip sourceSip = new SourceSip(pair.getKey(), buildFileSourcePath(instituteName, pair.getKey()), buildFileTargetPath(instituteName, pair.getKey()), (long) pair.getValue(), instituteName );
				//add newly created SourceSip to allSips
				allSips.add(sourceSip);			
			}
			else
			{
				logger.warn("Wrong file listed in sourcefolder ("+ pair.getKey()+"), "+config.getReasonWrongExtension());
				//reason.append(config.getReasonWrongExtension());
			}
		}
		
		return allSips;
	}
	

	/**
	 * Preparation of object that hold the listing file,
	 * the listing file contains the listing of the 
	 * institute sources
	 * 
	 * file existence will be checked and if needed created
	 * 
	 * file will be filled with the listing of all source files
	 * plus size if it is too old or has never been filled
	 * with data before 
	 * 
	 * @param String institute name
	 * @return SourceListingFile
	 */
	private SourceListingFile prepareSourceListingFile(String institute)
	{
		boolean fileWasMissing = false;
		int allowedAge = config.getListingFileAge();
		String sourceListingFileSuffix = config.getListingFileExtension();
		
		//source directory
		File sourceDir = new File(config.getSourcePath() + institute + ConfigProperties.getFileSeparator());
		//file that will contain the listing of the source directory
		File listingFile = new File(institute + sourceListingFileSuffix);		
		
		//in case listing file does not already exist
		if(!listingFile.exists())
		{
			try 
			{
				//create file
				listingFile.createNewFile();
				fileWasMissing = true;
				logger.debug("listing file had to created");
			} 
			catch (IOException e) 
			{
				logger.error("Cannot write listing file");
				System.exit(0);
			}
		}

		//create source listing object form the listingFile File object
		SourceListingFile sourceListing = new SourceListingFile(listingFile,sourceDir.getAbsolutePath());
		
		logger.debug("listingFile: " + listingFile.getName());
		
		//older than defined or the file did not exist in the first place
		if(sourceListing.getAgeInDays() > allowedAge || fileWasMissing)
		{
			//generate a new listing
			//use with caution because the can take a while
			sourceListing.generateListing();
		}
		else
		{
			//just a short info when file is not created
			logger.debug("Listing file not older than " + allowedAge + " days. No need to create a new file.");
		}
		
		return sourceListing;
	}
		
	
	/**
	 * Helper method to build the source path of 
	 * any file (supplied) in any institution (supplied)
	 * 
	 * @param String institute name
	 * @param String file name
	 * @return String complete source path to file
	 */
	private String buildFileSourcePath(String institute, String fileName)
	{
		String fileSourcePath = config.getSourcePath() + institute + ConfigProperties.getFileSeparator() + fileName;
		
		return fileSourcePath;
	}
	
	
	/**
	 * Helper method to build the target path of 
	 * any file (supplied) in any institution (supplied)
	 * 
	 * @param String institute name
	 * @param String file name
	 * @return String complete target path to file
	 */
	private String buildFileTargetPath(String institute, String fileName)
	{
		String fileTargetPath = config.getTargetPath() + institute + ConfigProperties.getFileSeparator() + fileName;
		
		return fileTargetPath;
	}	
	
	
	/**
	 * Complete business logic that decides
	 * what sip should be used for the current run
	 * 
	 * Rather long implementation but there are lots of
	 * interdependent dependencies to check for
	 * 
	 * @param SortedSet<SourceSip> all sips that are in the source folder
	 * @param String institution name
	 * @return SortedSet<SourceSip>
	 */
	private SortedSet<SourceSip> getFilesForCurrentRun(SortedSet<SourceSip> allSips, String instituteName)
	{
		logger.debug("collect files for current run");
		
		AccessDb db = new AccessDb(config);
		
		SortedSet<SourceSip> currentSips = new TreeSet<SourceSip>();
		int fileCounter = config.getMaxSourceFiles();

		
		Iterator<SourceSip> iterator = allSips.iterator();
		while(iterator.hasNext())
		{
			SourceSip sourceSip = iterator.next();
			boolean addToCurrentSips = true;
			StringBuilder reason = new StringBuilder(300);

			
			//only allow specified file extension
			if(!sourceSip.getFileExtension().equals(config.getAllowedArchiveType()))
			{
				addToCurrentSips = false;
				reason.append(config.getReasonWrongExtension());
			}
			
			//max number of files not reached
			if(fileCounter <= 0)
			{
				addToCurrentSips = false;
				reason.append(config.getReasonMaxNum());				
			}
	
			//file size is less or equal allowed file size
			if(sourceSip.getFileSize() > config.getMaxSourceFileSize())
			{
				addToCurrentSips = false;
				reason.append(config.getReasonFileSize());
			}
			
			//final list has no other source file with the same aleph id
			if(sipMapContainsAlephID(currentSips, sourceSip))
			{
				addToCurrentSips = false;
				reason.append(config.getReasonUnique());
			}
			
			if(sourceSip.getSipType().equals(SipTypeEnum.UNKNOWN))
			{
				addToCurrentSips = false;
				reason.append(config.getReasonSipTypeUnknwn());
			}
			
			//check uniqueness of file
			//no file with exactly the same name should be imported
			if(db.countRecordsWithAmdId(sourceSip.getAmdIdFromFilename()) > 0)
			{
				addToCurrentSips = false;
				reason.append(config.getReasonAlreadyInDb());
			}
			else
			{
				//start db access only if sip has not already 
				//been discarded for current Sips
				if(addToCurrentSips)
				{
					int numberOfRecordsWithAlephID = db.countRecordsWithAlephID(sourceSip.getAlephID());
					
					//no SIP in DB with this AlephID
					if(numberOfRecordsWithAlephID == 0) 
					{
						//the first record must be a master
						if(sourceSip.getSipType().equals(SipTypeEnum.GEN))
						{
							addToCurrentSips = false;
							reason.append(config.getReasonFirstMaster());
						}						
					}
					
					//one record is already in DB
					//and it has to be a master, so only SIP with gen are allowed
					if(numberOfRecordsWithAlephID > 0) 
					{
						//check if only one master per AlephID is in DB
						if(sourceSip.getSipType().equals(SipTypeEnum.MASTER))
						{
							addToCurrentSips = false;
							reason.append(config.getReasonSingleMaster());
						}
						
						//when only one record is in DB, it can only be followed by
						//a gen 1 SIP
						if((numberOfRecordsWithAlephID == 1) && (sourceSip.getGenVersion() != 1))
						{
							addToCurrentSips = false;
							reason.append(config.getReasonFirstDelta());
						}
						
						//all SIPs with the same AlephID must be finished
						if(db.countRecordsWithAlephID(sourceSip.getAlephID()) != db.countRecordsWithAlephIdAndFinished(sourceSip.getAlephID()))
						{
							addToCurrentSips = false;
							reason.append(config.getReasonFinished());
						}						
						
						//if all previous checks in current if clause are false
						if(addToCurrentSips && numberOfRecordsWithAlephID>1)
						{
							List<Map<String, String>> dbRecords = db.getRecordsWithAlephID(sourceSip.getAlephID());
							AlephTimestamp recordTimestamp = new AlephTimestamp((String) dbRecords.get(0).get(config.getDbRowAliasTimestamp()));
							SourceSip lastDbSip = new SourceSip(dbRecords.get(0).get(config.getDbRowSipName()),"/","/",0,"");
							
							//SIP timestamp must be higher than last from db / check for smaller or equal
							if(sourceSip.getTimestamp().compareTo(recordTimestamp) == 0|| sourceSip.getTimestamp().compareTo(recordTimestamp) == -1)
							{
								addToCurrentSips = false;
								reason.append(config.getReasonMustBeNewer());
							}							
							
							//current gen has be be exactly one higher than prior one
							if(lastDbSip.getGenVersion() != sourceSip.getGenVersion()-1)
							{
								addToCurrentSips = false;
								reason.append(config.getReasonDeltaPlusOne());
							}
						}
					}
				}
			}
			
			//generate info for each file
			if(addToCurrentSips)
			{
				currentSips.add(sourceSip);
				fileCounter--;
				logger.info(sourceSip.getFileName() + "(" + instituteName + ") is used"); 
				logger.debug(fileCounter + " free spots left in queue ");
			}
			else
			{
				logger.debug(sourceSip.getFileName() + "(" + instituteName + ") not used. Reason: " + reason.toString());
			}
			
			//like first conditional statement but break at this point 
			//to unsure that max number of files reached is in log file
			if(fileCounter <= 0)
			{
				break;
			}
		}
		
		logger.debug("getFilesForCurrentRun End");
		
		return currentSips;
	}
	
	
	/**
	 * Check if SortedSet of SIPs contains a SIP that has the same AlephID 
	 * as the supplied sourceSip
	 * 
	 * @param currentSips
	 * @param sourceSip
	 * @return boolean
	 */
	private boolean sipMapContainsAlephID(SortedSet<SourceSip> currentSips, SourceSip sourceSip)
	{
		boolean sameAlephID = false;

		Iterator<SourceSip> iterator = currentSips.iterator();
		while(iterator.hasNext())
		{
			SourceSip sip = iterator.next();
			if(sip.sameAlephId(sourceSip))
			{
				sameAlephID = true;
			}
		}
		
		return sameAlephID;
	}
	
	
	/**
	 * handler for Sips that have been chosen to be worked with 
	 * 
	 * @param currentSips
	 */
	private void handleSIPs(Set<SourceSip> currentSips)
	{
		
		for(SourceSip singleSip : currentSips)
		{
			AccessDb db = new AccessDb(config);
			File sipFile = new File(singleSip.getSourcePath());
			String instituteName = singleSip.getInstitute();

			//db-status-initialized = INITIALIZED
			db.insertSipIntoDB(singleSip);
			logger.info("SIP " + singleSip.getFileName() + " (" + singleSip.getFileSizeInMb() + "MB) started");
			
			copyZipFile(sipFile, instituteName);
			//db-status-copied = COPIEDFROMSOURCE
			db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusCopied());
			
			extractZipFile(sipFile, instituteName);
			//db-status-extracted = EXTRACTED
			db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusExtracted());
			
			//check if integrity is correct
			if (checkExtractedSipIntegrity(sipFile, singleSip, instituteName, db))
			{
				FileHandler extractFh = new FileHandler(sipFile, instituteName, config);
				
				makeExifCorrections(extractFh);
				//db-status-exif = EXIF-CHECKED+FIXED
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusExif());
				
				generateMetaDataFiles(extractFh);
				//db-status-metadata = METADATA-GENERATED
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusMetadata());
				
				//db-status-moved2target = MOVED-2-TARGET-DIRECTORY
				moveFromExtractToTarget(sipFile, instituteName); 
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusMoved2Target());
				
				removeFromPreExtract(sipFile, instituteName); 
				//db-status-preingest-finished = DB_STATUS_PREINGEST_FINISHED
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusPreingestFinished());
				logger.info("SIP " + sipFile.getName() + " finished"); 		
			}
			else
			{
				//TODO how to handle sip files when integrity not intact
				//keep zip in preextract OR 
				//removeFromPreExtract(sipFile, instituteName);
				//keep zip in extracted 
			}
		}

	}

	/**
	 * checks integrity of unzipped folder checks for mets, number of tif files
	 * and correct file endings
	 * 
	 * @param sipFile
	 *           extracted SIP file
	 * @return boolean
	 */
	private boolean checkExtractedSipIntegrity(File sipFile, SourceSip singleSip, String instituteName, AccessDb db)
	{
		boolean checkSuccess = true;
		boolean checkImages = false;
		boolean checkFulltext = false;

		
		//1. check: image file inside sip have allowed extension
		List<String> allowedImageFileEndings = config.getAllowedImageFileEndings();
		FileHandler fh = new FileHandler(sipFile, instituteName, config);		
		
		String[] imageNameArray = fh.getImageFilesArray();
		File metsFile = fh.getMetsFile();		
		
		if(imageNameArray != null)
		{
			checkImages = true;
			for (String singleFileName : imageNameArray)
			{
				if (!allowedImageFileEndings.contains(FileHandler.getFileExtension(singleFileName)))
				{
					checkSuccess = false;
					logger.warn(sipFile.getName() + " " +  config.getIntegrityWrongFiles() + " " + singleFileName);
					//db-status-integrityfailed-files
					db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusIntegrityWrongFiles());
				}
			}			
		}		
		
		
		//2. check: export_mets.xml exists
		if (!metsFile.exists())
		{
			checkSuccess = false;
			logger.warn(sipFile.getName() + " " + config.getIntegrityMissingMets());
			//db-status-integrityfailed-mets
			db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusIntegrityMissingMets());
		}


		//3. check: aleph id from file corresponds with aleph id from mets
		MetsReader mr = new MetsReader(fh.getMetsFile().getAbsolutePath(), config);
		if(!mr.getMets().getAlephid().equals(singleSip.getAlephID()))
		{
			checkSuccess = false;
			logger.warn(sipFile.getName() + " " + config.getIntegrityInvalidId() + " " + mr.getMets().getAlephid());
			//db-status-integrityfailed-alephid
			db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusIntegrityInvalidId());
		}
		
		
		//4. check: Images and Fulltext exists
		
		String fulltextFilePath = fh.getFulltextFilePath();
		File fl = new File(fulltextFilePath);
		
		if (fl.exists())
		{
			checkFulltext = true;
			logger.debug(fulltextFilePath);
			if (checkFulltext & checkImages)
			{
				//checkSuccess = false;
				logger.debug(sipFile.getName() + " : SIP contains Images and Fulltext XML");
				//db-status-sip-contains-fulltext
				//db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusContainsFulltext());
			}
		}
		
	
		return checkSuccess;
	}

	
	/**
	 * Checks all tif files in extract folder if exif info is correct
	 * makes necessary adjustments if needed
	 * 
	 * @param extractFh
	 */
	private void makeExifCorrections(FileHandler extractFh)
	{
		logger.debug("start checking exif information of all tif files");
		String[] imageFiles = extractFh.getImageFilesArray();
		String extractFilePath = extractFh.getImageFilePath();
		
		
		if(imageFiles != null)
		{
			ExifManipulator em = new ExifManipulator(config);
			
			for(String imageFilePath : imageFiles)
			{
				File singleImage = new File(extractFilePath + imageFilePath);
				
				if(em.hasWrongModifyDateFormat(singleImage))
				{
					em.makeExifCorrections(singleImage);
					logger.warn(singleImage.getName() + " has wrong ModifyDate format and was corrected");
				}
			}
			
			//removed because not needed when exif is NOT used with stay open option
			//em.close();
		}
		else
		{
			logger.debug("No images to check");
		}

		logger.debug("checking exif information finished");
	}
	

	/**
	 * Generating all relevant meta data files
	 * 
	 * @param sipFile
	 */
	private void generateMetaDataFiles(FileHandler extractFh)
	{
		logger.debug("start generating meta data files");
		
		ArchiveTracker tracker = new ArchiveTracker(extractFh.getWorkingPath().getName());
		String[] imageFiles = extractFh.getImageFilesArray();
		String[] fulltextFiles = extractFh.getFulltextFilesArray();
		
		
		MetsReader mr = new MetsReader(extractFh.getMetsFile().getAbsolutePath(), config);
		Mets mets = mr.getMets();

		tracker.setArchiveStatus("generateDc");
		generateDc(extractFh);		
		logger.debug("dc.xml for " +  extractFh.getCurrentFileName() + " generated");
		
		
		if (imageFiles != null)
		{
			if (fulltextFiles != null)
			{
				logger.info("SIP " + extractFh.getCurrentFileName() + " contains image and fulltext files");
				logger.debug("creation of ie.xml started");

				tracker.setArchiveStatus("generateIe");
				generateIe(extractFh, mets, true, true);
			}
			else
			{
				logger.info("SIP " + extractFh.getCurrentFileName() + " contains image files");
				logger.debug("creation of ie.xml started");
	
				tracker.setArchiveStatus("generateIe");
				generateIe(extractFh, mets, true, false);
			}
		}
		else if (fulltextFiles != null)
		{
			logger.info("SIP " + extractFh.getCurrentFileName() + " contains fulltext files");
			logger.debug("creation of ie.xml started");
			
			tracker.setArchiveStatus("generateIe");
			generateIe(extractFh, mets, false, true);
		}
		else
		{
			logger.info("SIP " + extractFh.getCurrentFileName() + " contains NO image and NO fulltext files");
			logger.debug("creation of ie.xml started");
			
			tracker.setArchiveStatus("generateIe");
			generateIe(extractFh, mets, false, false);
		}
		
		logger.debug("creation of ie.xml finished");
	}



	/**
	 * Handles creation of dc file
	 * 
	 * @return
	 */
	private void generateDc(FileHandler extractFh)
	{
		File file = extractFh.getDcExtractFile();
		String dcContent = "";

		DCBuilder dc = new DCBuilder(extractFh.getWorkingPath().getName());
		dcContent = dc.runBuilder();

		try
		{
			writeToFile(file, dcContent);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
			System.exit(0);
		}
	}


	/**
	 * Handles creation of simple ie file
	 * 
	 * @return
	 */
	private void generateIe(FileHandler extractFh, Mets mets, boolean hasImageFiles, boolean hasFulltextFiles)
	{
		File file = extractFh.getIeExtractFile();
		
		IEBuilder ie = new IEBuilder(mets, extractFh, config, hasImageFiles, hasFulltextFiles);
		String ieContent = ie.runBuilder();
		
		try
		{
			writeToFile(file, ieContent);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage());
			System.exit(0);
		}

	}


	/**
	 * check if institute folders exsists in preextract and extract path and
	 * creates them if necessary cleans folders from old, unused files of
	 * previous submission app runs
	 * 
	 */
	private void checkFileSystem()
	{
		// check if folders for institutes exists in preextract / extract
		List<String> institutesList = config.getSourceTargetInstitutes();
		for (String institute : institutesList)
		{
			String preExtractPath = config.getPreExtractPath() + institute;
			String extractPath = config.getExtractPath() + institute;
			String targetPath = config.getTargetPath() + institute;
			
			if (!new File(preExtractPath).exists())
			{
				new File(preExtractPath).mkdir();
			}
			if (!new File(extractPath).exists())
			{
				new File(extractPath).mkdir();
			}
			if(! new File(targetPath).exists())
			{
				new File(targetPath).mkdir();
			}
			
		}

	}


	/**
	 * copies file form source location to preextract location extracts file from
	 * preextract location to extract location
	 * 
	 * @param zipFile
	 *           zip file that is handled
	 */
	private void copyZipFile(File zipFile, String instituteName)
	{
		String preExtractPath = config.getPreExtractPath() + instituteName
				+ ConfigProperties.getFileSeparator();
	
		FileHandler singleSourceFile = new FileHandler(zipFile, config);
		logger.info("copy " + zipFile.getPath() + " to " + preExtractPath);
		singleSourceFile.copyFileTo(preExtractPath);
	}
	
	
	private void extractZipFile(File zipFile, String instituteName)
	{
		String preExtractPath = config.getPreExtractPath() + instituteName
				+ ConfigProperties.getFileSeparator();
		String extactFilePath = config.getExtractPath() + instituteName + ConfigProperties.getFileSeparator()
				+ zipFile.getName() + config.getSipDataPath();
		
		FileHandler singlePreExtractFile = new FileHandler(preExtractPath + zipFile.getName(), config);
		logger.debug("unzipping " + zipFile.getName() + " into " + extactFilePath);
		singlePreExtractFile.unzipFileTo(extactFilePath);
	}

	
	/**
	 * Removes file from pre-extract path
	 * 
	 * @param zipFile source file
	 * @param institute
	 */
	private void removeFromPreExtract(File zipFile, String institute)
	{
		String preExtractPath = config.getPreExtractPath() + institute + ConfigProperties.getFileSeparator() + zipFile.getName();
		logger.debug("delete " + preExtractPath);
		
		File toBeDeleted = new File(preExtractPath);
		toBeDeleted.delete();
	}
	
	
	/**
	 * two step move operation using TreeOperation
	 * copy tree from extract to target
	 * delete tree from extract
	 * 
	 * @param zipFile source file
	 * @param institute
	 */
	private void moveFromExtractToTarget(File zipFile, String institute)
	{

		String extractFilePath = config.getExtractPath() + institute + ConfigProperties.getFileSeparator() + zipFile.getName();	
		String targetFilePath = config.getTargetPath() + institute + ConfigProperties.getFileSeparator() + zipFile.getName();		
		
		try
		{
			logger.info("moving from " + extractFilePath + " to " + targetFilePath);
			TreeOperations.copyTree(extractFilePath, targetFilePath);
			logger.debug("delete " + extractFilePath);
			TreeOperations.deleteTree(extractFilePath);
		}
		catch (IOException e)
		{
			logger.error("IO Error: " + e.getMessage());
		}
	}
	
	
	
	/**
	 * Writes String content to a File
	 * 
	 * @param File file
	 * @param String content
	 * @throws IOException
	 */
	private void writeToFile(File file, String content) throws IOException
	{
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		output.write(content);
		output.close();
	}

}