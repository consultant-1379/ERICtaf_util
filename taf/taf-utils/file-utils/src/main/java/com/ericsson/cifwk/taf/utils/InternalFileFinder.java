package com.ericsson.cifwk.taf.utils;

import com.ericsson.cifwk.meta.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.utils.FileFinder.SKIP_ARCHIVES;
import static com.ericsson.cifwk.taf.utils.FileFinder.gatherLocationsToSearch;
import static com.ericsson.cifwk.taf.utils.FileFinder.getResultCacheKey;

/**
 * This class is for internal TAF usage only and is NOT part of TAF API.
 *
 * It is used to find a file in a class path.
 *
 * @author emihvol
 */
@API(Internal)
public class InternalFileFinder {

    private static Logger logger = LoggerFactory.getLogger(InternalFileFinder.class);

    private static Map<String, String> resultCache = new LinkedHashMap<>();

    /**
     * Execute combined search using searching in filesystem and inside all the jars on classpath or inside current directory
     *
     * @param searchTerm      - last part of the file name
     * @param initialLocation - location on filesystem to start the search - default to current directory
     * @return absolute path of the first found file, or <code>null</code> if nothing is found.
     */
    public static synchronized String findFile(String searchTerm, final String initialLocation) {
        if (logger.isTraceEnabled()) logger.trace("Looking for " + searchTerm + " starting in " + initialLocation);
        String result = resultCache.get(getResultCacheKey(searchTerm, initialLocation));
        if (result != null) {
            if (logger.isTraceEnabled()) logger.trace("Returning cached result");
            return result;
        }

        if (logger.isTraceEnabled()) logger.trace("Searching for " + searchTerm + " starting in " + initialLocation);


        final List<File> locationsToSearch = gatherLocationsToSearch(initialLocation);

        List<Callable<String>> tasks = new ArrayList<>();
        for (File elem : locationsToSearch) {
            tasks.add(new FindAndAddTask(searchTerm, initialLocation, elem));
        }

        for (Callable<String> task : tasks) {
            try {
                result = task.call();
                if (result != null && !result.contains(FileFinder.COBERTURA)) {
                    break;
                }
            } catch (Exception e) {
                throw ThrowableUtils.propagate(e);
            }
        }

        resultCache.put(getResultCacheKey(searchTerm, initialLocation), result);
        return result;
    }

    /**
     * Execute combined search using searching in filesystem and inside all the jars on classpath or inside current directory
     *
     * @param searchTerm - last part of the file name
     * @return list of absolute paths of found files
     */
    public static synchronized String findFile(String searchTerm) {
        return findFile(searchTerm, "");
    }

    protected static class FindAndAddTask implements Callable<String> {

        private String searchTerm;

        private String initialLocation;

        private File location;

        public FindAndAddTask(String searchTerm, String initialLocation, File location) {
            this.searchTerm = searchTerm;
            this.initialLocation = initialLocation;
            this.location = location;
        }

        @Override
        public String call() throws Exception {
            List<String> results = new ArrayList<>();
            try {
                if (ArchiveFileFinder.isFound(initialLocation, searchTerm, location.getAbsolutePath())) {
                    return location.getAbsolutePath();
                } else if (location.isDirectory()) {
                    results = FileFinder.findFileInFileSystem(searchTerm, location.getAbsolutePath(), initialLocation);
                } else if (ArchiveFileFinder.isArchive(location.getAbsolutePath()) && (!System.getProperties().containsKey(SKIP_ARCHIVES))) {
                    results = ArchiveFileFinder.findFileInJarFile(location, searchTerm, initialLocation);
                }
            } catch (Exception e) {
                logger.warn("Searching in location " + String.valueOf(location) + " failed " + String.valueOf(e));
            }

            if (results.isEmpty()) {
                return null;
            }
            return results.iterator().next();
        }
    }
}
