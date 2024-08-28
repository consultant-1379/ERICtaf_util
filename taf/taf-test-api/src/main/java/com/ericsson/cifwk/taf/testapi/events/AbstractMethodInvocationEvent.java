package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.taf.testapi.InvokedMethod;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/09/2016
 */
abstract class AbstractMethodInvocationEvent implements ContextAwareTestEvent {

    private final InvokedMethod invokedMethod;
    private final TestExecutionContext executionContext;

    AbstractMethodInvocationEvent(InvokedMethod invokedMethod, TestExecutionContext executionContext) {
        this.invokedMethod = invokedMethod;
        this.executionContext = executionContext;
    }

    public final InvokedMethod getMethod() {
        return invokedMethod;
    }

    @Override
    public TestExecutionContext getTestExecutionContext() {
        return executionContext;
    }
}
