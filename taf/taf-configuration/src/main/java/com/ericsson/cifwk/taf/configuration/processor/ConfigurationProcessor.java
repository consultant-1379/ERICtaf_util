package com.ericsson.cifwk.taf.configuration.processor;

import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;
import com.google.common.base.Function;
import org.apache.commons.configuration.Configuration;

public abstract class ConfigurationProcessor<T> implements Function<T, Configuration> {

    ConfigurationParser parser;

    protected ConfigurationProcessor(ConfigurationParser parser) {
        this.parser = parser;
    }

    abstract public Configuration apply(T t);

    public static ResourceConfigurationProcessor resourceProcessor(ConfigurationParser parser) {
        return new ResourceConfigurationProcessor(parser);
    }

    public static ArchiveResourceConfigurationProcessor archiveProcessor(ConfigurationParser parser) {
        return new ArchiveResourceConfigurationProcessor(parser);
    }

    public static HttpConfigurationProcessor httpProcessor(ConfigurationParser parser) {
        return new HttpConfigurationProcessor(parser);
    }
}

