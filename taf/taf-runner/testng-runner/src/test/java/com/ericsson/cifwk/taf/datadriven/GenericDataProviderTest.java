package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TafDataSourceAdapter;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class GenericDataProviderTest {

    @Test
    public void shouldGetMissingDataSourceMessage() throws Exception {
        Method testMethod = GenericDataProviderTest.class.getMethod("shouldGetMissingDataSourceMessage");
        String message = GenericDataProvider.getMissingDataSourceMessage(testMethod, "dsName", "filter",
                Lists.newArrayList(new TafDataSourceAdapter(), new MyDataSourceAdapter()));
        assertEquals("Failed to find an applicable data source for " +
                "{method='public void com.ericsson.cifwk.taf.datadriven.GenericDataProviderTest.shouldGetMissingDataSourceMessage() throws java.lang.Exception', " +
                "data source name='dsName', filter='filter'}. List of adapters that were tried to apply: [" +
                "com.ericsson.cifwk.taf.scenario.TafDataSourceAdapter," +
                "com.ericsson.cifwk.taf.datadriven.GenericDataProviderTest$MyDataSourceAdapter]", message);
    }

    private class MyDataSourceAdapter implements DataSourceAdapter {
        @Override
        public Optional<TestDataSource> provide(String name, @Nullable Method method, @Nullable DataSourceContext globalContext, Predicate predicate, Class dataRecordType) {
            return null;
        }
    }
}
