package com.ericsson.cifwk.taf.configuration.configurations;

import org.apache.commons.configuration.CompositeConfiguration;

import com.ericsson.cifwk.taf.configuration.builder.BuildFromHttpResources;
import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;
import com.ericsson.cifwk.taf.configuration.processor.ConfigurationProcessor;

public class TafHttpConfiguration extends CompositeConfiguration {

    final String url;

    public TafHttpConfiguration(String url) {
        this.url = url;
    }

    public TafHttpConfiguration build() {
        BuildFromHttpResources builder = new BuildFromHttpResources()
                .forUrl(url);
        builder.withParameter("type", "hosts")
                .applyEach(ConfigurationProcessor.httpProcessor(ConfigurationParser.HOST_JSON))
                .build(this);
        builder.withParameter("type", "properties")
                .applyEach(ConfigurationProcessor.httpProcessor(ConfigurationParser.PROPERTIES))
                .build(this);
        return this;
    }

}
