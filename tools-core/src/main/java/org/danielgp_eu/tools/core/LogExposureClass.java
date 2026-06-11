package org.danielgp_eu.tools.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * exposing things to Log
 */
public final class LogExposureClass {
    /**
     * Process Capture Need
     */
    /* default */ private static boolean needProcExposure = true;
    /**
     * Logger
     */
    public static final Logger LOGGER = LogManager.getLogger("org.danielgp-eu.tools");
    /**
     * standard Unknown feature
     */
    public static final String STR_I18N_UNKN_FTS = "Feature %s is NOT known in %s...";
    /**
     * standard Unknown
     */
    public static final String STR_I18N_UNKN = "Unknown";

    /**
     * Build message for I/O exception
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeInputOutputException(final String inStackTrace) {
        final String strFeedbackErr = String.format("Input/Output exception on... %s", inStackTrace);
        LOGGER.error(strFeedbackErr);
    }

    /**
     * Build message for I/O exception
     * @param customMsg custom message
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeInputOutputException(final String customMsg, final String inStackTrace) {
        final String strFeedbackErr = customMsg + String.format("... %s", inStackTrace);
        LOGGER.error(strFeedbackErr);
    }

    /**
     * Log Process Builder command conditionally
     * @param strCommand command to execute
     */
    public static void exposeProcessBuilder(final String strCommand) {
        if (getLogLevel().isLessSpecificThan(Level.INFO) && needProcExposure) {
            final String strFeedback = String.format("I intend to execute following shell command %s", strCommand);
            LOGGER.debug(strFeedback);
        } 
    }

    /**
     * Log Process Builder command conditionally
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeProjectModel(final String inStackTrace) {
        final String strFeedback = String.format("Error on getting project model... \"%s\"", inStackTrace);
        LOGGER.error(strFeedback);
    }

    /**
     * Build message for file operation error
     * @param strFileName file name
     * @param strStagTrace stag trace
     * @return message for file operation error
     */
    public static String getFileErrorMessage(final String strFileName, final String strStagTrace) {
        return String.format("Error encountered when attempting to write to %s file... %s", strFileName, strStagTrace);
    }

    /**
     * Get current Log Level
     * @return current Log Level
     */
    public static Level getLogLevel() {
        return LOGGER.getLevel();
    }

    /**
     * get Unsupported Feature
     * @param strDecision decision evaluated
     * @param strWhere which function this is called from
     * @return String with localized feedback
     */
    public static String getUnsupportedFeatures(final String strDecision, final String strWhere) {
        final String strFeedbackErr = String.format(STR_I18N_UNKN_FTS, strDecision, strWhere);
        LOGGER.error(strFeedbackErr);
        return strFeedbackErr;
    }

    /**
     * handle NameUnformatted
     * @param intRsParams number for parameters
     * @param strUnformatted original string
     * @param strReplacement replacements (1 to multiple)
     * @return String
     */
    public static String handleNameUnformattedMessage(final int intRsParams, final String strUnformatted, final Object... strReplacement) {
        return switch (intRsParams) {
            case 1 -> String.format(strUnformatted, strReplacement[0]);
            case 2 -> String.format(strUnformatted, strReplacement[0], strReplacement[1]);
            case 3 -> String.format(strUnformatted, strReplacement[0], strReplacement[1], strReplacement[2]);
            default -> getUnsupportedFeatures(String.valueOf(intRsParams), StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(STR_I18N_UNKN)));
        };
    }

    /**
     * Setter for Process Exposure
     * @param inProcExposure true or false for exposing process parameters to Log
     */
    public static void setProcessExposureNeed(final boolean inProcExposure) {
        needProcExposure = inProcExposure;
    }

    /**
     * Constructor
     */
    private LogExposureClass () {
        super();
    }

}
