package com.ericsson.cifwk.taf.datasource;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Internal test data source factory
 */
public final class TestDataSourceFactory {

    private TestDataSourceFactory() {
    }

    public static TestDataSource<DataRecord> createDataSource(Map<String, Object>... data) {
        return createDataSource(Arrays.asList(data));
    }

    public static TestDataSource<DataRecord> createDataSource(Iterable<Map<String, Object>> data) {
        return new TestDataSourceImpl(data);
    }

    public static DataRecord createDataRecord(String dataSourceName, Map<String, ?> data) {
        return new DataRecordImpl(dataSourceName, data);
    }

    public static DataRecord createDataRecord(Map<String, ?> data) {
        return new DataRecordImpl(data);
    }

    public static DataRecord createDataRecord() {
        return new DataRecordImpl();
    }

    public static <T extends DataRecord> TestDataSource<T> fromServiceRegistry(String name, Class<T> dataRecordType) {
        List<DataSourceAdapter> allDataSourceAdapters = ServiceRegistry.getAllDataSourceAdapters();
        for (DataSourceAdapter dataSourceAdapter : allDataSourceAdapters) {
            Optional<TestDataSource<T>> optionalDataSource = dataSourceAdapter.provide(name, null, null, null, dataRecordType);
            if (optionalDataSource.isPresent()) {
              // ServiceRegistry.getTestContextProvider().get().addDataSource(name, optionalDataSource.get());
                return optionalDataSource.get();
            }
        }
        throw new IllegalArgumentException(getMissingDataSourceMessage(allDataSourceAdapters, name));
    }

    @VisibleForTesting
    static String getMissingDataSourceMessage(List<DataSourceAdapter> dataSourceAdapters, String name) {
        List<String> dataSourceClassNames = Lists.newArrayList(Iterables.transform(dataSourceAdapters, new Function<DataSourceAdapter, String>() {
            @Override
            public String apply(DataSourceAdapter adapter) {
                return adapter.getClass().getName();
            }
        }));
        String listOfAdapterClasses = Joiner.on(",").skipNulls().join(dataSourceClassNames);
        return format("Failed to find an applicable data source for name '%s'. List of adapters that were tried to apply: [%s]", name,
                listOfAdapterClasses);
    }
}
