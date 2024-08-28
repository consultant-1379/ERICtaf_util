package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.testapi.InvokedMethodImpl;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestExecutionContextImpl;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResultImpl;
import com.ericsson.cifwk.taf.testapi.TestSuite;
import com.ericsson.cifwk.taf.testapi.events.AfterMethodInvocationEvent;
import com.ericsson.cifwk.taf.testapi.events.BeforeMethodInvocationEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.utils.TestCaseEventFactory;
import com.ericsson.cifwk.taf.testapi.events.TestClassEvent;
import com.ericsson.cifwk.taf.testapi.events.TestConfigurationEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.ericsson.cifwk.taf.testapi.events.utils.TestGroupEventFactory;
import com.ericsson.cifwk.taf.testapi.events.TestSessionEvent;
import com.ericsson.cifwk.taf.testapi.events.utils.TestSessionEventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static com.ericsson.cifwk.taf.testapi.events.TestEvent.ExecutionPhase;
/**
 * TestNG event listener that sends appropriate events to TAF test event listeners
 * (see {@link Subscribe}).
 */
public final class TafTestApiEventDispatcher implements IInvokedMethodListener, IExecutionListener, ISuiteListener,
        ITestListener, IConfigurationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TafTestApiEventDispatcher.class);
    private final TestEventBus testEventBus = ServiceRegistry.getTestEventBus();

    @Override
    public void onExecutionStart() {
        sendTestEvent(testSessionEvent(ExecutionPhase.START));
    }

    @Override
    public void onExecutionFinish() {
        sendTestEvent(testSessionEvent(ExecutionPhase.FINISH));
    }

    @Override
    public void beforeInvocation(final IInvokedMethod method, final ITestResult testResult) {
        sendTestEvent(new BeforeMethodInvocationEvent(invokedMethod(method), executionContext(testResult)));
    }

    @Override
    public void afterInvocation(final IInvokedMethod method, final ITestResult testResult) {
        sendTestEvent(new AfterMethodInvocationEvent(invokedMethod(method), executionContext(testResult)));
    }

    @Override
    public void onStart(final ISuite suite) {
        sendTestEvent(testSuiteEvent(suite, ExecutionPhase.START));
    }

    @Override
    public void onFinish(final ISuite suite) {
        sendTestEvent(testSuiteEvent(suite, ExecutionPhase.FINISH));
    }

    @Override
    public void onTestStart(final ITestResult result) {
        sendTestCaseEvent(result);
    }

    @Override
    public void onTestSuccess(final ITestResult result) {
        sendTestCaseEvent(result);
    }

    @Override
    public void onTestFailure(final ITestResult result) {
        sendTestCaseEvent(result);
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
        sendTestCaseEvent(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
        LOGGER.warn("'Test failure within success percentage' is not supported in TAF Test API");
    }

    @Override
    public void onStart(final ITestContext context) {
        sendTestEvent(testClassEvent(context, ExecutionPhase.START));
    }

    @Override
    public void onFinish(final ITestContext context) {
        sendTestEvent(testClassEvent(context, ExecutionPhase.FINISH));
    }

    @Override
    public void onConfigurationSuccess(final ITestResult iTestResult) {
        sendTestConfigurationEvent(iTestResult);
    }

    @Override
    public void onConfigurationFailure(final ITestResult iTestResult) {
        sendTestConfigurationEvent(iTestResult);
    }

    @Override
    public void onConfigurationSkip(final ITestResult iTestResult) {
        sendTestConfigurationEvent(iTestResult);
    }

    private void sendTestConfigurationEvent(ITestResult iTestResult) {
        sendTestEvent(testConfigurationEvent(iTestResult));
    }

    private void sendTestEvent(TestEvent testEvent) {
        testEventBus.post(testEvent);
    }

    private TestClassEvent testClassEvent(ITestContext context, ExecutionPhase executionPhase) {
        return new TestClassEvent(executionContext(context), executionPhase);
    }

    private TestCaseEvent testCaseEvent(ITestResult result) {
        return TestCaseEventFactory.create(executionResult(result), executionContext(result));
    }

    private TestMethodExecutionResult executionResult(ITestResult result) {
        return new TestMethodExecutionResultImpl(result);
    }

    private TestExecutionContext executionContext(ITestResult result) {
        return executionContext(result.getTestContext());
    }

    private TestExecutionContextImpl executionContext(ITestContext context) {
        return new TestExecutionContextImpl(context);
    }

    private TestGroupEvent testSuiteEvent(ISuite suite, ExecutionPhase executionPhase) {
        return TestGroupEventFactory.create(new TestSuite(suite), executionPhase);
    }

    private TestConfigurationEvent testConfigurationEvent(ITestResult iTestResult) {
        return new TestConfigurationEvent(executionResult(iTestResult), executionContext(iTestResult));
    }

    private TestSessionEvent testSessionEvent(ExecutionPhase executionPhase) {
        return TestSessionEventFactory.create(executionPhase);
    }

    private InvokedMethodImpl invokedMethod(IInvokedMethod method) {
        return new InvokedMethodImpl(method);
    }

    private void sendTestCaseEvent(ITestResult result) {
        sendTestEvent(testCaseEvent(result));
    }

}
