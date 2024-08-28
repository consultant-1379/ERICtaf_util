package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.annotations.TestOptions;
import com.ericsson.cifwk.taf.eventbus.Priority;
import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.mvel.TafMVELProcessor;
import com.ericsson.cifwk.taf.testapi.TestMethod;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseStartedEvent;
import com.ericsson.cifwk.taf.testapi.utils.TestResultHelper;

import java.lang.reflect.Method;

public class TestOptionsListener {

    @Subscribe
    @Priority(53)
    public void onTestStart(TestCaseStartedEvent event) {
        TestMethodExecutionResult testResult = event.getTestExecutionResult();
        Method javaMethod = TestResultHelper.getMethod(testResult);
        TestOptions testOptions = javaMethod.getAnnotation(TestOptions.class);

        if (testOptions != null) {
            setTimeoutIfApplicable(testResult.getTestMethod(), testOptions.timeout());
        }
    }

    private void setTimeoutIfApplicable(TestMethod testMethod, String timeoutString) {
        if (timeoutString == null || ("0".equals(timeoutString) && testMethod.getTimeOut() == 0L)) {
            return;
        }

        Long timeout = TafMVELProcessor.evalIfExpression(timeoutString, Long.class);
        testMethod.setTimeOut(timeout);
    }

}
