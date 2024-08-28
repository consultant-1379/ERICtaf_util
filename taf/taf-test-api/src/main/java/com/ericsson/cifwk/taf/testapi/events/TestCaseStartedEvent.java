package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Event that is sent on every test case (method annotated with <code>@Test</code>) start.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/08/2016
 */
@API(Internal)
public class TestCaseStartedEvent extends TestCaseEvent {

    public TestCaseStartedEvent(TestMethodExecutionResult executionResult, TestExecutionContext executionContext) {
        super(executionResult, executionContext);
    }

}
