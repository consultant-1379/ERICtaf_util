package com.ericsson.cifwk.taf.osgi.agent;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public final class TestUtils {

    private TestUtils() {
    }

    public static String readResource(String path) throws IOException {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(path);
        return new Scanner(in).useDelimiter("\\A").next();
    }

}
