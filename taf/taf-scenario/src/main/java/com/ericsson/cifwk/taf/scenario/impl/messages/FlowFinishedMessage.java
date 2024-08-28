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
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;
import com.google.common.base.Throwables;

/**
 * If you subscribe to this message, subscriber will be called for all flows (Scenario/User) with any result (Success/Failed)
 * To avoid unnecessary if clauses its possible subscribe to narrower events:
 *
 * |                     |                          Will be called on ↓                                    |
 * |  @Subscribe to ↓    | UserFlow Success | UserFlow Failed | ScenarioFlow Success | ScenarioFlow Failed |
 * |=====================|==================|=================|======================|=====================|
 * | FlowFinishedMessage | x                | x               | x                    | x                   |
 * | AnyFlowSuccess      | x                |                 | x                    |                     |
 * | AnyFlowFailed       |                  | x               |                      | x                   |
 * | UserFlowSuccess     | x                |                 |                      |                     |
 * | UserFlowFailed      |                  | x               |                      |                     |
 * | ScenarioFlowSuccess |                  |                 | x                    |                     |
 * | ScenarioFlowFailed  |                  |                 |                      | x                   |
 */

@API(Internal)
public abstract class FlowFinishedMessage implements ScenarioMessage {
    private final TestStepFlow flow;
    private final int vuserId;
    private final ScenarioType scenarioType;
    private final String executionId;

    protected FlowFinishedMessage(TestStepFlow flow, int vuserId, ScenarioType scenarioType, String executionId) {
        this.flow = flow;
        this.vuserId = vuserId;
        this.scenarioType = scenarioType;
        this.executionId = executionId;
    }

    public TestStepFlow getFlow() {
        return flow;
    }

    public int getVuserId() {
        return vuserId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public ScenarioType getScenarioType() {
        return scenarioType;
    }

    public static FlowFinishedMessage newInstance(ScenarioExecutionContext scenarioContext, TestStepFlow flow, int vuserId, String executionId) {
        if (scenarioContext.getScenarioFlowId().equals(flow.getId())) {
            return new ScenarioFlowSuccess(flow, vuserId, executionId, scenarioContext.getScenarioType());
        } else {
            return new UserFlowSuccess(flow, vuserId, executionId, scenarioContext.getScenarioType());
        }
    }

    public static FlowFinishedMessage newInstance(ScenarioExecutionContext scenarioContext, TestStepFlow flow, int vuserId, String executionId, Throwable testStepError) {
        if (scenarioContext.getScenarioFlowId().equals(flow.getId())) {
            return new ScenarioFlowFailed(flow, vuserId, executionId, testStepError, scenarioContext.getScenarioType());
        } else {
            return new UserFlowFailed(flow, vuserId, executionId, testStepError, scenarioContext.getScenarioType());
        }
    }

    public static class AnyFlowSuccess extends FlowFinishedMessage {
        protected AnyFlowSuccess(TestStepFlow flow, int vuserId, String executionId, ScenarioType scenarioType) {
            super(flow, vuserId, scenarioType, executionId);
        }
    }

    public static class AnyFlowFailed extends FlowFinishedMessage {
        private final Throwable error;

        protected AnyFlowFailed(TestStepFlow flow, int vuserId, String executionId, Throwable testStepError, ScenarioType scenarioType) {
            super(flow, vuserId, scenarioType, executionId);
            this.error = testStepError;
        }

        public Throwable getTestStepError() {
            return Throwables.getRootCause(error);
        }

        public Throwable getFullException() {
            return error;
        }
    }

    public static class UserFlowSuccess extends AnyFlowSuccess {
        protected UserFlowSuccess(TestStepFlow flow, int vuserId, String executionId, ScenarioType scenarioType) {
            super(flow, vuserId, executionId, scenarioType);
        }
    }

    public static class UserFlowFailed extends AnyFlowFailed {
        protected UserFlowFailed(TestStepFlow flow, int vuserId, String executionId, Throwable testStepError, ScenarioType scenarioType) {
            super(flow, vuserId, executionId, testStepError, scenarioType);
        }
    }

    public static class ScenarioFlowSuccess extends AnyFlowSuccess {
        protected ScenarioFlowSuccess(TestStepFlow flow, int vuserId, String executionId, ScenarioType scenarioType) {
            super(flow, vuserId, executionId, scenarioType);
        }
    }

    public static class ScenarioFlowFailed extends AnyFlowFailed {
        protected ScenarioFlowFailed(TestStepFlow flow, int vuserId, String executionId, Throwable testStepError, ScenarioType scenarioType) {
            super(flow, vuserId, executionId, testStepError, scenarioType);
        }
    }
}
