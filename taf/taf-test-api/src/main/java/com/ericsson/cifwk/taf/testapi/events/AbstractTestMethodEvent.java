package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 21/09/2016
 */
abstract class AbstractTestMethodEvent implements ContextAwareTestEvent {

    private final TestMethodExecutionResult executionResult;
    private final TestExecutionContext executionContext;
    private final ExecutionState state;

    public AbstractTestMethodEvent(TestMethodExecutionResult executionResult, TestExecutionContext executionContext) {
        this.executionResult = executionResult;
        this.executionContext = executionContext;
        this.state = executionResult.getExecutionState();
    }

    public TestMethodExecutionResult getTestExecutionResult() {
        return executionResult;
    }

    public ExecutionState getExecutionState() {
        return state;
    }

    @Override
    public TestExecutionContext getTestExecutionContext() {
        return executionContext;
    }

}