package com.ericsson.cifwk.taf.utils;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ericsson.cifwk.taf.utils.FileUtils.getCurrentDir;
import static com.ericsson.cifwk.taf.utils.InternalFileFinder.FindAndAddTask;
import static org.junit.Assert.*;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 11.04.2016
 */
public class InternalFileFinderTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void findFile() {
        findFileAndAssert("FileFinder", "", null);
        findFileAndAssert("FileFinder.class", "target/classes", "FileFinder.class");
        findFileAndAssert("FileFinder.class", "classes", "FileFinder.class");
    }

    @Test
    public void findAndAddAction() throws Exception {

        // file is found
        String result = unified(find("ArchiveFileFinder.class", "", "target/classes/"));
        String file = "com/ericsson/cifwk/taf/utils/ArchiveFileFinder.class";
        assertEquals(unified(getCurrentDir()) + "/target/classes/" + file, result);

        // file is found using initial location
        result = find("ArchiveFileFinder.class", "lasse", "target/classes/");
        assertThat(result, CoreMatchers.endsWith("ArchiveFileFinder.class"));

        // file is NOT found using initial location
        result = find("ArchiveFileFinder.class", "lasse2", "target/classes/");
        assertEquals(null, result);

        // archive is found
        result = unified(find("hamcrest-all-1.3.jar", "", "target/lib/hamcrest-all-1.3.jar"));
        assertThat(result, CoreMatchers.endsWith("target/lib/hamcrest-all-1.3.jar"));

        // archive is found using initial
        result = unified(find("hamcrest-all-1.3.jar", "lib", "target/lib/hamcrest-all-1.3.jar"));
        assertThat(result, CoreMatchers.endsWith("target/lib/hamcrest-all-1.3.jar"));

        // subfolder is found
        result = unified(find("sub-folder", "", "target/test-classes/"));
        assertThat(result, CoreMatchers.endsWith("target/test-classes/folder/sub-folder"));

        // subfolder is found using initial
        result = unified(find("sub-folder", "tes", "target/test-classes/"));
        assertThat(result, CoreMatchers.endsWith("target/test-classes/folder/sub-folder"));

        // file inside not supported archive - NOT found
        String libraryLocation = getHamcrestJar().replaceAll("\\.jar", ".7z");
        result = find("CoreMatchers.class", "", libraryLocation);
        assertEquals(null, result);

        // file inside archive is found
        libraryLocation = getHamcrestJar();
        result = unified(find("CoreMatchers.class", "", libraryLocation));
        assertThat(result, CoreMatchers.endsWith("target/tmp/files/hamcrest-all-1.3.jar/org/hamcrest/CoreMatchers.class"));

        // searching in archives is DISABLED - NOT found
        System.setProperty(FileFinder.SKIP_ARCHIVES, "");
        result = find("CoreMatchers.class", "", libraryLocation);
        assertEquals(null, result);
    }

    @Test
    public void testFileFind() {
        String result = unified(InternalFileFinder.findFile("FileFinder.java"));
        assertThat(result, CoreMatchers.endsWith("taf/utils/ArchiveFileFinder.java"));
    }

    @Test
    public void codebaseSearch() {
        String result = unified(InternalFileFinder.findFile("com\\ericsson\\cifwk/taf/utils/FileFinder.java", "src/main/java/"));
        assertThat(result, CoreMatchers.endsWith("taf/utils/FileFinder.java"));
    }

    @Test
    public void inJarSearch() {
        String result = unified(InternalFileFinder.findFile("properties", "META-INF"));
        assertThat(result, CoreMatchers.endsWith("/pom.properties"));
    }

    @Test
    public void searchForDir() {
        String result = unified(InternalFileFinder.findFile("com\\ericsson\\cifwk/taf/utils", "src/main/java/"));
        assertThat(result, CoreMatchers.endsWith("src/main/java/com/ericsson/cifwk/taf/utils"));
    }

    @Test
    public void testFileFindOfWrongFile() {
        assertNull(InternalFileFinder.findFile("FileFinder.groovy", "src/test/java/"));
    }

    @Test
    public void testFileFindStartingInNonexistingDir() {
        assertNull(InternalFileFinder.findFile("wrongFile", "nonexistingDir"));
    }

    private void findFileAndAssert(String searchTerm, String initialLocation, String expectedToBeFound) {
        String foundFile = InternalFileFinder.findFile(searchTerm, initialLocation);
        if (expectedToBeFound == null) {
            assertNull(foundFile);
        } else {
            assertThat(foundFile, CoreMatchers.endsWith(expectedToBeFound));
        }
    }

    private String getHamcrestJar() {
        List<File> locationsToSearch = FileFinder.gatherLocationsToSearch("");
        String libraryLocation = null;
        for(File locationToSearch : locationsToSearch) {
            String location = locationToSearch.getAbsolutePath();
            if (location.contains("hamcrest-all-1.3.jar")) {
                libraryLocation = location;
            }
        }
        assertNotNull("Dependency hamcrest-all might have been upgraded. Please update this test accordingly", libraryLocation);
        return libraryLocation;
    }

    private String find(String searchTerm, String initialLocation, String locationToSearch) throws Exception {
        List<String> results = new ArrayList<>();
        FindAndAddTask task = new FindAndAddTask(searchTerm, initialLocation, new File(locationToSearch));
        return task.call();
    }

    private static String unified(String string) {
        return string == null ? null : string.replaceAll("\\\\", "/");
    }

}
