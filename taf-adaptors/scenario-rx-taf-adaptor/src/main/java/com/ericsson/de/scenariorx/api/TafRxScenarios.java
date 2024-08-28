/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.api;

import static com.google.common.base.Preconditions.checkNotNull;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.de.scenariorx.impl.FlowBuilder;
import com.ericsson.de.scenariorx.impl.TafDataSource;
import com.ericsson.de.scenariorx.impl.TafTestStep;

/**
 * Entry point for all Taf Scenario Api.
 */
public class TafRxScenarios extends RxApi {

    /**
     * Create Definition of Data Source by Taf Name
     * Intended for (re)usage in {@link FlowBuilder#withDataSources}
     */
    public static <T extends DataRecord> RxDataSource<T> dataSource(final String tafName, Class<T> type) {
        return new TafDataSource.TafDataSourceProviderDefinition<>(tafName, type);
    }

    /**
     * Create Definition of Data Source
     * Intended for (re)usage in {@link FlowBuilder#withDataSources}
     */
    public static <T extends DataRecord> RxDataSource<T> dataSource(final String name, TestDataSource dataSource, Class<T> type) {
        return new TafDataSource.TafDataSourceBridge<>(name, dataSource, type);
    }

    /**
     * Creates a test step out of annotated method on provided object instance.
     * Method can contain any number of attributes annotated with @see{@link com.ericsson.cifwk.taf.annotations.Input}
     * so the framework would inject proper values at runtime.
     *
     * @param instance     object to invoke method on
     * @param testStepName test step id to locate
     * @return invocation object
     */
    @SuppressWarnings("WeakerAccess")
    public static RxTestStep annotatedMethod(Object instance, String testStepName) {
        checkNotNull(instance, "Object instance can't be null");
        checkNotNull(testStepName, "Test step name can't be null");
        return TafTestStep.getByName(instance, testStepName);
    }

    /**
     * Creates a builder for {@link RxCompositeExceptionHandler} which allows to call
     * multiple {@link RxExceptionHandler} in case when Exception is thrown, e.g.:
     * <pre>
     * .withExceptionHandler(
     *         compositeExceptionHandler()
     *                 .addExceptionHandler(handler1)
     *                 .addExceptionHandler(handler2)
     *                 .build()
     * )
     * </pre>
     *
     * @return builder
     */
    public static RxCompositeExceptionHandlerBuilder compositeExceptionHandler() {
        return new RxCompositeExceptionHandlerBuilder();
    }
}
