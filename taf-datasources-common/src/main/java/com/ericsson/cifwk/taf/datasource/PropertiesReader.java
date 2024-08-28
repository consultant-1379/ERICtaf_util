package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.DataHandler;

@API(Internal)
public class PropertiesReader implements ConfigurationSource {

    public static final String DATAPROVIDER_PROPERTY_PREFIX = "dataprovider";
    public static final String DATA_SOURCE_TYPE = "type";
    public static final String DATA_SOURCE_USAGE = "usage";
    public static final String DATA_SOURCE_STRATEGY = "strategy";

    final private String prefix;

    public PropertiesReader(String dataProviderName) {
        this.prefix = DATAPROVIDER_PROPERTY_PREFIX + "." + dataProviderName;
    }

    @Override
    public String getProperty(String key) {
        return (String) DataHandler.getAttribute(prefix + "." + key);
    }

}
