package com.ericsson.cifwk.taf.configuration.configurations;

import static com.ericsson.cifwk.taf.configuration.TafConfiguration.SYSTEM_USER_HOME;
import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_PROPERTIES_LOCATION;
import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_PROPERTIES_LOCATION_DEFAULT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.configuration.builder.BuildFromResource;
import com.ericsson.cifwk.taf.configuration.parser.ConfigurationParser;
import com.ericsson.cifwk.taf.configuration.processor.ConfigurationProcessor;

public class TafUserHomeConfiguration extends CompositeConfiguration {

    static final Logger logger = LoggerFactory.getLogger(TafUserHomeConfiguration.class);

    public static final String MSG_LOAD_CONFIGURATION_EXCEPTION = "Can't load UserHome configuration from %s because it is thrown %s";
    public static final String MSG_LOAD_CONFIGURATION_ERROR = "Can't load UserHome configuration because it does not exist %s";

    public TafUserHomeConfiguration build() {
        String userHome = System.getProperty(SYSTEM_USER_HOME);
        String tafPropertiesLocation = System.getProperty(TAF_PROPERTIES_LOCATION, TAF_PROPERTIES_LOCATION_DEFAULT);
        try {
            Path path = Paths.get(userHome, tafPropertiesLocation);
            if (Files.isDirectory(path)) {
                BuildFromResource builder = new BuildFromResource()
                        .forPath(path);
                builder.forMatching(Pattern.compile(".*\\.properties\\.json"))
                        .applyEach(ConfigurationProcessor.resourceProcessor(ConfigurationParser.HOST_JSON))
                        .build(this);
                builder.forMatching(Pattern.compile(".*\\.properties"))
                        .applyEach(ConfigurationProcessor.resourceProcessor(ConfigurationParser.PROPERTIES))
                        .build(this);
            } else {
                String msg = String.format(MSG_LOAD_CONFIGURATION_ERROR, path);
                logger.warn(msg);
            }
        } catch (Exception e) {
            String msg = MessageFormat.format(MSG_LOAD_CONFIGURATION_EXCEPTION, userHome + "/" + tafPropertiesLocation, e);
            logger.warn(msg);
        }
        return this;
    }
}
