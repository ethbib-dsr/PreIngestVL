# Submission application for Visual Library

Application that picks up zipped SIP capsules , extracts them, creates Rosetta mets.xml and copies everything to a target location.
Java Application started by separate shell script.

## Application start

com.exlibris.dps.submissionvl.AppStarter "config.test.properties" "log4j.properties" "7777"

* parameter 1: properties file
* parameter 2: log4j configuration
* parameter 3: [optional] port number, default: 7777

## Current Version: 1.7.5

### TODO ###

* sqlite tracking to replace current file based tracking system (1.7)
  * refactoring large parts of inner logic
  * implementing and handling of status
  * alephid and maybe mets section
  * note: removes complete file base tracking logic

### VERSION HISTORY ###

### version 1.7.5 (2021-09-30)
* DDE-881: Error warning when having a invalid export_mets.xml converted to a warning for logging and dailycheck purposes

### version 1.7.4 (2021-09-22)
* DDE-881: new version of parsing export_mets.xml. determing the correct dmdSec happens how by iterating over all dmdSec sections and choosing the correct either by AlephID oder DOI.

### version 1.7.3 (2021-05-25)
* DDE-796: submission is able to process Aleph- or DOI-type capsules. 
* Parsing of XML changed. Before XPATH is used, the correct section has to be determined (mets:structLink->mets:structMap TYPE="LOGICAL"->mets:dmdSec ID="").
* Removing unused properties and renaming of properties for parsing.
* Adding TRACKING_DOI field in tracking table for DOI tracking.
* Master is no longer necessary for submission. If not found, a warning is issued.

### version 1.7.2 (2021-01-28)
* Minor change in logging information. Correct file path used.

### version 1.7.1 (2021-01-06)
* Source clean up extended for all current submission. 

### version 1.7.0 (2020-12-01)
* DDE-797: Check for duplicate masters removed. A warning is written to the log file, if detected.

### version 1.6.9 (2020-11-26)
* DDE-797: Check for duplicate masters removed. Only conditions are: 1st item has to be a master. Preceding items have to be finished.

### version 1.6.8 (2020-11-17)
* DDE-802: Submission processing has now an additional parameter to control the number, i.e. the size of all processed sips for one run: max-import-size. If
* max-import-size=-1, limit set. In this case, make sure, that e.g. max-source-files is set to a reasonable number

### version 1.6.7 (2020-11-11)
* DDE-800: undone. Items are processed according to the source list order (every SourceSip gets a position, which is the order argument for the SortedSet).
* Stopfile: for every submission job, a stop file can be set (equal to the lockfile name, but with extension .stop). After the apps detects the stopfile, 
* SubmissionSingleton.handleSIPS() stops the processing loop and continues with the rest of the app logic. A stopfile can be created while a submission is running
* to stop a submission gracefully (without creating processing garbage, which has to be cleaned)


### version 1.6.6 (2020-11-04)
* DDE-795: when DOI is missing, a warning is logged. When aleph-ID cannot be retrieved, ZIP is ignored. Delta need no longer be in a sequence. But a master is required
* as first item.
* DDE-800: Items to be processed are now ordered by size.


### version 1.6.5 (2020-10-20)
* DDE-540, also transcription folders are considered. there is a new multi-value property "sip-text-directories". Property "sip-fulltext-directory" was removed.
* Default sip-text-directories=/fulltext/, but can be extended to sip-text-directories=/fulltext/,/transcription/ 


### version 1.6.4 (2020-08-19)
* Config files refactoring. There is a new default config file config.default.properties which has to be placed in the conf/ folder.
* Every specific config file, e.g. config.zb-erara.properties, does only overwrite the submission specific properties. The properties which
* are typically overwritten are marked with the value "needs to be set in specific config"

### version 1.6.3 (2020-08-12)
* When a source file was deleted before the cleanup job took place, we gonna add an entry to SUBMISSION_LIFECYCLE
* otherwise, this file would be considered for deletion every run, if not fixed manually, which is the same
* as adding an entry

### version 1.6.2 (2020-05-06)
* Source cleanup functionality with configuration. Can be configured on demand

### version 1.6.1 (2020-03-24)
* Fix for large number of status effecting the same item

### version 1.6 (2019-01-30)
* Validation of source data
  * hardening: check if source folder structure has only allowed folders
  * hardening: check file types
* existing lock will stop application and
  display appropriate error message

### version 1.5.2 (2018-12-13)

* Added lock file logic
* start of application will create a lock file per config that is run
* lock will be removed once current run has ended

### version 1.5.1 (2018-11-20)
* finding the correct alephid in mets that is also part of the file name (1.5)
  * extracting all relevant data from mets section that contains aleph id
  * doi, title, shelf location
  * optional: save correct alephid
  * note: refactoring of many key elements in code base
* Lock file to show which job is running (1.5.1)

### version 1.4.3 (2018-10-31)
* small refactoring of config file call

### version 1.4.2 (2018-10-30)
* version directory added
* version file moved to directory

### version 1.4.1 (2018-10-16)
* version output in log
* define individual log4j file location
* define individual port per run
* Cleanup start log and AppStarter class

### version 1.4 (2018-09-26)
* remove institutes from file listing (1.4)
* preparation for 2.0 upgrade
* config files need to be updated

### version 1.3
* added ability to handle fulltext folders in SIP_STATUS
* updated config.properties for fulltext file ha ####ndling
* updated config.properties for e-rara and e-manuscripta sources
* fixed time calcucation error for updating listing file content

#### version 1.2.1
* Refactoring of getAllSips in SourceFileListing
* Add extensive Javadoc to SourceFileListing, SubmissionSingleton and HashGenerator classes

#### version 1.2

* generate text file with all files names, abs path, file size in byte of source folder
* text file creation parameterized
  * 0 immediately
  * 1 one day
  * 2 2 days
  * 3 3 days
  * ...
* iterate over file content like and generate SortedSet<SourceSip>


#### version 1.1

* update XPath handling for recordIdentifier, has to be the last
  same goes for DOI
* 000224758_20101118T000102_master_ver1.zip mets file not found problem
* check file extensions

#### version 1.0.1
* exif without stay_open

#### version 1.0

* exif modification handling
   * check all tiff exif for correct ModifyDate format
   * fix ModifyDate if needed
   * do nothing if ModifyDate is empty or null
   * create md5 only after ModifyDate has been fixed
   * https://github.com/thebuzzmedia/exiftool (daemon mode for faster results)
* check gen logik
   * first gen after master must end with xxxx and have a larger date string
   * subsequent gen files must have a larger date string and exactly +1 in generation
* add entitytype "Book" for section generalIECharacteristics in ie.xml
* Update on server installtion of submission app

#### version 0.8.1
* make live tests on development system
* minor fixes to improved selection process of SIP files
* change readout of alephid from mets
* add / restructure handling of integrity of extracted SIP
	* add test if alephid from mets corresponds with SIP file name
		* if fails status INTEGRETY_ERROR-INVALID_ID
		* if fails keep extracted copy
		* SubmissionSingleton::checkExtractedSipIntegrity extended
	* add output info and programm handling
		* add status for INTEGRETY_ERROR-MISSING_METS
		* add status for INTEGRETY_ERROR-WRONG_FILES
* haltWithError refactoring
	* logger.error
	* system.exit

#### version 0.8
* large leap from previous version (lots of rewrites)
* database connectivity and file logic
   * create file list to work through
      * allow max allowed file size
	  * allow max number per institute
      * read out file list and compare with status in DB to find correct
         * acquire file list and order by file name for both institutes
         * acquire DB data
            * implement database connectivitiy
         * compare list and compile bulk
            * the same file is already in DB
            * if delta (gen1) a master must be FINISHED in DB
		    * no file with Aleph ID that currently is in DB with status <> FINISHED is allowed
            * no file with Aleph ID that and has DB status FINISHED but lower timestamp than in DB is allowed
			* one master per Aleph ID
			* first file per Aleph ID must be a master
            * no two files with the same Aleph ID are allowed per bulk
   * define status for the files
   * add each file to work with into DB
      * Timestamp + AlephID = AMD_ID in db 003866469_20120502T230108
   * handle files from current run (copy, extract, move)

#### version 0.5
* Test on server (run extractions)
* get real files listing in text file
* choose example files
* successful working version without db connectivity

#### version 0.4.1
* move extracted tree to target
* implement max number of files
* handle delta SIP without any images
* add simple logging mechanism / error handler
* paramter to supply location of app configuration

#### version 0.4
* building export_mets.xml extractor
* understanding ethdeposit xml generation
* fill ie.xml
* test run export

#### version 0.3

* fill dc.xml
* implement md5 checksum
* allow multiple source folders with corresponding target folders
* NOT implemented Ex Libris specific loggin
* NOT implemented Ex Libris specific xml Properties

#### version 0.2
* check if export_mets.xml exisits
* Create SIP Structure in sip-working folder
* Implement prototyp logic (source copy to pre-extract, pre-extract unzip to extract)
* create empty dc.xml
* create empty ie.xml
* md5 debug output for all files in sip folder

#### version 0.1
* Copy file, FileHandler
* Extract file, FileHandler
* Move to location, FileHandle
* Create Configuration handler
* Configuration file in Java properties file

#### preparation
* set up development environment
* set up IDE
* install and set up correct version of JVM
* execute previous projects
* understanding business domain and requirements
* [Jira DDE-167](https://spoc.ethbib.ethz.ch/browse/DDE-167)
