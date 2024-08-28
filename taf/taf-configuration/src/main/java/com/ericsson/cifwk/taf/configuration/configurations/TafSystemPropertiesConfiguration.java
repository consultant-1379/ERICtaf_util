package com.ericsson.cifwk.taf.configuration.configurations;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.PropertyConverter;

import java.util.Iterator;
import java.util.List;

public class TafSystemPropertiesConfiguration extends AbstractConfiguration {
    @Override
    protected void addPropertyDirect(String key, Object value) {
        System.getProperties().put(key, value);
    }

    @Override
    public boolean isEmpty() {
        return System.getProperties().isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return System.getProperties().containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        Object value = System.getProperties().get(key);
        if ((value instanceof String) && (!isDelimiterParsingDisabled())) {
            List<String> list = PropertyConverter.split((String) value, getListDelimiter(), true);
            return list.size() > 1 ? list : list.get(0);
        } else {
            return value;
        }
    }

    @Override
    public Iterator<String> getKeys() {
        return System.getProperties().stringPropertyNames().iterator();
    }

    protected void clearPropertyDirect(String key) {
        System.getProperties().remove(key);
    }
    



}
