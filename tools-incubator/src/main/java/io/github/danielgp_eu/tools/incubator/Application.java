package io.github.danielgp_eu.tools.incubator;

import java.util.Locale;
import java.util.Properties;

import io.github.danielgp_eu.tools.core.*;
import picocli.CommandLine;

/**
 * Main Command Line
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
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
