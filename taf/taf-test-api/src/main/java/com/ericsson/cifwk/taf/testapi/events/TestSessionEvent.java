package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * <p>Event bound to the overall test execution (includes all suites and tests) scope.</p>
 * <p>Sent when the overall (global) test execution kicks off or finishes.</p>
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/08/2016
 */
@API(Internal)
public abstract class TestSessionEvent implements TestEvent {

    private final ExecutionPhase executionPhase;

    public TestSessionEvent(ExecutionPhase executionPhase) {
        this.executionPhase = executionPhase;
    }

    public ExecutionPhase getExecutionPhase() {
        return executionPhase;
    }

}
