package com.ericsson.cifwk.taf.management;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Since;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a holder of all runtime data needed for Taf extensions.
 */
public final class TafContext {

    private static final Logger log = LoggerFactory.getLogger(TafContext.class);

    public static final String VUSER = "vuser";

    @Deprecated
    @Since(2.29)
    public static final String CONTEXT = "context";

    public static final String SUITE_NAME = "suiteName";
    public static final String USAGE = "usage";

    private static final Map<Method, List> methodCombinations = new ConcurrentHashMap<>();
    private static final Map<Method, Object[]> dataDrivenAttributes = new ConcurrentHashMap<>();

    private static final InheritableThreadLocal<TafExecutionAttributes> parentRuntimeAttributes = new InheritableThreadLocal<>();
    private static final ThreadLocal<TafExecutionAttributes> runtimeAttributes = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, TestDataSource<DataRecord>>> dataSources = new ThreadLocal<>();

    private TafContext() {
    }

    /**
     * 
     * @return map of method objects to list of execution combinations
     */
    public static Map<Method, List> getMethodAttributes() {
        return methodCombinations;
    }

    /**
     * 
     * @return map of method objects to the list of data driven attributes
     *         Object[0] refers to the contextShifter 
     *         Object[1] refers to the Data Driven Usage
     */
    public static Map<Method, Object[]> getDataDrivenAttributes() {
        return dataDrivenAttributes;
    }

    /**
     * Runtime attributes for current threads
     * @see TafExecutionAttributes
     * @return attributes container
     */
    public static TafExecutionAttributes getRuntimeAttributes() {
        TafExecutionAttributes attributes = runtimeAttributes.get();
        if (attributes == null) {
            attributes = new TafExecutionAttributes();
            runtimeAttributes.set(attributes);
        }
        return attributes;
    }

    /**
     * Runtime attributes to be inherited by child threads
     * @see TafExecutionAttributes
     * @return attributes container
     */
    public static TafExecutionAttributes getParentRuntimeAttributes(){
        TafExecutionAttributes attributes = parentRuntimeAttributes.get();
        if (attributes == null) {
            attributes = new TafExecutionAttributes();
            parentRuntimeAttributes.set(attributes);
        }
        return attributes;
    }

    /**
     * Data Sources (inherited by child threads)
     *
     * @return attributes container
     * @see TafExecutionAttributes
     */
    public static synchronized Map<String, TestDataSource<DataRecord>> getDataSources() {
        return Collections.unmodifiableMap(getDataSourcesInternal());
    }

    public static synchronized void setDataSource(String name, TestDataSource dataSource) {
        getDataSourcesInternal().put(name, dataSource);
    }

    private static synchronized Map<String, TestDataSource<DataRecord>> getDataSourcesInternal() {
        Map<String, TestDataSource<DataRecord>> attributes = dataSources.get();
        if (attributes == null) {
            attributes = Maps.newHashMap();
            dataSources.set(attributes);
            log.info("Setting context by thread: {}", Thread.currentThread().getId());
        }
        return attributes;
    }
}
