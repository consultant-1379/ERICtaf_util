package com.ericsson.cifwk.taf.eiffel.testng;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TafAnnotationManager;
import com.ericsson.cifwk.taf.TafAnnotationManagerFactory;
import com.ericsson.cifwk.taf.TestCaseBean;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.eiffel.EiffelAdapter;
import com.ericsson.cifwk.taf.eiffel.ExecutionEvent;
import com.ericsson.cifwk.taf.eiffel.exception.UnknownParentEventException;
import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.TestGroupResult;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseFailedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseSkippedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseStartedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseSucceededEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupStartedEvent;
import com.ericsson.cifwk.taf.testapi.utils.TestResultHelper;
import com.ericsson.duraci.datawrappers.ResultCode;
import com.ericsson.duraci.eiffelmessage.sending.exceptions.EiffelMessageSenderException;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder.removeTestSuiteEvent;
import static com.ericsson.cifwk.taf.eiffel.testng.ExecutionEventHolder.setTestSuiteEvent;

@API(Internal)
public class TafEiffelListener {

    // TODO: currently used for a few different event subscribers here, need to check whether it's needed
    public static final int TAF_EIFFEL_LISTENER_PRIORITY = 60;

    private static Logger LOGGER = LoggerFactory.getLogger(TafEiffelListener.class);

    private static final String EXECUTION_EVENT = "EXECUTION_EVENT";
    private static final String UNKNOWN_TEST_SUITE_ID_MSG = "Unknown test suite id";
    private static final String TEST_ID_UNDEFINED = "UNDEFINED";

    private static final String TW_PCKG = "testWarePackage";
    private static final String PCKG_DELIMITER = ".jar";
    private static final String PCKG_DEL_REGEXP = "\\.jar.*";

    private final EiffelAdapter adapter;

    public TafEiffelListener(EiffelAdapter adapter) {
        this.adapter = adapter;
    }

    @Priority(TAF_EIFFEL_LISTENER_PRIORITY)
    @Subscribe
    public void onTestGroupStart(TestGroupStartedEvent testGroupStartedEvent) {
        try {
            TestGroup testGroup = testGroupStartedEvent.getTestGroup();
            ExecutionEvent event = adapter.fireSuiteStarted(testGroup.getId());
            setTestGroupEvent(testGroup, event);
        } catch (EiffelMessageSenderException e) {
            LOGGER.error("Cannot send suite start message.", e);
        }
    }

    @Priority(TAF_EIFFEL_LISTENER_PRIORITY)
    @Subscribe
    public void onTestGroupFinish(TestGroupFinishedEvent testGroupFinishedEvent) {
        TestGroup testGroup = testGroupFinishedEvent.getTestGroup();
        try {
            ExecutionEvent suiteEvent = getSuiteEvent(testGroup);
            adapter.fireSuiteFinished(getResultCode(testGroup.getResults()), suiteEvent);
        } catch (UnknownParentEventException | EiffelMessageSenderException e) {
            LOGGER.error("Cannot send suite finish message.", e);
        }
        removeTestGroupEvent(testGroup);
    }

    @Priority(TAF_EIFFEL_LISTENER_PRIORITY)
    @Subscribe
    public void onTestFailure(TestCaseFailedEvent event) {
        onTestFinish(event.getTestExecutionResult());
    }

    @Priority(TAF_EIFFEL_LISTENER_PRIORITY)
    @Subscribe
    public void onTestSkipped(TestCaseSkippedEvent event) {
        TestExecutionContext executionContext = event.getTestExecutionContext();
        TestMethodExecutionResult result = event.getTestExecutionResult();
        onTestStart(result, executionContext);
        onTestFinish(result);
    }

    @Priority(TAF_EIFFEL_LISTENER_PRIORITY)
    @Subscribe
    public void onTestSuccess(TestCaseSucceededEvent event) {
        onTestFinish(event.getTestExecutionResult());
    }

    @Priority(TAF_EIFFEL_LISTENER_PRIORITY)
    @Subscribe
    public void onTestStart(TestCaseStartedEvent event) {
        onTestStart(event.getTestExecutionResult(), event.getTestExecutionContext());
    }

    private void onTestStart(TestMethodExecutionResult testResult, TestExecutionContext executionContext) {
        if (isTestSuiteAnnotationPresent(testResult)) {
            String suiteName = getTestSuiteNameFromAnnotation(testResult);
            if (!suiteName.equals(TestSuite.NULL)) {
                try {
                    ExecutionEvent event = adapter.fireSuiteStarted(suiteName);
                    setTestSuiteEvent(event);
                } catch (EiffelMessageSenderException e) {
                    LOGGER.error("Cannot send suite start message.", e);
                }
            }
            return;
        }

        final Method testMethod = TestResultHelper.getMethod(testResult);
        String testId = getTestId(testMethod);
        String testCaseTitle = getTestCaseTitle(testMethod);

        TestCaseBean testCaseBean = new TestCaseBean(testResult.getTestMethod().getParameters(), testMethod);
        Map<String, Object> parameters = testCaseBean.getParameters();

        try {
            ExecutionEvent suiteEvent = getSuiteEvent(executionContext.getTestGroup());
            adapter.fireTestCaseStarted(
                    testId,
                    testCaseTitle,
                    parameters,
                    suiteEvent
            );
        } catch (EiffelMessageSenderException | UnknownParentEventException e) {
            LOGGER.error("Cannot send test case start message.", e);
        }
    }

    private void onTestFinish(TestMethodExecutionResult result) {
        TestEvent.ExecutionState executionState = result.getExecutionState();
        ResultCode resultCode = getResultCode(executionState);

        if (isTestSuiteAnnotationPresent(result)) {
            String suiteName = getTestSuiteNameFromAnnotation(result);
            if (!suiteName.equals(TestSuite.NULL)) {
                try {
                    ExecutionEvent event = ExecutionEventHolder.getTestSuiteEvent();
                    adapter.fireSuiteFinished(resultCode, event);
                } catch (EiffelMessageSenderException | UnknownParentEventException e) {
                    LOGGER.error("Cannot send test suite finished event", e);
                } finally {
                    removeTestSuiteEvent();
                }
            }
            return;
        }

        Method testMethod = TestResultHelper.getMethod(result);
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("testId", getTestId(testMethod));
            parameters.put("testMethod", getFullyQualifiedMethodName(result));
            if (executionState != TestEvent.ExecutionState.SUCCEEDED) {
                parameters.put(TW_PCKG, getTestwarePackage(result));
            }

            adapter.fireTestCaseFinished(resultCode, parameters);
        } catch (EiffelMessageSenderException | UnknownParentEventException e) {
            LOGGER.error("Cannot send test case finished event", e);
        }
    }

    private String getTestId(Method testMethod) {
        return testMethod.isAnnotationPresent(TestId.class) ?
                testMethod.getAnnotation(TestId.class).id() : TEST_ID_UNDEFINED;
    }

    private String getTestCaseTitle(Method testMethod) {
        return testMethod.isAnnotationPresent(TestId.class) ?
                testMethod.getAnnotation(TestId.class).title() : testMethod.getName();
    }

    private String getFullyQualifiedMethodName(TestMethodExecutionResult executionResult) {
        Method testMethod = TestResultHelper.getMethod(executionResult);
        String testMethodName = testMethod.getName();
        String testClassName = executionResult.getTestMethod().getTestClass().getName();

        return testClassName + ":" + testMethodName;
    }

    ResultCode getResultCode(TestEvent.ExecutionState status) {
        switch (status) {
            case SUCCEEDED:
                return ResultCode.SUCCESS;
            case FAILED:
                return ResultCode.FAILURE;
            case SKIPPED:
                return ResultCode.NOT_BUILT;
            case STARTED:
            case NOT_STARTED:
                return ResultCode.NOT_SET;
            default:
                return ResultCode.UNRECOGNIZED;
        }
    }

    ResultCode getResultCode(List<TestGroupResult> results) {
        if (results.isEmpty()) {
            return ResultCode.NOT_SET;
        }

        ResultCode result = ResultCode.SUCCESS;
        for (TestGroupResult suiteResult : results) {
            if (suiteResult.getFailedTestCount() > 0) {
                return ResultCode.FAILURE;
            }
        }
        return result;
    }

    String getTestwarePackage(TestMethodExecutionResult result) {
        String sourceUrl = getSourceUrl(result);
        String packageName = null;
        if (sourceUrl.contains(PCKG_DELIMITER)) {
            String[] urlParts = sourceUrl.replaceAll(PCKG_DEL_REGEXP, "").split("\\/");
            packageName = urlParts[urlParts.length - 1];
        }
        return packageName;
    }

    @VisibleForTesting
    String getSourceUrl(TestMethodExecutionResult result) {
        return TestResultHelper.getSourceUrl(result);
    }

    private boolean isTestSuiteAnnotationPresent(TestMethodExecutionResult testResult) {
        return getTestSuiteAnnotation(testResult) != null;
    }

    private String getTestSuiteNameFromAnnotation(TestMethodExecutionResult testResult) {
        return getTestSuiteAnnotation(testResult).value();
    }

    private TestSuite getTestSuiteAnnotation(TestMethodExecutionResult testResult) {
        return getTafAnnotationManager(testResult).getAnyAnnotation(TestSuite.class);
    }

    private TafAnnotationManager getTafAnnotationManager(TestMethodExecutionResult testResult) {
        TafAnnotationManagerFactory tafAnnotationManagerFactory = ServiceRegistry.getTafAnnotationManagerFactory();
        return tafAnnotationManagerFactory.create(testResult);
    }

    protected void setTestGroupEvent(TestGroup testGroup, ExecutionEvent event) {
        testGroup.setAttribute(EXECUTION_EVENT, event);
    }

    protected ExecutionEvent getSuiteEvent(TestGroup suite) throws UnknownParentEventException {
        ExecutionEvent result = suite.getAttribute(EXECUTION_EVENT);
        if (result == null) {
            throw new UnknownParentEventException(UNKNOWN_TEST_SUITE_ID_MSG);
        }
        return result;
    }

    protected void removeTestGroupEvent(TestGroup testGroup) {
        testGroup.removeAttribute(EXECUTION_EVENT);
    }

}
