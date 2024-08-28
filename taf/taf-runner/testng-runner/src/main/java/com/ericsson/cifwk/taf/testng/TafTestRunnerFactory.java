package com.ericsson.cifwk.taf.testng;

import org.testng.*;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlTest;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * TestRunner factory for TafScheduledTestRunner to be used by SuiteRunners.<br />
 * (for activation use command line options
 * <code>-testrunfactory TafTestRunnerFactory</code> )
 */
public class TafTestRunnerFactory implements ITestRunnerFactory {

    @Override
    public TestRunner newTestRunner(ISuite iSuite, XmlTest xmlTest,
                                    Collection<IInvokedMethodListener> invokedMethodListeners, List<IClassListener> classListeners) {
        IConfiguration configuration = getConfiguration(iSuite);
        Boolean skipFailedInvocationCounts = getSkipFailedInvocationCounts(iSuite);
        return new TafTestRunner(configuration, iSuite, xmlTest,
                skipFailedInvocationCounts, invokedMethodListeners, classListeners);
    }

    TafVUserTestRunner newVUserTestRunner(ISuite iSuite, XmlTest xmlTest,
                                          Collection<IInvokedMethodListener> invokedMethodListeners, List<IClassListener> classListeners) {
        IConfiguration configuration = getConfiguration(iSuite);
        Boolean skipFailedInvocationCounts = getSkipFailedInvocationCounts(iSuite);
        return new TafVUserTestRunner(configuration, iSuite, xmlTest,
                skipFailedInvocationCounts, invokedMethodListeners, classListeners);
    }

    TafScheduledVUserTestRunner newScheduledVUserTestRunner(ISuite iSuite,
                                                            XmlTest xmlTest,
                                                            Collection<IInvokedMethodListener> iInvokedMethodListeners,
                                                            List<IClassListener> classListeners,
                                                            CountDownLatch latch) {
        IConfiguration configuration = getConfiguration(iSuite);
        Boolean skipFailedInvocationCounts = getSkipFailedInvocationCounts(iSuite);
        return new TafScheduledVUserTestRunner(configuration, iSuite, xmlTest,
                skipFailedInvocationCounts, iInvokedMethodListeners, classListeners, latch);
    }

    private IConfiguration getConfiguration(ISuite iSuite) {
        return getField(iSuite, SuiteRunner.class, "m_configuration");
    }

    private Boolean getSkipFailedInvocationCounts(ISuite iSuite) {
        return getField(iSuite, SuiteRunner.class,
                "m_skipFailedInvocationCounts");
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object instance, Class<?> type, String name) {
        Field field;
        try {
            field = type.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e); // NOSONAR
        }
    }

}
