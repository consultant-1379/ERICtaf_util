package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSourceProvider;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 *
 */
@API(Internal)
public final class InitialTestContext implements TestContext {

    private static final int DEFAULT_VUSER = 1;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, Object> operators = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, TestDataSource<DataRecord>> dataSources;

    private int vUser;

    public InitialTestContext() {
        this(DEFAULT_VUSER);
    }

    public InitialTestContext(int vuser) {
        this(Optional.<InitialTestContext>absent(), vuser);
    }

    InitialTestContext(Optional<InitialTestContext> parentContext, int vUser) {
        if (parentContext.isPresent()) {
            dataSources = parentContext.get().dataSources;
            attributes.putAll(parentContext.get().getAllAttributes());
        } else {
            dataSources = new ConcurrentHashMap<>(32, 0.75f, 1);
        }
        this.vUser = vUser;
    }

    @Override
    public <T> T getOperator(String key) {
        return (T) operators.get(key);
    }

    @Override
    public void setOperator(String key, Object value) {
        operators.put(key, value);
    }

    @Override
    public int getVUser() {
        return vUser;
    }

    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    @Override
    public Map<String, Object> getAllAttributes() {
        return Maps.newHashMap(attributes);
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public <T extends DataRecord> TestDataSource<T> dataSource(String name, Class<T> dataType) {
        if (!doesDataSourceExist(name)) {
            TestDataSource dataSource = TestDataSourceFactory.createDataSource();
            dataSource = TafDataSourceProvider.makeDataSourceTyped(dataSource, dataType);
            dataSources.putIfAbsent(name, dataSource);
        }
        return (TestDataSource<T>) dataSources.get(name);
    }

    @Override
    public TestDataSource<DataRecord> dataSource(String name) {
        return dataSource(name, DataRecord.class);
    }

    @Override
    public Map<String, TestDataSource<DataRecord>> getAllDataSources() {
        return Collections.unmodifiableMap(dataSources);
    }

    @Override
    public boolean doesDataSourceExist(String dataSourceName) {
        return dataSources.containsKey(dataSourceName);
    }

    @Override
    public void removeDataSource(String dataSourceName) {
        dataSources.remove(dataSourceName);
    }

    @Override
    public TestContext createContextForVUser(int vUser) {
        return new InitialTestContext(Optional.of(this), vUser);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addDataSource(String dataSourceName, TestDataSource dataSource) {
        Preconditions.checkNotNull(dataSourceName);
        dataSources.put(dataSourceName, dataSource);
    }

    @Override
    public void clearAttributes() {
        attributes.clear();
    }
}
