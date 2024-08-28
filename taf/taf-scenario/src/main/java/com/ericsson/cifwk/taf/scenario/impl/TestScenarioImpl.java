package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 *
 */
@API(Internal)
public final class TestScenarioImpl implements TestScenario {
    private final long id = idGenerator.incrementAndGet();
    private final String name;
    private final ScenarioType type;
    private final TestStepFlow flow;
    private final DataSourceDefinition[] dataSources;
    private int defaultVusers;

    public TestScenarioImpl(String name,
                            ScenarioType type,
                            TestStepFlow flow,
                            DataSourceDefinition[] dataSources,
                            int defaultVusers) {
        this.name = name;
        this.type = type;
        this.flow = flow;
        this.dataSources = dataSources;
        this.defaultVusers = defaultVusers;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ScenarioType getType() {
        return type;
    }

    @Override
    public TestStepFlow getFlow() {
        return flow;
    }

    @Override
    public DataSourceDefinition[] getDataSources() {
        return dataSources;
    }

    @Override
    public int getDefaultVusers() {
        return defaultVusers;
    }
}
