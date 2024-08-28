package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataSourceContext;
import com.ericsson.cifwk.taf.datasource.TestDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@API(Internal)
final class BasicDataSourceContext implements DataSourceContext {

    private final Map<String, TestDataSource> dataSources = new ConcurrentHashMap<>();

    @Override
    public Map<String, TestDataSource> getDataSources() {
        return dataSources;
    }

    @Override
    public void setDataSources(Map<String, TestDataSource> dataSources) {
        dataSources.clear();
        dataSources.putAll(dataSources);
    }

}
