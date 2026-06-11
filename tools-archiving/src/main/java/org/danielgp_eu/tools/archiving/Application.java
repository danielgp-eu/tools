package org.danielgp_eu.tools.archiving;

import java.util.Properties;

import org.danielgp_eu.tools.core.CommonInteractiveClass;
import org.danielgp_eu.tools.core.FileOperationsClass;

import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            ArchiveFolders.class
    }
)
public final class Application {

    /**
     * Constructor empty
     */
    private Application() {
        super();
    }

    /**
     * default
     * @param args input arguments
     */
    public static void main( final String[] args ) {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with 
        final int iExitCode = new CommandLine(new Application()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

}


/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ArchiveFolders",
                     description = "Archive sub-folders from a given folder")
class ArchiveFolders implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-aExe", "--archivingExecutable"},
            description = "Archiving executable (including full path, optional)")
    private String strArchivingExec;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-pwd", "--archivePassword"},
            description = "Password for archive encryption")
    private String strArchivePwd;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-ap", "--archivePrefix"},
            description = "Prefix to apply to archive name")
    private String strArchivePrefix;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-as", "--archiveSuffix"},
            description = "Suffix to apply to archive name")
    private String strArchiveSuffix;

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass optFolderNames = new CommonInteractiveClass.FolderNameOptionMixinClass();

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderDestinationOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderDestinationOptionMixinClass optFolderDest = new CommonInteractiveClass.FolderDestinationOptionMixinClass();

    @Override
    public void run() {
        final Properties propFolder = new Properties();
        if (strArchivingExec != null) {
            ArchivingClass.setArchivingExecutable(strArchivingExec);
        }
        ArchivingClass.setArchivePrefix(strArchivePrefix);
        ArchivingClass.setArchiveSuffix(strArchiveSuffix);
        if (strArchivePwd != null) {
            ArchivingClass.setArchivePwd(strArchivePwd);
        }
        final String[] inFolders = optFolderNames.getFolderNames();
        for (final String strFolder : inFolders) {
            propFolder.clear();
            final Properties folderProps = FileOperationsClass.StatisticsSubClass.getFolderStatisticsRecursive(strFolder, propFolder);
            ArchivingClass.setArchivingDir(strFolder);
            ArchivingClass.setArchiveNameWithinDestinationFolder(optFolderDest.getFolderDestination());
            ArchivingClass.archiveFolderAs7z();
            ArchivingClass.exposeArchivedStatistics(folderProps);
        }
    }

    /**
     * Constructor
     */
    protected ArchiveFolders() {
        super();
    }
}
