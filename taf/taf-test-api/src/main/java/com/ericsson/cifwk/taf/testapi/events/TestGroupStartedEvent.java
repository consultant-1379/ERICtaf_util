package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestGroup;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Event that is sent on test group start.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/11/2016
 */
@API(Internal)
public class TestGroupStartedEvent extends TestGroupEvent {

    public TestGroupStartedEvent(TestGroup testGroup) {
        super(testGroup, ExecutionPhase.START);
    }

}
