/**
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 */
package io.github.danielgp_eu.tools.web;

import org.apache.logging.log4j.Level;

import io.github.danielgp_eu.tools.core.*;
import io.github.danielgp_eu.tools.databases.DatabaseOperationsClass;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
                JavaJavaWebUserInterface.class
        }
)
public class Application {

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.setStartDateTime();
        LogExposureClass.ConfigurationSubClass.setLogLevel(Level.INFO);
        LogExposureClass.ConfigurationSubClass.setLogFile("logs/DanielGP_Tools-Web-");
        LogExposureClass.ConfigurationSubClass.initiate();
        ProjectClass.setPomFile("/tools-web-pom.xml");
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with provided arguments
        final int iExitCode = new CommandLine(new Application()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
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
