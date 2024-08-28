package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.management.TafContext;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

public class TestExecutionHelper {

    private TestExecutionHelper(){
        throw new IllegalAccessError("TestExecutionHelper class");
    }

    @API(Deprecated)
    @API.Since(2.31)
    @Deprecated
    public static String getCurrentOperatorMethod()
            throws NoSuchMethodException {
        return "unknown";
    }

    @API(Deprecated)
    @API.Since(2.31)
    @Deprecated
    public static String getCurrentOperatorMethod(StackTraceElement[] stackTrace)
            throws NoSuchMethodException {
        return "unknown";
    }

    /**
     * Get test method currently executed in current thread
     */
    @API(Deprecated)
    @API.Since(2.31)
    @Deprecated
    public static String getCurrentTestMethod() throws NoSuchMethodException {
        return null;
    }

    /**
     * Get number of current VUsers determined from DataHandler
     */
    @API(Deprecated)
    @API.Since(2.31)
    @Deprecated
    public static int getCurrentVUsers() {
        Integer vUser = TafContext.getRuntimeAttributes().getAttribute(
                TafContext.VUSER);
        if (vUser == null) {
            vUser = TafContext.getParentRuntimeAttributes().getAttribute(
                    TafContext.VUSER);
        }
        return (vUser == null) ? 1 : vUser;
    }

    public static void setCurrentSuiteName(String currentSuiteName) {
        TafContext.getParentRuntimeAttributes().setAttribute(TafContext.SUITE_NAME, currentSuiteName);
    }

    public static String getCurrentSuiteName() {
        return TafContext.getParentRuntimeAttributes().getAttribute(TafContext.SUITE_NAME);
    }
}
