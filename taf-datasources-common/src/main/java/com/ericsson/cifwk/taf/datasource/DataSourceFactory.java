package com.ericsson.cifwk.taf.datasource;

/**
 * Created by egergle on 15/03/2018.
 */
public interface DataSourceFactory {
    TestDataSource<DataRecord> createDataSource(String type, ConfigurationSource reader) throws UnknownDataSourceTypeException;
}
