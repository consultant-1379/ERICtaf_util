/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.impl;

import static com.ericsson.de.scenariorx.api.ScenarioContext.CONTEXT_RECORD_NAME;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.de.scenariorx.api.RxBasicDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecordWrapper;
import com.ericsson.de.scenariorx.api.ScenarioContext;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge between new and old context, to support existing operators on first migration stages.
 * Difference explained in more detail https://confluence-nam.lmera.ericsson.se/display/TAF/Context+and+Data+Sources
 * Will be deprecated after 1 migration phase.
 */
public class TafTestContextBridge implements TestContext {
    public static final String WARNING_VUSER = "getVUser() is not supported in Rx Scenarios! Value will always be `1`. Please remove vUser specific" +
            " logic. See https://confluence-nam.lmera.ericsson.se/display/TAF/Migration+Guide";
    public static final String WARNING_DATA_SOURCE = "Usage of context Data Sources will be deprecated in Rx Scenarios! " +
            "See https://confluence-nam.lmera.ericsson.se/display/TAF/Migration+Guide ";
    public static final String WARNING_ATTRIBUTES = "Usage of context attributes will be deprecated in Rx Scenarios! " +
            "See https://confluence-nam.lmera.ericsson.se/display/TAF/Migration+Guide ";

    private static final Logger logger = LoggerFactory.getLogger(TafTestContextBridge.class);

    private TestContext delegate;

    static TafTestContextBridge initOldContext(RxDataRecordWrapper dataRecord) {
        TafTestContextBridge oldContext = new TafTestContextBridge(dataRecord);

        TafTestContext.initialize(oldContext);

        return oldContext;
    }

    static Optional<Object> mergeOldContextAndReturnValue(TafTestContextBridge oldContext, Object result, String name) {
        TafTestContext.initialize(oldContext.delegate);

        HashMap<String, Object> mergedResult = Maps.newHashMap();

        mergedResult.putAll(oldContext.delegate.getAllAttributes());

        if (result != null) {
            if (result instanceof RxDataRecord) {
                mergedResult.putAll(RxDataRecord.class.cast(result).getAllFields());
            }

            mergedResult.put(name, result);
        }

        if (mergedResult.isEmpty()) {
            return Optional.absent();
        } else {
            Object dataRecord = RxBasicDataRecord.builder().setFields(mergedResult).build();
            return Optional.fromNullable(dataRecord);
        }
    }

    private TafTestContextBridge(RxDataRecordWrapper dataRecord) {
        delegate = TafTestContext.getContext().createContextForVUser(1);

        for (Map.Entry<String, Object> entry : getNewScenarioValues(dataRecord).entrySet()) {
            delegate.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Avoid initialize Context in Before Methods
     */
    private static Map<String, Object> getNewScenarioValues(RxDataRecordWrapper dataRecord) {
        if (dataRecord instanceof DataRecords.Forbidden) {
            return new HashMap<>();
        } else {
            ScenarioContext newScenarioContext = dataRecord.getFieldValue(CONTEXT_RECORD_NAME, ScenarioContext.class).get();
            return new HashMap<>(newScenarioContext.getAllFields());
        }
    }

    @Override
    public int getVUser() {
        logger.error(WARNING_VUSER + getCaller());
        return 1;
    }

    @Override
    public <T> T getAttribute(String key) {
        logger.warn(WARNING_ATTRIBUTES + getCaller());
        return delegate.getAttribute(key);
    }

    @Override
    public Map<String, Object> getAllAttributes() {
        logger.warn(WARNING_ATTRIBUTES + getCaller());
        return delegate.getAllAttributes();
    }

    @Override
    public void setAttribute(String key, Object value) {
        logger.warn(WARNING_ATTRIBUTES + getCaller());
        delegate.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        logger.warn(WARNING_ATTRIBUTES + getCaller());
        delegate.removeAttribute(key);
    }

    @Override
    public void clearAttributes() {
        logger.warn(WARNING_ATTRIBUTES + getCaller());
        delegate.clearAttributes();

    }

    @Override
    public TestDataSource<DataRecord> dataSource(String name) {
        return dataSource(name, DataRecord.class);
    }

    @Override
    public <T extends DataRecord> TestDataSource<T> dataSource(String name, Class<T> dataType) {
        return delegate.dataSource(name, dataType);
    }

    @Override
    public Map<String, TestDataSource<DataRecord>> getAllDataSources() {
        return delegate.getAllDataSources();
    }

    @Override
    public boolean doesDataSourceExist(String dataSourceName) {
        logger.warn(WARNING_DATA_SOURCE + getCaller());
        return delegate.doesDataSourceExist(dataSourceName);
    }

    @Override
    public void removeDataSource(String dataSourceName) {
        logger.warn(WARNING_DATA_SOURCE + getCaller());
        delegate.removeDataSource(dataSourceName);
    }

    @Override
    public void addDataSource(String dataSourceName, TestDataSource<? extends DataRecord> dataSource) {
        logger.warn(WARNING_DATA_SOURCE + getCaller());
        delegate.addDataSource(dataSourceName, dataSource);
    }

    @Override
    public TestContext createContextForVUser(int vUser) {
        return delegate.createContextForVUser(vUser);
    }

    @Override
    public <T> T getOperator(String key) {
        return delegate.getOperator(key);
    }

    @Override
    public void setOperator(String key, Object value) {
        delegate.setOperator(key, value);
    }

    private String getCaller() {
        return new Exception().getStackTrace()[3].toString();
    }
}
