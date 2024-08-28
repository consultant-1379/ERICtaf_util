package com.ericsson.cifwk.taf.scenario.impl;

import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.google.common.base.Joiner;

/**
 * This class records the flows in a scenario and if they were executed or not.
 * This enables checking for empty data sources.
 */
@API(Internal)
public class FlowExecutionRecorder {
    private static final Logger LOGGER = getLogger(FlowExecutionRecorder.class);
    private Map<Long, FlowExecution> flowExecutions = new HashMap<>();
    private String errorMessage = "";
    private TestStepFlow scenarioFlow = TestStepFlowImpl.nullFlow();

    /**
     * Get the flows in the scenario from the graph and record them
     * @param graph which contains the graph of execution
     */
    public void populate(final ScenarioGraph graph){
        for (final Long flowId : graph.getAllFlowIds()) {
            final TestStepFlow flow = graph.getFlow(flowId);
            flowExecutions.put(flowId, new FlowExecution(flow.getName()));
        }
    }

    /**
     * Record the execution count and datasources for a flow
     * @param message which contains the flow
     */
    public void recordExecution(final FlowFinishedAllIterationsMessage message){
        if(message.getFlow().getId().compareTo(scenarioFlow.getId()) == 0){
            return;
        }
        final TestStepFlow flow = message.getFlow();
        final int executionCount = message.getExecutionCount();
        final DataSourceDefinition[] dataSources = message.getDataSources();
        flowExecutions.get(flow.getId()).add(executionCount, dataSources);

    }

    /**
     * Check the flows to see if any weren't executed.
     * If a flow has an empty datasource the execution count for that flow will be null.
     * @return whether there were empty datasources or not
     */
    public boolean emptyDataSourcesExist(){
        final List<String> flowsNotExecuted = new ArrayList<>();
        final List<String> dataSourcesInFlowsNotExecuted = new ArrayList<>();
        boolean emptyDataSources = false;
        for (final Map.Entry<Long, FlowExecution> flowExecutionCount : flowExecutions.entrySet()) {
            final FlowExecution execution = flowExecutionCount.getValue();
            if(execution.getExecutionCount() == 0 && !execution.checkAllDataSourcesAllowEmpty()){
                flowsNotExecuted.add(execution.getName());
                dataSourcesInFlowsNotExecuted.addAll(execution.getFlowDataSourceNames());
                LOGGER.error("DataSource(s) {} does not allow empty entries, Setting Flow {} to broken. ", execution.getFlowDataSourceNames(), execution.getName());
                emptyDataSources = true;
            }
            errorMessage = "Flow(s) " + Joiner.on(", ").join(flowsNotExecuted) + " weren't executed!" +
                    " One of the data Source(s): " + Joiner.on(", ").join(dataSourcesInFlowsNotExecuted) +
                    " did not produce any records. Check filtering and sharing.";
        }
        return emptyDataSources;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void recordScenarioFlowId(final TestStepFlow flow) {
        this.scenarioFlow = flow;
    }
}
