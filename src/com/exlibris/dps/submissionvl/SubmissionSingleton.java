package com.exlibris.dps.submissionvl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.exlibris.dps.submissionvl.db.AccessDb;
import com.exlibris.dps.submissionvl.helper.SourceFileIntegrityChecker;
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
	public static SubmissionSingleton getInstance(String configString, String defaultConfigString)
	{
		if (instance == null)
		{
			synchronized (SubmissionSingleton.class)
			{
				if (instance == null)
				{
					instance = new SubmissionSingleton();
					config = new ConfigProperties(configString, defaultConfigString);
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

		//test new sip file extraction
		SortedSet<SourceSip> allSips = getAllSips();

		//extract usable list taking into account all constraints
		Set<SourceSip> currentRunSips = getFilesForCurrentRun(allSips);

		//handle extracted list
		handleSIPs(currentRunSips);
		
		if (config.getCleanupActive()) {
			SourceCleanUp cleanupJob = new SourceCleanUp(config);		
			cleanupJob.cleanupSourcePath();
		} else {
			logger.info("Source path cleanup not active");
		}

		logger.info("Submission App finished");
	}


	/**
	 * Extract a complete and up to date (in the defined
	 * boundaries) list of all source files and store them
	 * as SourceSip objects in a SortedSet for further
	 * use
	 *
	 * @return SortedSet<SourceSip> all file in sources
	 */
	private SortedSet<SourceSip> getAllSips()
	{
			
		//empty sorted set to be filled with files
		SortedSet<SourceSip> allSips = new TreeSet<SourceSip>();
		//SourceListingFile that gets generated if needed
		SourceListingFile sl = prepareSourceListingFile();

		//HashMap of file names and size in bytes
		Map<String, Long> fileMap = sl.getListingFileContent();
		//Iterator for fileMap to allow iterating
		Iterator<Entry<String, Long>> it = fileMap.entrySet().iterator();
		
		int sipPosition = 1;

		//iterate over each file fileMap
		while(it.hasNext())
		{
			//get next entry of fileMap from iterator
			Map.Entry<String,Long> pair = (Map.Entry<String, Long>) it.next();
			//creade a new SourceSip object for each file found in fileMap
			//only allow specified file extension
			if(pair.getKey().endsWith(config.getAllowedArchiveType()))
			{
				SourceSip sourceSip = new SourceSip(pair.getKey(), buildFileSourcePath(pair.getKey()), buildFileTargetPath(pair.getKey()), (long) pair.getValue(), sipPosition);
				//add newly created SourceSip to allSips
				allSips.add(sourceSip);
				sipPosition++;
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
	 * the listing file
	 *
	 * file existence will be checked and if needed created
	 *
	 * file will be filled with the listing of all source files
	 * plus size if it is too old or has never been filled
	 * with data before
	 *
	 * @return SourceListingFile
	 */
	private SourceListingFile prepareSourceListingFile()
	{
		boolean fileWasMissing = false;
		int allowedAge = config.getListingFileAge();

		//source directory
		File sourceDir = new File(config.getSourcePath());
		//file that will contain the listing of the source directory
		File listingFile = new File(config.getListingFileName());

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
	 * any file (supplied)
	 *
	 * @param String file name
	 * @return String complete source path to file
	 */
	private String buildFileSourcePath(String fileName)
	{
		String fileSourcePath = config.getSourcePath() + fileName;

		return fileSourcePath;
	}


	/**
	 * Helper method to build the target path of
	 * any file (supplied)
	 *
	 * @param String file name
	 * @return String complete target path to file
	 */
	private String buildFileTargetPath(String fileName)
	{
		String fileTargetPath = config.getTargetPath() + fileName;

		return fileTargetPath;
	}
	
	/**
	 * Helper method to evaluate the available import space in bytes.
	 * If config.getMaxImportSize() is set, i.e. result > -1, then the
	 * configured max import size is compared towards free disk space
	 * of target path.	 
	 * 
	 * @return number of available bytes for import
	 */	
	private long getAvailableImportSpace() {
		
		long availableImportSpaceInBytes = 0;
		
		try {		
			File targetPath = new File(config.getTargetPath());
			
			availableImportSpaceInBytes = targetPath.getFreeSpace();
			
			if (config.getMaxImportSize() > -1) {
				availableImportSpaceInBytes = Math.min(config.getMaxImportSize(), availableImportSpaceInBytes);
			} 	
		} catch (Exception e) {
			logger.error("Could not evaluate available import space. "+e.getMessage());
			if (config.getMaxImportSize() > -1) {
				availableImportSpaceInBytes = config.getMaxImportSize();
			} else {
				logger.debug("Available import size not configured, i.e. set to 0");
				availableImportSpaceInBytes = 0;
			}
		}
		
		return availableImportSpaceInBytes;
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
	private SortedSet<SourceSip> getFilesForCurrentRun(SortedSet<SourceSip> allSips)
	{
		logger.debug("collect files for current run");

		AccessDb db = new AccessDb(config);

		SortedSet<SourceSip> currentSips = new TreeSet<SourceSip>();
		int fileCounter = config.getMaxSourceFiles();
		
		// get available import space. either df of target path or configured value
		long availableImportSpaceInBytes = getAvailableImportSpace();
		long startSpace = availableImportSpaceInBytes;
		
		boolean maxImportSizeReached = false;		
		
		// Lambda function to output a file size
		Function<Long, String> getSizeAsString = (s) -> {
			
			// Some stuff for calculating and printing the size
			// The result of a division of two longs is a long (i.e. rounded down)
			// One of the arguments has to be a float if we want a result with decimals
			
			float calcSize = s;			
			DecimalFormat decimalFormat = new DecimalFormat("#.00");
			
			try {
				// if we have more than 1 GB
				if ((s/ConfigProperties.FILESIZ_GB) > 0) {
					return decimalFormat.format(calcSize / ConfigProperties.FILESIZ_GB) + "GB";
				// if we have more than 1 MB
				} else if ((s /ConfigProperties.FILESIZ_MB) > 0) {
					return decimalFormat.format(calcSize / ConfigProperties.FILESIZ_MB) + "MB";
				} else {
					return (s / ConfigProperties.FILESIZ_KB) + "KB";
				} 
			} catch(Exception e) {
				return "N/A ("+e.getMessage()+")";
			}
		};


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

			//final list has no other source file from the same item family (used to be Aleph-ID, now DOI)
			if(sipMapContainsFamilyItem(currentSips, sourceSip))
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
			if(db.countRecordsWithAmdId(sourceSip) > 0)
			{
				addToCurrentSips = false;
				reason.append(config.getReasonAlreadyInDb());
			}
			else
			{
				
				//check if available import size is reached only if the file is used
				if (addToCurrentSips && (availableImportSpaceInBytes - sourceSip.getFileSize() < 0))
				{
					addToCurrentSips = false;
					reason.append(config.getReasonMaxImportSize() + " = "+getSizeAsString.apply(startSpace) + ". SIP size = "+getSizeAsString.apply(sourceSip.getFileSize()));
					// when no more space for a SIP, we stop the processing
					maxImportSizeReached = true;
				}	
				
				//start db access only if sip has not already
				//been discarded for current Sips
				if(addToCurrentSips)
				{				
					int numberOfRecordsWithAlephOrDOI = db.countRecordsWithAlephOrDOI(sourceSip);

					//no SIP in DB with this AlephID
					if(numberOfRecordsWithAlephOrDOI == 0)
					{
						//the first record must be a master						
						if(sourceSip.getSipType().equals(SipTypeEnum.GEN))
						{
							//addToCurrentSips = false;
							//reason.append(config.getReasonFirstMaster());
							
							// DDE-796: Since change of capsule name and shutdown of Alpeh, the AlephID is no longer the only
							// identifier (new capsules have Alma ID in recordIdentifiert). DOI is the new identifiert.
							// We cannot guarantee, that we find a master if we have mixed capsules. So we only make a warning
							// if we cannot track a parent/master
							logger.warn("Cannot evaluate a preceding master: "+sourceSip.getFileName());
						}
					}

					// Since 26.11.2020 - also duplicate masters are allowed, only condition is, that previous items
					// with the same Aleph ID are finished. Does this still make sense? Minimal precaution?
					if(numberOfRecordsWithAlephOrDOI > 0)						
					{
						//check if only one master per AlephID is in DB. if not, a warning is logged
						if(sourceSip.getSipType().equals(SipTypeEnum.MASTER))
						{
							logger.warn("Duplicate master detected: "+sourceSip.getFileName());
						}
						
						//all SIPs with the same AlephID or DOI must be finished
						if(db.countRecordsWithAlephOrDOI(sourceSip) != db.countRecordsWithAlephIdOrDoiAndFinished(sourceSip))
						{
							addToCurrentSips = false;
							reason.append(config.getReasonFinished());
						}
					}
				}
			}

			//generate info for each file
			if(addToCurrentSips)				
			{
				currentSips.add(sourceSip);
				availableImportSpaceInBytes -= sourceSip.getFileSize();
				fileCounter--;
				logger.info(sourceSip.getFileName() + " is used ("+getSizeAsString.apply(sourceSip.getFileSize())+")");
				logger.debug(fileCounter + " free spots left in queue ");
				logger.debug("Available space for current run is "+getSizeAsString.apply(availableImportSpaceInBytes));
			}
			else
			{
				logger.debug(sourceSip.getFileName() + " not used. Reason: " + reason.toString());
			}

			// like first conditional statement but break at this point
			// to ensure that max number of files reached or max import size reached 
			// is in log file
			if ((fileCounter <= 0) || (maxImportSizeReached))
			{
				break;
			}
		}

		logger.debug("getFilesForCurrentRun End");

		return currentSips;
	}


	/**
	 * Check if SortedSet of SIPs contains a SIP that has the same AlephID or DOI
	 * as the supplied sourceSip (DDE-796)
	 * We check either for AlephID or DOI. As submissionvl updates are always IEs
	 * we do not care wether a capsule with AlephID and one with DOI, which belong to the
	 * same VisualLibrary item, are submitted in the same run. If they have either same DOI or AlephID
	 * we deny a simultaneous submission.
	 *
	 * @param currentSips
	 * @param sourceSip
	 * @return boolean
	 */
	private boolean sipMapContainsFamilyItem(SortedSet<SourceSip> currentSips, SourceSip sourceSip)
	{
		boolean sameFamily = false;

		Iterator<SourceSip> iterator = currentSips.iterator();
		while(iterator.hasNext())
		{
			SourceSip sip = iterator.next();
			if(sip.sameCapsuleID(sourceSip))
			{
				sameFamily = true;
				break;
			}
		}

		return sameFamily;
	}


	/**
	 * handler for Sips that have been chosen to be worked with
	 *
	 * @param currentSips
	 */
	private void handleSIPs(Set<SourceSip> currentSips)
	{		
		// Preparation for the stop file path
		File configFile = new File(config.CONFIG_PATH);
		
		Path stopFilePath = Paths.get(System.getProperty("user.dir") + File.separator +
				                      configFile.getName().replaceAll(AppStarter.CONF_EXTENSION, "") + ".stop");
	    

		for(SourceSip singleSip : currentSips)
		{
			File stopFile = stopFilePath.toFile();
			
			if (stopFile.exists()) {
				logger.warn("Stopfile <"+stopFile.getName()+"> found. Processing sips stopped. Proceeding to next step.");
				try {
					if (stopFile.delete()) {
						logger.warn("Stopfile deleted.");
					}
				} catch(Exception e) {
					logger.warn("Problem when deleting stopfile: "+e.getMessage());
				}
				break;
			}
			
			AccessDb db = new AccessDb(config);
			File sipFile = new File(singleSip.getSourcePath());

			//db-status-initialized = INITIALIZED
			db.insertSipIntoDB(singleSip);
			logger.info("SIP " + singleSip.getFileName() + " (" + singleSip.getFileSizeInMb() + "MB) started");

			copyZipFile(sipFile);
			//db-status-copied = COPIEDFROMSOURCE
			db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusCopied());

			extractZipFile(sipFile);
			//db-status-extracted = EXTRACTED
			db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusExtracted());


			SourceFileIntegrityChecker sfChecker = new SourceFileIntegrityChecker(sipFile, singleSip, config);
			sfChecker.runIntegrityCheck();

			//integrity ok
			if(sfChecker.getIntegrity())
			{
				FileHandler extractFh = new FileHandler(sipFile, config);

				makeExifCorrections(extractFh);
				//db-status-exif = EXIF-CHECKED+FIXED
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusExif());

				// DDE-796: For Tracking purposes, we have to use DOI, which can be used after extracting the Metadata
				
				String doi;
				try {
					doi = generateMetaDataFiles(extractFh, singleSip);
				} catch (Exception e) {	
					logger.error("Error in generateMetadataFiles: "+e.getMessage());
					db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusIntegrityWrongXMLStructure());
					removeFromPreExtract(sipFile);
					continue;
				}
				//db-status-metadata = METADATA-GENERATED
				if (doi.isEmpty()) {
					db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusMetadata());
				} else {
					db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusMetadata(), doi);					
				}

				//db-status-moved2target = MOVED-2-TARGET-DIRECTORY
				moveFromExtractToTarget(sipFile);
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusMoved2Target());

				removeFromPreExtract(sipFile);
				//db-status-preingest-finished = DB_STATUS_PREINGEST_FINISHED
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), config.getDbStatusPreingestFinished());
				logger.info("SIP " + sipFile.getName() + " finished");
				logger.info("-");
			}
			//integrity nok
			else
			{
				db.updateStatusFromAmdId(singleSip.getAmdIdFromFilename(), sfChecker.getDbStatusNotice());
				removeFromPreExtract(sipFile);
				logger.debug(sipFile.getName() + " was ignored");
			}
		}
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
	 * @return DOI
	 * @throws Exception 
	 */
	private String generateMetaDataFiles(FileHandler extractFh, SourceSip currentSip) throws Exception
	{
		logger.debug("start generating meta data files");

		ArchiveTracker tracker = new ArchiveTracker(extractFh.getWorkingPath().getName());
		String[] imageFiles = extractFh.getImageFilesArray();
		String[] textFiles = extractFh.getTextFilesArray();


		MetsReader mr = new MetsReader(extractFh.getMetsFile().getAbsolutePath(), config);
		mr.initDomParsing(currentSip);
		Mets mets = mr.getMets();

		tracker.setArchiveStatus("generateDc");
		generateDc(extractFh);
		logger.debug("dc.xml for " +  extractFh.getCurrentFileName() + " generated");


		if (imageFiles != null)
		{
			if (textFiles != null)
			{
				logger.info("SIP " + extractFh.getCurrentFileName() + " contains image and text files");
				logger.debug("creation of ie.xml started");

				tracker.setArchiveStatus("generateIe");
				generateIe(extractFh, mets, currentSip, true, true);
			}
			else
			{
				logger.info("SIP " + extractFh.getCurrentFileName() + " contains image files");
				logger.debug("creation of ie.xml started");

				tracker.setArchiveStatus("generateIe");
				generateIe(extractFh, mets, currentSip, true, false);
			}
		}
		else if (textFiles != null)
		{
			logger.info("SIP " + extractFh.getCurrentFileName() + " contains text files");
			logger.debug("creation of ie.xml started");

			tracker.setArchiveStatus("generateIe");
			generateIe(extractFh, mets, currentSip, false, true);
		}
		else
		{
			logger.info("SIP " + extractFh.getCurrentFileName() + " contains NO image and NO text files");
			logger.debug("creation of ie.xml started");

			tracker.setArchiveStatus("generateIe");
			generateIe(extractFh, mets, currentSip, false, false);
		}

		logger.debug("creation of ie.xml finished");
		
		return mets.getDoi();
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
	private void generateIe(FileHandler extractFh, Mets mets, SourceSip currentSip, boolean hasImageFiles, boolean hasTextFiles)
	{
		File file = extractFh.getIeExtractFile();

		IEBuilder ie = new IEBuilder(mets, extractFh, config, currentSip, hasImageFiles, hasTextFiles);
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
	 * check if folders exsists in preextract and extract path and
	 * creates them
	 *
	 */
	private void checkFileSystem()
	{
			if (!new File(config.getPreExtractPath()).exists())
			{
				logger.warn(config.getPreExtractPath() + " does not exist, will be created");
				new File(config.getPreExtractPath()).mkdir();
			}
			if (!new File(config.getExtractPath()).exists())
			{
				logger.warn(config.getExtractPath() + " does not exist, will be created");
				new File(config.getExtractPath()).mkdir();
			}
			if(! new File(config.getTargetPath()).exists())
			{
				logger.warn(config.getTargetPath() + " does not exist, will be created");
				new File(config.getTargetPath()).mkdir();
			}
	}


	/**
	 * copies file form source location to preextract location extracts file from
	 * preextract location to extract location
	 *
	 * @param zipFile
	 *           zip file that is handled
	 */
	private void copyZipFile(File zipFile)
	{
		FileHandler singleSourceFile = new FileHandler(zipFile, config);
		logger.info("copy " + zipFile.getPath() + " to " + config.getPreExtractPath());
		singleSourceFile.copyFileTo(config.getPreExtractPath());
	}


	/**
	 * Extract zip to extract location
	 *
	 * @param zipFile
	 */
	private void extractZipFile(File zipFile)
	{
		String extactFilePath = config.getExtractPath() + zipFile.getName() + config.getSipDataPath();

		FileHandler singlePreExtractFile = new FileHandler(config.getPreExtractPath() + zipFile.getName(), config);
		logger.debug("unzipping " + zipFile.getName() + " into " + extactFilePath);
		singlePreExtractFile.unzipFileTo(extactFilePath);
	}


	/**
	 * Removes file from pre-extract path
	 *
	 * @param zipFile source file
	 */
	private void removeFromPreExtract(File zipFile)
	{
		String preExtractPath = config.getPreExtractPath() + zipFile.getName();
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
	 */
	private void moveFromExtractToTarget(File zipFile)
	{

		String extractFilePath = config.getExtractPath() + zipFile.getName();
		String targetFilePath = config.getTargetPath() + zipFile.getName();

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
