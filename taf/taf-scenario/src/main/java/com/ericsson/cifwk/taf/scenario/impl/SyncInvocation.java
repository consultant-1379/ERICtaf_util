/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowExecutionCancelledMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioMessage;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ControlInvocation;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.InitializableByFlow;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ScenarioMessageSubscriber;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

@API(Internal)
public class SyncInvocation implements ControlInvocation, InitializableByFlow, ScenarioMessageSubscriber {
    private final long id = idGenerator.incrementAndGet();
    private String name;
    private SyncPoint syncPoint = null;
    private Long flowId = null;

    private static final Logger logger = LoggerFactory
            .getLogger(SyncInvocation.class);

    public SyncInvocation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void addParameter(String key, Object value) {
        throw new UnsupportedOperationException("Sync Invocation does not allow params");
    }

    @Override
    public void alwaysRun() {
        //do nothing
    }

    @Override
    public boolean isAlwaysRun() {
        return true;
    }

    @Override
    public void initialize(TestStepFlow testStepFlow, ScenarioExecutionContext scenarioExecutionContext) {
        ScenarioGraph graph = scenarioExecutionContext.getGraph();
        List<ScenarioGraph.TestStepNode> nodes = graph.getNodes(this);
        flowId = getFlowId(nodes);
        syncPoint = new SyncPoint("Sync Invocation " + this.id, nodes.size(), graph.toString());
    }

    private Long getFlowId(List<ScenarioGraph.TestStepNode> nodes) {
        Long flowId = nodes.get(0).getFlow().getId();
        for (ScenarioGraph.TestStepNode node : nodes) {
            if (!flowId.equals(node.getFlow().getId())) {
                throw new IllegalStateException("Scenario graph is broken");
            }
        }
        return flowId;
    }

    @Override
    public Optional<Object> run(TestStepRunner testStepRunner,
                                ScenarioExecutionContext scenarioExecutionContext,
                                LinkedHashMap<String, DataRecord> dataSourcesRecords,
                                TestContext context,
                                List<DataRecordTransformer> dataRecordTransformers) throws Exception {
        Preconditions.checkNotNull(syncPoint, "initialize method has not been called");
        syncPoint.await();

        return Optional.absent();
    }

    @Override
    public void onReceive(ScenarioMessage message, ScenarioGraph.TestStepNode node) {
        logger.debug("Sync Point {}: received message {}", id, message);
        if (message instanceof FlowFinishedAllIterationsMessage) {
            syncPoint.stopWaiting();
        } else if (message instanceof FlowFinishedMessage.AnyFlowFailed) {
            syncPoint.stopWaiting();
        } else if (message instanceof FlowExecutionCancelledMessage) {
            syncPoint.moveForward(); //stopWaiting will be called by following Failed
        }
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}

