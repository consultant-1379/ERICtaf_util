package com.ericsson.cifwk.taf.utils;

import com.ericsson.cifwk.meta.API;

import java.io.File;
import java.util.Comparator;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Sorts class path files so that Ericsson dependencies and folders are first.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 19.04.2016
 */
@API(Internal)
class ClassPathComparator implements Comparator<File> {

    @Override
    public int compare(File f1, File f2) {
        String ericsson = "ericsson";
        if (f1.getAbsolutePath().contains(ericsson)) {
            if (f2.getAbsolutePath().contains(ericsson)) {
                return 0;
            }
            return -1;
        }
        if (f2.getAbsolutePath().contains(ericsson)) {
            return 1;
        }

        if (f1.isDirectory()) {
            if (f2.isDirectory()) {
                return 0;
            }
            return -1;
        }
        if (f2.isDirectory()) {
            return 1;
        }

        return 0;
    }
}
