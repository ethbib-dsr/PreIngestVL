<<<<<<< HEAD
# Submission application for Visual Library #
=======
# Submission application for Visual Library
>>>>>>> master

Application that picks up zipped SIP capsules , extracts them, creates Rosetta mets.xml and copies everything to a target location.
Java Application started by separate shell script.

<<<<<<< HEAD
## Application start ##
=======
## Application start
>>>>>>> master

com.exlibris.dps.submissionvl.AppStarter "config.test.properties" "log4j.properties" "7777"

* parameter 1: properties file
* parameter 2: log4j configuration
* parameter 3: [optional] port number, default: 7777

<<<<<<< HEAD
## Current Version: 1.5.1 ##
=======
## Current Version: 1.6
>>>>>>> master

### TODO ###

* sqlite tracking to replace current file based tracking system (1.6)
  * refactoring large parts of inner logic
  * implementing and handling of status
  * alephid and maybe mets section
  * note: removes complete file base tracking logic
<<<<<<< HEAD
* Validation of source data
  * hardening: check if source folder structure has only allowed folders
  * hardening: check file types
  * add third folder for import (transcript)

### VERSION HISTORY ###

### version 1.5.1 (2018-11-20) ###
=======
* Flexible validation of sources data structure
  * add third folder for import (transcription)

### VERSION HISTORY ###

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
>>>>>>> master
* finding the correct alephid in mets that is also part of the file name (1.5)
  * extracting all relevant data from mets section that contains aleph id
  * doi, title, shelf location
  * optional: save correct alephid
  * note: refactoring of many key elements in code base
* Lock file to show which job is running (1.5.1)

<<<<<<< HEAD
### version 1.4.3 (2018-10-31) ###
* small refactoring of config file call

### version 1.4.2 (2018-10-30) ###
* version directory added
* version file moved to directory

### version 1.4.1 (2018-10-16) ###
=======
### version 1.4.3 (2018-10-31)
* small refactoring of config file call

### version 1.4.2 (2018-10-30)
* version directory added
* version file moved to directory

### version 1.4.1 (2018-10-16)
>>>>>>> master
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
<<<<<<< HEAD
* updated config.properties for fulltext file handling
=======
* updated config.properties for fulltext file ha ####ndling
>>>>>>> master
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
