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
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioGraph;

@API(Internal)
public class ScenarioStartedMessage implements ScenarioMessage {
    private final TestScenario scenario;
    private final ScenarioGraph graph;

    public ScenarioStartedMessage(TestScenario scenario, ScenarioGraph graph) {
        this.scenario = scenario;
        this.graph = graph;
    }

    public TestScenario getScenario() {
        return scenario;
    }

    public ScenarioGraph getGraph() {
        return graph;
    }
}
