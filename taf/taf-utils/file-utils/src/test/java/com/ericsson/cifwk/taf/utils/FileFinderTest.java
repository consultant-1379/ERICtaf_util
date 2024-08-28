package com.ericsson.cifwk.taf.utils;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.cglib.core.CollectionUtils.transform;

import static com.ericsson.cifwk.taf.utils.FileFinder.FindAndAddTask;
import static com.ericsson.cifwk.taf.utils.FileFinder.SKIP_ARCHIVES;
import static com.ericsson.cifwk.taf.utils.FileFinder.SKIP_CLASSPATH;
import static com.ericsson.cifwk.taf.utils.FileUtils.getCurrentDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.mockito.cglib.core.Transformer;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 11.04.2016
 */
public class FileFinderTest {

    private boolean preparedAlready = false;
    private final String EXT_PROPS = "EXT_PROPS";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void gatherLocationsToSearch() {
        List<String> locations = gatherLocationsToSearch("");

        // contains current folder
        assertTrue(locations.contains(unified(getCurrentDir())));

        // contains class path (folders, Maven archives, no JDK libraries)
        assertThat(locations, hasItem(containsString("file-utils/target/classes")));
        assertThat(locations, hasItem(containsString("/repository")));
        assertThat(locations, hasItem(containsString(".jar")));
        assertThat(locations, hasItem(containsString("junit/junit/4.13/junit-4.13.jar")));

          // class path is excluded
        System.setProperty(SKIP_CLASSPATH, "");
        assertFalse(gatherLocationsToSearch("").contains(".jar"));
    }

    @Test
    public void getResultCacheKey() {
        String key = FileFinder.getResultCacheKey("searchTerm", "initialLocation");
        assertEquals("searchTerminitialLocation", key);
    }

    @Test
    public void postProcess() {
        List<String> results = asList("duplicate", "", null, "duplicate", "something containing 'cobertura' keyword");
        List<String> processed = FileFinder.postProcess(results);

        // checking results
        assertEquals(3, processed.size());
        Iterator<String> next = results.iterator();
        assertEquals("duplicate", next.next());
        assertEquals("", next.next());
        assertEquals(null, next.next());

        // checking that original results are not affected
        assertEquals(5, results.size());
    }

    @Test
    public void gatherLocationsToSearchStartingFrom() {

        // contains current folder
        String currentFolder = getCurrentFolder();
        assertTrue(gatherLocationsToSearch("").contains(currentFolder));
        assertTrue(gatherLocationsToSearch(null).contains(currentFolder));
        assertTrue(gatherLocationsToSearch("not-existing-folder").contains(currentFolder));
        assertTrue(gatherLocationsToSearch(currentFolder).contains(currentFolder));

        // contains provided folder instead
        String existingFolder = unified(new File(currentFolder).getParent());
        assertTrue(gatherLocationsToSearch(existingFolder).contains(existingFolder));
        assertFalse(gatherLocationsToSearch(existingFolder).contains(currentFolder));
    }

    @Test
    public void findFileInFileSystem() {

        // find multiple files
        List<String> found = FileFinder.findFileInFileSystem("FileFinder.class", "target/classes", "");
        assertList(found, "/ArchiveFileFinder", "/FileFinder", "/InternalFileFinder.class");

        // find the file
        found = FileFinder.findFileInFileSystem("/FileFinder.class", "target/classes", "");
        assertList(found, "/FileFinder");

        // folder is NOT found
        found = FileFinder.findFileInFileSystem("folder", "target/classes", "");
        assertList(found);

        // resource in folder is found
        String resource = "toBeFound.txt";
        found = unified(FileFinder.findFileInFileSystem(resource, "target/", ""));
        assertFalse(found.isEmpty());
        assertThat(found.iterator().next(), endsWith("folder/sub-folder/toBeFound.txt"));

        // search is case sensitive - NOT found
        found = FileFinder.findFileInFileSystem(resource.toUpperCase(), "target/", "");
        assertList(found);

        // search in archives: file found
        String libraryLocation = getHamcrestJar();
        String libraryContainer = new File(libraryLocation).getParent();
        found = FileFinder.findFileInFileSystem("CoreMatchers.class", libraryContainer, "");
        assertList(found, "CoreMatchers.class");

        // search in archives is DISABLED: file is NOT found
        System.setProperty(SKIP_ARCHIVES, "");
        found = FileFinder.findFileInFileSystem("CoreMatchers.class", libraryContainer, "");
        assertList(found);
    }

    @Test
    @Ignore("As per JIRA https://jira-nam.lmera.ericsson.se/browse/CIS-40830")
    public void findFile() {
        findFileAndAssert("FileFinder", "");
        findFileAndAssert("FileFinder.class", "target/classes", "FileFinder.class", "ArchiveFileFinder.class", "InternalFileFinder.class");
        findFileAndAssert("FileFinder.class", "classes", "FileFinder.class", "ArchiveFileFinder.class", "InternalFileFinder.class");
    }

    @Test
    public void findAndAddAction() throws Exception {

        // file is found
        List<String> results = find("ArchiveFileFinder.class", "", "target/classes/");
        String file = "com/ericsson/cifwk/taf/utils/ArchiveFileFinder.class";
        assertList(results, unified(getCurrentDir()) + "/target/classes/" + file);

        // file is found using initial location
        results = find("ArchiveFileFinder.class", "lasse", "target/classes/");
        assertList(results, "ArchiveFileFinder.class");

        // file is NOT found using initial location
        results = find("ArchiveFileFinder.class", "lasse2", "target/classes/");
        assertList(results);

        // archive is found
        results = find("hamcrest-all-1.3.jar", "", "target/lib/hamcrest-all-1.3.jar");
        assertList(results, "target/lib/hamcrest-all-1.3.jar");

        // archive is found using initial
        results = find("hamcrest-all-1.3.jar", "lib", "target/lib/hamcrest-all-1.3.jar");
        assertList(results, "target/lib/hamcrest-all-1.3.jar");

        // subfolder is found
        results = find("sub-folder", "", "target/test-classes/");
        assertList(results, "target/test-classes/folder/sub-folder");

        // subfolder is found using initial
        results = find("sub-folder", "tes", "target/test-classes/");
        assertList(results, "target/test-classes/folder/sub-folder");

        // file inside not supported archive - NOT found
        String libraryLocation = getHamcrestJar().replaceAll("\\.jar", ".7z");
        results = find("CoreMatchers.class", "", libraryLocation);
        assertList(results);

        // file inside archive is found
        libraryLocation = getHamcrestJar();
        results = find("CoreMatchers.class", "", libraryLocation);
        assertList(results, "target/tmp/files/hamcrest-all-1.3.jar/org/hamcrest/CoreMatchers.class");

        // searching in archives is DISABLED - NOT found
        System.setProperty(SKIP_ARCHIVES, "");
        results = find("CoreMatchers.class", "", libraryLocation);
        assertList(results);
    }

    @Test
    public void findClass() {

        // searching by suffix
        assertFoundClasses("FileFinder", FileFinder.class, ArchiveFileFinder.class, InternalFileFinder.class);
        assertFoundClasses("FileFinder.class", FileFinder.class, ArchiveFileFinder.class, InternalFileFinder.class);

        // nested classes are found as well
        assertFoundClasses("NestedStaticClass.class", FileFinderTest.NestedStaticClass.class);
        assertFoundClasses("NestedInstanceClass", FileFinderTest.NestedInstanceClass.class);
    }

    @Test
    public void testFileFind() {
        assertTrue(!FileFinder.findFile("FileFinder.java").isEmpty());
    }

    @Test
    public void codebaseSearch() {
        assertTrue(!FileFinder.findFile("com\\ericsson\\cifwk/taf/utils/FileFinder.java", "src/main/java/").isEmpty());
    }

    @Test
    public void inJarSearch() {
        assertTrue(!FileFinder.findFile("properties", "META-INF").isEmpty());
    }

    @Test
    public void searchForDir() {
        assertTrue(!FileFinder.findFile("com\\ericsson\\cifwk/taf/utils", "src/main/java/").isEmpty());
    }

    @Test
    public void testFileFindOfWrongFile() {
        assertTrue(FileFinder.findFile("FileFinder.groovy", "src/test/java/").isEmpty());
    }

    @Test
    public void testFileFindStartingInNonexistingDir() {
        assertTrue(FileFinder.findFile("wrongFile", "nonexistingDir").isEmpty());
    }

    @Test
    public void searchForClass() {
        assertFalse(FileFinder.findClass("GroovyObject.class").isEmpty());
        assertFalse(FileFinder.findClass("FileFinder").isEmpty());
        assertTrue(FileFinder.findClass("ASDASD").isEmpty());
    }

    @SuppressWarnings("unchecked")
    private List<String> gatherLocationsToSearch(String initialLocation) {
        List<File> foundLocations = FileFinder.gatherLocationsToSearch(initialLocation);
        return transform(foundLocations, new FileToString());
    }

    private void findFileAndAssert(String searchTerm, String initialLocation, String... expectedToBeFound) {
        List<String> foundFiles = FileFinder.findFile(searchTerm, initialLocation);
        assertList(foundFiles, expectedToBeFound);
    }

    @SuppressWarnings("unchecked")
    private void assertList(List<String> foundFiles, String... expectedToBeFound) {
        foundFiles = unified(foundFiles);
        String errorMessage = String.format("Expected %s but was %s", asList(expectedToBeFound), foundFiles);
        assertEquals(errorMessage, expectedToBeFound.length, foundFiles.size());
        for (String expectedFile : expectedToBeFound) {
            assertThat(foundFiles, CoreMatchers.hasItems(CoreMatchers.containsString(expectedFile)));
        }
    }

    private String getHamcrestJar() {
        List<File> locationsToSearch = FileFinder.gatherLocationsToSearch("");
        String libraryLocation = null;
        for (File locationToSearch : locationsToSearch) {
            String location = locationToSearch.getAbsolutePath();
            if (location.contains("hamcrest-all-1.3.jar")) {
                libraryLocation = location;
            }
        }
        assertNotNull("Dependency hamcrest-all might have been upgraded. Please update this test accordingly", libraryLocation);
        return libraryLocation;
    }

    private List<String> find(String searchTerm, String initialLocation, String locationToSearch) throws Exception {
        List<String> results = new ArrayList<>();
        FindAndAddTask task = new FindAndAddTask(searchTerm, initialLocation, results, new File(locationToSearch));
        task.call();
        return results;
    }

    private void assertFoundClasses(String searchTerm, Class<?>... expectedToBeFound) {
        List<Class> foundClasses = FileFinder.findClass(searchTerm);
        String errorMessage = String.format("Expected %s but was %s", asList(expectedToBeFound), foundClasses);
        assertEquals(errorMessage, expectedToBeFound.length, foundClasses.size());
        for (Class<?> expectedClass : expectedToBeFound) {
            assertThat(foundClasses, (Matcher) CoreMatchers.hasItems(expectedClass));
        }
    }

    private static List<String> unified(List<String> strings) {
        List<String> result = new ArrayList<>();
        for (String string : strings) {
            result.add(unified(string));
        }
        return result;
    }

    private static String unified(String string) {
        return string.replaceAll("\\\\", "/");
    }

    private String getCurrentFolder() {
        return unified(getCurrentDir());
    }

    public void prepareFiles() throws IOException {
        if (!preparedAlready) {
            String externalPropsFolder = folder.newFolder(EXT_PROPS).getAbsolutePath();
            File ntf = folder.newFolder("notToFind");
            ntf = new File(ntf.getAbsolutePath() + "/" + EXT_PROPS);
            ntf.mkdirs();

            ResourceGroovyMethods.leftShift(new File(ntf.getAbsolutePath() + "/oncp.properties"), "content");
            File jarFile = folder.newFile(EXT_PROPS + "/props.jar");
            folder.newFolder(EXT_PROPS + "/test.properties");

            System.getProperties().setProperty("java.class.path", System.getProperties().getProperty("java.class.path") + System.getProperties().getProperty("path.separator") + ntf.getAbsolutePath());

            JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile));
            JarEntry propInJarFile = new JarEntry(externalPropsFolder + "/some.properties");
            propInJarFile.setTime(System.currentTimeMillis());
            jar.putNextEntry(propInJarFile);
            jar.write("someContent".getBytes(), 0, StringGroovyMethods.size("someContent"));
            jar.closeEntry();
            jar.close();
            preparedAlready = true;
        }

    }

    public static class NestedStaticClass {

    }

    public class NestedInstanceClass {

    }

    private static class FileToString implements Transformer {
        @Override
        public Object transform(Object value) {
            return unified(((File) value).getPath());
        }
    }

    @Test
    public void shouldLookOnlyInSpecifiedLocation() throws IOException {
        prepareFiles();
        String initialLocation = folder.getRoot().getAbsolutePath() + "/" + EXT_PROPS;

        List found = FileFinder.findFile("properties", initialLocation);
        assertEquals(found.size(), 2);
    }


    @Test
    public void shouldSkipJarSearchingInSpecifiedLocation() throws IOException {
        prepareFiles();
        System.setProperty(SKIP_ARCHIVES, "");
        List found = FileFinder.findFile("properties", folder.getRoot().getAbsolutePath() + "/" + EXT_PROPS);
        assertEquals(found.size(), 1);
        System.clearProperty(SKIP_ARCHIVES);
    }

    @Test
    @Ignore
    public void shouldSkipClasspathSearch() throws IOException {
        prepareFiles();
        System.getProperties().setProperty("java.class.path", System.getProperties().getProperty("java.class.path") + System.getProperties().getProperty("path.separator") + folder.getRoot().getAbsolutePath() + "/" + EXT_PROPS);
        System.setProperty(SKIP_CLASSPATH, "");
        List found = FileFinder.findFile("properties", "/" + EXT_PROPS);
        assertEquals(found.size(), 0);
        System.clearProperty(SKIP_CLASSPATH);
    }

    @Test
    public void shouldLookOnlyInSpecifiedDirAndSkipJars() throws IOException {
        prepareFiles();
        System.setProperty(SKIP_CLASSPATH, "");
        System.setProperty(SKIP_ARCHIVES, "");
        List found = FileFinder.findFile("properties", folder.getRoot().getAbsolutePath() + "/" + EXT_PROPS);
        assertEquals(found.size(), 1);
        System.clearProperty(SKIP_CLASSPATH);
        System.clearProperty(SKIP_ARCHIVES);
    }

}
