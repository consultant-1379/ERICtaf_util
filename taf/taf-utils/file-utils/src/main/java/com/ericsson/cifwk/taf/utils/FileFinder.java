package com.ericsson.cifwk.taf.utils;

import com.ericsson.cifwk.taf.utils.CollectionUtils.Java7Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.ericsson.cifwk.taf.utils.CollectionUtils.filter;
import static com.ericsson.cifwk.taf.utils.CollectionUtils.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * This class is used to search for a filename/pattern specified
 *
 * @author erobemu, ethomev
 */
public class FileFinder extends ArchiveFileFinder {

    public static final String SKIP_CLASSPATH = "taf.skip.classpath";

    public static final String SKIP_ARCHIVES = "taf.skip.archives";

    public static final String JAVA_CLASS_PATH = "java.class.path";

    public static final String PATH_SEPARATOR = "path.separator";

    protected static final String COBERTURA = "cobertura";

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFinder.class);

    private static Map<String, List<String>> resultCache = new LinkedHashMap<>();

    /**
     * Get all locations to be searched starting with initial directory and classpath
     */
    protected static List<File> gatherLocationsToSearch(String initialLocation) {
        List<File> result;
        if (initialLocation != null && !initialLocation.isEmpty() && new File(initialLocation).exists()) {
            result = new ArrayList<>(singletonList(new File(initialLocation)));
        } else {
            result = new ArrayList<>(singletonList(new File(FileUtils.getCurrentDir())));
        }

        if (!System.getProperties().containsKey(SKIP_CLASSPATH)) {
            result.addAll(getClassLoaderAsList());
            Collections.sort(result, new ClassPathComparator());
        }
        if (LOGGER.isTraceEnabled()) LOGGER.trace("Locations to search " + String.valueOf(result));

        return result;
    }

    private static List<File> getClassLoaderAsList() {
        List<File> result = new ArrayList<>();
        ClassLoader classLoader = FileFinder.class.getClassLoader();
        URL[] urls = ((URLClassLoader) classLoader).getURLs();

        result.addAll(transform(asList(urls), new Java7Function<URL, File>() {
            @Override
            public File apply(URL s) {
                return new File(s.getFile());
            }
        }));

        return result;
    }

    /**
     * Method performs recursive search on filesystem
     *
     * @param searchTerm      - last part of the file path
     * @param initialLocation - initial location for recursive search
     */
    public static List<String> findFileInFileSystem(final String searchTerm, String startPoint, final String initialLocation) {
        final List<String> result = new ArrayList<>();
        try {
            eachFileRecurse(new File(startPoint), new Java7Function<File, Object>() {
                @Override
                public Object apply(File file) {
                    if (LOGGER.isTraceEnabled())
                        LOGGER.trace("Searching " + String.valueOf(file) + " for " + searchTerm);
                    if (ArchiveFileFinder.isFound(initialLocation, searchTerm, file.getAbsolutePath())) {
                        result.add(file.getAbsolutePath());
                    } else if (ArchiveFileFinder.isArchive(file.getAbsolutePath()) && (!System.getProperties().containsKey(SKIP_ARCHIVES))) {
                        result.addAll(ArchiveFileFinder.findFileInJarFile(file, searchTerm, initialLocation));
                    }
                    return null;
                }
            });
        } catch (FileNotFoundException e) {
            throw ThrowableUtils.propagate(e);
        }
        return result;
    }

    protected static void eachFileRecurse(File self, Java7Function<File, Object> closure) throws FileNotFoundException, IllegalArgumentException {
        checkDirectory(self);
        final File[] files = self.listFiles();
        // null check because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4803836
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                closure.apply(file);
                eachFileRecurse(file, closure);
            } else {
                closure.apply(file);
            }
        }
    }

    protected static void checkDirectory(File directory) throws FileNotFoundException, IllegalArgumentException {
        if (!directory.exists()) {
            throw new FileNotFoundException(directory.getAbsolutePath());
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The provided File object is not a directory: " + directory.getAbsolutePath());
        }
    }

    protected static String getResultCacheKey(final String searchTerm, final String initialLocation) {
        return searchTerm + initialLocation;
    }

    /**
     * Execute combined search using searching in filesystem and inside all the jars on classpath or inside current directory
     *
     * @param searchTerm      - last part of the file path
     * @param initialLocation - location on filesystem to start the search - default to current directory
     * @return list of absolute paths of found files
     */
    public static synchronized List<String> findFile(String searchTerm, final String initialLocation) {
        if (LOGGER.isTraceEnabled()) LOGGER.trace("Looking for " + searchTerm + " starting in " + initialLocation);
        List<String> results = (resultCache.get(getResultCacheKey(searchTerm, initialLocation)));
        if (results != null) {
            if (LOGGER.isTraceEnabled()) LOGGER.trace("Returning cached result");
            return results;
        } else {
            results = new CopyOnWriteArrayList<>();
        }

        if (LOGGER.isTraceEnabled()) LOGGER.trace("Searching for " + searchTerm + " starting in " + initialLocation);


        final List<File> locationsToSearch = gatherLocationsToSearch(initialLocation);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (File elem : locationsToSearch) {
            tasks.add(new FindAndAddTask(searchTerm, initialLocation, results, elem));
        }

        for (Callable<Void> task : tasks) {
            try {
                task.call();
            } catch (Exception e) {
                throw ThrowableUtils.propagate(e);
            }
        }

        results = postProcess(results);
        resultCache.put(getResultCacheKey(searchTerm, initialLocation), results);
        return results;
    }

    /**
     * Execute combined search using searching in filesystem and inside all the jars on classpath or inside current directory
     *
     * @param searchTerm - last part of the file name
     * @return list of absolute paths of found files
     */
    public static synchronized List<String> findFile(String searchTerm) {
        return FileFinder.findFile(searchTerm, "");
    }

    /**
     * Find class by last part of its name
     *
     * @return list of Class objects that can be loaded
     */
    public static List<Class> findClass(String searchTerm) {

        final List<Class> result = new ArrayList<>();
        if (!searchTerm.endsWith(".class")) {
            searchTerm = searchTerm + ".class";
        }
        List<String> classFiles = findFile(searchTerm);
        if (LOGGER.isTraceEnabled()) LOGGER.trace("Found following class files {}", String.valueOf(classFiles));

        List<String> classNames = filter(transform(classFiles, new Java7Function<String, String>() {
            @Override
            public String apply(String s) {
                return stripFileNameToClassName(s);
            }
        }), new CollectionUtils.Java7Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s != null;
            }
        });

        Collection<String> uniqueClassNames = new LinkedHashSet<>(classNames);
        for (String className : uniqueClassNames) {
            try {
                LOGGER.trace("Trying to load class {}", className);
                result.add(Thread.currentThread().getContextClassLoader().loadClass(className));
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Loading class {} failed {}", className, String.valueOf(e));
            }
        }
        return result;
    }

    /**
     * Creates new list by removing duplicates and cobertura files
     */
    protected static List<String> postProcess(List<String> resultToPostprocess) {
        Collection<String> uniqueValues = new LinkedHashSet<>(resultToPostprocess);
        return CollectionUtils.filter(new ArrayList<>(uniqueValues), new CollectionUtils.Java7Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s == null || !s.contains(COBERTURA);
            }
        });
    }

    protected static class FindAndAddTask implements Callable<Void> {

        private String searchTerm;

        private String initialLocation;

        private List<String> results;

        private File location;

        public FindAndAddTask(String searchTerm, String initialLocation, List<String> results, File location) {
            this.searchTerm = searchTerm;
            this.initialLocation = initialLocation;
            this.results = results;
            this.location = location;
        }

        @Override
        public Void call() throws Exception {
            try {
                if (ArchiveFileFinder.isFound(initialLocation, searchTerm, location.getAbsolutePath())) {
                    results.add(location.getAbsolutePath());
                } else if (location.isDirectory()) {
                    results.addAll(findFileInFileSystem(searchTerm, location.getAbsolutePath(), initialLocation));
                } else if (ArchiveFileFinder.isArchive(location.getAbsolutePath()) && (!System.getProperties().containsKey(SKIP_ARCHIVES))) {
                    results.addAll(ArchiveFileFinder.findFileInJarFile(location, searchTerm, initialLocation));
                }
            } catch (Exception e) {
                LOGGER.warn("Searching in location " + String.valueOf(location) + " failed " + String.valueOf(e), e);
            }
            return null;
        }
    }
}
