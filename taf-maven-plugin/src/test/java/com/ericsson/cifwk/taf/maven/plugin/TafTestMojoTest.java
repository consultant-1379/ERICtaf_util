package com.ericsson.cifwk.taf.maven.plugin;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ekonsla on 21/12/2015.
 */
public class TafTestMojoTest {

    private TafTestMojo unit = new TafTestMojo();

    private MavenProject project;

    @Before
    public void setUp() {
        project = new MavenProject();
        Build build = new Build();
        build.setDirectory("project-home");
        project.setBuild(build);
        unit.project = project;
    }

    @Test
    public void processPropertiesDefault(){
        unit.projectName = "sample-test";
        unit.name = "name";
        unit.suites = "suites";
        unit.groups = "groups";

        unit.processProperties();

        Properties actual = unit.properties;

        assertEquals("sample-test", actual.getProperty("projectName"));
        assertEquals("name", actual.getProperty("name"));
        assertEquals("suites", actual.getProperty("suites"));
        assertEquals("groups", actual.getProperty("groups"));

        assertEquals("true", actual.getProperty("fetchLogs"));
        assertEquals("", actual.getProperty("legacylogging"));
        assertEquals("25", actual.getProperty("suitethreadpoolsize"));
    }


    @Test
    public void processPropertiesSetFromProperties(){

        Properties set = new Properties();
        set.setProperty("projectName", "sample-test");
        set.setProperty("name", "name");
        set.setProperty("suites", "suites");
        set.setProperty("groups", "groups");

        set.setProperty("fetchLogs","false");
        set.setProperty("logdir", "theDir");
        set.setProperty("legacylogging", "cat");
        set.setProperty("suitethreadpoolsize", "88");
        unit.properties = set;

        unit.processProperties();

        Properties actual = unit.properties;

        assertEquals("sample-test", actual.getProperty("projectName"));
        assertEquals("name", actual.getProperty("name"));
        assertEquals("suites", actual.getProperty("suites"));
        assertEquals("groups", actual.getProperty("groups"));

        assertEquals("false", actual.getProperty("fetchLogs"));
        assertEquals("theDir", actual.getProperty("logdir"));
        assertEquals("cat", actual.getProperty("legacylogging"));
        assertEquals("88", actual.getProperty("suitethreadpoolsize"));
    }

    @Test
    public void skipTestsFlag(){
        Properties set = new Properties();
        set.setProperty("projectName", "sample-test");
        set.setProperty("name", "name");
        set.setProperty("skipTests", "");
        unit.properties = set;
        unit.processProperties();
        Properties actual = unit.properties;
        assertTrue("Tests should be marked as skipped", unit.shouldSkipTests());
    }

    @Test
    public void skipTafTestsFlag(){
        Properties set = new Properties();
        set.setProperty("projectName", "sample-test");
        set.setProperty("name", "name");
        set.setProperty("skipTafTests", "");
        unit.properties = set;
        unit.processProperties();
        Properties actual = unit.properties;
        assertTrue("Tests should be marked as skipped", unit.shouldSkipTests());
    }

    @Test
    public void suitesConfiguration() {
        MojoExecutor.Element[] suiteElements = unit.suitesConfiguration("success.xml,success2.xml");
        assertEquals(2, suiteElements.length);
        assertSuiteElement(suiteElements[0], "success.xml");
        assertSuiteElement(suiteElements[1], "success2.xml");
    }

    @Test
    public void containsAndNotEmpty(){
        Map<String, String> it = null;
        assertFalse(TafTestMojo.containsAndNotEmpty(it, "nullMap"));

        it = new HashMap<>();
        it.put("nullKey", null);
        assertFalse(TafTestMojo.containsAndNotEmpty(it, "nullKey"));

        it = new HashMap<>();
        it.put("emptyKey", "");
        assertFalse(TafTestMojo.containsAndNotEmpty(it, "emptyKey"));

        it = new HashMap<>();
        it.put("keyToContain", "notEmpty");
        assertTrue(TafTestMojo.containsAndNotEmpty(it, "keyToContain"));

    }

    private void assertSuiteElement(MojoExecutor.Element suiteElement, String expectedfileName)  {
        try {
            assertEquals("suiteXmlFile", FieldUtils.readField(suiteElement, "name", true));
            assertEquals(project.getBuild().getDirectory() + "/taf/suites/" + expectedfileName, FieldUtils.readField(suiteElement, "text", true));
        } catch (IllegalAccessException e) {
            fail("unable to read field of suiteElement");
        }
    }
}
