package com.ericsson.cifwk.taf.scenario.impl.teststepinvocation;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioGraph;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioMessage;

@API(Internal)
public interface ScenarioMessageSubscriber {
    void onReceive(ScenarioMessage message, ScenarioGraph.TestStepNode node);
}
