package com.ericsson.cifwk.taf.testapi.events.utils;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionStartedEvent;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/11/2016
 */
@API(Internal)
public class TestSessionEventFactory {

    public static TestSessionEvent create(TestEvent.ExecutionPhase executionPhase) {
        switch (executionPhase) {
            case START:
                return new TestSessionStartedEvent();
            case FINISH:
                return new TestSessionFinishedEvent();
            default:
                throw new UnsupportedOperationException(String.format("Unknown execution phase %s", executionPhase));
        }
    }

}
