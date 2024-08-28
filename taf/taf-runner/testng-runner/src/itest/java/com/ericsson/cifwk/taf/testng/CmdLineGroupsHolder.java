package com.ericsson.cifwk.taf.testng;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 13/05/2016
 */
public class CmdLineGroupsHolder {

    private static final ThreadLocal<String> GROUPS = new ThreadLocal<String>();

    public static void setGroups(String value) {
        GROUPS.set(value);
    }

    public static String getGroups() {
        return GROUPS.get();
    }

}
