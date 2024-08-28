package com.ericsson.cifwk.taf.testrunner;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * The representation of TAF test execution.
 * The implementation is loaded via SPI by {@link TafSurefireProvider} to run tests.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 12/12/2016
 */
@API(Stable)
public interface TafTestSession {

    /**
     * Method for preparing the test session for start. Should start all found TAF plugins.
     * @param options details of tests to be run
     */
    void init(TafTestSessionOptions options);

    /**
     * Starts TAF test execution.
     */
    void start();

    /**
     * Cleans up the resources, should shut down all found TAF plugins.
     */
    void shutdown();

}
