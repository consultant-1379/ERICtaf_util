package com.ericsson.cifwk.taf.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static com.ericsson.cifwk.taf.utils.ArchiveFileFinder.stripFileNameToClassName;
import static com.ericsson.cifwk.taf.utils.FileUtils.getCurrentDir;
import static java.io.File.separator;
import static java.nio.charset.Charset.forName;
import static org.junit.Assert.*;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 11.04.2016
 */
public class ArchiveFileFinderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void isArchive() throws IOException {

        // supported file extensions
        assertTrue(isArchive("fileName.zip"));
        assertTrue(isArchive("fileName.rar"));
        assertTrue(isArchive("fileName.jar"));
        assertTrue(isArchive("fileName.war"));
        assertTrue(isArchive("fileName.ear"));

        // unsupported file extensions
        assertFalse(isArchive("fileName.7z"));

        // not existing file
        assertFalse(ArchiveFileFinder.isArchive("/not-existing-folder/fileName.zip"));

        // folder is not archive
        File specialFolder = new File(folder.getRoot(), "folder.jar");
        specialFolder.mkdirs();
        assertFalse(ArchiveFileFinder.isArchive(specialFolder.getAbsolutePath()));

        // text file in folder matching archive pattern
        File file = new File(specialFolder, "index.html");
        file.createNewFile();
        String filePath = file.getAbsolutePath();
        assertFalse(ArchiveFileFinder.isArchive(filePath));
    }

    @Test
    public void unifyFileSeparator() {
        assertEquals("##opt##lame##folder##subfolder", ArchiveFileFinder.unifyFileSeparator("/opt\\lame/folder\\subfolder"));
    }

    @Test
    public void isFound() {

        // checking unification
        assertTrue(ArchiveFileFinder.isFound("\\opt/lame", "lame\\doc.txt", "/opt/lame\\doc.txt"));

        // checking match
        assertTrue(ArchiveFileFinder.isFound("", "doc.txt", "/opt/lame/doc.txt"));
        assertTrue(FileFinder.isFound("opt", "doc.txt", "/opt/lame/doc.txt"));
        assertTrue(ArchiveFileFinder.isFound("lame", "doc.txt", "/opt/lame/doc.txt"));
        assertTrue(ArchiveFileFinder.isFound("", "", ""));
        assertTrue(ArchiveFileFinder.isFound("/opt/lame", "lame/doc.txt", "/opt/lame/doc.txt"));
        assertTrue(ArchiveFileFinder.isFound("/opt/lame/doc.txt", "/opt/lame/doc.txt", "/opt/lame/doc.txt"));

        // checking not match
        assertFalse(ArchiveFileFinder.isFound("", "lame", "/opt/lame/doc.txt"));
        assertFalse(ArchiveFileFinder.isFound("wrong-folder", "doc.txt", "/opt/lame/doc.txt"));
    }

    @Test
    public void populateCache() throws IOException {
        try {
            ArchiveFileFinder.populateCache("nonExistingArchive", new HashMap<String, List<String>>());
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }

        File zipFile = createZipWithEmptyEntries("com/ericsson/Main.class", "LICENSE");
        Map<String, List<String>> cache = new HashMap<>();
        ArchiveFileFinder.populateCache(zipFile.getAbsolutePath(), cache);
        assertEquals(1, cache.size());
        List<String> foundEntries = cache.get(zipFile.getAbsolutePath());
        assertEquals(2, foundEntries.size());
        Collections.sort(foundEntries);
        assertEquals("LICENSE", foundEntries.get(0));
        assertEquals("com/ericsson/Main.class", foundEntries.get(1));
    }

    @Test
    public void populateCacheWithWrongZip() throws IOException {
        Map<String, List<String>> cache = new HashMap<>();
        File existingFile = createWrongZipFile();
        try {
            ArchiveFileFinder.populateCache(existingFile.getAbsolutePath(), cache);
            fail();
        } catch(Exception e) {
            assertTrue(e.getCause() instanceof ZipException);
        }
        assertTrue(cache.isEmpty());
    }

    @Test
    public void populateCacheFromWrongFileLocation() {
        try {
            ArchiveFileFinder.populateCache("not-existing-file.zip");
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
    }

    @Test
    public void unzip() throws IOException {
        String zipFile = createZipWithEntries("content", "data/content.txt").getAbsolutePath();
        File targetFile = new File(folder.getRoot(), "content.txt");
        assertFalse(targetFile.exists());
        ArchiveFileFinder.unzip(zipFile, targetFile.getAbsolutePath(), "data/content.txt");
        assertFileContent("content", targetFile);
    }

    @Test
    public void unzipToNotExistingSubfolder() throws IOException {
        String zipFile = createZipWithEntries("content", "data/content.txt").getAbsolutePath();
        File targetFile = new File(folder.getRoot(), "subfolder/subfolder/content.txt");
        assertFalse(targetFile.exists());
        assertFalse(targetFile.getParentFile().exists());

        ArchiveFileFinder.unzip(zipFile, targetFile.getAbsolutePath(), "data/content.txt");
        assertFileContent("content", targetFile);
    }

    @Test
    public void unzipNonExistingEntry() throws IOException {
        String zipFile = createZipWithEntries("content", "data/content.txt").getAbsolutePath();
        File targetFile = new File(folder.getRoot(), "content.txt");
        assertFalse(targetFile.exists());

        ArchiveFileFinder.unzip(zipFile, targetFile.getAbsolutePath(), "data/non-existing-file.txt");
        assertFalse(targetFile.exists());
    }

    @Test
    public void unzipWrongZip() throws IOException {
        String zipFile = createWrongZipFile().getAbsolutePath();
        File targetFile = new File(folder.getRoot(), "content.txt");
        assertFalse(targetFile.exists());

        try {
            ArchiveFileFinder.unzip(zipFile, targetFile.getAbsolutePath(), "data/content.txt");
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ZipException);
        }
    }

    @Test
    public void unzipOverridingTargetFile() throws IOException {
        String zipFile = createZipWithEntries("new content", "data/content.txt").getAbsolutePath();
        File targetFile = new File(folder.getRoot(), "content.txt");
        assertFalse(targetFile.exists());
        targetFile.createNewFile();
        Files.write(Paths.get(targetFile.getAbsolutePath()), "old content".getBytes());
        assertFileContent("old content", targetFile);

        ArchiveFileFinder.unzip(zipFile, targetFile.getAbsolutePath(), "data/content.txt");
        assertFileContent("new content", targetFile);
    }

    @Test
    public void unzipEntryAndUpdateCache() throws IOException {

        // ZIP file
        final String ENTRY_NAME = "folder/content.txt";
        String zipFile = createZipWithEntries("content", ENTRY_NAME).getAbsolutePath();

        // initial cache
        Map<String, List<String>> cache = new HashMap<>();
        List<String> entries = new ArrayList<>();
        cache.put(zipFile, entries);
        entries.add(ENTRY_NAME);
        assertTrue(entries.contains(ENTRY_NAME));

        // cache is updated
        String unzipped = ArchiveFileFinder.unzipEntryAndUpdateCache(zipFile, ENTRY_NAME, cache);
        entries = cache.get(zipFile);
        assertEquals(1, entries.size());
        assertFalse(entries.contains(ENTRY_NAME));
        String actualEntry = entries.iterator().next();
        String expectedEntry = String.format("target%stmp%sfiles/%s/%s", separator, separator, new File(zipFile).getName(), ENTRY_NAME);
        assertEquals(expectedEntry, actualEntry);

        // checking unzipped file
        assertEquals(getCurrentDir() + separator + expectedEntry, unzipped);
        assertFileContent("content", new File(unzipped));
    }

    @Test
    public void testStripFileNameToClassName() throws ClassNotFoundException {

        // regular class
        assertEquals("com.ericsson.Test", stripFileNameToClassName("com/ericsson/Test.class"));

        // supported target folders / prefixes
        assertEquals("com.ericsson.Test", stripFileNameToClassName("target/classes/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("target/test-classes/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("folder/target/classes/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("folder/target/test-classes/com/ericsson/Test.class"));

        // supported archives
        assertEquals("com.ericsson.Test", stripFileNameToClassName("folder/.zip/folder/.ear/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("folder/.jar/target/classes/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("/archive.jar/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("/archive.war/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("/archive.ear/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("/archive.zip/com/ericsson/Test.class"));
        assertEquals("com.ericsson.Test", stripFileNameToClassName("/archive.jar/com/ericsson/Test.class"));
        assertEquals(".archive.7z.com.ericsson.Test", stripFileNameToClassName("/archive.7z/com/ericsson/Test.class"));

        // testing nested classes
        assertEquals("com.ericsson.Test$NestedStaticClass", stripFileNameToClassName("com/ericsson/Test$NestedStaticClass.class"));
        assertEquals("com.ericsson.Test$NestedInstanceClass", stripFileNameToClassName("com/ericsson/Test$NestedInstanceClass.class"));

        // checking what class name is correct for class loading mechanism
        Class.forName(ArchiveFileFinderTest.NestedStaticClass.class.getName()); // ArchiveFileFinder$NestedStaticClass
        Class.forName(ArchiveFileFinderTest.NestedInstanceClass.class.getName()); // ArchiveFileFinder$NestedInstanceClass
        try {
            Class.forName(ArchiveFileFinderTest.NestedStaticClass.class.getCanonicalName()); // ArchiveFileFinder.NestedStaticClass
            fail();
        } catch (ClassNotFoundException e) {
            // OK
        }

        // edge cases
        assertEquals("", stripFileNameToClassName(""));
        try {
            stripFileNameToClassName(null);
            fail();
        } catch(RuntimeException e) {
            assertEquals("Null value is not allowed", e.getMessage());
        }
    }

    @Test
    public void findFileInJarFile() throws IOException {
        File archiveFile = createZipWithEntries("content",
                "com/ericsson/Main.class",
                "com/ericsson/application/Main.class",
                "META-INF/aop.xml");

        // regular search
        findFileInJarFileAndAssert(0, archiveFile, "non-existing-entry");
        findFileInJarFileAndAssert(0, archiveFile, "Main");
        findFileInJarFileAndAssert(1, archiveFile, "aop.xml");
        findFileInJarFileAndAssert(1, archiveFile, "META-INF/aop.xml");
        findFileInJarFileAndAssert(2, archiveFile, "Main.class");
        findFileInJarFileAndAssert(2, archiveFile, "class");

        // search in specific folders
        findFileInJarFileAndAssert(2, archiveFile, "Main.class", "com/ericsson");
        findFileInJarFileAndAssert(2, archiveFile, "Main.class", "com");
        findFileInJarFileAndAssert(0, archiveFile, "Main.class", "ericsson");
    }

    private boolean isArchive(String fileName) throws IOException {
        File file = folder.newFile(fileName);
        return ArchiveFileFinder.isArchive(file.getAbsolutePath());
    }

    private void assertFileContent(String expected, File file) throws IOException {
        assertTrue(file.exists());
        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())), forName("utf-8"));
        assertEquals(expected, content);
    }

    private File createWrongZipFile() throws IOException {
        File existingFile = folder.newFile("wrong-" + UUID.randomUUID() + ".zip");
        FileOutputStream out = new FileOutputStream(existingFile);
        out.write(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        out.close();
        return existingFile;
    }

    private File createZipWithEmptyEntries(String... entryNames) throws IOException {
        return createZipWithEntries("", entryNames);
    }

    private File createZipWithEntries(String content, String... entryNames) throws IOException {
        File zipFile = folder.newFile(UUID.randomUUID().toString() + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        for (String entryName : entryNames) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write(content.getBytes(forName("utf-8")));
            out.closeEntry();
        }
        out.close();
        return zipFile;
    }

    private void findFileInJarFileAndAssert(int expectedFound, File archiveFile, String searchTerm) throws IOException {
        findFileInJarFileAndAssert(expectedFound, archiveFile, searchTerm, "");
    }

    private void findFileInJarFileAndAssert(int expectedFound, File archiveFile, String searchTerm, String startingDirectory) throws IOException {
        List<String> found = ArchiveFileFinder.findFileInJarFile(archiveFile, searchTerm, startingDirectory);
        assertEquals(expectedFound, found.size());
    }

    public static class NestedStaticClass {

    }

    public class NestedInstanceClass {

    }

}
