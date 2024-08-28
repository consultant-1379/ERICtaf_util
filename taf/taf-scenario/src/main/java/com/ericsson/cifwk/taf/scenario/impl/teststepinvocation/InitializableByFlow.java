package com.ericsson.cifwk.taf.scenario.impl.teststepinvocation;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;

@API(Internal)
public interface InitializableByFlow {
    void initialize(TestStepFlow testStepFlow, ScenarioExecutionContext scenarioExecutionContext);
}
