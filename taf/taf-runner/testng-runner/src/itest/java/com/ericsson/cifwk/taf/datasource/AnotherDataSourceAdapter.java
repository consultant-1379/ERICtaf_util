package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/05/2016
 */
public class AnotherDataSourceAdapter implements DataSourceAdapter<DataRecord> {

    public static final String SUPPORTED_DATA_SOURCE_NAME = "externalAdapterDS";
    private final TestDataSource<DataRecord> dataSource;

    public AnotherDataSourceAdapter() {
        Map<String, Object> data = Maps.newHashMap();
        data.put("netsimHostIP", "1.0.0.1");
        data.put("netsimHostName", "netsim1");
        dataSource = TestDataSourceFactory.createDataSource(data);
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
