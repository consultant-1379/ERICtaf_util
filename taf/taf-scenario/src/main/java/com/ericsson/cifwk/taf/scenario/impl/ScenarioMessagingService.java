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
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowExecutionCancelledMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioMessage;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ScenarioMessageSubscriber;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;

@API(Internal)
public class ScenarioMessagingService {
    private ScenarioGraph graph;
    private static final Logger logger = LoggerFactory.getLogger(ScenarioMessagingService.class);

    public ScenarioMessagingService(ScenarioGraph graph) {
        this.graph = graph;
    }

    @Subscribe
    public void onFlowFinishedAllIterationsMessage(FlowFinishedAllIterationsMessage message) {
        sendMessageToAllStepsInFlow(message.getFlow(), message.getVuserId(), message, false);
    }

    @Subscribe
    public void onFlowExecutionCancelledMessage(FlowExecutionCancelledMessage message) {
        ScenarioGraph.TestStepNode node = graph.getNode(message.getInvocation(), message.getVuserId());
        for (ScenarioGraph.TestStepNode testStepNode : graph.getChildren(node)) {
            sendMessageToAllStepsInFlow(testStepNode.getFlow(), testStepNode.getVUser(), message, false);
        }
    }

    @Subscribe
    public void onAnyFlowFailed(FlowFinishedMessage.AnyFlowFailed message) {
        sendMessageToAllStepsInFlow(message.getFlow(), message.getVuserId(), message, false);
    }

    public void sendMessageToAllStepsInFlow(TestStepFlow flow, int vuserId, ScenarioMessage message, boolean delverOnceToTestStepInvocation) {
        if (flow.getTestSteps().isEmpty()) {
            return;
        }

        TestStepInvocation first = getFirst(flow.getTestSteps(), null);
        TestStepInvocation last = getLast(flow.getTestSteps());
        ScenarioGraph.TestStepNode testStepNode = graph.getNode(first, vuserId);

        sendMessage(testStepNode, last, message, delverOnceToTestStepInvocation, Sets.<Long>newHashSet());
    }

    public void sendMessage(ScenarioGraph.TestStepNode node, ScenarioMessage message, boolean delverOnceToTestStepInvocation) {
        sendMessage(node, null, message, delverOnceToTestStepInvocation, new HashSet<Long>());
    }

    /**
     * @param delverOnceToTestStepInvocation if node have multiple parents each will propagate same message
     */
    private void sendMessage(ScenarioGraph.TestStepNode node, TestStepInvocation last, ScenarioMessage message,
                             boolean delverOnceToTestStepInvocation, Set<Long> invocationsReceivedMessages) {
        TestStepInvocation testStep = node.getTestStep();
        Long testStepId = testStep.getId();
        if (testStep instanceof ScenarioMessageSubscriber && !invocationsReceivedMessages.contains(testStepId)) {
            ScenarioMessageSubscriber.class.cast(testStep).onReceive(message, node);
        }
        if (delverOnceToTestStepInvocation) {
            invocationsReceivedMessages.add(testStepId);
        }
        if (last != null && testStepId.equals(last.getId())) {
            return;
        }
        for (ScenarioGraph.TestStepNode childNode : graph.getChildren(node)) {
            sendMessage(childNode, last, message, delverOnceToTestStepInvocation, invocationsReceivedMessages);
        }
    }
}
