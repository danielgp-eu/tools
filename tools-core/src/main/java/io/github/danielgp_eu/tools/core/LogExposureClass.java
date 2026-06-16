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
package io.github.danielgp_eu.tools.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;

/**
 * exposing things to Log
 */
public final class LogExposureClass {
    /** Process Capture Need variable  */
    /* default */ private static boolean needProcExposure = true;
    /** Logger variable */
    public static final Logger LOGGER = LogManager.getLogger(LogExposureClass.class);
    /** standard Unknown feature constant */
    public static final String STR_I18N_UNKN_FTS = "Feature %s is NOT known in %s...";
    /** standard Unknown constant */
    public static final String STR_I18N_UNKN = "Unknown";

    /**
     * Constructor
     */
    private LogExposureClass () {
        super();
    }

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
     * Getter current Log Level
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
     * Setter for Process Exposure
     * @param inProcExposure true or false for exposing process parameters to Log
     */
    public static void setProcessExposureNeed(final boolean inProcExposure) {
        needProcExposure = inProcExposure;
    }

    /**
     * Configuration management
     * see https://www.baeldung.com/log4j2-programmatic-config
     * and https://github.com/apache/logging-log4j2/blob/2.x/log4j-core/src/main/resources/Log4j-levels.xsd
     */
    public static final class ConfigurationSubClass {
        /** Log builder */
        private static final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        /** PatternLayout variable */
        private static LayoutComponentBuilder patternLayout;

        // Private constructor to prevent instantiation
        private ConfigurationSubClass() {
            // intentionally blank
        }

        public static void setLogLevelToParentAndChild(final Level inputLevel, final String fileLogName) {
            patternLayout = buildPatternLayout(builder);
            final AppenderComponentBuilder console = builder
                    .newAppender("stdout", "Console")
                    .add(patternLayout);
            builder.add(console);
            buildRollingFile(fileLogName, "rest");
            buildRollingFile(fileLogName, "error");
            final RootLoggerComponentBuilder rootLogger = builder
                    .newRootLogger(inputLevel)
                    .add(builder.newAppenderRef("rollingRest"))
                    .add(builder.newAppenderRef("rollingError"))
                    .addAttribute("additivity", false);
            builder.add(rootLogger);
        }

        private static void buildRollingFile(final String pathFileBaseName, final String strType) {
            final String logFileName = pathFileBaseName + strType + ".log";
            final String filePattern = logFileName.replace(".log", "%d{yyyy-MM-dd-HH}-%i.log");
            final ComponentBuilder<?> policy = buildPolicies(builder);
            final FilterComponentBuilder levelRangeFilter = switch ( strType ) {
                case "error" -> buildLevelRangeFilter(builder, "FATAL", "ERROR");
                case "rest"  -> buildLevelRangeFilter(builder, "WARN", "ALL");
                default      -> buildLevelRangeFilter(builder, "FATAL", "ALL"); // includes all levels
            };
            final AppenderComponentBuilder rollingFile  = builder
                    .newAppender("rollingRest", "RollingFile")
                    .addAttribute("fileName", logFileName)
                    .addAttribute("filePattern", filePattern)
                    .add(patternLayout)
                    .addComponent(policy)
                    .add(levelRangeFilter);
            builder.add(rollingFile);
        }

        /**
         * 
         * @param builder input logger
         * @param minLevel starting Level
         * @param maxLevel ending Level
         * @return LevelRangeFilter
         */
        private static FilterComponentBuilder buildLevelRangeFilter(final ConfigurationBuilder<BuiltConfiguration> builder, final String minLevel, final String maxLevel) {
            final FilterComponentBuilder flow = builder.newFilter("LevelRangeFilter", Filter.Result.ACCEPT, Filter.Result.DENY);
            flow.addAttribute("minLevel", minLevel);
            flow.addAttribute("maxLevel", maxLevel);
            return flow;
        }

        /**
         * Building Pattern Layout
         * @param builder input logger
         * @return standard
         */
        private static LayoutComponentBuilder buildPatternLayout(final ConfigurationBuilder<BuiltConfiguration> builder) {
            final String logPattern = "%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [%-6p] %C{3}.%M(%F:%L) - %m%n";
            final LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
            standard.addAttribute("PatternLayout", logPattern);
            return standard;
        }

        /**
         * Building Policies
         * @param builder input logger
         * @return ComponentBuilder
         */
        private static ComponentBuilder<?> buildPolicies(final ConfigurationBuilder<BuiltConfiguration> builder) {
            final ComponentBuilder<?> triggeringPolicy = builder.newComponent("Policies");
            final ComponentBuilder<?> timeBasedPolicy = builder
                    .newComponent("TimeBasedTriggeringPolicy")
                    .addAttribute("interval", 1)
                    .addAttribute("modulate", true);
            triggeringPolicy.addComponent(timeBasedPolicy);
            final ComponentBuilder<?> sizeBasedPolicy = builder
                    .newComponent("SizeBasedTriggeringPolicy")
                    .addAttribute("size", "20M");
            triggeringPolicy.addComponent(sizeBasedPolicy);
            return triggeringPolicy;
        }

    }

}
