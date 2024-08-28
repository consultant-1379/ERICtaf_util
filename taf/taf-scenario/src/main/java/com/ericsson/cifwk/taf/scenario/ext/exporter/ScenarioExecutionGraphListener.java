/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.ext.exporter;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.ErrorNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.FlowEndedNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.FlowNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.GraphNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.LabeledEdge;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.RootNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.ScenarioFinishedNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.SyncNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.TestStepNode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.impl.ParallelInvocation;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioGraph;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;
import com.ericsson.cifwk.taf.scenario.impl.SyncInvocation;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.TestStepFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.TestStepStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ControlInvocation;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;

@API(Internal)
public class ScenarioExecutionGraphListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ScenarioExecutionGraphListener.class);

    private ScenarioGraph staticGraph;
    private ScenarioExecutionGraph dynamicGraph = new ScenarioExecutionGraph();
    private Table<Long, Integer, Stack<GraphNode>> nodesByFlowIdAndVUser =
            HashBasedTable.create(); //rows - flowId; column - vUser; cell - stack of nodes
    private RootNode root;
    private boolean showSyncPoints;

    public ScenarioExecutionGraphListener(Boolean showSyncPoints) {
        this.showSyncPoints = showSyncPoints;
    }

    @Subscribe
    public void onScenarioStarted(ScenarioStartedMessage message) {
        synchronized (dynamicGraph) {
            try {
                staticGraph = message.getGraph();

                TestScenario scenario = message.getScenario();

                root = new RootNode(scenario);
                dynamicGraph.addVertex(root);

                // Check if scenario is DataDriven scenario: if so, each vUser has its own root Node
                if (checkDataDrivenScenario(scenario)) {

                    int totalvUsers = getTotalvUsers(scenario);

                    for (int vUserScenario = 1; vUserScenario <= totalvUsers; vUserScenario++) {
                        startFlow(scenario.getFlow(), vUserScenario, root);
                    }
                } else {
                    startFlow(scenario.getFlow(), 1, root);
                }

            } catch (NullPointerException ex) {
                LOGGER.info("Scenario graph might not be created correctly");
                LOGGER.debug("Exception ", ex);
            }
        }
    }

    //@Subscribe this is called by GraphMlListenerBuilder.GraphMlFinalizer to assure execution order
    public void onScenarioFinished(ScenarioFinishedMessage message) {
        synchronized (dynamicGraph) {
            root.finish();

            TestScenario scenario = message.getScenario();
            ScenarioFinishedNode scenarioFinishedNode = new ScenarioFinishedNode(scenario);
            if (checkDataDrivenScenario(scenario)) {
                int totalvUsers = getTotalvUsers(scenario);

                for (int vUserScenario = 1; vUserScenario <= totalvUsers; vUserScenario++) {
                    appendToFlow(scenario.getFlow(), vUserScenario, scenarioFinishedNode);
                }
            } else {
                appendToFlow(scenario.getFlow(), 1, scenarioFinishedNode);
            }
        }
    }

    private static int getTotalvUsers(final TestScenario scenario) {
        int vUsers = scenario.getFlow().getRunOptions().getVUsers();
        return vUsers == -1 ? 1 : vUsers;
    }

    @Subscribe
    public void onFlowStarted(FlowStartedMessage.UserFlow message) {
        synchronized (dynamicGraph) {
            try {
                TestStepFlow flow = message.getFlow();
                int vUserId = message.getVuserId();
                LinkedHashMap<String, DataRecord> dataSourcesRecords = message.getDataSourcesRecords();

                FlowNode flowNode = new FlowNode(vUserId, flow, dataSourcesRecords);
                dynamicGraph.addVertex(flowNode);

                GraphNode lastTestStep = getLatestTestStep(flow, vUserId);
                if (shouldLabelEdge(vUserId, lastTestStep)) {
                    dynamicGraph.addEdge(lastTestStep, flowNode, new LabeledEdge("VUser " + vUserId));
                } else {
                    dynamicGraph.addEdge(lastTestStep, flowNode);
                }

                nodesByFlowIdAndVUser.get(flow.getId(), vUserId).push(flowNode);
            } catch (NullPointerException ex) {
                LOGGER.info("Scenario graph might not be created correctly");
                LOGGER.debug("Exception ", ex);
            }
        }
    }

    @Subscribe
    public void onTestStepStarted(TestStepStartedMessage message) {
        synchronized (dynamicGraph) {
            try {
                TestStepFlow flow = message.getFlow();
                int vuserId = message.getVuserId();
                TestStepInvocation invocation = message.getInvocation();

                if (invocation instanceof ParallelInvocation) {
                    GraphNode lastTestStep = getLatestTestStep(flow, vuserId);

                    ScenarioGraph.TestStepNode testStepNode = staticGraph.getNode(invocation, vuserId);
                    Collection<ScenarioGraph.TestStepNode> children = staticGraph.getChildren(testStepNode);

                    for (ScenarioGraph.TestStepNode child : children) {
                        startFlow(child.getFlow(), child.getVUser(), lastTestStep);
                    }
                } else if (shouldShowOnGraph(invocation)) {
                    GraphNode testStep = getTestStepNode(message, vuserId, invocation);
                    appendToFlow(flow, vuserId, testStep);
                }
            } catch (NullPointerException ex) {
                LOGGER.info("Scenario graph might not be created correctly");
                LOGGER.debug("Exception ", ex);
            }
        }
    }


    /**
     * Will be called on both {@link TestStepFinishedMessage.Success} and {@link TestStepFinishedMessage.Failed}
     */
    @Subscribe
    public void onTestStepFinished(TestStepFinishedMessage message) {
        synchronized (dynamicGraph) {
            try {
                TestStepFlow flow = message.getFlow();
                int vuserId = message.getVuserId();
                TestStepInvocation invocation = message.getInvocation();

                if (invocation instanceof ParallelInvocation) {
                    ScenarioGraph.TestStepNode testStepNode = staticGraph.getNode(invocation, vuserId);
                    Collection<ScenarioGraph.TestStepNode> children = staticGraph.getChildren(testStepNode);

                    FlowEndedNode flowEndedNode = new FlowEndedNode(invocation.getClass(), children, vuserId);
                    dynamicGraph.addVertex(flowEndedNode);

                    nodesByFlowIdAndVUser.get(flow.getId(), vuserId).push(flowEndedNode);

                    for (ScenarioGraph.TestStepNode child : children) {
                        GraphNode latestInSubflow = getLatestTestStep(child.getFlow(), child.getVUser());
                        if (!(latestInSubflow instanceof ErrorNode)) {
                            dynamicGraph.addEdge(latestInSubflow, flowEndedNode);
                        }
                    }
                } else if (shouldShowOnGraph(invocation)) {
                    GraphNode latestInSubflow = getLatestTestStep(flow, vuserId);
                    if (latestInSubflow.getError() == null) {
                        latestInSubflow.finish();
                    }
                }
            } catch (NullPointerException ex) {
                LOGGER.info("Scenario graph might not be created correctly");
                LOGGER.debug("Exception ", ex);
            }
        }
    }

    @Subscribe
    public void onTestStepFailed(TestStepFinishedMessage.Failed message) {
        synchronized (dynamicGraph) {
            try {
                TestStepFlow flow = message.getFlow();
                int vuserId = message.getVuserId();

                GraphNode latestTestStep = getLatestTestStep(flow, vuserId);
                if (latestTestStep instanceof TestStepNode &&
                        ((TestStepNode) latestTestStep).getTestStep() == message.getInvocation()) {
                    latestTestStep.finish(message.getError());
                }
            } catch (NullPointerException ex) {
                LOGGER.info("Scenario graph might not be created correctly");
                LOGGER.debug("Exception ", ex);
            }
        }
    }

    @Subscribe
    public void onUserFailed(FlowFinishedMessage.UserFlowFailed message) {
        synchronized (dynamicGraph) {
            try {
                TestStepFlow flow = message.getFlow();
                int vuserId = message.getVuserId();

                ErrorNode errorNode = new ErrorNode(vuserId, flow, message.getTestStepError());
                appendToFlow(flow, vuserId, errorNode);
            } catch (NullPointerException ex) {
                LOGGER.info("Scenario graph might not be created correctly");
                LOGGER.debug("Exception ", ex);
            }
        }
    }

    protected ScenarioExecutionGraph getDynamicGraph() {
        return dynamicGraph;
    }

    private void startFlow(TestStepFlow flow, int vUser, GraphNode firstNode) {
        Stack<GraphNode> mainFlow = new Stack<>();
        mainFlow.push(firstNode);
        nodesByFlowIdAndVUser.put(flow.getId(), vUser, mainFlow);
    }

    private void appendToFlow(TestStepFlow flow, int vuserId, GraphNode testStep) {
        dynamicGraph.addVertex(testStep);
        Stack<GraphNode> graphNodes = nodesByFlowIdAndVUser.get(flow.getId(), vuserId);
        GraphNode lastTestStep = graphNodes.peek();
        dynamicGraph.addEdge(lastTestStep, testStep);
        graphNodes.add(testStep);
    }

    private boolean shouldLabelEdge(int vUserId, GraphNode lastTestStep) {
        return lastTestStep.getVUser() != vUserId
                || lastTestStep instanceof RootNode
                || lastTestStep instanceof FlowEndedNode;
    }

    private boolean shouldShowOnGraph(TestStepInvocation invocation) {
        if (invocation instanceof SyncInvocation && showSyncPoints) {
            return true;
        }

        return !(invocation instanceof ControlInvocation);
    }

    private GraphNode getLatestTestStep(TestStepFlow flow, int vuserId) {
        return nodesByFlowIdAndVUser.get(flow.getId(), vuserId).peek();
    }

    private GraphNode getTestStepNode(TestStepStartedMessage message, int vuserId, TestStepInvocation invocation) {
        GraphNode testStep;
        if (invocation instanceof SyncInvocation) {
            testStep = new SyncNode(invocation, vuserId);
        } else {
            testStep = new TestStepNode(invocation, message.getDataSourcesRecords(), vuserId, null);
        }
        return testStep;
    }

    private boolean checkDataDrivenScenario(TestScenario scenarioToCheck) {
        return scenarioToCheck.getType().equals(ScenarioType.DATA_DRIVEN);
    }
}
