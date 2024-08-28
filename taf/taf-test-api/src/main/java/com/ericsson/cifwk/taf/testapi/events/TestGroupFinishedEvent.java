package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestGroup;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Event that is sent on test group finish.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/11/2016
 */
@API(Internal)
public class TestGroupFinishedEvent extends TestGroupEvent {

    public TestGroupFinishedEvent(TestGroup testGroup) {
        super(testGroup, ExecutionPhase.FINISH);
    }

}
