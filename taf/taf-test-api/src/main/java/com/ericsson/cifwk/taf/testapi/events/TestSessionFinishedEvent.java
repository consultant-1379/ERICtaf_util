package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Event that is sent on test session (the main, overall test execution process) finish.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 05/12/2016
 */
@API(Internal)
public class TestSessionFinishedEvent extends TestSessionEvent {

    public TestSessionFinishedEvent() {
        super(ExecutionPhase.FINISH);
    }

}
