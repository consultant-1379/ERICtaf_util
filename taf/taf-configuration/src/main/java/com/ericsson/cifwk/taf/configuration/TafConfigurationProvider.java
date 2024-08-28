package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.taf.spi.ConfigurationProvider;

public class TafConfigurationProvider implements ConfigurationProvider {

    private static final TafConfigurationProvider provider = new TafConfigurationProvider();

    private TafConfiguration configuration;
    private ConfigurationBuilder builder;

    public TafConfigurationProvider() {
        this(new TafConfigurationBuilder());
    }

    public TafConfigurationProvider(ConfigurationBuilder builder) {
        this.builder = builder;
    }

    public static TafConfiguration provide() {
        return provider.getConfiguration();
    }

    public TafConfiguration getConfiguration() {
        if (configuration == null) {
            synchronized (this) {
                if (configuration == null) {
                    configuration = builder.build();
                }
            }
        }
        return configuration;
    }

    @Override
    public Configuration get() {
        return provide();
    }

    public interface ConfigurationBuilder {
        TafConfiguration build();
    }
}
