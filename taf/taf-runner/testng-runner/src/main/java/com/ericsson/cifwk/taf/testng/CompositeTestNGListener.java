package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.management.TafBootstrap;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.internal.MethodInstance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @deprecated use {@link TafTestApiEventDispatcher} and tests events.
 * Left until all outer extensions start to use Test API instead of TestNG API.
 */
@API(API.Quality.Deprecated)
@Deprecated
public final class CompositeTestNGListener implements IInvokedMethodListener,
        IExecutionListener, ISuiteListener, ITestListener, IMethodInterceptor, IConfigurationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeTestNGListener.class);

    public static final String ERROR_MESSAGE = "Exception in TestNG listener";

    static final Map<Integer, IInvokedMethodListener> methodListeners = new ConcurrentSkipListMap<>();
    static final Map<Integer, ITestListener> testListeners = new ConcurrentSkipListMap<>();
    static final Map<Integer, IConfigurationListener> configurationListeners = new ConcurrentSkipListMap<>();
    static final Map<Integer, IExecutionListener> executionListeners = new ConcurrentSkipListMap<>();
    static final Map<Integer, ISuiteListener> suiteListeners = new ConcurrentSkipListMap<>();
    // TODO: introduce a separate SPI-loaded class for method interception or expose GroupsListener directly via SPI if possible
    static final Map<Integer, IMethodInterceptor> methodInterceptors = new ConcurrentSkipListMap<>();

    public CompositeTestNGListener() {
        // Starting in case Taf is invoked from IDE
        TafBootstrap bootstrap = new TafBootstrap();
        // Initializes TAF Plugins
        bootstrap.init();
    }

    public static void addListener(ITestNGListener listener, int priority) {
        if (listener instanceof IInvokedMethodListener) {
            methodListeners.put(checkPriorityIsInUse(methodListeners, priority), (IInvokedMethodListener) listener);
        }
        if (listener instanceof ITestListener) {
            testListeners.put(checkPriorityIsInUse(testListeners, priority), (ITestListener) listener);
        }
        if (listener instanceof IConfigurationListener) {
            configurationListeners.put(checkPriorityIsInUse(testListeners, priority), (IConfigurationListener) listener);
        }
        if (listener instanceof IExecutionListener) {
            executionListeners.put(checkPriorityIsInUse(executionListeners, priority), (IExecutionListener) listener);
        }
        if (listener instanceof ISuiteListener) {
            suiteListeners.put(checkPriorityIsInUse(suiteListeners, priority), (ISuiteListener) listener);
        }
        if (listener instanceof IMethodInterceptor) {
            methodInterceptors.put(checkPriorityIsInUse(methodInterceptors, priority), (IMethodInterceptor) listener);
        }
    }

    /**
     * Checks if a priority value is in use, calculates the next highest priority value
     * available for a specific listen and return new priority value
     *
     * @return priority Return original priority or a new priority value if specified priority is unavailable
     */
    private static int checkPriorityIsInUse(Map<Integer, ? extends ITestNGListener> listener, int priority) {
        int resultPriority = priority;
        while (listener.get(resultPriority) != null) {
            resultPriority++;
        }
        return resultPriority;
    }

    @Override
    public void onExecutionStart() {
        Exception suspendedException = null;
        for (IExecutionListener listener : executionListeners.values()) {
            try {
                listener.onExecutionStart();
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onExecutionFinish() {
        Exception suspendedException = null;
        for (IExecutionListener listener : executionListeners.values()) {
            try {
                listener.onExecutionFinish();
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }


    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        Exception suspendedException = null;
        for (IInvokedMethodListener listener : methodListeners.values()) {
            try {
                listener.beforeInvocation(method, testResult);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        Exception suspendedException = null;
        for (IInvokedMethodListener listener : methodListeners.values()) {
            try {
                listener.afterInvocation(method, testResult);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onStart(ISuite suite) {
        Exception suspendedException = null;
        for (ISuiteListener listener : suiteListeners.values()) {
            try {
                listener.onStart(suite);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onFinish(ISuite suite) {
        Exception suspendedException = null;
        for (ISuiteListener listener : suiteListeners.values()) {
            try {
                listener.onFinish(suite);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onTestStart(ITestResult result) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onTestStart(result);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onTestSuccess(result);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onTestFailure(result);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onTestSkipped(result);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onTestFailedButWithinSuccessPercentage(result);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onStart(ITestContext context) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onStart(context);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);

    }

    @Override
    public void onFinish(ITestContext context) {
        Exception suspendedException = null;
        for (ITestListener listener : testListeners.values()) {
            try {
                listener.onFinish(context);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods,
                                           ITestContext context) {
        Collections.sort(methods, MethodInstance.SORT_BY_INDEX);
        List<IMethodInstance> resultMethods = methods;
        // TODO: there can be only one method interceptor in TestNG
        for (IMethodInterceptor listener : methodInterceptors.values()) {
            resultMethods = listener.intercept(resultMethods, context);
        }
        return resultMethods;
    }

    @Override
    public void onConfigurationSuccess(ITestResult iTestResult) {
        Exception suspendedException = null;
        for (IConfigurationListener listener : configurationListeners.values()) {
            try {
                listener.onConfigurationSuccess(iTestResult);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onConfigurationFailure(ITestResult iTestResult) {
        Exception suspendedException = null;
        for (IConfigurationListener listener : configurationListeners.values()) {
            try {
                listener.onConfigurationFailure(iTestResult);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    @Override
    public void onConfigurationSkip(ITestResult iTestResult) {
        Exception suspendedException = null;
        for (IConfigurationListener listener : configurationListeners.values()) {
            try {
                listener.onConfigurationSkip(iTestResult);
            } catch (Exception e) {
                LOGGER.error(ERROR_MESSAGE, e);
                suspendedException = e;
            }
        }
        throwOnDemand(suspendedException);
    }

    private static void throwOnDemand(Exception suspendedException) {
        if (suspendedException != null) {
            throw new RuntimeException(suspendedException); // NOSONAR
        }
    }

    @VisibleForTesting
    public static int getMethodListenersCount() {
        return methodListeners.size();
    }

    @VisibleForTesting
    public static int getTestListenersCount() {
        return testListeners.size();
    }

    @VisibleForTesting
    public static int getConfigurationListenersCount() {
        return configurationListeners.size();
    }

    @VisibleForTesting
    public static int getExecutionListenersCount() {
        return executionListeners.size();
    }

    @VisibleForTesting
    public static int getSuiteListenersCount() {
        return suiteListeners.size();
    }

    @VisibleForTesting
    public static int getMethodInterceptorsCount() {
        return methodInterceptors.size();
    }

    @VisibleForTesting
    public static void cleanUpTestNgListeners() {
        CompositeTestNGListener.methodListeners.clear();
        CompositeTestNGListener.executionListeners.clear();
        CompositeTestNGListener.suiteListeners.clear();
        CompositeTestNGListener.testListeners.clear();
        CompositeTestNGListener.configurationListeners.clear();
        cleanUpMethodInterceptors();
    }

    @VisibleForTesting
    static void cleanUpMethodInterceptors() {
        CompositeTestNGListener.methodInterceptors.clear();
    }

}
