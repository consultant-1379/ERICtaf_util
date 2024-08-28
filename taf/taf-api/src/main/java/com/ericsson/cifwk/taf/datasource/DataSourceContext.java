package com.ericsson.cifwk.taf.datasource;

import java.util.Map;

/**
 * Global context containing global test data sources
 */
public interface DataSourceContext {

    /**
     * Get data sources in this context
     * @return map data source and it's name as a key
     */
    Map<String, TestDataSource> getDataSources();

    /**
     * Set data sources in context
     * @param dataSources to set in context
     */
    void setDataSources(Map<String, TestDataSource> dataSources);

}
