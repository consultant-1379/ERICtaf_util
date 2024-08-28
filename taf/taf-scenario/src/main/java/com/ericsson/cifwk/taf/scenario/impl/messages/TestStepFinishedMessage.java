/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl.messages;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;

@API(Internal)
public abstract class TestStepFinishedMessage implements ScenarioMessage {
    private final TestStepFlow flow;
    private final TestStepInvocation invocation;
    private final int vuserId;

    protected TestStepFinishedMessage(TestStepFlow flow, TestStepInvocation invocation, int vuserId) {
        this.flow = flow;
        this.invocation = invocation;
        this.vuserId = vuserId;
    }

    public TestStepInvocation getInvocation() {
        return invocation;
    }

    public int getVuserId() {
        return vuserId;
    }

    public TestStepFlow getFlow() {
        return flow;
    }

    public static TestStepFinishedMessage newInstance(TestStepFlow flow, TestStepInvocation invocation, int vuserId) {
        return new Success(flow, invocation, vuserId);
    }

    public static TestStepFinishedMessage newInstance(TestStepFlow flow, TestStepInvocation invocation, int vuserId, Throwable error) {
        return new Failed(flow, invocation, vuserId, error);
    }

    public static class Success extends TestStepFinishedMessage {
        protected Success(TestStepFlow flow, TestStepInvocation invocation, int vuserId) {
            super(flow, invocation, vuserId);
        }
    }

    public static class Failed extends TestStepFinishedMessage {
        private final Throwable error;

        protected Failed(TestStepFlow flow, TestStepInvocation invocation, int vuserId, Throwable error) {
            super(flow, invocation, vuserId);
            this.error = error;
        }

        public Throwable getError() {
            return error;
        }
    }
}
