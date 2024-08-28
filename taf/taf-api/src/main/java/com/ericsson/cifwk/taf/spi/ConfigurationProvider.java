package com.ericsson.cifwk.taf.spi;

import com.ericsson.cifwk.taf.configuration.Configuration;

/**
 * SPI interface
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/03/2016
 */
public interface ConfigurationProvider {

    Configuration get();

}
