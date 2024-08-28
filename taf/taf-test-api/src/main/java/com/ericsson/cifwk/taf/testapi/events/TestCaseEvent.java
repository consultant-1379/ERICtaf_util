package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Event that is sent on every test case (method annotated with <code>@Test</code>) execution state - start, failure, skip, success.
 * Subscribe to it if you want to do some smart processing based on status, or subscribe to subclasses if it makes more sense in your case.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/08/2016
 */
@API(Internal)
public abstract class TestCaseEvent extends AbstractTestMethodEvent {

    public TestCaseEvent(TestMethodExecutionResult executionResult, TestExecutionContext executionContext) {
        super(executionResult, executionContext);
    }

}
