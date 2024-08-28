package com.ericsson.cifwk.taf.maven.plugin;

import com.ericsson.cifwk.taf.maven.commons.ProcessUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Created by ekonsla on 16/12/2015.
 */
@Mojo(name = "test",
defaultPhase = LifecyclePhase.TEST,
requiresDependencyResolution = ResolutionScope.TEST,
requiresProject = true,
threadSafe = true)
public class TafTestMojo extends AbstractMojo {

    public static final String ALLURE_ISSUES_TRACKER_PROP_NAME = "allure.issues.tracker.pattern";
    public static final String MAVEN_DEPENDENCY_PLUGIN_VERSION = "2.10";
    public static final String SKIP_TESTS = "skipTests";
    public static final String SKIP_TAF_TESTS = "skipTafTests";

    private static final String MAVEN_SUREFIRE_PLUGIN_VERSION = "2.18";
    private static final String ASPECTJ_VERSION = "1.8.3";
    private static final String TAF_SUREFIRE_PROVIDER_VERSION = "3.3.0";

    @Parameter(property = SKIP_TESTS, defaultValue = "${maven.test.skip}")
    boolean skipTests = false;

    @Parameter
    private Map<String, String> suitesXml;

    @Parameter
    private Map<String, String> copyDependencies;

    @Parameter
    private Map<String, String> selenium;

    @Parameter(defaultValue = "com.ericsson.cifwk.taf.Taf")
    String mainClass;
    @Parameter(defaultValue = "")
    String commandlineArgs = "";
    @Parameter(property = "projectName", defaultValue = "${project.name}")
    String projectName;
    @Parameter(property = "tafSurefireProviderVersion", defaultValue = TafTestMojo.TAF_SUREFIRE_PROVIDER_VERSION)
    String tafSurefireProviderVersion;
    @Parameter(property = "mavenSurefirePluginVersion", defaultValue = TafTestMojo.MAVEN_SUREFIRE_PLUGIN_VERSION)
    String mavenSurefirePluginVersion;
    @Parameter(property = "aspectjVersion", defaultValue = TafTestMojo.ASPECTJ_VERSION)
    String aspectjVersion;
    @Parameter(property = "name", defaultValue = "TAFTEST")
    String name;
    @Parameter(property = "suites", defaultValue = "")
    String suites = "";
    @Parameter(property = "groups", defaultValue = "")
    String groups = "";
    @Parameter
    String dir;
    @Parameter(property = "allure.issues.tracker.pattern", defaultValue = "http://jira-oss.lmera.ericsson.se/browse/%s")
    String allureIssuesTrackerPattern;
    @Parameter
    Properties properties = new Properties();
    @Parameter(property = "useSurefire", defaultValue = "false")
    boolean useSurefire;
    @Parameter(property = "useSurefireJavaAgent", defaultValue = "false")
    boolean useSurefireJavaAgent;
    @Parameter(property = "unpackSeleniumDrivers", defaultValue = "true")
    boolean unpackSeleniumDrivers;
    @Parameter(property = "forkCount")
    String forkCount;
    @Parameter(property = "reuseForks")
    Boolean reuseForks;
    @Parameter(property = "parallel")
    String parallel;
    @Parameter(property = "threadCount")
    String threadCount;

    @Component
    protected MavenProject project;
    @Component
    protected MavenSession mavenSession;
    @Component
    protected BuildPluginManager pluginManager;
    @Component
    Settings settings;

    MojoExecutor.ExecutionEnvironment pluginEnv;

    private String dependencyOutputDirectory;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        logProcessId();
        pluginEnv = executionEnvironment(project, mavenSession, pluginManager);

        if (useSurefire) {
            executeSurefirePlugin();
        } else {
            executeCustomPlugin();
        }
    }

    private void executeSurefirePlugin() throws MojoFailureException, MojoExecutionException {
        getLog().info("Surefire plugin is being used.");
        unpackSuites();
        copyDependencies();
        if (unpackSeleniumDrivers) {
            unpackSeleniumDrivers();
        }
        testViaSurefirePlugin();
    }

    private void executeCustomPlugin() throws MojoFailureException, MojoExecutionException {
        getLog().warn("Surefire plugin is not used. Please consider configuring TAF Maven Plugin with useSurefire property.");
        unpackSuites();
        copyDependencies();
        if (unpackSeleniumDrivers) {
            unpackSeleniumDrivers();
        }
        test();
    }

    private void unpackSuites() throws MojoExecutionException, MojoFailureException {
        String includeGroupIds = containsAndNotEmpty(suitesXml, "includeGroupIds") ? suitesXml.get("includeGroupIds") : project.getGroupId();
        String includeArtifactIds = containsAndNotEmpty(suitesXml, "includeArtifactIds") ? suitesXml.get("includeArtifactIds") : "";
        String excludeGroupIds = containsAndNotEmpty(suitesXml, "excludeGroupIds") ? suitesXml.get("excludeGroupIds") : "";
        String excludeArtifactIds = containsAndNotEmpty(suitesXml, "excludeArtifactIds") ? suitesXml.get("excludeArtifactIds") : "";
        String outputDirectory = containsAndNotEmpty(suitesXml, "outputDirectory") ? suitesXml.get("outputDirectory") : project.getBuild().getDirectory() + "/taf/suites";

        Xpp3Dom cfg = configuration(
                element(name("includeGroupIds"), includeGroupIds),
                element(name("includeArtifactIds"), includeArtifactIds),
                element(name("excludeGroupIds"), excludeGroupIds),
                element(name("excludeArtifactIds"), excludeArtifactIds),
                element(name("includeTypes"), "jar"),
                element(name("includes"), "suites/*.xml"),
                element(name("outputDirectory"), outputDirectory),
                element(name("overWriteIfNewer"), "false"),
                element(name("excludeTransitive"), "true")
        );
        if (getLog().isDebugEnabled()) {
            getLog().debug("[TAF-MAVEN-PLUGIN].unpack-suites:\n\r" + cfg.toString());
        }
        Plugin dependencyPlugin = plugin("org.apache.maven.plugins", "maven-dependency-plugin", MAVEN_DEPENDENCY_PLUGIN_VERSION);
        executeMojo(dependencyPlugin, goal("unpack-dependencies"), cfg, pluginEnv);

        File suitesDirectory = new File(outputDirectory + "/suites");

        for (File it : suitesDirectory.listFiles() != null ? suitesDirectory.listFiles() : new File[0]) {
            if (it.isFile() && it.getName().endsWith(".xml")) {
                File to = new File(it.getParent() + "/../" + it.getName());
                if (to.exists()) {
                    to.delete();
                }
                try {
                    Files.move(it, to);
                } catch (IOException e) {
                    getLog().error("Could not move files", e);
                }
            } else {
                it.delete();
            }
        }
        try {
            FileUtils.deleteDirectory(suitesDirectory);
        } catch (IOException e) {
            getLog().error("Could not delete directory", e);
        }
    }

    private void copyDependencies() throws MojoExecutionException, MojoFailureException {
        String includeGroupIds = containsAndNotEmpty(copyDependencies, "includeGroupIds") ? copyDependencies.get("includeGroupIds") : project.getGroupId();
        String includeArtifactIds = containsAndNotEmpty(copyDependencies, "includeArtifactIds") ? copyDependencies.get("includeArtifactIds") : "";
        String excludeGroupIds = containsAndNotEmpty(copyDependencies, "excludeGroupIds") ? copyDependencies.get("excludeGroupIds") : "";
        String excludeArtifactIds = containsAndNotEmpty(copyDependencies, "excludeArtifactIds") ? copyDependencies.get("excludeArtifactIds") : "";
        dependencyOutputDirectory = containsAndNotEmpty(copyDependencies, "outputDirectory") ? copyDependencies.get("outputDirectory") : project.getBuild().getDirectory() + "/lib";

        Xpp3Dom cfg = configuration(
                element(name("includeGroupIds"), "com.ericsson.cifwk," + includeGroupIds),
                element(name("includeArtifactIds"), includeArtifactIds),
                element(name("excludeGroupIds"), excludeGroupIds),
                element(name("excludeArtifactIds"), "taf-ui-selenium-driver," + excludeArtifactIds),
                element(name("includeScope"), "runtime"),
                element(name("copyPom"), "false"),
                element(name("outputDirectory"), dependencyOutputDirectory),
                element(name("overWriteReleases"), "false"),
                element(name("overWriteSnapshots"), "true"),
                element(name("overWriteIfNewer"), "true"),
                element(name("stripClassifier"), "true"),
                element(name("stripVersion"), "false")
        );
        if (getLog().isDebugEnabled()) {
            getLog().debug("[TAF-MAVEN-PLUGIN].copy-dependencies:\n\r" + cfg.toString());
        }
        Plugin dependencyPlugin = plugin("org.apache.maven.plugins", "maven-dependency-plugin", MAVEN_DEPENDENCY_PLUGIN_VERSION);
        executeMojo(dependencyPlugin, goal("copy-dependencies"), cfg, pluginEnv);
    }

    private void unpackSeleniumDrivers() throws MojoExecutionException, MojoFailureException {

        String includeGroupIds = containsAndNotEmpty(selenium, "includeGroupIds") ? selenium.get("includeGroupIds") : "com.ericsson.cifwk.selenium.drivers";
        String includeArtifactIds = containsAndNotEmpty(selenium, "includeArtifactIds") ? selenium.get("includeArtifactIds") : "";
        String outputDirectory = containsAndNotEmpty(selenium, "outputDirectory") ? selenium.get("outputDirectory") : project.getBuild().getDirectory() + "/selenium-drivers";

        Xpp3Dom cfg = configuration(
                element(name("includeGroupIds"), includeGroupIds),
                element(name("includeArtifactIds"), includeArtifactIds),
                element(name("type"), "zip"),
                element(name("outputDirectory"), outputDirectory)
        );

        if (getLog().isDebugEnabled()) {
            getLog().debug("[TAF-MAVEN-PLUGIN].unpack-selenium-drivers:\n\r" + cfg.toString());
        }
        Plugin dependencyPlugin = plugin("org.apache.maven.plugins", "maven-dependency-plugin", MAVEN_DEPENDENCY_PLUGIN_VERSION);
        executeMojo(dependencyPlugin, goal("unpack-dependencies"), cfg, pluginEnv);

        cfg = configuration(
            element(name("tasks"),
                element("chmod", attributes(
                        attribute("dir", outputDirectory),
                        attribute("perm", "ugo+rx"))
                )
            )
        );

        Plugin antRunPlugin = plugin("org.apache.maven.plugins", "maven-antrun-plugin", "1.3");
        executeMojo(antRunPlugin, goal("run"), cfg, pluginEnv);
    }

    private void test() throws MojoExecutionException, MojoFailureException {
        processProperties();

        if (!commandlineArgs.contains("-dir ")) {
            String suitesDirectory = StringUtils.isNotBlank(dir) ?
                    dir :
                    containsAndNotEmpty(suitesXml, "outputDirectory") ? suitesXml.get("outputDirectory") : project.getBuild().getDirectory() + "/taf/suites";
            addToCmdLine("-dir " + suitesDirectory);
        }

        Xpp3Dom cfg = configuration(
                element("mainClass", mainClass),
                element("commandlineArgs", commandlineArgs),
                element("classpathScope", "test"),
                element("systemProperties", systemProperties(false))
        );

        if (getLog().isDebugEnabled()) {
            getLog().debug("[TAF-MAVEN-PLUGIN].test:\\n\\r" + cfg.toString());
        }

        if (shouldSkipTests()) {
            getLog().info("[TAF-MAVEN-PLUGIN] TEST SKIPPED");
        } else {
            Plugin execPlugin = plugin("org.codehaus.mojo", "exec-maven-plugin", "1.2.1");
            executeMojo(execPlugin, goal("java"), cfg, pluginEnv);
        }
    }

    private void testViaSurefirePlugin() throws MojoExecutionException, MojoFailureException {
        processProperties();

        // If groups are defined, the Surefire should not fail the tests, as they may just have been filtered out
        String failIfNoTests = StringUtils.isBlank(groups) ? "true" : "false";
        Xpp3Dom cfg = configuration(
                element("suiteXmlFiles", suitesConfiguration(suites)),
                element("testSourceDirectory", project.getBuild().getSourceDirectory()),
                element("testClassesDirectory", project.getBuild().getDirectory() +  "/taf/"),
                element("printSummary", "true"),
                element("failIfNoTests", failIfNoTests),
                element("groups", groups),
                element("systemPropertyVariables", systemProperties(true))
        );
        addConfigElementIfDefined(cfg, "forkCount", forkCount);
        addConfigElementIfDefined(cfg, "reuseForks", reuseForks);
        addConfigElementIfDefined(cfg, "parallel", parallel);
        addConfigElementIfDefined(cfg, "threadCount", threadCount);

        if (properties.getProperty("suitethreadpoolsize") != null) {
            addConfigElement(cfg, "properties", element("suitethreadpoolsize", properties.getProperty("suitethreadpoolsize")));
        }
        if (useSurefireJavaAgent) {
            addConfigElement(cfg, "argLine", "-javaagent:" + settings.getLocalRepository() + "/org/aspectj/aspectjweaver/" + aspectjVersion + "/aspectjweaver-" + aspectjVersion + ".jar");
        }

        Dependency surefireProvider = new Dependency();
        surefireProvider.setGroupId("com.ericsson.cifwk.taf");
        surefireProvider.setArtifactId("taf-surefire-provider");
        surefireProvider.setVersion(tafSurefireProviderVersion);

        List<Dependency> dependencies = Lists.newArrayList();
        dependencies.add(surefireProvider);
        if (useSurefireJavaAgent) {
            addAspectjWeaverDependency(dependencies);
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("[TAF-MAVEN-PLUGIN].test:\n\r" + cfg.toString());
        }
        if (shouldSkipTests()) {
            getLog().info("[TAF-MAVEN-PLUGIN] TEST SKIPPED");
        } else {
            Plugin execPlugin = plugin("org.apache.maven.plugins", "maven-surefire-plugin", mavenSurefirePluginVersion, dependencies);
            getLog().info("Starting test execution at " + new Date());
            executeMojo(execPlugin, goal("test"), cfg, pluginEnv);
        }
    }

    @VisibleForTesting
    protected void processProperties() {
        properties.setProperty("projectName", StringUtils.isNotBlank(properties.getProperty("projectName")) ? properties.getProperty("projectName") : projectName);
        properties.setProperty("name", StringUtils.isNotBlank(properties.getProperty("name")) ? properties.getProperty("name") : name);
        if (StringUtils.isNotBlank(properties.getProperty("suites")) || StringUtils.isNotBlank(suites)) {
            properties.setProperty("suites", StringUtils.isNotBlank(properties.getProperty("suites")) ? properties.getProperty("suites") : suites);
        }
        if (StringUtils.isNotBlank(properties.getProperty("groups")) || StringUtils.isNotBlank(groups)) {
            properties.setProperty("groups", StringUtils.isNotBlank(properties.getProperty("groups")) ? properties.getProperty("groups") : groups);
        }
        properties.setProperty("fetchLogs", property("fetchLogs", "true"));
        properties.setProperty("legacylogging", property("legacylogging", ""));
        properties.setProperty("suitethreadpoolsize", property("suitethreadpoolsize", "25"));
        if (properties.containsKey(SKIP_TESTS) || properties.containsKey(SKIP_TAF_TESTS)) {
            skipTests = true;
        }
    }

    private void addConfigElementIfDefined(Xpp3Dom configuration, String paramName, Object paramValue) {
        if (paramValue != null) {
            addConfigElement(configuration, paramName, paramValue);
        }
    }

    private void addConfigElement(Xpp3Dom configuration, String paramName, Element paramValue) {
        configuration.addChild(element(paramName, paramValue).toDom());
    }

    private void addConfigElement(Xpp3Dom configuration, String paramName, Object paramValue) {
        configuration.addChild(element(paramName, paramValue.toString()).toDom());
    }

    private void addAspectjWeaverDependency(List<Dependency> dependencies) {
        Dependency aspectjWeaver = new Dependency();
        aspectjWeaver.setGroupId("org.aspectj");
        aspectjWeaver.setArtifactId("aspectjweaver");
        aspectjWeaver.setVersion(aspectjVersion);
        dependencies.add(aspectjWeaver);
    }

    @VisibleForTesting
    protected Element[] suitesConfiguration(String suites) {
        String[] suiteXmls = suites.split(",");
        String suitesXmlFilesPrefix = project.getBuild().getDirectory() + "/taf/suites/";
        final List<Element> suitesConfiguration = new ArrayList<>();

        for (String xml : suiteXmls) {
            suitesConfiguration.add(new Element("suiteXmlFile", suitesXmlFilesPrefix + xml));
        }

        return suitesConfiguration.toArray(new Element[suitesConfiguration.size()]);
    }

    String property(String name, String defaultValue) {

        return StringUtils.isNotBlank(properties.getProperty(name)) ? properties.getProperty(name) :
                StringUtils.isNotBlank(System.getProperty(name)) ? System.getProperty(name) :
                        defaultValue;
    }

    Element[] systemProperties(boolean forSurefireRun) {
        final List<Element> systemProperties = new ArrayList<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            Element property = systemProperty(key, value, forSurefireRun);
            if (!(shouldSkipProperty(forSurefireRun, key))) {
                systemProperties.add(property);
            }
        }

        systemProperties.add(systemProperty(ALLURE_ISSUES_TRACKER_PROP_NAME, "http://jira-oss.lmera.ericsson.se/browse/%s", forSurefireRun));

        return systemProperties.toArray(new Element[systemProperties.size()]);
    }

    private Boolean shouldSkipProperty(boolean forSurefireRun, String property) {
        return forSurefireRun && ("groups".equals(property) || "suites".equals(property));
    }

    public Element systemProperty(String key, String value, boolean forSurefireRun) {
        if (forSurefireRun) {
            return element(key, value);
        } else {
            return element("systemProperty", element("key", key), element("value", value));
        }
    }

    private void logProcessId() {
        try {
            int currentPid = ProcessUtils.getCurrentPid();
            getLog().info("Current process ID is " + currentPid);
            getLog().info("Current timestamp is " + new Date());
        } catch (Exception e) {
            getLog().error(e.getMessage());
        }
    }

    @VisibleForTesting
    protected static boolean containsAndNotEmpty(Map<String, String> it, String key) {
        return (it != null) && it.containsKey(key) && StringUtils.isNotBlank(it.get(key));
    }

    private void addToCmdLine(String param) {
        commandlineArgs = commandlineArgs + " " + param;
    }

    @VisibleForTesting
    protected boolean shouldSkipTests() {
        return skipTests || System.getProperties().getProperty(SKIP_TESTS) != null || System.getProperties().getProperty(SKIP_TAF_TESTS) != null;
    }
}
