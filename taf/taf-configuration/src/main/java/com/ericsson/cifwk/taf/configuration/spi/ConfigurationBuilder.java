package com.ericsson.cifwk.taf.configuration.spi;

import static com.ericsson.cifwk.meta.API.Quality.Experimental;

import org.apache.commons.configuration.Configuration;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;

/**
 * Interface which can be implemented by testware to load their own configuration building through SPI
 */
@API(Experimental)
public interface ConfigurationBuilder {

    /**
     * This enables the implementation to retrieve information from the current configuration
     * @param configuration
     */
    void setup(TafConfiguration configuration);

    /**
     * The conditions under which this builder should be applied.
     * @return
     */
    boolean shouldBeBuilt();

    /**
     * The implementation of a configuration to be included into the configuration
     * @return
     */
    Configuration build();
}