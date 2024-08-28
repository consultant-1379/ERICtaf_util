package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public abstract class ExtendedScenarioListener extends AbstractScenarioListener {

    public void onFlowFinished(TestStepFlow testStepFlow, TestFlowResult result){
        // no implementation
    }
    public void onTestStepFinished(TestStepInvocation invocation, TestStepResult result) {
        // no implementation
    }
}
