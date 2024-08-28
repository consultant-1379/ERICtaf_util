package com.ericsson.cifwk.taf.eiffel.testng;

import com.ericsson.cifwk.taf.eiffel.ExecutionEvent;
import com.ericsson.cifwk.taf.eiffel.exception.UnknownParentEventException;

/**
 * This class keeps track of execution ID and event ID for current test case and test suite.
 */
public class ExecutionEventHolder {

    private static final String UNKNOWN_TEST_CASE_ID_MSG = "Unknown test case id";
    private static final String UNKNOWN_TEST_SUITE_ID_MSG = "Unknown test suite id";

    private static ThreadLocal<ExecutionEvent> testSuiteEvent = new InheritableThreadLocal<>();
    private static ThreadLocal<ExecutionEvent> testCaseEvent = new InheritableThreadLocal<>();



    public static void setTestCaseEvent(ExecutionEvent event) {
        testCaseEvent.set(event);
    }

    public static ExecutionEvent getTestCaseEvent() throws UnknownParentEventException {
        ExecutionEvent event = testCaseEvent.get();
        if (event == null) {
            throw new UnknownParentEventException(UNKNOWN_TEST_CASE_ID_MSG);
        }
        return event;
    }

    public static void removeTestCaseEvent() {
        testCaseEvent.remove();
    }

    public static void setTestSuiteEvent(ExecutionEvent event) {
        testSuiteEvent.set(event);
    }

    public static ExecutionEvent getTestSuiteEvent() throws UnknownParentEventException {
        ExecutionEvent event = testSuiteEvent.get();
        if (event == null) {
            throw new UnknownParentEventException(UNKNOWN_TEST_SUITE_ID_MSG);
        }
        return event;
    }

    public static void removeTestSuiteEvent() {
        testSuiteEvent.remove();
    }

}
