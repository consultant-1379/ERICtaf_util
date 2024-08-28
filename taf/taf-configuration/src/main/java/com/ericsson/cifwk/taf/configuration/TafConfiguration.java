package com.ericsson.cifwk.taf.configuration;

public interface TafConfiguration extends Configuration {

    String TAF_HTTP_CONFIG_URL = "taf.http_config.url";

    String TAF_PROPERTIES_LOCATION = "taf.properties.location";
    String TAF_PROPERTIES_LOCATION_DEFAULT = "taf_properties";

    String TAF_PROFILES = "taf.profiles";
    String TAF_PROFILE_LOCATION = "taf_profiles";

    String TAF_SKIP_ARCHIVES = "taf.skip.archives";
    String SYSTEM_USER_HOME = "user.home";

    /**
     * Return runtime configuration
     *
     * @return TafConfiguration
     */
    TafConfiguration getRuntimeConfiguration();
}

