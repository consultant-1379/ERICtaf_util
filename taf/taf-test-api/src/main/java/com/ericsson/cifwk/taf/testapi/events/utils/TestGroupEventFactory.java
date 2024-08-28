package com.ericsson.cifwk.taf.testapi.events.utils;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupStartedEvent;
import com.google.common.base.Preconditions;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/11/2016
 */
@API(Internal)
public class TestGroupEventFactory {

    public static TestGroupEvent create(TestGroup testGroup, TestEvent.ExecutionPhase executionPhase) {
        Preconditions.checkArgument(testGroup != null, "Test group is undefined");
        Preconditions.checkArgument(executionPhase != null, "Execution phase is undefined");

        switch (executionPhase) {
            case START:
                return new TestGroupStartedEvent(testGroup);
            case FINISH:
                return new TestGroupFinishedEvent(testGroup);
            default:
                throw new UnsupportedOperationException(String.format("Unknown execution phase %s", executionPhase));
        }
    }

}
