package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Test event that has information about test context.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 20/09/2016
 */
@API(Internal)
public interface ContextAwareTestEvent extends TestEvent {

    /**
     * @return current execution context.
     */
    TestExecutionContext getTestExecutionContext();

}
