package com.ericsson.cifwk.taf.utils;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.utils.CollectionUtils.Java7Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.utils.CollectionUtils.filter;
import static com.ericsson.cifwk.taf.utils.CollectionUtils.transform;
import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Utility class to find a file inside an archive
 */
@API(Internal)
class ArchiveFileFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveFileFinder.class);

    private static final List<String> ARCHIVES = Arrays.asList("\\.jar", "\\.zip", "\\.ear", "\\.war", "\\.rar");

    private static final List<Pattern> ARCHIVE_PATTERNS;

    static {
        ARCHIVE_PATTERNS = transform(ARCHIVES, new Java7Function<String, Pattern>() {
            @Override
            public Pattern apply(String s) {
                return Pattern.compile(s + "$");
            }
        });
    }

    public static String tmpDir = "target" + File.separator + "tmp" + File.separator + "files";

    private static Map<String, List<String>> cachedJarContent = new HashMap<>();

    /**
     * Check if a particular file is an archive
     */
    public static boolean isArchive(final String absoluteFilePath) {
        for (Pattern it : ARCHIVE_PATTERNS) {
            if (it.matcher(absoluteFilePath).find()) {
                return new File(absoluteFilePath).isFile();
            }
        }
        return false;
    }

    /**
     * Utility method to make sure Windows and nix class path is looking the same
     */
    protected static String unifyFileSeparator(String locationString) {
        return locationString.replaceAll("\\\\|/", "##");
    }

    /**
     * Method to verify if provided file strings (folder and file name) are found in provided file path.
     */
    protected static boolean isFound(String fileFolder, String fileName, String inFilePath) {
        String currentFilePath = unifyFileSeparator(inFilePath);
        boolean fileNameMatches = currentFilePath.endsWith(unifyFileSeparator(fileName));
        boolean containsInitialLocation = currentFilePath.contains(unifyFileSeparator(fileFolder));
        if (LOGGER.isTraceEnabled()) {
            if (fileNameMatches && containsInitialLocation)
                LOGGER.trace("Found match " + inFilePath + " with " + fileName + " and " + fileFolder);
        }

        return fileNameMatches && containsInitialLocation;
    }

    /**
     * Populate archive entries cache (archive location -> entry names)
     */
    protected static void populateCache(String archiveLocation) {
        populateCache(archiveLocation, cachedJarContent);
    }

    protected static void populateCache(final String archiveLocation, final Map<String, List<String>> cache) {
        try {
            ZipFile zipFile = new ZipFile(archiveLocation);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            List<String> zipEntryNames = new ArrayList<>();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                zipEntryNames.add(zipEntry.getName());
            }
            cache.put(archiveLocation, zipEntryNames);
            zipFile.close();
        } catch (ZipException e) {
            LOGGER.error(archiveLocation + " contains wrong archive", e);
            throw ThrowableUtils.propagate(e);
        } catch (IOException e) {
            LOGGER.error(archiveLocation + " couldn't be read", e);
            throw ThrowableUtils.propagate(e);
        }
    }

    /**
     * Get entries for specified file from cached data
     */
    private static synchronized List<String> getEntries(String location) {
        if (cachedJarContent.get(location) == null) {
            populateCache(location);
        }
        return cachedJarContent.get(location);
    }

    /**
     * Unzip entry from archive file to specified location
     *
     * @param archiveLocation - location of the archive file
     * @param targetFileName  - file to be created for unzipped content
     * @param entryToUnzip    - name of the file inside archive to unzip
     */
    protected static void unzip(final String archiveLocation, String targetFileName, final String entryToUnzip) {

        // creating folders if required
        final File cacheFile = new File(targetFileName);
        cacheFile.getParentFile().mkdirs();

        // reading ZIP headers
        ZipFile zip;
        try {
            zip = new ZipFile(archiveLocation);
        } catch (IOException e) {
            throw ThrowableUtils.propagate(e);
        }

        // unzipping
        try {
            ZipEntry entry = zip.getEntry(entryToUnzip);
            if (entry == null) {
                LOGGER.error(format("There is no entry '%s' in archive '%s'", entryToUnzip, archiveLocation));
                return;
            }
            unzip(zip.getInputStream(entry), cacheFile);
        } catch (IOException e) {
            throw ThrowableUtils.propagate(e);
        } finally {
            closeQuietly(zip);
        }
    }

    private static void unzip(InputStream zipStream, File targetFile) {
        try {
            Files.copy(zipStream, Paths.get(targetFile.getAbsolutePath()), REPLACE_EXISTING);
        } catch (IOException e) {
            closeQuietly(zipStream);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
            // OK
        }
    }

    /**
     * Unzips archive entry provided into temporary folder in current directory,
     * updates the cache to reference to new file location and
     * returns the path of unzipped file.
     */
    protected static synchronized String unzipEntryAndUpdateCache(String archiveLocation, String entryName) {
        return unzipEntryAndUpdateCache(archiveLocation, entryName, cachedJarContent);
    }

    protected static synchronized String unzipEntryAndUpdateCache(final String archiveLocation, String entryName, Map<String, List<String>> cache) {
        if (entryName.startsWith(tmpDir)) return entryName;
        String newLocation = (tmpDir + "/" + new File(archiveLocation).getName() + "/" + entryName).replace(":", "");
        unzip(archiveLocation, newLocation, entryName);
        cache.get(archiveLocation).remove(entryName);
        cache.get(archiveLocation).add(newLocation);
        return FileUtils.getCurrentDir() + File.separator + newLocation;
    }

    /**
     * Search for entries inside a archive file
     */
    public static List<String> findFileInJarFile(File archiveFile, final String searchTerm, final String startingDirectory) {
        final String location = archiveFile.getAbsolutePath();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Searching for " + searchTerm + " inside " + startingDirectory + " in file " + String.valueOf(archiveFile));
        }
        List<String> entries = getEntries(location);
        return transform(filter(entries, new CollectionUtils.Java7Predicate<String>() {
            @Override
            public boolean test(String s) {
                String className = stripFileNameToClassName(s);
                String prefix = stripFileNameToClassName(startingDirectory);
                return className != null && prefix != null && className.startsWith(prefix);
            }
        }.and(new CollectionUtils.Java7Predicate<String>() {
            @Override
            public boolean test(String s) {
                return isFound(startingDirectory, searchTerm, s);
            }
        })), new Java7Function<String, String>() {
            @Override
            public String apply(String s) {
                return unzipEntryAndUpdateCache(location, s);
            }
        });
    }

    /**
     * Search for entries inside a archive file
     */
    public static List<String> findFileInJarFile(File archiveFile, String searchTerm) {
        return ArchiveFileFinder.findFileInJarFile(archiveFile, searchTerm, "");
    }

    /**
     * For given file path tries to extract class name:
     * - replaces file separator with '.'
     * - truncates supported archives prefix
     * - truncates target classes folder prefix (target/classes ot target/test-classes)
     */
    protected static String stripFileNameToClassName(String fileName) {

        // precondition
        if (fileName == null) {
            throw new RuntimeException("Null value is not allowed");
        }

        String localRunLocation = "target.classes.|target.test-classes.";
        String allArchivers = StringUtils.join(ARCHIVES, ".|") + "|" + localRunLocation;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Splitting " + fileName + " with regexp: " + allArchivers + ": " + Arrays.toString(fileName.split(allArchivers)));
        }

        String[] substrings = fileName.split(allArchivers);
        if (substrings.length != 0) {
            String className = substrings[substrings.length - 1];
            return className.replaceAll("\\\\|/", ".").replaceAll("\\.class$", "");
        } else {
            throw new IllegalStateException(String.format("Path '%s' doesn't match any pattern (%s)", fileName, allArchivers));
        }
    }

}
