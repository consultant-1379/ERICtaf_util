package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.scenario.TafDataSourceAdapter;
import com.ericsson.cifwk.taf.scenario.datasources.ExternalDataSourceAdapter;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TafDataSourceDefinitionTest {

    @Test
    public void testGetMissingDataSourceMessage() throws Exception {
        String message = TestDataSourceFactory.getMissingDataSourceMessage(new ArrayList<DataSourceAdapter>(Lists.newArrayList(new ExternalDataSourceAdapter(), new TafDataSourceAdapter())), "dsName");
        assertEquals("Failed to find an applicable data source for name 'dsName'. " +
                "List of adapters that were tried to apply: [" +
                "com.ericsson.cifwk.taf.scenario.datasources.ExternalDataSourceAdapter," +
                "com.ericsson.cifwk.taf.scenario.TafDataSourceAdapter]", message);
    }
}