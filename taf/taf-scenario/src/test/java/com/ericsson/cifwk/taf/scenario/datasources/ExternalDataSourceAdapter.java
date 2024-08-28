package com.ericsson.cifwk.taf.scenario.datasources;

import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/05/2016
 */
public class ExternalDataSourceAdapter implements DataSourceAdapter<DataRecord> {

    public static final String SUPPORTED_DATA_SOURCE_NAME = "externalAdapterDS";
    private final TestDataSource<DataRecord> dataSource;

    public ExternalDataSourceAdapter() {
        dataSource = TestDataSourceFactory.createDataSource(
                item(1, 1, 2),
                item(2, 1, 3),
                item(-1, 1, 0));
    }

    private HashMap<String, Object> item(int var1, int var2, int sum) {
        HashMap<String, Object> items = new HashMap<>();
        items.put("var1", var1);
        items.put("var2", var2);
        items.put("sum", sum);
        return items;
    }

    @Override
    public Optional<TestDataSource<DataRecord>> provide(String name, @Nullable Method method,
                                                        @Nullable DataSourceContext globalContext,
                                                        Predicate<? super DataRecord> predicate, Class<DataRecord> dataRecordType) {
        return Optional.fromNullable(getReadyDataSource(name));
    }

    private TestDataSource<DataRecord> getReadyDataSource(String dataSourceName) {
        return (SUPPORTED_DATA_SOURCE_NAME.equals(dataSourceName)) ? dataSource : null;
    }

}
