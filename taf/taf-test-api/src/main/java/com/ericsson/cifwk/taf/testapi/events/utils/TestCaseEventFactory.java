package com.ericsson.cifwk.taf.testapi.events.utils;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseFailedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseSkippedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseStartedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseSucceededEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.google.common.base.Preconditions;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
@API(Internal)
public class TestCaseEventFactory {

    public static TestCaseEvent create(TestMethodExecutionResult executionResult, TestExecutionContext executionContext) {
        Preconditions.checkArgument(executionResult != null, "Execution result is undefined");
        Preconditions.checkArgument(executionContext != null, "Execution context is undefined");

        TestEvent.ExecutionState executionState = executionResult.getExecutionState();
        switch (executionState) {
            case STARTED:
                return new TestCaseStartedEvent(executionResult, executionContext);
            case SKIPPED:
                return new TestCaseSkippedEvent(executionResult, executionContext);
            case SUCCEEDED:
                return new TestCaseSucceededEvent(executionResult, executionContext);
            case FAILED:
                return new TestCaseFailedEvent(executionResult, executionContext);
            default:
                throw new UnsupportedOperationException(String.format("Unknown test case state %s", executionState));
        }
    }

}
