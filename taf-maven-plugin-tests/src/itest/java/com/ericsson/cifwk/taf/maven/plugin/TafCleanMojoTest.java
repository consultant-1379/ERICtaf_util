package com.ericsson.cifwk.taf.maven.plugin;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.ericsson.cifwk.taf.maven.plugin.AssertUtils.assertDirectoriesDoesntExist;
import static com.ericsson.cifwk.taf.maven.plugin.AssertUtils.assertSuiteExists;
import static com.ericsson.cifwk.taf.maven.plugin.MavenTestRunner.buildTestware;
import static com.ericsson.cifwk.taf.maven.plugin.MavenTestRunner.copyPomToTemp;
import static com.ericsson.cifwk.taf.maven.plugin.MavenTestRunner.runPom;

/**
 * Created by ekonsla on 27/01/2016.
 */
public class TafCleanMojoTest {

    @BeforeClass
    public static void setUp() throws Exception {
        buildTestware();
    }

    @Test
    public void shoulCleanDirectories() throws Exception {
        String workspace = copyPomToTemp("no-surefire-pom.xml");

        runPom(workspace + File.separator + "no-surefire-pom.xml", "clean", "test");

        assertSuiteExists(workspace, "mySuite.xml");

        runPom(workspace + File.separator + "no-surefire-pom.xml", "clean");

        assertDirectoriesDoesntExist(workspace);
    }
}
