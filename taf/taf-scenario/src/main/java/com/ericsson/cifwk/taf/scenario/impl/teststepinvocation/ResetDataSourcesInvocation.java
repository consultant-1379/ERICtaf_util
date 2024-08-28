package com.ericsson.cifwk.taf.scenario.impl.teststepinvocation;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.SyncSingleInvocation;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ScenarioDataSource;

import java.util.concurrent.ConcurrentHashMap;

@API(Internal)
public class ResetDataSourcesInvocation extends SyncSingleInvocation {
    public ResetDataSourcesInvocation() {
        super("Reset data sources");
    }

    @Override
    protected void runOnce(ScenarioExecutionContext scenarioExecutionContext) {
        ConcurrentHashMap<String, ScenarioDataSource> dataSources = scenarioExecutionContext.getDataSourceContext().getDataSources();
        dataSources.clear();
    }
}
