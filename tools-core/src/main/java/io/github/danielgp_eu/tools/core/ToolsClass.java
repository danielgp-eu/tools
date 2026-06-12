package io.github.danielgp_eu.tools.core;

import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            AnalyzeColumnsFromCsvFiles.class,
            AnalyzePomFiles.class,
            CalculateSunriseAndSunset.class,
            CaptureChecksumsOfFilesFromFoldersIntoCsvFile.class,
            CaptureEnvironmentDetailsIntoJsonFile.class,
            CaptureImportsFromJavaSourceFilesIntoCsvFile.class,
            CaptureWindowsApplicationsInstalledIntoCsvFile.class,
            CleanOlderFilesFromFolder.class,
            ExperimentalFeature.class,
            GetInformationFromDatabase.class,
            GetSubFoldersFromFolders.class,
            JavaJavaWebUserInterface.class
    }
)
public final class ToolsClass {

    /**
     * Constructor empty
     */
    private ToolsClass() {
        super();
    }

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with 
        final int iExitCode = new CommandLine(new ToolsClass()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "AnalyzeColumnsFromCsvFiles",
                     description = "Analyze columns from CSV file")
class AnalyzeColumnsFromCsvFiles implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass optFileNames = new CommonInteractiveClass.InFileNameOptionMixinClass();

    /**
     *
     * @param strFileName input File
     * @param intColToEval number of column to evaluate (starting from 0)
     * @param intColToGrpBy number of column to group by (starting from 0)
     */
    private static void storeWordFrequencyIntoCsvFile(final String strFileName,
                                                      final Integer intColToEval,
                                                      final Integer intColToGrpBy) {
        // Group values by category
        final Map<String, List<String>> groupedColumns = FileOperationsClass.ContentReadingSubClass.getListOfValuesFromColumnGroupedByAnotherColumnValuesFromCsvFile(strFileName, intColToEval, intColToGrpBy);
        // Define merge rules
        final Map<List<String>, String> mergeRules = Map.of(
                List.of("ARRAY", "OBJECT", "VARIANT"), "COMPOSITE__STRUCTURED",
                List.of("FLOAT", "NUMBER"), "COMPOSITE__NUMERIC",
                List.of("DATETIME", "TIMESTAMP", "TIMESTAMP_LTZ", "TIMESTAMP_NTZ", "TIMESTAMP_TZ"), "COMPOSITE__TIMESTAMP",
                List.of("BINARY", "TEXT", "VARCHAR"), "COMPOSITE__TEXT"
        );
        final Map<String, List<String>> grpCols = BasicStructuresClass.ListAndMapSubClass.mergeKeys(groupedColumns, mergeRules);
        final String strFeedback = "=".repeat(20) + strFileName + "=".repeat(20);
        LogExposureClass.LOGGER.info(strFeedback);
        FileOperationsClass.ContentWritingSubClass.setCsvColumnSeparator(',');
        grpCols.forEach((keyDataType, valList) -> {
            final String strColFileName = strFileName.replace(".csv", "__columns.csv");
            final String strFeedbackFile = "Writing file " + strColFileName;
            LogExposureClass.LOGGER.info(strFeedbackFile);
            FileOperationsClass.ContentWritingSubClass.setCsvLinePrefix(keyDataType);
            FileOperationsClass.ContentWritingSubClass.writeStringListToCsvFile(strColFileName, "DataType,Column", valList);
            final String strFeedbackWrt = String.format("Writing file for %s which has %s values", keyDataType, valList.size());
            LogExposureClass.LOGGER.info(strFeedbackWrt);
            final Map<String, Long> sorted = BasicStructuresClass.ListAndMapSubClass.getWordCounts(valList, "(_| )");
            FileOperationsClass.ContentWritingSubClass.writeLinkedHashMapToCsvFile(strFileName.replace(".csv", "__words.csv"), "DataType,Word,Occurrences", sorted);
        });
    }

    @Override
    public void run() {
        final String[] inFiles = optFileNames.getInFileNames();
        for (final String strFileName : inFiles) {
            storeWordFrequencyIntoCsvFile(strFileName, 3, 4);
        }
    }

    /**
     * Constructor
     */
    protected AnalyzeColumnsFromCsvFiles() {
        // intentionally blank
    }

}

/**
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "AnalyzePomFiles",
                     description = "Exposes information from one or multiple Project Object Model (Apache Maven configuration file)")
class AnalyzePomFiles implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass optFileNames = new CommonInteractiveClass.InFileNameOptionMixinClass();

    @Override
    public void run() {
        final String strFeedbackThis = String.format("For this project relevant POM information is: {%s}", ProjectClass.ApplicationSubClass.getApplicationDetails());
        LogExposureClass.LOGGER.info(strFeedbackThis);
        final String[] inFiles = optFileNames.getInFileNames();
        for (final String strFileName : inFiles) {
            ProjectClass.setExternalPomFile(strFileName);
            ProjectClass.loadProjectModel();
            final String strFeedback = String.format("For given POM file %s relevant information is: {%s}", strFileName, ProjectClass.ApplicationSubClass.getApplicationDetails());
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected AnalyzePomFiles() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CalculateSunriseAndSunset",
                     description = "Calculates Sunrise and Sunset for one or more location")
class CalculateSunriseAndSunset implements Runnable {

    /**
     * option for Longitude
     */
    @CommandLine.Option(
            names = {"-lon", "--longitude"},
            description = "Longitude",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private double[] dblLongitude;

    /**
     * option for Latitude
     */
    @CommandLine.Option(
            names = {"-lat", "--latitude"},
            description = "Latitude",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private double[] dblLatitude;

    /**
     * option for Zone Name
     */
    @CommandLine.Option(
            names = {"-zn", "--zoneName"},
            description = "Zone Name",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private String[] strZoneName;

    /**
     * option for Zone Name
     */
    @CommandLine.Option(
            names = {"-ld", "--locationDetail"},
            description = "Location details: name,country,division,town",
            arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
            required = true)
    private String[] strLocationDetail;

    @Override
    public void run() {
        int intCounter = 0;
        for (final String crtLocationDetail : strLocationDetail) {
            SunClass.setZoneId(strZoneName[intCounter]);
            SunClass.setLatitude(dblLatitude[intCounter]);
            SunClass.setLongitude(dblLongitude[intCounter]);
            final Properties crtProperties = SunClass.getSunRiseAndSet(crtLocationDetail);
            final String strFeedback = String.format("Details are: %s", crtProperties);
            LogExposureClass.LOGGER.debug(strFeedback);
            intCounter++;
        }
    }

    /**
     * Constructor
     */
    protected CalculateSunriseAndSunset() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureChecksumsOfFilesFromFolderIntoCsvFile",
                     description = "Get statistics for all files within a given folder")
class CaptureChecksumsOfFilesFromFoldersIntoCsvFile implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optOutFileName = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inAlgorithms = {"SHA-256", "SHA3-256"};
        FileOperationsClass.StatisticsSubClass.setChecksumAlgorithms(inAlgorithms);
        final String[] inFolders = optFolderNames.getFolderNames();
        final String outCsvFile = optOutFileName.getOutFileName();
        for (final String strFolder : inFolders) {
            final ZonedDateTime startComputeTime = ZonedDateTime.now(ZoneId.systemDefault());
            FileOperationsClass.StatisticsSubClass.captureFileStatisticsFromFolder(strFolder, outCsvFile);
            final Duration objDuration = Duration.between(startComputeTime, ZonedDateTime.now(ZoneId.systemDefault()));
            final String strFeedback = String.format("For the folder %s calculated checksums are stored in the file %s operation completed in %s (which means %s | %s)", strFolder, outCsvFile, objDuration.toString(), TimingClass.ConversionSubClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime"), TimingClass.ConversionSubClass.convertNanosecondsIntoSomething(objDuration, "TimeClock"));
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected CaptureChecksumsOfFilesFromFoldersIntoCsvFile() {
        super();
    }
}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "CaptureEnvironmentDetailsIntoJsonFile",
                     description = "Captures execution environment details into Log file")
class CaptureEnvironmentDetailsIntoJsonFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String strEnvDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        final String strOutFileName = optionOut.getOutFileName();
        final String strFeedback = String.format("Environment details are %s and will intend to write it to %s file", strEnvDetails, strOutFileName);
        LogExposureClass.LOGGER.info(strFeedback);
        FileOperationsClass.ContentWritingSubClass.writeRawTextToFile(strOutFileName, strEnvDetails);
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected CaptureEnvironmentDetailsIntoJsonFile() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureImportsFromJavaSourceFilesIntoCsvFile",
                     description = "Get import inventory from all Java source files within a given folder")
class CaptureImportsFromJavaSourceFilesIntoCsvFile implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inFolders = optFolderNames.getFolderNames();
        final String outCsvFile = optionOut.getOutFileName();
        for (final String strFolder : inFolders) {
            FileOperationsClass.ContentReadingSubClass.extractImportStatementsFromJavaSourceFilesIntoCsvFile(Path.of(strFolder), Path.of(outCsvFile));
        }
    }

    /**
     * Constructor
     */
    protected CaptureImportsFromJavaSourceFilesIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureWindowsApplicationsInstalledIntoCsvFile",
                     description = "Run the experimental new feature")
class CaptureWindowsApplicationsInstalledIntoCsvFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String outCsvFile = optionOut.getOutFileName();
        ShellingClass.PowerShellExecutionSubClass.captureWindowsApplicationsIntoCsvFile(outCsvFile);
    }

    /**
     * Constructor
     */
    protected CaptureWindowsApplicationsInstalledIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CleanOlderFilesFromFolder",
                     description = "Clean files older than a given number of days")
class CleanOlderFilesFromFolder implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * String for FileName
     */
    @CommandLine.Option(
            names = {"-dLmt", "--daysOlderLimit"},
            description = "Limit number of days to remove files from",
            arity = "1",
            required = true)
    private int intDaysOlderLimit;

    @Override
    public void run() {
        FileOperationsClass.DeletingSubClass.OlderClass.setCleanedFolderStatistics(true);
        final String[] inFolders = optFolderNames.getFolderNames();
        for (final String strFolder : inFolders) {
            FileOperationsClass.DeletingSubClass.OlderClass.setOrResetCleanedFolderStatistics();
            FileOperationsClass.DeletingSubClass.OlderClass.deleteFilesOlderThanGivenDays(strFolder, intDaysOlderLimit);
            final Map<String, Long> statsClndFldr = FileOperationsClass.DeletingSubClass.OlderClass.getCleanedFolderStatistics();
            final String strFeedback = String.format("Folder %s has been cleaned eliminating %s files and freeing %s bytes in terms of disk space...", strFolder, statsClndFldr.get("Files"), statsClndFldr.get("Size"));
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected CleanOlderFilesFromFolder() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ExperimentalFeature",
                     description = "Run the experimental new feature")
class ExperimentalFeature implements Runnable {

    @Override
    public void run() {
        // no-op
        final String strPackage = "com.github.oshi:oshi-core-ffm";
        final String strVersion = RemoteInformationRetrievalClass.getLatestVersionFromMavenCentralRepository(strPackage);
        final String strFeedback = String.format("For package %s latest version is: %s", strPackage, strVersion);
        LogExposureClass.LOGGER.info(strFeedback);
        final String strWebSite = RegularExpressionsClass.buildCentralMavenRepositoryUniformResourceLocator(strPackage);
        final String[] packageParts = strPackage.split(":");
        final String strRemoteFileUrl = String.format("%s%s/%s-%s.jar", strWebSite, strVersion, packageParts[1], strVersion);
        final String strFeedback2 = String.format("Remote file is: %s", strRemoteFileUrl);
        LogExposureClass.LOGGER.info(strFeedback2);
        final Properties urlAttributes = RemoteInformationRetrievalClass.requestHttp(strRemoteFileUrl, "AttributesFromHeader");
        final String strFeedback3 = String.format("Retrieved attributes from header are: %s", urlAttributes);
        LogExposureClass.LOGGER.info(strFeedback3);
        final String strChecksumUrl = strRemoteFileUrl + ".sha256";
        final String checksumValue = RemoteInformationRetrievalClass.requestHttp(strChecksumUrl, BasicStructuresClass.STR_CONTENT).getOrDefault(BasicStructuresClass.STR_CONTENT, "MISSING").toString().trim().toLowerCase(Locale.ENGLISH);
        final String strFeedback4 = String.format("SHA-256 from %s has content: %s", strChecksumUrl, checksumValue);
        LogExposureClass.LOGGER.info(strFeedback4);
    }

    /**
     * Constructor
     */
    protected ExperimentalFeature() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "GetInformationFromDatabase",
                     description = "Gets information from Database into Log file")
class GetInformationFromDatabase implements Runnable {

    /**
     * Known Database Types
     */
	/* default */ static final List<String> LST_DB_TYPES = Arrays.asList(
        "MySQL",
        "Snowflake"
    );

    /**
     * Known Information Types
     */
	/* default */ static final List<String> LST_INFO_TYPES = Arrays.asList(
        "Columns",
        "Databases",
        "Schemas",
        "TablesAndViews",
        "Views",
        "ViewsLight"
    );

    /**
     * String for Database Type
     */
    @CommandLine.Option(
        names = { "-dbTp", "--databaseType" },
        description = "Type of Database",
        arity = "1",
        required = true,
        completionCandidates = DatabaseTypes.class)
    private String strDbType;

    /**
     * String for Information Type
     */
    @CommandLine.Option(
        names = { "-infTp", "--informationType" },
        description = "Type of Information",
        arity = BasicStructuresClass.ARITY_ONE_OR_MORE,
        required = true,
        completionCandidates = InfoTypes.class)
    private String strInfoType;

    /**
     * Listing available options
     */
    /* default */ static class DatabaseTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_DB_TYPES.iterator();
        }
    }

    /**
     * Listing available options
     */
    /* default */ static class InfoTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_INFO_TYPES.iterator();
        }
    }

    /**
     * Action logic
     *
     * @param strDatabaseType type of Database (predefined values)
     */
    private static void performAction(final String strDatabaseType, final String strLclInfoType) {
        Properties properties = new Properties();
        switch (strDatabaseType) {
            case "MySQL":
                properties = DatabaseOperationsClass.SpecificMySqlSubClass.getConnectionPropertiesForMySQL();
                DatabaseOperationsClass.SpecificMySqlSubClass.performMySqlPreDefinedAction(strLclInfoType, properties);
                break;
            case "Snowflake":
                DatabaseOperationsClass.SpecificSnowflakeSubClass.performSnowflakePreDefinedAction(strLclInfoType, properties);
                break;
            default:
                final String strFeedback = String.format("Unknown %s argument received in %s, do not know what to do with it, therefore will quit, bye!", strDatabaseType, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                break;
        }
    }

    @Override
    public void run() {
        if (!LST_DB_TYPES.contains(strDbType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --databaseType: " + strDbType + ". Valid values are: " + LST_DB_TYPES
            );
        }
        if (!LST_INFO_TYPES.contains(strInfoType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --informationType: " + strInfoType + ". Valid values are: " + LST_INFO_TYPES
            );
        }
        performAction(strDbType, strInfoType);
    }

    /**
     * Constructor
     */
    protected GetInformationFromDatabase() {
        super();
    }
}

/**
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "GetSubFoldersFromFolders",
                     description = "Captures sub-folders from a Given Folder into Log file")
class GetSubFoldersFromFolders implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inFolders = optFolderNames.getFolderNames();
        for (final String strFolder : inFolders) {
            final List<String> arraySubFolders = FileOperationsClass.RetrievingSubClass.getSubFoldersFromFolder(strFolder);
            final String strFeedback = String.format("Considering folder %s following sub-folders were found: %s", strFolder, arraySubFolders);
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    protected GetSubFoldersFromFolders() {
        super();
    }

}

/**
 * Supports web interface
 */
@CommandLine.Command(
    name = "JavaJavaWebUserInterface", 
    description = "Initiate JavaJava web user interface")
class JavaJavaWebUserInterface implements Runnable {

    /**
     * String for Database
     */
    @CommandLine.Option(
        names = {"-dbr", "--databaseReleases"},
        description = "Database Name with Releases",
        arity = BasicStructuresClass.ARITY_ONLY_ONE,
        required = true
    )
    private static String strDbReleases;

    /**
     * String for Database
     */
    @CommandLine.Option(
        names = {"-p", "--port"},
        description = "Port Number for web user interface",
        arity = BasicStructuresClass.ARITY_ONLY_ONE,
        required = true
    )
    private static long portNumber;

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    @Override
    public void run() {
        UndertowClass.setWebPort(String.valueOf(portNumber));
        DatabaseOperationsClass.SpecificSqLiteSubClass.setInternalDatabase(strDbReleases);
        WebClass.SoftwareReleasesSubClass.setReleasesDatabase(strDbReleases);
        WebClass.setFolderNamesForChecksumExposure(optFolderNames.getFolderNames());
        UndertowClass.setRootHandler(WebClass.handleWebContent());
        UndertowClass.runWebServer();
    }

    /**
     * Constructor
     */
    protected JavaJavaWebUserInterface() {
        // intentionally blank
    }
}
