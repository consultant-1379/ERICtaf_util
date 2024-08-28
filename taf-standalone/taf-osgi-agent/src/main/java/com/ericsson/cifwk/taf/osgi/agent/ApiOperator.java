package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Marker for classes to be registered on the agent.
 * Implementation is recommended, but not required by agent.
 */
@API(Stable)
public interface ApiOperator {
}
