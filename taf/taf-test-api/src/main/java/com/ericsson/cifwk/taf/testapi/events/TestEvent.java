package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Basic interface of all events sent by TAF Test API during test execution.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/08/2016
 */
@API(Internal)
public interface TestEvent {

    enum ExecutionPhase {
        START,
        FINISH
    }

    enum ExecutionState {
        STARTED,
        SKIPPED,
        SUCCEEDED,
        FAILED,
        NOT_STARTED
    }

}
