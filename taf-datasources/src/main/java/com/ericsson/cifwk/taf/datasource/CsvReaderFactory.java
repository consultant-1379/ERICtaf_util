/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * <pre>
 * Class Name: CsvReaderFactory
 * Description: Factory for CSV reader.
 * suite.
 * </pre>
 **/
@API(Internal)
public final class CsvReaderFactory {

    public static final String COMMENT_DELIMITER_PROPERTY = "taf.data.comment.identifier";
    public static final String UNIX_FILE_SEPARATOR = "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReaderFactory.class);
    private static final int RETRY_CNT = 3;
    private static final String[] CUSTOM_SCHEME = { "http", "https", "ftp", "test" }; // Test scheme should be use fot unit Test

    /* instance variables */
    private final String location;
    private final String uri;
    private final String dataProviderDelimiter;

    /**
     * <pre>
     * Name: CsvReaderFactory(String, String, String, String)
     * Description: instance constructor. Initialize instance values.
     * </pre>
     *
     * @param location
     *         filesystem location (from resources/data directory) or URI location (new feature)
     * @param uri
     *         URL location of CSV file
     * @param dataProviderDelimiter
     *         property used to specify field separation (default is comma)
     */
    public CsvReaderFactory(final String location, final String uri, final String dataProviderDelimiter) {
        this.location = location;
        this.uri = uri;
        this.dataProviderDelimiter = dataProviderDelimiter;
    }

    @VisibleForTesting
    static String getSearchTerm(final String location) {
        if (containsFileSeparator(location)) {
            return location;
        }

        final String searchTerm;
        if (!isWindowsOs()) {
            searchTerm = startsWithFileSeparator(location) ? location : File.separator + location;
        } else {
            if (Paths.get(location).isAbsolute() || startsWithFileSeparator(location)) {
                searchTerm = location;
            } else {
                searchTerm = File.separator + location;
            }
        }
        return searchTerm;
    }

    private static boolean isWindowsOs() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static boolean startsWithFileSeparator(final String location) {
        return location.startsWith(File.separator) || location.startsWith(UNIX_FILE_SEPARATOR);
    }

    private static boolean containsFileSeparator(final String location) {
        return location.contains(File.separator) || location.contains(UNIX_FILE_SEPARATOR);
    }

    /**
     * <pre>
     * Name: createCsvReader()
     * Description: Creating new CSV reader using method (loadFromLocation, loadFromUri) related to property selected ("location", "uri").
     *              N.B. = "location" property can contain both filesystem location an URI location.
     * </pre>
     *
     * @return New CsvMapReader object.
     */
    public CsvMapReader createCsvReader() {
        final UrlValidator urlValidator = new UrlValidator(CUSTOM_SCHEME);
        if (location == null) {
            return loadFromUri(uri, dataProviderDelimiter);
        } else {
            if (urlValidator.isValid(location)) {
                return loadFromUri(location, dataProviderDelimiter);
            } else {
                return loadFromLocation(location, dataProviderDelimiter);
            }
        }
    }

    private CsvMapReader loadFromUri(final String uri, final String dataProviderDelimiter) {
        Exception exception = new Exception();
        for (int i = 0; i < RETRY_CNT; i++) {
            try {
                final URL url = new URL(uri);
                final InputStream stream = url.openStream();
                return loadCsv(new InputStreamReader(stream), dataProviderDelimiter);
            } catch (final Exception e) {
                exception = e;
                LOGGER.error("Exception occurred loading CSV file from URI: {}, Retry number: {} ", uri, RETRY_CNT);
            }
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                LOGGER.trace("Sleep interrupt exception: " + e);
            }
        }
        throw Throwables.propagate(exception);
    }

    /**
     * <pre>
     * Name: loadFromLocation()
     * Description: This protected function is used to get CSV file from filesystem. It get CSV file location and seach it in filesistem from
     * selected mount point.
     * </pre>
     *
     * @param location
     *         CSV file location (in classpath or absolute)
     * @param dataProviderDelimiter
     *         Delimiter used in file (default is ',')
     *
     * @return CSV file Reader.
     */
    @VisibleForTesting
    protected CsvMapReader loadFromLocation(String location, final String dataProviderDelimiter) {
        location = getOsSpecificPath(location);
        final String searchTerm = getSearchTerm(location);
        // Searching file in Class Path (from current location)
        String path = InternalFileFinder.findFile(searchTerm, "");
        if (path == null) {
            // Get File from absolute Path and check if it exist.
            File fileToRead = new File(location);
            if(fileToRead.exists() && fileToRead.isFile()) {
                path = location;
            } else {
                final String msg = String.format("Can't open CSV file [%s], because does not exist.", location);
                throw new RuntimeException("File '" + location + "' does not exist in filesystem!");
            }
        }
        try {
            return loadCsv(new FileReader(path), dataProviderDelimiter);
        } catch (final FileNotFoundException e) {
            final String msg = String.format("Can't open CSV file [%s], because it is not found", location);
            throw new RuntimeException(e);
        }
    }

    private String getOsSpecificPath(final String location) {
        return Paths.get(location).toString();
    }

    private CsvMapReader loadCsv(final Reader reader, final String dataProviderDelimiter) {
        final CsvPreference csvPreference = determineCsvPreference(dataProviderDelimiter);
        return new CsvMapReaderWrapper(reader, csvPreference);
    }

    private CsvPreference determineCsvPreference(final String dataProviderDelimiter) {
        final String commentDelimiter = DataHandler.getConfiguration().getProperty(COMMENT_DELIMITER_PROPERTY, "#");
        final CommentStartsWith commentStartsWith = new CommentStartsWith(commentDelimiter);
        if (!Strings.isNullOrEmpty(dataProviderDelimiter)) {
            return new CsvPreference.Builder('"', (int) dataProviderDelimiter.charAt(0), "\n").skipComments(commentStartsWith).build();
        } else if (DataHandler.getAttribute(CsvDataSource.DELIMITER_ATTRIBUTE) != null && !DataHandler.getAttribute(CsvDataSource.DELIMITER_ATTRIBUTE)
                .equals("")) {
            final String delimiter = DataHandler.getAttribute(CsvDataSource.DELIMITER_ATTRIBUTE).toString();
            return new CsvPreference.Builder('"', (int) delimiter.charAt(0), "\n").skipComments(commentStartsWith).build();
        } else {
            return new CsvPreference.Builder('"', ',', "\n").skipComments(commentStartsWith).build();
        }
    }

    @Override
    public String toString() {
        return "CsvReaderFactory{" + "uri='" + uri + '\'' + ", location='" + location + '\'' + '}';
    }
}
