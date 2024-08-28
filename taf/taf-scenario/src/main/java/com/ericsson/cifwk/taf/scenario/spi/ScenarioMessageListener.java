package com.ericsson.cifwk.taf.scenario.spi;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;

/**
 * This interface doesn't have any methods to implement because subscription to Scenario Messages is done via
 * Guava com.google.common.eventbus.Subscribe annotation.
 * See example in com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListener
 */
@API(Internal)
public interface ScenarioMessageListener {
}
