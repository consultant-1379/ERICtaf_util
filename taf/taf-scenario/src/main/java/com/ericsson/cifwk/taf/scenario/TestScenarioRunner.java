package com.ericsson.cifwk.taf.scenario;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;

/**
 * Executable test step scenario runner.
 * Scenario contains multiple flow and every flow contains multiple test steps.
 */
@API(Stable)
public interface TestScenarioRunner {

    /**
     * Starts scenario and blocks the current thread.
     */
    void start(TestScenario scenario);

    /**
     * Returns registered scenario event listener.
     *
     * @return listener instance
     */
    ScenarioListener getListener();

}
