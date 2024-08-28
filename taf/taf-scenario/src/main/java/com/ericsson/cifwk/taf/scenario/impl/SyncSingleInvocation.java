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
import com.ericsson.cifwk.taf.scenario.BasicTestStepInvocation;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioMessage;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ControlInvocation;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.InitializableByFlow;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ScenarioMessageSubscriber;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Invocation that runs single time not dependent on vUser count and its position in hierarchy
 * all callers will be synchronized before execution
 */
@API(Internal)
public abstract class SyncSingleInvocation extends BasicTestStepInvocation implements ControlInvocation, InitializableByFlow, ScenarioMessageSubscriber {
    private final long id = idGenerator.incrementAndGet();
    private SyncPoint2 syncPoint = null;
    private Integer runnersCount = null;
    private volatile Throwable exception = null;

    private static final Logger logger = LoggerFactory.getLogger(SyncSingleInvocation.class);
    private Long flowId;

    public SyncSingleInvocation(String name) {
        super(name, true);
    }

    @Override
    public Optional<Object> run(TestStepRunner testStepRunner, ScenarioExecutionContext scenarioExecutionContext, LinkedHashMap<String, DataRecord> dataSourcesRecords, TestContext context, List<DataRecordTransformer> dataRecordTransformers) throws Exception {

        Preconditions.checkNotNull(runnersCount, "initialize method has not been called");
        if (runnersCount == 1) { //optimization: Avoid unnecessary complexity in case there's no one to sync with
            runOnce(scenarioExecutionContext);
        } else {
            syncPoint.checkIn(testStepRunner.getExecutionId());
            if (exception != null) { //this is ok, since it is _single_ invocation
                Throwables.propagate(exception);
            }
        }

        return Optional.absent();
    }

    protected abstract void runOnce(ScenarioExecutionContext scenarioExecutionContext);

    @Override
    public void initialize(TestStepFlow flow, final ScenarioExecutionContext scenarioExecutionContext) {
        ScenarioGraph graph = scenarioExecutionContext.getGraph();
        runnersCount = graph.getRunnersCount(flow);
        flowId = flow.getId();

        if (runnersCount > 1) {
            syncPoint = new SyncPoint2(name, runnersCount, graph.toString()) {
                @Override
                protected void onAdvance() {
                    try {
                        runOnce(scenarioExecutionContext);
                    } catch (final Throwable e) {
                        exception = e; // throwing exception onAdvance may hang app
                    }
                }
            };
        }
    }

    @Override
    public boolean isAlwaysRun() {
        return true;
    }

    @Override
    public void onReceive(ScenarioMessage message, ScenarioGraph.TestStepNode node) {
        if (runnersCount <= 1) {
            return;
        }

        if (message instanceof FlowFinishedAllIterationsMessage) {
            logger.debug(name + " received" + message);
            FlowFinishedAllIterationsMessage cast = FlowFinishedAllIterationsMessage.class.cast(message);
            if (flowId.equals(cast.getFlow().getId())) {
                syncPoint.cancelSelf(cast.getExecutionId());
            } else {
                syncPoint.cancelParent(cast.getExecutionId());
            }
        }

        if (message instanceof FlowFinishedMessage.AnyFlowFailed) {
            FlowFinishedMessage.AnyFlowFailed cast = FlowFinishedMessage.AnyFlowFailed.class.cast(message);
            if (flowId.equals(cast.getFlow().getId())) {
                syncPoint.cancelSelf(cast.getExecutionId());
            } else {
                syncPoint.cancelParent(cast.getExecutionId());
            }
        }
    }
}
