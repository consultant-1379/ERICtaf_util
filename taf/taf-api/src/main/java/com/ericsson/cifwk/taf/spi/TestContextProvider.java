package com.ericsson.cifwk.taf.spi;

import com.ericsson.cifwk.taf.TestContext;

/**
 * SPI interface
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/03/2016
 */
public interface TestContextProvider {

    /**
     * Provide current thread context
     *
     * @return a new {@code TestContext} for current thread
     */
    TestContext get();

    /**
     * Replaces current thread context with existing object.
     *
     * @param testContext testContext to set
     */
    void initialize(final TestContext testContext);

    /**
     * Check whether context exists and is initialized for current thread
     * @return
     */
    boolean isContextInitialized();

    /**
     * Removes current thread context.
     * After this method call {@link TestContextProvider#isContextInitialized()} should return {@code false}
     */
    void removeContext();

    /**
     * Initializes the context for defined vUser
     * @param vUser vUser ID
     */
    void initialize(int vUser);
}
