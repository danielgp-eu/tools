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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Project related goodies
 */
public final class ProjectClass {
    /** External POM file to be considered (Optional) */
    private static String externalPomFile = "";
    /** special value */
    private static final String INTERNAL_POM = "/pom.xml";
    /** holder of Managed Versions */
    private static Map<String, Object> managedVersions;
    /** holder of Plugin Management Versions */
    private static Map<String, Object> pluginCentralVers;
    /** working POM file as String */
    private static String pomFile;
    /** current Project Model Interpolator */
    private static StringSearchInterpolator prjInterpolator;
    /** current Project Model */
    private static Model prjModel;

    /**
     * retrieving first Developer within Project Model
     * @param projectModel input Project Model
     * @return String with first developer value
     */
    public static String getFirstDeveloper(final Model projectModel) {
        final java.util.List<Developer> prjDevs = projectModel.getDevelopers();
        String prjFirstDeveloper = "Developer(s) not defined";
        if (prjDevs != null
            && !prjDevs.isEmpty()) {
                prjFirstDeveloper = projectModel.getDevelopers().getFirst().getName();
        }
        return prjFirstDeveloper;
    }

    /**
     * Getter for pomFile
     * @return String
     */
    public static String getPomFile() {
        return pomFile;
    }

    /**
     * Getter for projectModel
     * @return Model
     */
    public static Model getProjectModel() {
        if (prjModel == null) {
            loadProjectModel();
        }
        return prjModel;
    }

    /**
     * get Project Model
     * @param reader class for reading XML
     * @param pomReference string for Project Object Model location
     * @return Model object
     */
    private static Model getProjectModelFromSystem(final MavenXpp3Reader reader, final String pomReference) {
        Model model = null;
        try(BufferedReader bReader = Files.newBufferedReader(Path.of(pomReference), StandardCharsets.UTF_8)) {
            model = reader.read(bReader);
        } catch (IOException | XmlPullParserException ex) {
            LogExposureClass.exposeProjectModel(Arrays.toString(ex.getStackTrace()));
        }
        return model;
    }

    /**
     * get Project Model
     * @param reader class for reading XML files
     * @param pomReference string for Project Object Model location
     * @return Model object
     */
    private static Model getProjectModelFromInsideJar(final MavenXpp3Reader reader, final String pomReference) {
        Model model = null;
        try (InputStream inputStream = ProjectClass.class.getResourceAsStream(pomReference)) {
            model = reader.read(inputStream);
        } catch (IOException | XmlPullParserException ex) {
            LogExposureClass.exposeProjectModel(Arrays.toString(ex.getStackTrace()));
        }
        return model;
    }

    /**
     * get POM value through interpolation if needed
     * @param rawValue original value
     * @return String
     */
    private static String getProjectModelValueWithInterpolationIfNeeded(final String rawValue) {
        String finalValue = rawValue;
        if (rawValue == null || rawValue.isBlank()) {
            finalValue = "";
        } else if (rawValue.startsWith("${")
                && rawValue.endsWith("}")) {
            try {
                finalValue = prjInterpolator.interpolate(rawValue);
            } catch (InterpolationException e) {
                final String strFeedback = String.format("InterpolationException %s", Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }
        return finalValue;
    }

    /**
     * Map with current project module libraries
     * @return Map with modules name and its version
     */
    public static Map<String, Object> getProjectModuleLibraries() {
        // Initialize the concurrent map
        final Map<String, Object> moduleMap = new ConcurrentHashMap<>();
        // Get the boot layer (the primary module layer)
        ModuleLayer.boot().modules().forEach(module -> {
            final String strName = module.getName();
            final String strVersion = module.getDescriptor().toNameAndVersion().substring(strName.length() + 1);
            moduleMap.put(strName, strVersion);
        });
        return moduleMap;
    }

    /**
     * Exposes project version
     * @param prjModel input Project Model
     * @return String with version
     */
    public static String getProjectVersion(final Model prjModel) {
        String prjVersion = prjModel.getVersion();
        final String strFeedback = String.format("I picked initial version as %s", prjVersion);
        LogExposureClass.LOGGER.info(strFeedback);
        if (prjVersion == null) {
            prjVersion = prjModel.getParent().getVersion();
            final String strFeedback2 = String.format("As initial version was NULL I re-picked version from parent and is %s", prjVersion);
            LogExposureClass.LOGGER.info(strFeedback2);
        }
        if (prjVersion.startsWith("${")
                && prjVersion.endsWith("}")) {
            prjVersion = getProjectModelValueWithInterpolationIfNeeded(prjVersion);
            final String strFeedback3 = String.format("Final interpolated version is %s", prjVersion);
            LogExposureClass.LOGGER.info(strFeedback3);
        }
        return prjVersion;
    }

    /**
     * Load POM for current project
     */
    public static void loadProjectModel() {
        setPomFile();
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        if (Files.exists(Path.of(pomFile))) {
            prjModel = getProjectModelFromSystem(reader, pomFile);
        } else {
            prjModel = getProjectModelFromInsideJar(reader, pomFile);
        }
        LoaderSubClass.loadComponents();
    }

    /**
     * Setter for externalPomFile
     * @param inExtPomFile input external POM file
     */
    public static void setExternalPomFile(final String inExtPomFile) {
        externalPomFile = inExtPomFile;
    }

    /**
     * set the POM to work with
     */
    private static void setPomFile() {
        if (!externalPomFile.isBlank()) {
            final String strFeedback = String.format("External POM file %s is being considered!", externalPomFile);
            LogExposureClass.LOGGER.debug(strFeedback);
        }
        if (BasicStructuresClass.isRunningFromJar()) {
            if (externalPomFile.isBlank()) {
                pomFile = INTERNAL_POM;
            } else {
                pomFile = externalPomFile;
            }
        } else {
            if (externalPomFile.isBlank()) {
                final StringBuilder sbPom = new StringBuilder(100);
                final String strPrjFolder = BasicStructuresClass.getCurrentFolder();
                sbPom.append(strPrjFolder).append(File.separator).append("pom.xml");
                pomFile = sbPom.toString();
            } else {
                pomFile = externalPomFile;
            }
        }
    }

    /**
     * initiating Components class
     */
    public static final class ApplicationSubClass {
        /**
         * constant for Build Plugins
         */
        /* default */ public static final String STR_PLUGINS_BUILD = "BuildPlugins";
        /**
         * constant for Profile Plugins
         */
        /* default */ public static final String STR_PLUGINS_PRFLE = "ProfilePlugins";

        /**
         * Application details
         * @return String
         */
        public static String getApplicationDetails() {
            final Model prjModel = getProjectModel();
            final StringBuilder strJsonString = new StringBuilder(100);
            strJsonString.append("\"Application\":{\"")
                    .append(prjModel.getGroupId() == null ? prjModel.getParent().getGroupId() : prjModel.getGroupId())
                    .append(':')
                    .append(prjModel.getArtifactId())
                    .append("\":\"")
                    .append(prjModel.getVersion() == null ? prjModel.getParent().getVersion() : prjModel.getVersion())
                    .append('\"');
            final Map<String, Object> projDependencies = ComponentsSubClass.getProjectModelComponent(BasicStructuresClass.STR_DEPENDENCIES);
            if (!projDependencies.isEmpty()) {
                strJsonString.append(",\"Dependencies\":")
                        .append(JsonOperationsClass.getMapIntoJsonString(projDependencies));
            }
            final Map<String, Object> projBuildPlugins = ComponentsSubClass.getProjectModelComponent(STR_PLUGINS_BUILD);
            if (!projBuildPlugins.isEmpty()) {
                strJsonString.append(",\"Build Plugins\":")
                        .append(JsonOperationsClass.getMapIntoJsonString(projBuildPlugins));
            }
            final Map<String, Object> projPrflPlugins = ComponentsSubClass.getProjectModelComponent(STR_PLUGINS_PRFLE);
            if (!projPrflPlugins.isEmpty()) {
                strJsonString.append(",\"Profile Plugins\":")
                        .append(JsonOperationsClass.getMapIntoJsonString(projPrflPlugins));
            }
            if (!prjModel.getModules().isEmpty()) {
                strJsonString.append(getComponentModulesDetailsIfProjectModulesArePresent(prjModel));
            }
            final Map<String, Object> projLibModules = getProjectModuleLibraries();
            strJsonString.append(",\"Library Modules\":")
                    .append(JsonOperationsClass.getMapIntoJsonString(projLibModules));
            final String strFeedback = "I just captured Application information...";
            LogExposureClass.LOGGER.debug(strFeedback);
            return strJsonString.append('}').toString();
        }

        /**
         * Application Details into Map
         *
         * @return Map
         */
        public static Map<String, Object> getApplicationDetailsIntoMap() {
            final Map<String, Object> appDetails = new ConcurrentHashMap<>();
            final Model prjModel = getProjectModel();
            appDetails.put("Application - "
                    + (prjModel.getGroupId() == null ? prjModel.getParent().getGroupId() : prjModel.getGroupId())
                    + ":" + prjModel.getArtifactId(),
                    prjModel.getVersion() == null ? prjModel.getParent().getVersion() : prjModel.getVersion());
            final Map<String, Object> projDependencies = ComponentsSubClass.getProjectModelComponent(BasicStructuresClass.STR_DEPENDENCIES);
            if (!projDependencies.isEmpty()) {
                projDependencies.forEach((strKey, objValue) -> appDetails.put("Direct Dependency - " + strKey, objValue));
            }
            final Map<String, Object> projBuildPlugins = ComponentsSubClass.getProjectModelComponent(STR_PLUGINS_BUILD);
            if (!projBuildPlugins.isEmpty()) {
                projBuildPlugins.forEach((strKey, objValue) -> appDetails.put("Build Plugins - " + strKey, objValue));
            }
            final Map<String, Object> projPrflPlugins = ComponentsSubClass.getProjectModelComponent(STR_PLUGINS_PRFLE);
            if (!projPrflPlugins.isEmpty()) {
                projPrflPlugins.forEach((strKey, objValue) -> appDetails.put("Profile Plugins - " + strKey, objValue));
            }
            final Map<String, Object> projLibModules = getProjectModuleLibraries();
            if (!projLibModules.isEmpty()) {
                projLibModules.forEach((strKey, objValue) -> appDetails.put("Library Module - " + strKey, objValue));
            }
            return appDetails;
        }

        /**
         * expose Project Modules (if defined)
         * @param prjModel current project model
         * @return JSON String with module details
         */
        private static String getComponentModulesDetailsIfProjectModulesArePresent(final Model prjModel) {
            final StringBuilder strJsonString = new StringBuilder(100);
            strJsonString.append(",\"Component Modules\":[");
            final Path pathPomFile = Path.of(getPomFile());
            final StringBuilder strJsonModule = new StringBuilder(100);
            prjModel.getModules().forEach(module -> {
                final String crtModulePom = pathPomFile.getParent()
                        + File.separator + module + File.separator + "pom.xml";
                if (!strJsonModule.isEmpty()) {
                    strJsonModule.append(',');
                }
                setExternalPomFile(crtModulePom);
                loadProjectModel();
                final Model prjModuleModel = getProjectModel();
                String mdlVersion = prjModuleModel.getVersion();
                if (mdlVersion == null) {
                    mdlVersion = prjModel.getVersion();
                }
                strJsonModule.append("{\"POM\":\"")
                        .append(crtModulePom.replace("\\", "\\\\"))
                        .append("\",\"")
                        .append(prjModel.getGroupId() == null ? prjModel.getParent().getGroupId() : prjModel.getGroupId())
                        .append(':')
                        .append(prjModuleModel.getArtifactId())
                        .append("\":\"")
                        .append(mdlVersion)
                        .append('\"')
                        ;
                final Map<String, Object> mdlDependencies = ComponentsSubClass.getProjectModelComponent(BasicStructuresClass.STR_DEPENDENCIES);
                if (!mdlDependencies.isEmpty()) {
                    strJsonModule.append(",\"Dependencies\":")
                            .append(JsonOperationsClass.getMapIntoJsonString(mdlDependencies));
                }
                strJsonModule.append('}');
            });
            strJsonString.append(strJsonModule).append(']');
            return strJsonString.toString();
        }

        /**
         * Constructor
         */
        private ApplicationSubClass() {
            // intentionally left blank
        }

    }

    /**
     * initiating Components class
     */
    public static final class LoaderSubClass {

        /**
         * Load all components: Dependencies and Plug-ins
         */
        public static void loadComponents() {
            if (prjModel.getProperties() != null) {
                loadProjectModelInterpolator();
            }
            if (prjModel.getDependencyManagement() != null) {
                loadProjectModelCentralDependencies();
            }
            if (prjModel.getBuild() != null
                    && prjModel.getBuild().getPluginManagement() != null) {
                loadProjectModelPluginManagement();
            }
        }

        /**
         * Loading central dependency management if set
         */
        private static void loadProjectModelCentralDependencies() {
            final Map<String, Object> centralDeps = new ConcurrentHashMap<>();
            for (final Dependency dependency : prjModel.getDependencyManagement().getDependencies()) {
                final String strKey = dependency.getGroupId() + ":" + dependency.getArtifactId();
                final String strVersion = getProjectModelValueWithInterpolationIfNeeded(dependency.getVersion());
                centralDeps.put(strKey, strVersion);
            }
            managedVersions = centralDeps;
        }

        /**
         * Loading central plugin management if set
         */
        private static void loadProjectModelPluginManagement() {
            final Map<String, Object> centralPlugM = new ConcurrentHashMap<>();
            for (final Plugin plugin : prjModel.getBuild().getPluginManagement().getPlugins()) {
                final String strKey = plugin.getGroupId() + ":" + plugin.getArtifactId();
                final String strVersion = getProjectModelValueWithInterpolationIfNeeded(plugin.getVersion());
                centralPlugM.put(strKey, strVersion);
            }
            pluginCentralVers = centralPlugM;
        }

        /**
         * Loading Properties for current Project Model if set
         */
        private static void loadProjectModelInterpolator() {
            final StringSearchInterpolator interpolator = new StringSearchInterpolator();
            final Properties props = prjModel.getProperties();
            interpolator.addValueSource(new MapBasedValueSource(props));
            prjInterpolator = interpolator;
        }

        /**
         * Constructor
         */
        private LoaderSubClass() {
            // intentionally left blank
        }
    }

    /**
     * initiating Components class
     */
    public static final class ComponentsSubClass {
        /**
         * special value
         */
        private static final String UNKNOWN = "UNKNOWN";

        /**
         * Project Build Plugins exposed
         * @return Map
         */
        public static Map<String, Object> getProjectModelComponent(final String strComponentName) {
            Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            switch(strComponentName) {
                case ApplicationSubClass.STR_PLUGINS_BUILD:
                    if (prjModel.getBuild() != null) {
                        mapToReturn = getBuildPlugins();
                    }
                    break;
                case BasicStructuresClass.STR_DEPENDENCIES:
                    if (prjModel.getDependencies() != null) {
                        mapToReturn = getDependencies();
                    }
                    break;
                case ApplicationSubClass.STR_PLUGINS_PRFLE:
                    if (prjModel.getProfiles() != null) {
                        mapToReturn = getProfilePlugins();
                    }
                    break;
                default:
                    break;
            }
            return mapToReturn;
        }

        /**
         * Build Plugins gathering
         * @return Map
         */
        private static Map<String, Object> getBuildPlugins() {
            final Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            prjModel.getBuild().getPlugins().forEach(plugin -> {
                final String strKey = plugin.getGroupId() + ":" + plugin.getArtifactId();
                String strVersion = getProjectModelValueWithInterpolationIfNeeded(plugin.getVersion());
                if (strVersion.isEmpty()
                        && managedVersions != null
                        && !managedVersions.isEmpty()
                        && pluginCentralVers != null
                        && !pluginCentralVers.isEmpty()) {
                    strVersion = pluginCentralVers.getOrDefault(strKey, UNKNOWN).toString();
                }
                mapToReturn.put(strKey, strVersion);
            });
            return mapToReturn;
        }

        /**
         * Dependencies gathering
         * @return Map
         */
        private static Map<String, Object> getDependencies() {
            final Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            prjModel.getDependencies().forEach(dependency -> {
                final String strKey = dependency.getGroupId() + ":" + dependency.getArtifactId();
                String strVersion = getProjectModelValueWithInterpolationIfNeeded(dependency.getVersion());
                if (strVersion.isEmpty()
                        && managedVersions != null
                        && !managedVersions.isEmpty()) {
                    strVersion = managedVersions.getOrDefault(strKey, UNKNOWN).toString();
                }
                mapToReturn.put(strKey, strVersion);
            });
            return mapToReturn;
        }

        /**
         * Profile Plugins gathering
         * @return Map
         */
        private static Map<String, Object> getProfilePlugins() {
            final Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            prjModel.getProfiles().forEach(profile -> {
                if (profile.getBuild() != null) {
                    profile.getBuild().getPlugins().forEach(plugin -> {
                        final String strKey = plugin.getGroupId() + ":" + plugin.getArtifactId();
                        String strVersion = getProjectModelValueWithInterpolationIfNeeded(plugin.getVersion());
                        if (strVersion.isEmpty()
                                && managedVersions != null
                                && !managedVersions.isEmpty()
                                && pluginCentralVers != null
                                && !pluginCentralVers.isEmpty()) {
                            strVersion = pluginCentralVers.getOrDefault(strKey, UNKNOWN).toString();
                        }
                        mapToReturn.put(strKey, strVersion);
                    });
                }
            });
            return mapToReturn;
        }

        /**
         * Constructor
         */
        private ComponentsSubClass() {
            // intentionally left blank
        }

    }

    /**
     * Constructor
     */
    private ProjectClass() {
        // intentionally left blank
    }

}
