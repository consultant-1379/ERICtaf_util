package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Current method case execution result (test can be still in progress).
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/06/2016
 */
@API(Internal)
public interface TestMethodExecutionResult {

    /**
     * @return <code>true</code> if test method succeeded, <code>false</code> otherwise.
     */
    boolean isSuccess();

    /**
     * @return current execution state
     */
    TestEvent.ExecutionState getExecutionState();

    /**
     * @return the test method which the current result corresponds to.
     */
    TestMethod getTestMethod();

    /**
     * @return The throwable that was thrown while running the
     * method, or null if no exception was thrown.
     */
    Throwable getThrowable();

}
