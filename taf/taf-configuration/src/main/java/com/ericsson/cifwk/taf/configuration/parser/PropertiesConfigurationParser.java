package com.ericsson.cifwk.taf.configuration.parser;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.InputStream;

class PropertiesConfigurationParser implements ConfigurationParser {

    @Override
    public Configuration parse(InputStream is) throws ConfigurationException {
        PropertiesConfiguration configuration = new PropertiesConfiguration();
        configuration.load(is);
        return configuration;
    }
}
