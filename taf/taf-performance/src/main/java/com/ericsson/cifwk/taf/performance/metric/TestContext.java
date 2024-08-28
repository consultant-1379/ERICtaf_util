package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.testapi.TestMethod;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;

import java.lang.reflect.Method;

public class TestContext {

    private static final ThreadLocal<String> currentSuiteName = new InheritableThreadLocal<>();

    private static final ThreadLocal<String> currentTestName = new InheritableThreadLocal<>();

    private static final ThreadLocal<Long> testStartedMillis = new InheritableThreadLocal<>();

    public static void set(TestCaseEvent event) {
        currentSuiteName.set(getSuiteName(event));
        currentTestName.set(getTestName(event));
        testStartedMillis.set(System.currentTimeMillis());
    }

    public static String getSuiteName(TestCaseEvent event) {
        return event.getTestExecutionContext().getTestGroup().getId();
    }

    public static String getTestName(TestCaseEvent event) {
        TestMethodExecutionResult executionResult = event.getTestExecutionResult();
        TestMethod testMethod = executionResult.getTestMethod();
        String testName = testMethod.getName();
        Method javaMethod = testMethod.getJavaMethod();
        if (javaMethod.getAnnotation(TestId.class) != null) {
            TestId tid = javaMethod.getAnnotation(TestId.class);
            testName = String.format("%s %s", tid.id(), tid.title());
        }
        return testName;
    }

    public static void reset() {
        currentSuiteName.remove();
        currentTestName.remove();
        testStartedMillis.remove();
    }

    public static String getCurrentSuiteName() {
        return currentSuiteName.get();
    }

    public static String getCurrentTestName() {
        return currentTestName.get();
    }

    public static Long getTestStartedMillis() {
        return testStartedMillis.get();
    }
}
