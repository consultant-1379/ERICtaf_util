package com.ericsson.cifwk.taf.configuration.configurations;

import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;

import com.ericsson.cifwk.taf.configuration.builder.BuildFromResource;
import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;
import com.ericsson.cifwk.taf.configuration.processor.ConfigurationProcessor;

public class TafFileConfiguration extends CompositeConfiguration {

    private final String path;

    public TafFileConfiguration(String path) {
        this.path = path.replaceAll("[\\\\/]", ".");
    }

    public TafFileConfiguration build() {
        BuildFromResource builder = new BuildFromResource()
                .forPackage(path);
        String match = path.replaceAll("[\\.]", "[\\\\./\\\\\\\\]") + "[\\./\\\\].*\\.properties";
        builder.forMatching(Pattern.compile(match + "\\.json"))
                .applyEach(ConfigurationProcessor.resourceProcessor(ConfigurationParser.HOST_JSON))
                .build(this);
        builder.forMatching(Pattern.compile(match))
                .applyEach(ConfigurationProcessor.resourceProcessor(ConfigurationParser.PROPERTIES))
                .build(this);
        return this;
    }

}
