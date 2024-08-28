package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testng.CompositeTestNGListener;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import java.util.concurrent.Callable;

import static com.ericsson.cifwk.taf.testng.CompositeTestNGListener.*;
import static org.junit.Assert.*;

public class AllureTafPluginTest {

    private AllureTafPlugin plugin = new AllureTafPlugin();

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void disableKeySetToTrue() throws Exception {
        System.setProperty(AllureTafPlugin.DISABLE_ALLURE_PROPERTY, "true");
        assertFalse(plugin.shouldAddAllure());
    }

    @Test
    public void disableKeySet() throws Exception {
        System.setProperty(AllureTafPlugin.DISABLE_ALLURE_PROPERTY, "");
        assertFalse(plugin.shouldAddAllure());
    }

    @Test
    @Ignore
    /**
     * Tests which TestNG listeners are added,
     * checks that (patched / proper) version of TestNG is used (bug with double listeners).
     *
     * Test is disabled as affecting all the integration tests for Allure.
     */
    public void init() throws Exception {
        Listeners executionListeners = new Listeners(new ExecutionListeners());
        Listeners methodInterceptors = new Listeners(new MethodInterceptors());
        Listeners methodListeners = new Listeners(new MethodListeners());
        Listeners suiteListeners = new Listeners(new SuiteListeners());
        Listeners testListeners = new Listeners(new TestListeners());
        Listeners configurationListeners = new Listeners(new ConfigurationListeners());

        plugin.init();

        executionListeners.assertDelta(0);
        methodInterceptors.assertDelta(0);
        methodListeners.assertDelta(0);
        suiteListeners.assertDelta(1);
        testListeners.assertDelta(1);
        configurationListeners.assertDelta(1);

        plugin.shutdown();
        CompositeTestNGListener.cleanUpTestNgListeners();
    }

    private static class Listeners {

        private int initial;

        private Callable<Integer> callable;

        public Listeners(Callable<Integer> callable) throws Exception {
            this.callable = callable;
            this.initial = callable.call();
        }

        public void assertDelta(int expectedDelta) throws Exception {
            int delta = callable.call() - initial;
            assertEquals(expectedDelta, delta);
        }

    }

    private static class ExecutionListeners implements Callable<Integer> {
        @Override
        public Integer call() {
            return getExecutionListenersCount();
        }
    }

    private static class MethodInterceptors implements Callable<Integer> {
        @Override
        public Integer call() {
            return getMethodInterceptorsCount();
        }
    }

    private static class MethodListeners implements Callable<Integer> {
        @Override
        public Integer call() {
            return getMethodListenersCount();
        }
    }

    private static class SuiteListeners implements Callable<Integer> {
        @Override
        public Integer call() {
            return getSuiteListenersCount();
        }
    }

    private static class TestListeners implements Callable<Integer> {
        @Override
        public Integer call() {
            return getTestListenersCount();
        }
    }

    private static class ConfigurationListeners implements Callable<Integer> {
        @Override
        public Integer call() {
            return getConfigurationListenersCount();
        }
    }

}
