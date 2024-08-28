package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;

import java.io.Serializable;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Current test group execution result (test group can still be running).
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/12/2016
 */
@API(Internal)
public interface TestGroupResult extends Serializable {

    /**
     * @return current failed test count.
     */
    int getFailedTestCount();

    /**
     * @return <code>true</code> if none of test group tests failed so far.
     */
    boolean isSuccess();

}
