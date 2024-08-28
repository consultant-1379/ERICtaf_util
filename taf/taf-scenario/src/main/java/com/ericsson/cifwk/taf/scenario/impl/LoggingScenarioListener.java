package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(Stable)
public final class LoggingScenarioListener implements ScenarioListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingScenarioListener.class);

    @Override
    public void onScenarioStarted(TestScenario scenario) {
        LOG.info("VUser: {} Starting scenario : {}", vuser(), scenario.getName());
    }

    @Override
    public void onScenarioFinished(TestScenario scenario) {
        LOG.info("VUser: {} Finishing scenario : {}", vuser(), scenario.getName());
    }

    @Override
    public void onFlowStarted(TestStepFlow flow) {
        LOG.info("VUser: {} Starting flow : {}", vuser(), flow.getName());
    }

    @Override
    public void onFlowFinished(TestStepFlow flow) {
        LOG.info("VUser: {} Finishing flow : {}", vuser(), flow.getName());
    }

    @Override
    public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
        String argumentString = Joiner.on("; ").useForNull("null").join(args);
        LOG.info("Starting test step ");
        LOG.debug("VUser: {} Starting test step : {}({})", vuser(), invocation.toString(), argumentString);
    }

    @Override
    public void onTestStepFinished(TestStepInvocation invocation) {
        LOG.debug("VUser: {} Finishing test step : {}", vuser(), invocation.toString());
        LOG.info("Finished test step ");
    }

    private int vuser() {
        return ServiceRegistry.getTestContextProvider().get().getVUser();
    }

}
