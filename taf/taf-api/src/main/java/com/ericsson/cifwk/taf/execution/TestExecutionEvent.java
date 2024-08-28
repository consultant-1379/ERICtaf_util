/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.execution;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Event type that can be used to determine when to auto-close test resources
 * TODO: deprecate and introduce a better named alternative.
 */
@API(Stable)
public enum TestExecutionEvent {
    /**
     * each time a test finishes
     */
    ON_TEST_FINISH,

    /**
     * after the SuiteRunner has run all the test suites
     */
    ON_SUITE_FINISH,

    /**
     * on completion of execution
     */
    ON_EXECUTION_FINISH;
}
