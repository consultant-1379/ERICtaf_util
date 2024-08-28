package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.taf.configuration.configurations.TafClasspathConfiguration;
import com.ericsson.cifwk.taf.configuration.configurations.TafConfigurationImpl;
import com.ericsson.cifwk.taf.configuration.configurations.TafFileConfiguration;
import com.ericsson.cifwk.taf.configuration.configurations.TafHttpConfiguration;
import com.ericsson.cifwk.taf.configuration.configurations.TafProfileConfiguration;
import com.ericsson.cifwk.taf.configuration.configurations.TafRuntimeConfiguration;
import com.ericsson.cifwk.taf.configuration.configurations.TafSystemPropertiesConfiguration;
import com.ericsson.cifwk.taf.configuration.configurations.TafUserHomeConfiguration;
import com.ericsson.cifwk.taf.configuration.interpol.HostLookup;
import com.ericsson.cifwk.taf.configuration.spi.ConfigurationBuilder;

import org.apache.commons.configuration.*;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ServiceLoader;

import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_HTTP_CONFIG_URL;
import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_PROFILES;
import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_PROPERTIES_LOCATION;
import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_PROPERTIES_LOCATION_DEFAULT;
import static com.ericsson.cifwk.taf.configuration.TafConfiguration.TAF_SKIP_ARCHIVES;

public class TafConfigurationBuilder implements TafConfigurationProvider.ConfigurationBuilder {

    static final Logger LOGGER = LoggerFactory.getLogger(TafConfigurationBuilder.class);

    @Override
    public TafConfiguration build() {
        TafConfigurationImpl configuration = new TafConfigurationImpl();
        return build(configuration);
    }

    public TafConfiguration build(TafConfigurationImpl configuration) {
        ConfigurationInterpolator.registerGlobalLookup(HostLookup.PREFIX, new HostLookup());
        configuration.addConfiguration(new TafRuntimeConfiguration());
        configuration.addConfiguration(new TafSystemPropertiesConfiguration());
        configuration.addConfiguration(new EnvironmentConfiguration());

        if (configuration.containsKey(TAF_HTTP_CONFIG_URL)) {
            String[] urls = configuration.getStringArray(TAF_HTTP_CONFIG_URL);
            for (String url: urls) {
                configuration.addConfiguration(new TafHttpConfiguration(url).build());
            }
        }


        configuration.addConfiguration(new TafUserHomeConfiguration().build());
        boolean skipArchives = configuration.containsKey(TAF_SKIP_ARCHIVES);
        if (configuration.containsKey(TAF_PROFILES)) {
            String[] profiles = configuration.getStringArray(TAF_PROFILES);
            for (String profile : profiles) {
                configuration.addConfiguration(new TafProfileConfiguration(profile).build(skipArchives));
            }
        }

        String propertiesLocation = configuration.getString(TAF_PROPERTIES_LOCATION, TAF_PROPERTIES_LOCATION_DEFAULT);
        configuration.addConfiguration(new TafFileConfiguration(propertiesLocation).build());
        if (!skipArchives) {
            configuration.addConfiguration(new TafClasspathConfiguration(TAF_PROPERTIES_LOCATION_DEFAULT).build());
        }
        //
        if (skipArchives) LOGGER.warn(MessageFormat.format("{0} used during TAF Configuration build", TAF_SKIP_ARCHIVES));
        LOGGER.debug(TafConfigurationUtils.trace(configuration.getConfiguration()));
        //
        for(ConfigurationBuilder builder : ServiceLoader.load(ConfigurationBuilder.class)){
            builder.setup(configuration);
            if(builder.shouldBeBuilt()){
                configuration.addConfiguration(builder.build());
            }
        }
        return configuration;
    }


}

