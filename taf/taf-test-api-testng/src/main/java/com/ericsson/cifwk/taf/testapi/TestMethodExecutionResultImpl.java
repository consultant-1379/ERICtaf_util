package com.ericsson.cifwk.taf.testapi;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import org.testng.ITestResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 09/07/2016
 */
@API(Internal)
public class TestMethodExecutionResultImpl implements TestMethodExecutionResult {

    private final ITestResult testResult;
    private final TestMethod testMethod;

    public TestMethodExecutionResultImpl(ITestResult testResult) {
        this.testResult = testResult;
        this.testMethod = new TestMethodImpl(testResult.getMethod(), testResult);
    }

    @Override
    public Throwable getThrowable() {
        return testResult.getThrowable();
    }

    @Override
    public boolean isSuccess() {
        return testResult.isSuccess();
    }

    @Override
    public TestEvent.ExecutionState getExecutionState() {
        int status = testResult.getStatus();
        switch (status) {
            case 0:
                return TestEvent.ExecutionState.NOT_STARTED;
            case ITestResult.STARTED:
                return TestEvent.ExecutionState.STARTED;
            case ITestResult.FAILURE:
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                return TestEvent.ExecutionState.FAILED;
            case ITestResult.SKIP:
                return TestEvent.ExecutionState.SKIPPED;
            case ITestResult.SUCCESS:
                return TestEvent.ExecutionState.SUCCEEDED;
            default:
                throw new UnsupportedOperationException(String.format("Unknown test execution state code: %d", status));
        }
    }

    @Override
    public TestMethod getTestMethod() {
        return testMethod;
    }

    public ITestResult getTestResult() {
        return testResult;
    }

    @Override
    public String toString() {
        return "TestMethodExecutionResultImpl{" +
                "testResult=" + testResult +
                ", testMethod=" + testMethod +
                '}';
    }
}
