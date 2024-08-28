package com.ericsson.cifwk.taf.maven.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by ekonsla on 28/01/2016.
 */
public class AssertUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(AssertUtils.class);

    private static final String TEST_OUTPUT_DIR = "test-output/";
    private static final String TMP_DIR = "tmp/";
    private static final String SUITES_DIR = "target/taf/suites/";
    private static final String[] SUREFIRE_REPORTS = {
            "target/surefire-reports/ConfigurationTest.txt",
            "target/surefire-reports/TEST-ConfigurationTest.xml"
    };

    public static void assertDirectoriesDoesntExist(String workspace){
        assertDirDoesntExist(workspace + "/" + TEST_OUTPUT_DIR);
        assertDirDoesntExist(workspace + "/" + TMP_DIR);
    }

    public static void assertSuiteExists(String workspace, String suite) throws URISyntaxException {
        assertFileExists(workspace + "/" + SUITES_DIR + suite);
    }

    public static void assertSurefireExists(String workspace) throws URISyntaxException {
        for (String report : SUREFIRE_REPORTS){
            assertFileExists(workspace + "/" + report);
        }
    }

    private static void assertDirExists(String dirPath){
        ClassLoader classLoader = AssertUtils.class.getClassLoader();
        try {
            URL resource  = classLoader.getResource(dirPath);
            assertThat(resource).isNotNull().withFailMessage(dirPath + " doesn't exist");
            File f = new File(resource.toURI());
            assertThat(f.exists()).isTrue().withFailMessage("file " + dirPath + " doesn't exist");
            assertThat(f.isDirectory()).isTrue().withFailMessage(dirPath + " is not a directory");
        } catch (URISyntaxException e) {
            fail(dirPath + " doesn't exist");
        }
    }

    private static void assertFileExists(String filePath){
        ClassLoader classLoader = AssertUtils.class.getClassLoader();
        try {
            URL resource  = classLoader.getResource(filePath);
            assertThat(resource).isNotNull().withFailMessage(filePath + " doesn't exist");
            File f = new File(resource.toURI());
            assertThat(f.exists()).isTrue().withFailMessage("file " + filePath + " doesn't exist");
            assertThat(f.isDirectory()).isFalse().withFailMessage(filePath + " is a directory");
        } catch (URISyntaxException e) {
            fail(filePath + " doesn't exist");
        }
    }

    private static void assertDirDoesntExist(String dirPath){
        ClassLoader classLoader = AssertUtils.class.getClassLoader();
        try {
            URL resource  = classLoader.getResource(dirPath);
            if (resource == null) {
                LOGGER.info("passed");
                return;
            }
            File f = new File(resource.toURI());
            assertThat(f.exists()).isFalse().withFailMessage("file " + dirPath + " exists");
        }  catch (URISyntaxException e) {
            LOGGER.info("passed");
        }
    }
}
