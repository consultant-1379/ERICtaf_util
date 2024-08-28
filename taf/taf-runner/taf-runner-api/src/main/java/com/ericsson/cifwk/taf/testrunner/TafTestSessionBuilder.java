package com.ericsson.cifwk.taf.testrunner;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.MultipleServiceBindingsException;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Utility class for building {@link TafTestSession} instances.
 * Relies on the implementation of {@link TafTestSession} to be available via SPI.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 12/12/2016
 */
@API(Stable)
public class TafTestSessionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafTestSessionBuilder.class);

    private final TafTestSessionOptions options = new TafTestSessionOptions();
    private final List<Object> testEventListeners = Lists.newArrayList();

    public TafTestSessionBuilder() {
    }

    /**
     * @see TafTestSessionOptions#setTestGroupDefinitions(List)
     * @param groupDefinitions
     * @return builder instance
     */
    public TafTestSessionBuilder withTestGroupDefinitions(List<String> groupDefinitions) {
        options.setTestGroupDefinitions(groupDefinitions);
        return this;
    }

    /**
     * @see TafTestSessionOptions#setTestClasses(List)
     * @param classes
     * @return builder instance
     */
    public TafTestSessionBuilder withTestClasses(Class[] classes) {
        options.setTestClasses(Arrays.asList(classes));
        return this;
    }

    /**
     * Add a lifecycle listener to test session. Call multiple times to add few listeners.
     * @param listener
     * @see TafTestSessionOptions#setTestEventListeners(List)
     * @return builder instance
     */
    public TafTestSessionBuilder withTestEventListener(Object listener) {
        testEventListeners.add(listener);
        return this;
    }

    /**
     * @see TafTestSessionOptions#setTestTags(List)
     * @param testTags
     * @return builder instance
     */
    public TafTestSessionBuilder withTestTags(List<String> testTags) {
        options.setTestTags(testTags);
        return this;
    }

    /**
     * <p>Builds and intializes instance of {@link TafTestSession} using the options set via <code>with*</code> methods.
     * After this call {@link TafTestSession#start()} to start test execution.</p>
     *<p>Loads the implementation of {@link TafTestSession} via SPI and will fail if no implementations
     * or few of them are available.</p>
     * @return TAF test session
     */
    public TafTestSession build() {
        TafTestSession tafTestSession = getNewInstance();
        options.setTestEventListeners(testEventListeners);
        tafTestSession.init(options);
        return tafTestSession;
    }

    @VisibleForTesting
    protected TafTestSession getNewInstance() {
        try {
            return ServiceRegistry.getUniqueServiceInstance(TafTestSession.class);
        } catch (MultipleServiceBindingsException e) {
            LOGGER.error("Only one TAF runtime engine is supported per single execution", e);
            throw new RuntimeException(e);
        }
    }
}
