package com.ericsson.cifwk.taf.configuration.configurations;

import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_PROFILE_LOCATION;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TafProfileConfiguration extends CompositeConfiguration {

    static final Logger logger = LoggerFactory.getLogger(TafUserHomeConfiguration.class);

    final String profile;

    public TafProfileConfiguration(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public Configuration build(boolean skipArchives) {
        String path = TAF_PROFILE_LOCATION + "." + profile;
        //
        this.addConfiguration(new TafFileConfiguration(path).build());
        if (!skipArchives) {
            this.addConfiguration(new TafClasspathConfiguration(path).build());
        }
        if (this.isEmpty()) {
            logger.warn("Can't found any configuration for profile:" + profile + " in" + TAF_PROFILE_LOCATION);
        }
        return this;
    }

}
