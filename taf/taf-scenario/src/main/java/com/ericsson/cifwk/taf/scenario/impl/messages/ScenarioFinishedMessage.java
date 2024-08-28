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
import com.ericsson.cifwk.taf.scenario.TestScenario;

@API(Internal)
public class ScenarioFinishedMessage implements ScenarioMessage {
    private final TestScenario scenario;
    private boolean success;

    public ScenarioFinishedMessage(TestScenario scenario, boolean success) {
        this.scenario = scenario;
        this.success = success;
    }

    public TestScenario getScenario() {
        return scenario;
    }

    public boolean isSuccessfull() {
        return success;
    }
}
