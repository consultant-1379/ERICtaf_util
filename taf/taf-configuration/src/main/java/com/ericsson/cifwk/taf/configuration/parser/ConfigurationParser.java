package com.ericsson.cifwk.taf.configuration.parser;

import java.io.InputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

public interface ConfigurationParser {

    ConfigurationParser HOST_JSON = new JsonHostConfigurationParser();
    ConfigurationParser PROPERTIES = new PropertiesConfigurationParser();

    /*
     * monitors schema is not used ConfigurationParser MONITOR_JSON = new
     * JsonMonitoringConfigurationParser();
     */

    Configuration parse(InputStream is) throws ConfigurationException;

}
