package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Test execution data holder, contains information about test runtime.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/06/2016
 */
@API(Internal)
public interface TestExecutionContext {

    /**
     * Returns the associated test group.
     * @return associated test group
     */
    TestGroup getTestGroup();

}
