package com.ericsson.cifwk.taf.scenario.impl.teststepinvocation;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.SyncSingleInvocation;

@API(Internal)
public class RunnableSyncSingleInvocation extends SyncSingleInvocation {
    private Runnable runnable;

    public RunnableSyncSingleInvocation(Runnable runnable, String name) {
        super(name);
        this.runnable = runnable;
    }

    @Override
    protected void runOnce(ScenarioExecutionContext scenarioExecutionContext) {
        runnable.run();
    }
}
