package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.pool.DataPoolStrategy;
import com.ericsson.cifwk.taf.management.TafRunnerContext;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Provides instance of TestDataSource based on configuration.
 */
@API(Internal)
public final class TafDataSourceFactory implements DataSourceFactory {

    public static final String TYPE_CSV = "csv";
    public static final String TYPE_CLASS = "class";

    @Deprecated
    public static TestDataSource<DataRecord> dataSourceOfType(String type,
                                                              ConfigurationSource reader,
                                                              DataPoolStrategy strategy) throws UnknownDataSourceTypeException {

        return TafDataSourceProvider.dataSourceOfType(new TafDataSourceFactory(), type, reader, strategy);
    }

    @Override
    public TestDataSource<DataRecord> createDataSource(String type, ConfigurationSource reader) throws UnknownDataSourceTypeException {
        TestDataSource<DataRecord> dataSource;
        if (TYPE_CSV.equalsIgnoreCase(type)) {
            dataSource = new CsvDataSource();
        } else if (TYPE_CLASS.equalsIgnoreCase(type)) {
            dataSource = new ClassDataSource();
        } else {
            throw new UnknownDataSourceTypeException(type);
        }

        dataSource.init(reader);
        TafRunnerContext context = TafRunnerContext.getContext();
        context.addCloseable(dataSource);
        return dataSource;
    }
}
