package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;

import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Test context for data persistence between test steps.
 * There are two types of test context - global and vuser specific.
 * Global context is copied to each vuser context. Vuser contexts are isolated from each other.
 */
@API(Stable)
public interface TestContext {

    /**
     * Returns the ID of the current vUser (1..N)
     * @return  the ID of the current vUser
     */
    int getVUser();

    /**
     * Sets the context attribute
     * @param key   attribute name
     * @param value attribute value
     */
    void setAttribute(String key, Object value);

    /**
     * Gets the value of the context attribute
     * @param key   attribute name
     * @param <T>   attribute class
     * @return  the value of the attribute or <code>null</code> of the attribute wasn't set
     */
    <T> T getAttribute(String key);

    /**
     * Returns a map of all attributes set in context
     * @return  all attributes set in context
     */
    Map<String, Object> getAllAttributes();

    /**
     * Removes the attribute from context
     * @param key   attribute name
     */
    void removeAttribute(String key);

    /**
     * Clears all attributes associated with this context
     */
    void clearAttributes();

    /**
     * Returns the instance of context data source. If data source with the provided name doesn't exist, it will be added
     * to the context and then returned.
     *
     * @param name data source name
     * @return  the instance of context data source with <code>dsName</code> name. If data source with such name wasn't
     * initiated before, an empty data source will be returned.
     */
    TestDataSource<DataRecord> dataSource(String name);

    /**
     * Returns a type safe instance of data source.
     *
     * @param name data source name
     * @param dataType data type class
     * @param <T> data type
     * @return data source
     */
    <T extends DataRecord> TestDataSource<T> dataSource(String name, Class<T> dataType);

    /**
     * Get a list of all registered data sources in given context.
     *
     * @return map of data source names to data source instances
     */
    Map<String, TestDataSource<DataRecord>> getAllDataSources();

    /**
     * Returns <code>true</code> if the data source with such name already exists in context; <code>false</code> otherwise.
     * @param dataSourceName    data source name
     * @return  <code>true</code> if the data source with such name already exists in context; <code>false</code> otherwise.
     */
    boolean doesDataSourceExist(String dataSourceName);

    /**
     * Removes the data source from the context.
     * @param dataSourceName    name of the data source to be removed
     */
    void removeDataSource(String dataSourceName);

    /**
     * Adds existing data source to the context.
     *
     * @param dataSourceName name of data source
     * @param dataSource data source
     */
    void addDataSource(String dataSourceName, TestDataSource<? extends DataRecord> dataSource);

    /**
     * Creates child context for given vUser
     * @param vUser
     * @return child context
     */
    TestContext createContextForVUser(int vUser);

    /**
     * Retrieve operator instance from context
     *
     * @param key operator name
     * @param <T> type of operator.
     * @return operator instance previously set with {@link #setOperator(String, Object)} method.
     */
    <T> T getOperator(String key);

    /**
     * Sets operator instance into context. Operator instance can be retrieved with {@link #getOperator(String)} method.
     *
     * @param key operator name
     * @param value operator instance
     */
    void setOperator(String key, Object value);
}
