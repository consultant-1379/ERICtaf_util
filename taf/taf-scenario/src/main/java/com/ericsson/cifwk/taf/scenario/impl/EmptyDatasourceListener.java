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

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.exception.EmptyDataSourceException;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioStartedMessage;
import com.google.common.eventbus.Subscribe;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class EmptyDatasourceListener {

    private FlowExecutionRecorder flowExecutionRecorder = new FlowExecutionRecorder();
    private AtomicBoolean failuresInScenario = new AtomicBoolean(false);
    private ScenarioExecutionContext context;

    public EmptyDatasourceListener(ScenarioExecutionContext context) {
        this.context = context;
    }

    @Subscribe
    public void onScenarioStarted(ScenarioStartedMessage message) {
        flowExecutionRecorder.populate(message.getGraph());
    }

    @Subscribe
    public void onFlowFinishedAllIterationsMessagee(FlowFinishedAllIterationsMessage message) {
        flowExecutionRecorder.recordExecution(message);
    }

    @Subscribe
    public void onAnyFlowFailed(FlowFinishedMessage.AnyFlowFailed message) {
        failuresInScenario.set(true);
    }

    @Subscribe
    public void onScenarioFinished(ScenarioFinishedMessage message) {
        if(!failuresInScenario.get() && flowExecutionRecorder.emptyDataSourcesExist()){
            context.setBroken(new EmptyDataSourceException(flowExecutionRecorder.getErrorMessage(), new IllegalStateException()));
        }
    }

}
