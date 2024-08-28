package com.ericsson.cifwk.taf.testrunner;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.management.TafBootstrap;
import com.ericsson.cifwk.taf.testng.TafTestRunnerFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.testng.ITestNGListener;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 12/12/2016
 */
@API(Internal)
public class TafTestNgTestSession implements TafTestSession {

    private static final int THREAD_POOL_SIZE = 10;
    private static final String HAVE_TO_HAVE_TESTS_MSG = "Should define either test suites or test classes to run";

    private TafBootstrap tafBootstrap;
    private RelaxedTestNG testEngine;

    @Override
    public void init(TafTestSessionOptions options) {
        tafBootstrap = initTafBootstrap();
        testEngine = createTestNGInstance();
        testEngine.setSuiteThreadPoolSize(THREAD_POOL_SIZE);

        List<Object> listeners = options.getTestEventListeners();
        if (!listeners.isEmpty()) {
            addListeners(listeners);
        }

        loadPlugins();

        List<Class> testClasses = options.getTestClasses();
        List<String> testGroupDefinitions = options.getTestGroupDefinitions();
        Preconditions.checkArgument(!testClasses.isEmpty() || !testGroupDefinitions.isEmpty(), HAVE_TO_HAVE_TESTS_MSG);

        if (!testClasses.isEmpty()) {
            testEngine.setTestClasses(Iterables.toArray(testClasses, Class.class));
            // If test classes are set explicitly, run only them
            return;
        }

        if (!testGroupDefinitions.isEmpty()) {
            testEngine.setTestSuites(testGroupDefinitions);
        }

        List<String> limitedTestGroups = options.getTestTags();
        if (!limitedTestGroups.isEmpty()) {
            testEngine.setGroups(Joiner.on(",").join(limitedTestGroups));
        }

    }

    @VisibleForTesting
    TafBootstrap initTafBootstrap() {
        return new TafBootstrap();
    }

    @VisibleForTesting
    void addListeners(List<Object> listeners) {
        TestEventBus testEventBus = getTestEventBus();
        for (Object listener : listeners) {
            if (listener instanceof ITestNGListener) {
                testEngine.addListener((ITestNGListener)listener);
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
        testEngine.run();
    }

    @Override
    public void shutdown() {
        tafBootstrap.shutdown();
    }

    @VisibleForTesting
    RelaxedTestNG createTestNGInstance() {
        RelaxedTestNG relaxedTestNG = new RelaxedTestNG();
        relaxedTestNG.setTestRunnerFactory(new TafTestRunnerFactory());
        return relaxedTestNG;
    }

    static class RelaxedTestNG extends TestNG {
        @Override
        protected void setTestRunnerFactory(ITestRunnerFactory factory) {
            super.setTestRunnerFactory(factory);
        }
    }

}
