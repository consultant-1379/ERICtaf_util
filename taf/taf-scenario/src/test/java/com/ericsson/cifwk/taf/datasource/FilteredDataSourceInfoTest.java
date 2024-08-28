package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.scenario.impl.datasource.FilteredDataSourceInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FilteredDataSourceInfoTest {

    public static final String USER = "user";

    /**
     * This TC is to check the size is zero if no such column exists in mentioned DS
     */
    @Test
    public void testGetFilteredDataSourceSize() throws Exception {
        int size = FilteredDataSourceInfo.getFilteredDataSourceSize(USER, "suites", "op1");
        assertEquals(0,size);

    }

}
