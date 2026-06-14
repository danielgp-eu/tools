/*
 * Copyright 2026 Daniel-Gheorghe Popiniuc
 *
 * Licensed under the Mozilla Public License Version 2.0 (the "License");
 *
 * MPL 2.0 is a copy-left license that is easy to comply with.
 * You must make the source code for any of your changes available under MPL,
 *   but you can combine the MPL software with proprietary code,
 *   as long as you keep the MPL code in separate files.
 * Version 2.0 is, by default, compatible with LGPL and GPL version 2 or greater.
 * You can distribute binaries under a proprietary license,
 *   as long as you make the source available under MPL.
 */
package io.github.danielgp_eu.tools.incubator;

import java.util.Locale;
import java.util.Properties;

import io.github.danielgp_eu.tools.core.*;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
                AnalyzePomFiles.class,
                CalculateSunriseAndSunset.class,
                ExperimentalFeature.class
        }
)
public class Application 
{
    public static void main( String[] args ) {
        CommonInteractiveClass.setStartDateTime();
        ProjectClass.setPomFile("/tools-incubator-pom.xml");
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with provided arguments
        final int iExitCode = new CommandLine(new Application()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
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
            ProjectClass.setPomFile(strFileName);
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
