package com.ericsson.cifwk.taf.scenario.impl.messages;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;

/**
 * Difference from {@link FlowFinishedMessage} that it is called on every Data Record iteration.
 * This one is called when Data Source Iteration is finished
 */
@API(Internal)
public class FlowFinishedAllIterationsMessage implements ScenarioMessage {
    private final TestStepFlow flow;
    private final int vuserId;
    private String executionId;
    private final DataSourceDefinition[] dataSources;
    private final int executionCount;

    public FlowFinishedAllIterationsMessage(TestStepFlow flow, int vuserId, DataSourceDefinition[] dataSources, String executionId, int executionCount) {
        this.flow = flow;
        this.vuserId = vuserId;
        this.executionId = executionId;
        this.dataSources = dataSources;
        this.executionCount = executionCount;
    }

    public TestStepFlow getFlow() {
        return flow;
    }

    public int getVuserId() {
        return vuserId;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public DataSourceDefinition[] getDataSources() {
        return dataSources;
    }

    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String toString() {
        return "FlowFinishedAllIterationsMessage{" +
                "flow=" + flow +
                ", vuserId=" + vuserId +
                ", executionId='" + executionId + '\'' +
                ", dataSources=" + dataSources +
                ", executionCount=" + executionCount +
                '}';
    }
}
