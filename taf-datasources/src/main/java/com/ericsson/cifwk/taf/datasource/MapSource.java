package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 *
 */
@API(Internal)
public class MapSource implements ConfigurationSource {

    private final Map<String, String> properties = Maps.newHashMap();

    public MapSource(Map<String, String> configuration) {
        this.properties.putAll(configuration);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

}
