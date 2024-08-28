package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;

/**
 * Extension point to get notified about scenario execution events.
 */
@API(Stable)
public interface ScenarioListener {

    /**
     * Listener that does nothing.
     */
    ScenarioListener NULL = new ScenarioListener() {

        @Override
        public void onScenarioStarted(TestScenario scenario) {
        }

        @Override
        public void onScenarioFinished(TestScenario scenario) {
        }

        @Override
        public void onFlowStarted(TestStepFlow flow) {
        }

        @Override
        public void onFlowFinished(TestStepFlow flow) {
        }

        @Override
        public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
        }

        @Override
        public void onTestStepFinished(TestStepInvocation invocation) {
        }

    };

    void onScenarioStarted(TestScenario scenario);

    void onScenarioFinished(TestScenario scenario);

    void onFlowStarted(TestStepFlow flow);

    void onFlowFinished(TestStepFlow flow);

    void onTestStepStarted(TestStepInvocation invocation, Object[] args);

    void onTestStepFinished(TestStepInvocation invocation);

}
