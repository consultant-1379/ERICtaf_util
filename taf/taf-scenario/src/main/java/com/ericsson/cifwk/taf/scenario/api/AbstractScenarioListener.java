package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;

/**
 *
 */
@API(Stable)
public abstract class AbstractScenarioListener implements ScenarioListener {

    @Override
    public void onScenarioStarted(TestScenario scenario) {
        // no implementation
    }

    @Override
    public void onScenarioFinished(TestScenario scenario) {
        // no implementation
    }

    @Override
    public void onFlowStarted(TestStepFlow flow) {
        // no implementation
    }

    @Override
    public void onFlowFinished(TestStepFlow flow) {
        // no implementation
    }

    @Override
    public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
        // no implementation
    }

    @Override
    public void onTestStepFinished(TestStepInvocation invocation) {
        // no implementation
    }

}
