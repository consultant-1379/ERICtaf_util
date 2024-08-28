package com.ericsson.cifwk.taf.scenario;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Representation of individual test scenario.
 */
@API(Stable)
public interface TestScenario {
    AtomicLong idGenerator = new AtomicLong();

    /**
     * Returns uniq id of the scenario
     * @return
     */
    Long getId();


    /**
     * Provides name of this scenario.
     *
     * @return name
     */
    String getName();

    /**
     * Provides type of scenario
     *
     * @return type
     */
    ScenarioType getType();

    /**
     * Retrieves list of flows.
     *
     * @return flows included in this scenario
     */
    TestStepFlow getFlow();

    /**
     * Gets scenario-specific data sources.
     *
     * @return data sources
     */
    DataSourceDefinition[] getDataSources();

    /**
     * Gets default count of vusers.
     *
     * @return default count of vusers
     */
    int getDefaultVusers();
}
