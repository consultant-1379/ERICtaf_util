package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestGroup;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Event that is sent on all test group execution phases.
 * Subscribe to it if you want to do some smart processing based on status, or subscribe to subclasses if it makes more sense in your case.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/08/2016
 */
@API(Internal)
public abstract class TestGroupEvent implements TestEvent {

    private final TestGroup testGroup;
    private final ExecutionPhase executionPhase;

    public TestGroupEvent(TestGroup testGroup, ExecutionPhase executionPhase) {
        this.testGroup = testGroup;
        this.executionPhase = executionPhase;
    }

    public TestGroup getTestGroup() {
        return testGroup;
    }

    public ExecutionPhase getExecutionPhase() {
        return executionPhase;
    }

}
