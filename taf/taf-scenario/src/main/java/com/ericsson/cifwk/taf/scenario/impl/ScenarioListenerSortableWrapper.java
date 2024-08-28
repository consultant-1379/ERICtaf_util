package com.ericsson.cifwk.taf.scenario.impl;
/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;

@API(Internal)
public class ScenarioListenerSortableWrapper {
    private final ScenarioListener scenarioListener;
    private final Integer priority;

    public ScenarioListenerSortableWrapper(ScenarioListener scenarioListener, Integer priority) {
        this.scenarioListener = scenarioListener;
        this.priority = priority;
    }

    public ScenarioListener getScenarioListener() {
        return scenarioListener;
    }

    public Integer getPriority() {
        return priority;
    }
}
