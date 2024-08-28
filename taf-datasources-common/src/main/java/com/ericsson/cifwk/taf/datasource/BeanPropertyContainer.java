package com.ericsson.cifwk.taf.datasource;

import java.util.Map;

class BeanPropertyContainer {

    private final Map<String, Object> properties;

    public BeanPropertyContainer(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
