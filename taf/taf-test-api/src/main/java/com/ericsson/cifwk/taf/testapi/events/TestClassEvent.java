package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Sent when test case class (class that has test methods annotated as <code>@Test</code>) is started
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/09/2016
 */
@API(Internal)
public class TestClassEvent implements ContextAwareTestEvent {

    private final TestExecutionContext testExecutionContext;
    private final ExecutionPhase executionPhase;

    public TestClassEvent(TestExecutionContext testExecutionContext, ExecutionPhase executionPhase) {
        this.testExecutionContext = testExecutionContext;
        this.executionPhase = executionPhase;
    }

    @Override
    public TestExecutionContext getTestExecutionContext() {
        return testExecutionContext;
    }

    public ExecutionPhase getExecutionState() {
        return executionPhase;
    }

}
