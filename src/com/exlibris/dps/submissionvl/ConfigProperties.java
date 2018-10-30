package com.exlibris.dps.submissionvl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Configuration Object
 * 
 * Getter for all variables in config.properties Each variable has to have a
 * constant
 * 
 * 
 * @author Lars Haendler
 * 
 */
public class ConfigProperties
{
	// relative path of config.properties file
	final String CONFIG_PATH;

	//common file size option in byte
	public final static long FILESIZ_BYTE = 1024;
	public final static long FILESIZ_KB = (long) Math.pow(FILESIZ_BYTE, 1);
	public final static long FILESIZ_MB = (long) Math.pow(FILESIZ_BYTE, 2);
	public final static long FILESIZ_GB = (long) Math.pow(FILESIZ_BYTE, 3);	
	
	// constants for keys in config.properties
	final static String PROPERTIES_MULTI_VALUE_DIVIDER = "properties-multi-value-divider";
	final static String SOURCE_PATH_KEY = "source-path";
	final static String TARGET_PATH_KEY = "target-path";
	final static String EXTRACT_PATH_KEY = "extract-path";
	final static String PRE_EXTRACT_PATH_KEY = "pre-extract-path";
	final static String MAX_SOURCE_FILES = "max-source-files";
	final static String SIP_DATA_PATH = "sip-data-path";
	final static String SIP_XML_PATH = "sip-xml-path";
	final static String SIP_IMAGE_DIRECTOY = "sip-image-directory";
	final static String SIP_FULLTEXT_DIRECTOY = "sip-fulltext-directory";
	final static String ALLOWED_SOURCE_FILE_ENDINGS = "allowed-source-file-endings";
	final static String ALLOWED_IMAGE_FILE_ENDINGS = "allowed-image-file-endings";
	final static String METS_FILE_NAME = "mets-file-name";
	final static String ALLOWED_ARCHIVE_TYPE = "allowed-archive-type";
	final static String DC_FILE_NAME = "dc-file-name";
	final static String IE_FILE_NAME = "ie-file-name";
	final static String MAX_SOURCE_FILE_SIZE = "max-source-file-size";
	final static String LISTING_FILE_AGE = "listing-file-age"; 
	final static String LISTING_FILE_NAME = "listing-file-name";
	
	final static String XML_DC_TITLE = "xml-dc-title";
	final static String XML_DCTERMS_IS_PART = "xml-dcterms-is-part";
	final static String XML_DC_IDENTIFIER = "xml-dc-identifier";
	final static String XML_DC_RELATION = "xml-dc-relation";
	final static String XML_DC_DATE = "xml-dc-date";
	final static String XML_DCTERMS_ALTERNATIVE = "xml-dcterms-alternative";
	final static String XML_DC_SOURCE = "xml-dc-source";
	final static String XML_GENERAL_REV_NO = "xml-general-rev-no";
	final static String XML_GENERAL_DIG_ORIG = "xml-general-dig-orig";
	final static String XML_DC_INSTITUTE ="xml-dc-institute";
	
	final static String XML_PATH_DIVIDER = "xml-path-divider";
	final static String XML_DOMAIN_ROOT = "xml-domain-root";
	final static String XML_CMS_SYSTEM = "xml-cms-system";
	final static String XML_FIXITY_ALGORITHM_MD5 = "xml-fixity-algorithm-md5";
	final static String XML_FIXITY_ALGORITHM_SHA1 = "xml-fixity-algorithm-sha1";
	final static String XML_METS_FILE_TYPE = "xml-mets-file-type";
	final static String XML_DC_DATE_FORMATTING = "xml-dc-date-formatting";
	final static String XML_GENERAL_IE_ENTITYTYPE = "xml-general-ie-entitytype";
	
	final static String DB_CONNECTION_URL = "db-connection-url";
	final static String DB_DRIVER_NAME = "db-driver-name";
	final static String DB_USERNAME = "db-username";
	final static String DB_PASSWORD = "db-password";
	final static String DB_CURRENT_TIME_JAVAFORMAT = "db-current-time-javaformat";
	final static String DB_CURRENT_TIME_ORACLEFORMAT = "db-current-time-oracleformat";
	final static String DB_ERARA_TABLE = "db-erara-table";
	final static String DB_INSERT_WORKSPACE_ID = "db-insert-workspace-id";
	final static String DB_INSERT_SIP_TYPE = "db-insert-sip-type";
	final static String DB_ROW_WORKFLOW = "db-row-workflow";
	final static String DB_ROW_WORKSPACE = "db-row-workspace";
	final static String DB_ROW_AMD = "db-row-amd";
	final static String DB_ROW_SUBMITTS = "db-row-submitts";
	final static String DB_ROW_SOURCEPATH = "db-row-sourcepath";
	final static String DB_ROW_SIPTYPE = "db-row-siptype";
	final static String DB_ROW_SIPSTATUS = "db-row-sipstatus";
	final static String DB_ROW_SIPNAME = "db-row-sipname";
	final static String DB_ROW_SIPPATH = "db-row-sippath";
	final static String DB_ROW_UPDATEDT = "db-row-updatedt";
	final static String DB_ROW_ALIAS_TIMESTAMP = "db-row-alias-timestamp";
	final static String DB_UPDATE_STATUS_FINAL = "db-update-status-final";
	final static String DB_SELECT_COUNT_AMDID = "db-select-count-amdid";
	final static String DB_SELECT_COUNT_WHERE = "db-select-count-where";
	final static String DB_SELECT_ALEPHID_WHERE = "db-select-alephid-where";
	final static String DB_SELECT_ALEPHID_ORDER = "db-select-alephid-order";
	final static String DB_ROW_STATUS_FINISHED = "db-row-status-finished";
	
	final static String DB_STATUS_INITIALIZED = "db-status-initialized";
	final static String DB_STATUS_COPIED = "db-status-copied";
	final static String DB_STATUS_EXTRACTED = "db-status-extracted";
	final static String DB_STATUS_EXIF = "db-status-exif";
	final static String DB_STATUS_METADATA = "db-status-metadata";
	final static String DB_STATUS_MOVED2TARGET = "db-status-moved2target";
	final static String DB_STATUS_PREINGEST_FINISHED = "db-status-preingest-finished";
	final static String DB_STATUS_INTEGRITY_INVALIDID = "db-status-integrity-invalidid";
	final static String DB_STATUS_INTEGRITY_MISSINGMETS = "db-status-integrity-missingmets";
	final static String DB_STATUS_INTEGRITY_WRONGFILES = "db-status-integrity-wrongfiles";
	final static String DB_STATUS_CONTAINS_FULLTEXT = "db-status-sip-contains-fulltext";
	
	final static String REASON_MAX_NUM = "reason-max-num";
	final static String REASON_FILESIZE = "reason-filesize";
	final static String REASON_UNIQUE = "reason-unique";
	final static String REASON_ALREADY_IN_DB = "reason-already-in-db";
	final static String REASON_SINGLE_MASTER = "reason-single-master";
	final static String REASON_MUSTBE_NEWER = "reason-mustbe-newer";
	final static String REASON_FINISHED = "reason-finished";
	final static String REASON_FIRST_MASTER = "reason-first-master";
	final static String REASON_SIP_TYPE_UNKNW = "reason-sip-type-unknwn";
	final static String REASON_FIRST_DELTA = "reason-first-delta";
	final static String REASON_DELTA_PLUSONE = "reason-delta-plusone"; 
	final static String REASON_WRONG_EXTENSION = "reason-wrong-extension";
	
	final static String INTEGRITY_INVALID_ID = "integrity-invalid-id";
	final static String INTEGRITY_MISSING_METS = "integrity-missing-mets";
	final static String INTEGRITY_WRONG_FILES = "integrity-wrong-files";
	
	final static String EXIF_DATE_FIRST_WRONG_CHAR = "exif-date-first-wrong-char";
	final static String EXIF_DATE_FIRST_WRONG_CHAR_POS = "exif-date-first-wrong-char-pos";
	final static String EXIF_DATE_FIRST_WRONG_CHAR_FIX = "exif-date-first-wrong-char-fix";
	final static String EXIF_DATE_SECOND_WRONG_CHAR = "exif-date-second-wrong-char";
	final static String EXIF_DATE_SECOND_WRONG_CHAR_POS = "exif-date-second-wrong-char-pos";
	final static String EXIF_DATE_SECOND_WRONG_CHAR_FIX = "exif-date-second-wrong-char-fix";

	final static String XPATH_ALEPHID = "xpath-alephid";
	final static String XPATH_DOI = "xpath-doi";
	final static String XPATH_ALT_TITLE = "xpath-alt_title";
	final static String XPATH_LOCATION = "xpath-location";
	final static String REGEX_ALEPH_ID = "regex-alephid";
	final static String FILE_NODE_NAME = "file-node-name";
	final static String FILe_ATTRIBUTE_ID = "file-attribute-id";
	final static String FILE_ATTRIBUTE_CREATED = "file-attribute-created";
	final static String FILE_ATTRIBUTE_MIMETYPE = "file-attribute-mimetype";
	final static String FILE_ATTRIBUTE_CHECKSUM = "file-attribute-checksum";
	final static String FILE_ATTRIBUTE_CHECKSUMTYPE = "file-attribute-checksumtype";
	final static String FILE_ATTRIBUTE_SIZE = "file-attribute-size";
	final static String FILENAME_NODE_NAME = "filename-node-name";
	final static String FILENAME_ATTRIBUTE_NAME = "filename-attribute-name";

	
	/**
	 * Constructor, needs path to config.properties file
	 * 
	 * @param configPath
	 */
	public ConfigProperties(String configPath)
	{
		if(!configPath.startsWith(File.separator))
		{
			CONFIG_PATH = File.separator + configPath;
		}
		else
		{
			CONFIG_PATH = configPath;	
		}
	}


	/**
	 * returns config property for supplied key
	 * 
	 * @param key
	 *           from property to extract
	 * @return String value for property key
	 */
	private String getElementFromProperty(String key)
	{
		String r = "";
		String canPath = "";

		Properties prop = new Properties();

		try
		{
			canPath = new File("." + CONFIG_PATH).getCanonicalPath();
			InputStream inputStream = new FileInputStream(canPath);
			prop.load(inputStream);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("FileNotFoundException: " + e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
		}

		r = prop.getProperty(key).trim();

		return r;
	}


	/**
	 * returns List with all elements trimmed
	 * 
	 * @param propertyList
	 *           input List
	 * @return List<String>
	 */
	private List<String> trimAllListElements(List<String> propertyList)
	{
		List<String> returnList = new ArrayList<String>();

		for (String listElement : propertyList)
		{
			returnList.add(listElement.trim());
		}

		return returnList;
	}


	/**
	 * returns List of String of all values in a multi value property
	 * 
	 * @param String
	 *           key from property
	 * @return List<String>
	 */
	private List<String> getListFromMultiValueProperty(String key)
	{
		List<String> returnList = new ArrayList<String>();
		String multiValuePropterty = getElementFromProperty(key);

		if (multiValuePropterty.contains(getPropertiesMultiValueDivider()))
		{
			returnList = Arrays.asList(multiValuePropterty.split(getPropertiesMultiValueDivider()));
		}
		else
		{
			returnList.add(multiValuePropterty);
		}

		return trimAllListElements(returnList);
	}


	/**
	 * Getter for source path
	 * 
	 * @return String source path from config.properties
	 */
	public String getSourcePath()
	{
		return getElementFromProperty(SOURCE_PATH_KEY);
	}


	/**
	 * Getter for target path
	 * 
	 * @return String target path from config.properties
	 */
	public String getTargetPath()
	{
		return getElementFromProperty(TARGET_PATH_KEY);
	}


	/**
	 * Getter for extract path
	 * 
	 * @return String extract path from config.properties
	 */
	public String getExtractPath()
	{
		return getElementFromProperty(EXTRACT_PATH_KEY);
	}


	/**
	 * Getter for pre-extract path
	 * 
	 * @return String pre-extract path from config.properties
	 */
	public String getPreExtractPath()
	{
		return getElementFromProperty(PRE_EXTRACT_PATH_KEY);
	}


	/**
	 * Getter for max source files
	 * 
	 * @return int number of files
	 */
	public int getMaxSourceFiles()
	{
		return Integer.parseInt(getElementFromProperty(MAX_SOURCE_FILES));
	}

	/**
	 * Getter for maximum age of listing file in days
	 * 
	 * @return int number of days
	 */
	public int getListingFileAge()
	{
		return Integer.parseInt(getElementFromProperty(LISTING_FILE_AGE));
	}

	
	/**
	 * Getter for listing file name
	 * 
	 * @return String file name
	 */
	public String getListingFileName()
	{
		return getElementFromProperty(LISTING_FILE_NAME);
	}	
	
	
	/**
	 * Getter for SIP data path
	 * 
	 * @return String sip-data-path
	 */
	public String getSipDataPath()
	{
		return getElementFromProperty(SIP_DATA_PATH);
	}


	/**
	 * Getter for SIP xml path
	 * 
	 * @return String sip-xml-path
	 */
	public String getSipXmlPath()
	{
		return getElementFromProperty(SIP_XML_PATH);
	}


	/**
	 * Getter for SIP image directory name
	 * 
	 * @return String sip-image-directory
	 */
	public String getSipImageDirectory()
	{
		return getElementFromProperty(SIP_IMAGE_DIRECTOY);
	}


	/**
	 * Getter for SIP fulltext directory name
	 * 
	 * @return String sip-fulltext-directory
	 */
	public String getSipFulltextDirectory()
	{
		return getElementFromProperty(SIP_FULLTEXT_DIRECTOY);
	}


	/**
	 * returns String array with allowed image endings multiple file endings are
	 * extracted by using the PROPERTIES_MULTI_VALUE_DIVIDER
	 * 
	 * @return List<String> with all image file endings
	 */
	public List<String> getAllowedImageFileEndings()
	{
		return getListFromMultiValueProperty(ALLOWED_IMAGE_FILE_ENDINGS);
	}


	/**
	 * Getter for returning of PROPERTIES_MULTI_VALUE_DIVIDER
	 * 
	 * @return String properties divider
	 */
	public String getPropertiesMultiValueDivider()
	{
		return getElementFromProperty(PROPERTIES_MULTI_VALUE_DIVIDER);
	}


	/**
	 * Getter for return mets-file-name
	 * 
	 * @return String mets file name
	 */
	public String getMetsFileName()
	{
		return getElementFromProperty(METS_FILE_NAME);
	}


	/**
	 * Getter for return allowed-archive-type
	 * 
	 * @return String allowed archive type
	 */
	public String getAllowedArchiveType()
	{
		return getElementFromProperty(ALLOWED_ARCHIVE_TYPE);
	}


	/**
	 * Getter for return dc-file-name
	 * 
	 * @return String dc file name
	 */
	public String getDcFileName()
	{
		return getElementFromProperty(DC_FILE_NAME);
	}


	/**
	 * Getter for return ie-file-name
	 * 
	 * @return String ie file name
	 */
	public String getIeFileName()
	{
		return getElementFromProperty(IE_FILE_NAME);

	}

	
	/**
	 * Getter for system file separator
	 * 
	 * @return String
	 */
	static public String getFileSeparator()
	{
		return System.getProperty("file.separator");
	}


	/**
	 * Getter for xml path divider
	 * 
	 * @return String
	 */
	public String getXmlPathDivider()
	{
		return getElementFromProperty(XML_PATH_DIVIDER);
	}


	/**
	 * Getter for xml domain root
	 * 
	 * @return String
	 */
	public String getXmlDomainRoot()
	{
		return getElementFromProperty(XML_DOMAIN_ROOT);
	}
	
	/**
	 * Getter for xml CMS system key
	 * 
	 * @return String
	 */
	public String getXmlCmsSystem()
	{
		return getElementFromProperty(XML_CMS_SYSTEM);
	}
	

	/**
	 * Getter for xml fixity algorithm md5
	 * 
	 * @return String
	 */
	public String getXmlFixityAlgorithMd5()
	{
		return getElementFromProperty(XML_FIXITY_ALGORITHM_MD5);
	}

	
	/**
	 * Getter for xml fixity algorithm sha-1
	 * 
	 * @return String
	 */
	public String getXmlFixityAlgorithSha1()
	{
		return getElementFromProperty(XML_FIXITY_ALGORITHM_SHA1);
	}	
	
	
	/**
	 * Getter for xml mets file type
	 * 
	 * @return
	 */
	public String getXmlMetsFileType()
	{
		return getElementFromProperty(XML_METS_FILE_TYPE);
	}	
	
	
	/**
	 * Getter for xml dc title
	 * 
	 * @return
	 */
	public String getXmlDcTitle()
	{
		return getElementFromProperty(XML_DC_TITLE);
	}
	
	/**
	 * Getter for xml dc terms is part of
	 * 
	 * @return
	 */
	public String getXmlDcTermsIsPart()
	{
		return getElementFromProperty(XML_DCTERMS_IS_PART);
	}
	
	
	/**
	 * Getter for xml dc identifier
	 * 
	 * @return
	 */
	public String getXmlDcIdentifier()
	{
		return getElementFromProperty(XML_DC_IDENTIFIER);
	}
	
	
	/**
	 * Getter for xml dc relation
	 * 
	 * @return
	 */
	public String getXmlDcRelation()
	{
		return getElementFromProperty(XML_DC_RELATION);
	}
	

	/**
	 * Getter for xml dc date format
	 * 
	 * @return
	 */
	public String getXmlDcDateFormatting()
	{
		return getElementFromProperty(XML_DC_DATE_FORMATTING);
	}
	
	
	/**
	 * Getter for xml dc date
	 * 
	 * @return
	 */
	public String getXmlDcDate()
	{
		return getElementFromProperty(XML_DC_DATE);
	}
	
	
	/**
	 * Getter for xml dc date
	 * 
	 * @return
	 */
	public String getXmlDcTermsAlternative()
	{
		return getElementFromProperty(XML_DCTERMS_ALTERNATIVE);
	}
	
	
	/**
	 * Getter for xml dc date
	 * 
	 * @return
	 */
	public String getXmlDcSource()
	{
		return getElementFromProperty(XML_DC_SOURCE);
	}
	
	
	/**
	 * Getter for xml content general revision number
	 * 
	 * @return
	 */
	public String getXmlGeneralRevNo()
	{
		return getElementFromProperty(XML_GENERAL_REV_NO);
	}
	
	
	/**
	 * Getter for xml content general digital original
	 * 
	 * @return
	 */
	public String getXmlGeneralDigOrig()
	{
		return getElementFromProperty(XML_GENERAL_DIG_ORIG);
	}
	
	
	/**
	 * Getter for DC terms:isPartof institute name
	 * 
	 * @return
	 */
	public String getXmlDcInstitute()
	{
		return getElementFromProperty(XML_DC_INSTITUTE);
	}	
	
	
	/**
	 * Getter for ie xml entitytype in DNX section
	 * 
	 * @return
	 */
	public String getXmlGeneralIeEntitytype()
	{
		return getElementFromProperty(XML_GENERAL_IE_ENTITYTYPE);
	}


	/**
	 * Getter for db connection driver name
	 * 
	 * @return
	 */
	public String getDbDriverName()
	{
		return getElementFromProperty(DB_DRIVER_NAME); 
	}
	
	
	/**
	 * Getter for db connection url
	 * 
	 * @return
	 */
	public String getDbConnectionUrl()
	{
		return getElementFromProperty(DB_CONNECTION_URL);
	}
	
	
	/**
	 * Getter for db connection user name
	 * 
	 * @return
	 */
	public String getDbUsername()
	{
		return getElementFromProperty(DB_USERNAME); 
	}
	
	
	/**
	 * Getter for db connection password
	 * 
	 * @return
	 */
	public String getDbPassword()
	{
		return getElementFromProperty(DB_PASSWORD);
	}	
	

	/**
	 * Getter for insert table
	 * 
	 * @return
	 */
	public String getDbEraraTable()
	{
		return getElementFromProperty(DB_ERARA_TABLE);
	}
	
	
	/**
	 * Getter for workspace_id value
	 * 
	 * @return
	 */
	public String getDbInsertWorkspaceId()
	{
		return getElementFromProperty(DB_INSERT_WORKSPACE_ID);
	}
	
	
	/**
	 * Getter for java time format configuration
	 * 
	 * @return
	 */
	public String getDbCurrentTimeJavaFormat()
	{
		return getElementFromProperty(DB_CURRENT_TIME_JAVAFORMAT);
	}
	
	
	
	/**
	 * Getter for oracle time format configuration
	 * 
	 * @return
	 */
	public String getDbCurrentTimeOracleFormat()
	{
		return getElementFromProperty(DB_CURRENT_TIME_ORACLEFORMAT);
	}	
	
	
	/**
	 * Getter value for sip_type
	 * 
	 * @return
	 */
	public String getDbInsertSipType()
	{
		return getElementFromProperty(DB_INSERT_SIP_TYPE);
	}
	
	
	/**
	 * Getter value for column workflow
	 * 
	 * @return
	 */
	public String getDbRowWorkflow()
	{
		return getElementFromProperty(DB_ROW_WORKFLOW);
	}
	
	
	/**
	 * Getter value for column workspace
	 * 
	 * @return
	 */
	public String getDbRowWorkspace()
	{
		return getElementFromProperty(DB_ROW_WORKSPACE);
	}	
	
	
	/**
	 * Getter value for column amd_id
	 * 
	 * @return
	 */
	public String getDbRowAmd()
	{
		return getElementFromProperty(DB_ROW_AMD);
	}
	
	
	/**
	 * Getter value for column submit_timestamp
	 * 
	 * @return
	 */
	public String getDbRowSubmitTs()
	{
		return getElementFromProperty(DB_ROW_SUBMITTS);
	}
	
	
	/**
	 * Getter value for column source_path
	 * 
	 * @return
	 */
	public String getDbRowSourcePath()
	{
		return getElementFromProperty(DB_ROW_SOURCEPATH);
	}
	
	
	/**
	 * Getter value for column sip_type
	 * 
	 * @return
	 */
	public String getDbRowSipType()
	{
		return getElementFromProperty(DB_ROW_SIPTYPE);
	}
	
	
	/**
	 * Getter value for column sip_status
	 * 
	 * @return
	 */
	public String getDbRowSipStatus()
	{
		return getElementFromProperty(DB_ROW_SIPSTATUS);
	}	
	
	
	/**
	 * Getter value for column sip_name
	 * 
	 * @return
	 */
	public String getDbRowSipName()
	{
		return getElementFromProperty(DB_ROW_SIPNAME);
	}
	
	
	/**
	 * Getter value for column sip_path
	 * 
	 * @return
	 */
	public String getDbRowSipPath()
	{
		return getElementFromProperty(DB_ROW_SIPPATH);
	}

	
	/**
	 * Getter value for column update_dt
	 * 
	 * @return
	 */
	public String getDbRowUpdateDate()
	{
		return getElementFromProperty(DB_ROW_UPDATEDT);
	}
	
	
	/**
	 * Getter value for alias timestamp
	 * 
	 * @return
	 */
	public String getDbRowAliasTimestamp()
	{
		return getElementFromProperty(DB_ROW_ALIAS_TIMESTAMP);
	}
	
	/**
	 * Getter value for select count query
	 * 
	 * @return
	 */
	public String getDbSelectCountAmdId()
	{
		return getElementFromProperty(DB_SELECT_COUNT_AMDID);
	}
	
	
	/**
	 * Getter value for select count where condition
	 * 
	 * @return
	 */
	public String getDbSelectCountWhere()
	{
		return getElementFromProperty(DB_SELECT_COUNT_WHERE);
	}
	
	
	/**
	 * Getter value for select segment of timestamp 
	 * 
	 * @return
	 */
	public String getDbSelectAlephIdWhere()
	{
		return getElementFromProperty(DB_SELECT_ALEPHID_WHERE);
	}
	
	
	/**
	 * Getter value for order for select for aleph rows
	 * 
	 * @return
	 */
	public String getDbSelectAlephIdOrder()
	{
		return getElementFromProperty(DB_SELECT_ALEPHID_ORDER);
	}	
	

	/**
	 * Getter for value of sip_status 
	 * 
	 * @return
	 */
	public String getDbUpdateStatusFinal()
	{
		return getElementFromProperty(DB_UPDATE_STATUS_FINAL);
	}	
	
	/**
	 * Getter for value of sip status copied
	 * 
	 * @return
	 */
	public String getDbStatusCopied()
	{
		return getElementFromProperty(DB_STATUS_COPIED);
	}
	
	
	/**
	 * Getter for value of sip status extracted
	 * 
	 * @return
	 */
	public String getDbStatusExtracted()
	{
		return getElementFromProperty(DB_STATUS_EXTRACTED);
	}

	
	/**
	 * Getter for value of sip status exifs have beend checked and fixed
	 * 
	 * @return
	 */
	public String getDbStatusExif()
	{
		return getElementFromProperty(DB_STATUS_EXIF);
	}	
	
	
	/**
	 * Getter for value of sip status metadata added
	 * 
	 * @return
	 */
	public String getDbStatusMetadata()
	{
		return getElementFromProperty(DB_STATUS_METADATA);
	}
	
	
	/**
	 * Getter for value of sip status moved 2 target directory
	 * 
	 * @return
	 */
	public String getDbStatusMoved2Target()
	{
		return getElementFromProperty(DB_STATUS_MOVED2TARGET);
	}
	
	
	/**
	 * Getter for value of sip status preingest process finished
	 * 
	 * @return
	 */
	public String getDbStatusPreingestFinished()
	{
		return getElementFromProperty(DB_STATUS_PREINGEST_FINISHED);
	}
	
	
	/**
	 * Getter for value of sip status that file has been initialized
	 * 
	 * @return
	 */
	public String getDbStatusInitialized()
	{
		return getElementFromProperty(DB_STATUS_INITIALIZED);
	}	
	
	
	/**
	 * Getter for value of integrity problems with extracted zip
	 * aleph ID of file does not match with ID in mets.xml
	 * 
	 * @return
	 */
	public String getDbStatusIntegrityInvalidId()
	{
		return getElementFromProperty(DB_STATUS_INTEGRITY_INVALIDID);
	}	
	
	
	/**
	 * Getter for value of integrity problems with extracted zip
	 * mets.xml is not part of zip
	 * 
	 * @return
	 */
	public String getDbStatusIntegrityMissingMets()
	{
		return getElementFromProperty(DB_STATUS_INTEGRITY_MISSINGMETS);
	}		
	
	
	/**
	 * Getter for value of integrity problems with extracted zip
	 * files are not of the correct type / wrong extension
	 * 
	 * @return
	 */
	public String getDbStatusIntegrityWrongFiles()
	{
		return getElementFromProperty(DB_STATUS_INTEGRITY_WRONGFILES);
	}		
	
	
	/**
	 * Getter for value of zip with fultext folder
	 * files are not allowed
	 * 
	 * @return
	 */
	public String getDbStatusContainsFulltext()
	{
		return getElementFromProperty(DB_STATUS_CONTAINS_FULLTEXT);
	}		
	
	
	/**
	 * Getter for reason max number
	 * 
	 * @return
	 */
	public String getReasonMaxNum()
	{
		return getElementFromProperty(REASON_MAX_NUM);
	}

	
	/**
	 * Getter for reason file too big
	 * 
	 * @return
	 */
	public String getReasonFileSize()
	{
		return getElementFromProperty(REASON_FILESIZE);
	}	
	
	
	/**
	 * Getter for reason aleph id in current run must be unique
	 * 
	 * @return
	 */
	public String getReasonUnique()
	{
		return getElementFromProperty(REASON_UNIQUE);
	}	
	
	
	/**
	 * Getter for reason file already in DB
	 * 
	 * @return
	 */
	public String getReasonAlreadyInDb()
	{
		return getElementFromProperty(REASON_ALREADY_IN_DB);
	}		
	
	
	/**
	 * Getter for reason a master is already in the DB
	 * 
	 * @return
	 */
	public String getReasonSingleMaster()
	{
		return getElementFromProperty(REASON_SINGLE_MASTER);
	}	
	

	/**
	 * Getter for reason file must be newer than 
	 * what is already with same Aleph ID in DB
	 * 
	 * @return
	 */
	public String getReasonMustBeNewer()
	{
		return getElementFromProperty(REASON_MUSTBE_NEWER);
	}		
	
	
	/**
	 * Getter for reason that there are other SIPs with 
	 * same Aleph ID that are current not finished
	 * 
	 * @return
	 */
	public String getReasonFinished()
	{
		return getElementFromProperty(REASON_FINISHED);
	}	
	
	
	/**
	 * Getter for reason that first file must be a master 
	 * 
	 * @return
	 */
	public String getReasonFirstMaster()
	{
		return getElementFromProperty(REASON_FIRST_MASTER);
	}	

	
	/**
	 * Getter for reason that SIP type is unknows
	 * neither master nor gen
	 * 
	 * @return
	 */
	public String getReasonSipTypeUnknwn()
	{
		return getElementFromProperty(REASON_SIP_TYPE_UNKNW);
	}	
	
	
	/**
	 * Getter for reason that first delta/gen must have gen1
	 * 
	 * @return
	 */
	public String getReasonFirstDelta()
	{
		return getElementFromProperty(REASON_FIRST_DELTA);
	}	
	
	
	/**
	 * Getter for reason that delta SIP must always one taller
	 * than latest delta SIP in DB
	 * 
	 * @return
	 */
	public String getReasonDeltaPlusOne()
	{
		return getElementFromProperty(REASON_DELTA_PLUSONE);
	}		

	
	/**
	 * Getter for wrong SIP file extension
	 * 
	 * @return
	 */
	public String getReasonWrongExtension()
	{
		return getElementFromProperty(REASON_WRONG_EXTENSION);
	}

	
	/**
	 * Getter for final status of SIP when Rosetta has completed all work on/with it
	 * 
	 * @return
	 */
	public String getDbRowStatusFinished()
	{
		return getElementFromProperty(DB_ROW_STATUS_FINISHED);
	}
	
	
	/**
	 * Getter for reason integrity when aleph id in file name
	 * does not equal aleph id in mets.xml
	 * 
	 * @return
	 */
	public String getIntegrityInvalidId()
	{
		return getElementFromProperty(INTEGRITY_INVALID_ID);
	}
	
	
	/**
	 * Getter for reason integrity when mets.xml is missing
	 * 
	 * @return
	 */
	public String getIntegrityMissingMets()
	{
		return getElementFromProperty(INTEGRITY_MISSING_METS);
	}
	
	
	/**
	 * Getter reason integrity when wrong file extensions are in zip
	 * 
	 * @return
	 */
	public String getIntegrityWrongFiles()
	{
		return getElementFromProperty(INTEGRITY_WRONG_FILES);
	}	
	
	
	/**
	 * Getter for first wrong char in ModifyDate of exif
	 * 
	 * @return
	 */
	public char getExifDateFirstWrongChar()
	{
		return getElementFromProperty(EXIF_DATE_FIRST_WRONG_CHAR).toCharArray()[0];
	}
	
	
	/**
	 * Getter for position of first wrong char in ModifyDate of exif
	 * 
	 * @return
	 */
	public int getExifDateFirstWrongCharPos()
	{
		return Integer.parseInt(getElementFromProperty(EXIF_DATE_FIRST_WRONG_CHAR_POS));
	}
	
	
	/**
	 * Getter for fix of first wrong char in ModifyDate of exif
	 * 
	 * @return
	 */
	public char getExifDateFirstWrongCharFix()
	{
		return getElementFromProperty(EXIF_DATE_FIRST_WRONG_CHAR_FIX).toCharArray()[0];
	}
	
	
	/**
	 * Getter for second wrong char in ModifyDate of exif
	 * 
	 * @return
	 */
	public char getExifDateSecondWrongChar()
	{
		return getElementFromProperty(EXIF_DATE_SECOND_WRONG_CHAR).toCharArray()[0];
	}
	
	
	/**
	 * Getter for position of second wrong char in ModifyDate of exif
	 * 
	 * @return
	 */
	public int getExifDateSecondWrongCharPos()
	{
		return Integer.parseInt(getElementFromProperty(EXIF_DATE_SECOND_WRONG_CHAR_POS));
	}
	
	
	/**
	 * Getter for fix of second wrong char in ModifyDate of exif
	 * 
	 * @return
	 */
	public char getExifDateSecondWrongCharFix()
	{
		return getElementFromProperty(EXIF_DATE_SECOND_WRONG_CHAR_FIX).toCharArray()[0];
	}
	
	
	/**
	 * Getter for X-Path to AlephID in export_mets.xml
	 * 
	 * @return
	 */
	public String getXpathAlephID()
	{
		return getElementFromProperty(XPATH_ALEPHID);
	}
	
	
	/**
	 * Getter for X-Path to DOI in export_mets.xml
	 * 
	 * @return
	 */
	public String getXpathDOI()
	{
		return getElementFromProperty(XPATH_DOI);
	}	
	
	
	/**
	 * Getter for X-Path to alternative Title in export_mets.xml
	 * 
	 * @return
	 */
	public String getXpathAlternativeTitle()
	{
		return getElementFromProperty(XPATH_ALT_TITLE);
	}	
	
	
	/**
	 * Getter for X-Path to Location in export_mets.xml
	 * 
	 * @return
	 */
	public String getXpathLocation()
	{
		return getElementFromProperty(XPATH_LOCATION);
	}	
	
	
	/**
	 * Getter for regex Aleph ID String
	 * 
	 * @return String
	 */
	public String getRegexAlephId()
	{
		return getElementFromProperty(REGEX_ALEPH_ID);
	}


	/**
	 * Getter for node that holds the file name
	 * 
	 * @return
	 */
	public String getFileNodeName()
	{
		return getElementFromProperty(FILE_NODE_NAME);
	}
	
	
	/**
	 * Getter for the attribute that holds the file name
	 * 
	 * @return
	 */
	public String getFileAttributeId()
	{
		return getElementFromProperty(FILe_ATTRIBUTE_ID);
	}
	
	
	/**
	 * Getter for the attribute that holds the file created date
	 * 
	 * @return
	 */
	public String getFileAttributeCreated()
	{
		return getElementFromProperty(FILE_ATTRIBUTE_CREATED);
	}
	
	
	/**
	 * Getter for the attribute that holds the mime type of the file
	 * 
	 * @return
	 */
	public String getFileAttributeMimetype()
	{
		return getElementFromProperty(FILE_ATTRIBUTE_MIMETYPE);
	}
	
	/**
	 * Getter for the attribute that holds the checksum for the file
	 * 
	 * @return
	 */
	public String getFileAttributeChecksum()
	{
		return getElementFromProperty(FILE_ATTRIBUTE_CHECKSUM);
	}
	
	
	/**
	 * Getter for the attribute that holds the checksum type for the file
	 * 
	 * @return
	 */
	public String getFileAttributeChecksumtype()
	{
		return getElementFromProperty(FILE_ATTRIBUTE_CHECKSUMTYPE);
	}
	

	/**
	 * Getter for the attribute that holds the file size
	 * 
	 * @return
	 */
	public String getFileAttributeSize()
	{
		return getElementFromProperty(FILE_ATTRIBUTE_SIZE);
	}
	
	
	/**
	 * Getter for the node that holds the file name
	 * 
	 * @return
	 */
	public String getFilenameNodeName()
	{
		return getElementFromProperty(FILENAME_NODE_NAME);
	}
	
	
	/**
	 * Getter for the attribute that hold the actual file name
	 * 
	 * @return
	 */
	public String getFilenameAttributeName()
	{
		return getElementFromProperty(FILENAME_ATTRIBUTE_NAME);
	}
	
	
	/**
	 * Getter for current system time formated by configured current time format
	 * 
	 * @return
	 */
	public String getCurrentTime()
	{
		DateFormat dateFormat = new SimpleDateFormat(getDbCurrentTimeJavaFormat());
		Date date = new Date();
		
		return dateFormat.format(date);
	}
	
	
	/**
	 * Getter for max-source-file-size that converts String in bytes
	 * according to supplied unit (M, G)
	 * 
	 * @return
	 */
	public long getMaxSourceFileSize()
	{
		long resultInBytes = 0;
		int maxFileSize = 0;
		String size = getElementFromProperty(MAX_SOURCE_FILE_SIZE);
		char unit = size.charAt(size.length()-1);
		
		if(unit == 'G')
		{
			maxFileSize = Integer.parseInt(size.substring(0, size.length()-1));
			resultInBytes = maxFileSize*ConfigProperties.FILESIZ_GB;			
		}
		else if(unit == 'M')
		{
			maxFileSize = Integer.parseInt(size.substring(0, size.length()-1));
			resultInBytes = maxFileSize*ConfigProperties.FILESIZ_MB;			
		}
		else
		{
			resultInBytes = Integer.parseInt(size);
		}

		
		return resultInBytes;
	}
	
}