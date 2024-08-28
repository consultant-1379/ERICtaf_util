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

import java.util.Comparator;

import com.ericsson.cifwk.meta.API;

@API(Internal)
public class ScenarioListenerSortableWrapperComparator implements Comparator<ScenarioListenerSortableWrapper> {
    @Override
    public int compare(ScenarioListenerSortableWrapper scenarioListenerSortableWrapper, ScenarioListenerSortableWrapper scenarioListenerSortableWrapper2) {
        return scenarioListenerSortableWrapper2.getPriority().compareTo(scenarioListenerSortableWrapper.getPriority());
    }
}
