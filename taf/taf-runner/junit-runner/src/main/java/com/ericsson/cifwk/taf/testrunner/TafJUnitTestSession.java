package com.ericsson.cifwk.taf.testrunner;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.management.TafBootstrap;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 30/01/2017
 */
@API(Internal)
public class TafJUnitTestSession implements TafTestSession {

    private static final String HAVE_TO_HAVE_TESTS_MSG = "Should define either test suites or test classes to run";

    private TafBootstrap tafBootstrap;

    private JUnitCore testEngine;

    private Class[] testClasses = new Class[]{};

    @Override
    public void init(TafTestSessionOptions options) {
        tafBootstrap = initTafBootstrap();
        testEngine = createJUnitInstance();

        List<Object> listeners = options.getTestEventListeners();
        if (!listeners.isEmpty()) {
            addListeners(listeners);
        }

        loadPlugins();

        List<Class> testClasses = options.getTestClasses();
        checkArgument(!testClasses.isEmpty(), HAVE_TO_HAVE_TESTS_MSG);

        this.testClasses = Iterables.toArray(testClasses, Class.class);
    }

    private JUnitCore createJUnitInstance() {
        return new JUnitCore();
    }

    @VisibleForTesting
    TafBootstrap initTafBootstrap() {
        return new TafBootstrap();
    }

    @VisibleForTesting
    void addListeners(List<Object> listeners) {
        TestEventBus testEventBus = getTestEventBus();
        for (Object listener : listeners) {
            if (listener instanceof RunListener) {
                testEngine.addListener((RunListener) listener);
            } else {
                testEventBus.register(listener);
            }
        }
    }

    @VisibleForTesting
    TestEventBus getTestEventBus() {
        return ServiceRegistry.getTestEventBus();
    }

    private void loadPlugins() {
        tafBootstrap.init();
    }

    @Override
    public void start() {
        testEngine.run(testClasses);
    }

    @Override
    public void shutdown() {
        tafBootstrap.shutdown();
    }

}
