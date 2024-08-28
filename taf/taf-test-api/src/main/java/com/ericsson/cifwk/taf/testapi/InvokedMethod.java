package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Invoked test method data holder
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 20/06/2016
 */
@API(Internal)
public interface InvokedMethod {

    /**
     * @return true if this method is a test method
     */
    boolean isTestMethod();

    /**
     * @return true if this method is a configuration method (@BeforeXXX or @AfterXXX)
     */
    boolean isConfigurationMethod();

    /**
     * @return the test method
     */
    TestMethod getTestMethod();

    /**
     * @return current result of test execution
     */
    TestMethodExecutionResult getTestMethodExecutionResult();

    /**
     * @return timestamp of when this method was invoked (timestamp of the start).
     */
    long getInvocationTimestamp();

}
