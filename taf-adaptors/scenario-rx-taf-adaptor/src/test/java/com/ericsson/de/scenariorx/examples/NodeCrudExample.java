package com.ericsson.de.scenariorx.examples;

import static com.ericsson.de.scenariorx.api.RxApi.contextDataSource;
import static com.ericsson.de.scenariorx.api.RxApi.flow;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.api.DebugGraphMode;
import com.ericsson.de.scenariorx.api.RxContextDataSource;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxFlow;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.ericsson.de.scenariorx.examples.mocks.HttpTool;
import org.testng.annotations.Test;

public class NodeCrudExample {
    private static final String DELETE_NODE = "DELETE_NODE";
    private static final String CONFIRM_NODE_SYNCED = "CONFIRM_NODE_SYNCED";
    private static final String NODES_TO_ADD = "NODES_TO_ADD";
    private static final String ADDED_NODES = "ADDED_NODES";
    private static final String SYNC_NODE = "SYNC_NODES";
    private static final String ADD_NODE = "ADD_NODE";
    private static final String LOGIN = "LOGIN";
    private static final String CONFIRM_NODE_ADDED = "CONFIRM_NODE_ADDED";
    private static final String LOGOUT = "LOGOUT";

    @Test
    public void nodeCrudExample() {
        TafTestContext.getContext().setAttribute("context", "value");

        RxDataSource<TafNode> myNodes = TafRxScenarios.dataSource(NODES_TO_ADD, TafDataSources.fromCsv("node.csv"), TafNode.class);

        RxContextDataSource<TafNode> addedNodes = contextDataSource("addedNodes", TafNode.class);
        RxContextDataSource<TafNode> syncedNodes = contextDataSource("syncedNodes", TafNode.class);

        RxScenario nodeAgnosticScenario = TafRxScenarios.scenario()
                .addFlow(
                        flow("Add, sync & delete node scenario")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN))
                                .addSubFlow(addNode(myNodes, addedNodes))
                                .addSubFlow(syncNode(addedNodes, syncedNodes))
                                .addSubFlow(userFlowThatNeedsSyncedNodes(syncedNodes))
                                .addSubFlow(deleteNode(addedNodes)).alwaysRun()
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGOUT))
                                .build())
                .build();

        TafRxScenarios
                .runner()
                .withDebugLogEnabled()
                .withGraphExportMode(DebugGraphMode.ALL)
                .build()
                .run(nodeAgnosticScenario);
    }

    private RxFlow addNode(RxDataSource<TafNode> nodesToAdd, RxContextDataSource<TafNode> result) {
        return flow("Add node")
                .addTestStep(TafRxScenarios.annotatedMethod(this, ADD_NODE))
                .addTestStep(TafRxScenarios.annotatedMethod(this, CONFIRM_NODE_ADDED)
                        .collectResultsToDataSource(result))
                .withDataSources(nodesToAdd.rename(NODES_TO_ADD))
                .build();

    }

    private RxFlow syncNode(RxContextDataSource<TafNode> addedNodes, RxContextDataSource<TafNode> syncedNodes) {
        return flow("Sync node")
                .addTestStep(TafRxScenarios.annotatedMethod(this, SYNC_NODE))
                .addTestStep(
                        TafRxScenarios.annotatedMethod(this, CONFIRM_NODE_SYNCED)
                                .collectResultsToDataSource(syncedNodes))
                .withDataSources(addedNodes.rename(NODES_TO_ADD))
                .build();
    }

    private RxFlow userFlowThatNeedsSyncedNodes(RxContextDataSource<TafNode> syncedNodes) {
        return flow("userFlowThatNeedsSyncedNodes")
                .addTestStep(TafRxScenarios.annotatedMethod(this, "printAddedNode"))
                .withVUsers(2)
                .withDataSources(syncedNodes)
                .build();
    }

    private RxFlow deleteNode(RxContextDataSource<TafNode> addedNodes) {
        return flow("Delete Node")
                .addTestStep(TafRxScenarios.annotatedMethod(this, DELETE_NODE))
                .withDataSources(addedNodes.rename(ADDED_NODES))
                .build();
    }

    @TestStep(id = LOGIN)
    public HttpTool login() {
        HttpTool httpTool = new HttpTool("", "");
        TafTestContext.getContext().setAttribute("httpTool", httpTool);

        return httpTool;
    }

    @TestStep(id = LOGOUT)
    public void logout() {
        TafTestContext.getContext().removeAttribute("httpTool");
    }

    @TestStep(id = ADD_NODE)
    public void addNode(@Input(NODES_TO_ADD) TafNode node) {
        System.out.println(ADD_NODE + node.getNetworkElementId());
    }

    @TestStep(id = CONFIRM_NODE_ADDED)
    public TafNode testStep6(@Input(NODES_TO_ADD) TafNode node) {
        System.out.println(CONFIRM_NODE_ADDED + node.getNetworkElementId());
        return node;
    }

    @TestStep(id = SYNC_NODE)
    public void testStep3(@Input(NODES_TO_ADD) TafNode node) {
        System.out.println(SYNC_NODE + node.getNetworkElementId());
    }

    @TestStep(id = CONFIRM_NODE_SYNCED)
    public TafNode testStep4(@Input(NODES_TO_ADD) TafNode node) {
        System.out.println(CONFIRM_NODE_SYNCED + node.getNetworkElementId());
        return node;
    }

    @TestStep(id = DELETE_NODE)
    public void testStep1(@Input(ADDED_NODES) TafNode node) {
        System.out.println(DELETE_NODE + node.getNetworkElementId());
    }

    @TestStep(id = "printAddedNode")
    public void testStep2(@Input("networkElementId") String networkElementId) {
        System.out.println(networkElementId);
    }
}
