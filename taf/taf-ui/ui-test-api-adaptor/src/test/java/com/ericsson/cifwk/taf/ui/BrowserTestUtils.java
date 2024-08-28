package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import org.junit.Assert;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 19.04.2016
 */
public class BrowserTestUtils {

    public static String findHtmlPage(String fileName) {
        String foundFile = InternalFileFinder.findFile(fileName);
        if (foundFile == null) {
            Assert.fail("'" + fileName + "' not found!");
        }
        return "file:///" + foundFile;
    }

}
