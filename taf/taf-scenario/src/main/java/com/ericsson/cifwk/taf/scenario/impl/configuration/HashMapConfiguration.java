/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl.configuration;

import com.ericsson.cifwk.taf.scenario.spi.ScenarioConfiguration;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import java.util.Map;

/**
 * Basic ScenarioConfiguration implementation
 */
public class HashMapConfiguration implements ScenarioConfiguration {
    final Map<String, Object> configuration;

    public HashMapConfiguration() {
        this.configuration = Maps.newHashMap();
    }

    public HashMapConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public void setProperty(String key, Object value) {
        configuration.put(key, value);
    }

    @Override
    public <T> T getProperty(String key, T defaultValue, Function<String, T> converter) {
        Object property = configuration.get(key);
        if (property == null) {
            return defaultValue;
        }

        return converter.apply(property.toString());
    }

    @Override
    public Boolean getProperty(String key, Boolean defaultValue) {
        return getProperty(key, defaultValue, new Function<String, Boolean>() {
            @Override
            public Boolean apply(String input) {
                return Boolean.valueOf(input);
            }
        });
    }

    @Override
    public Integer getProperty(String key, Integer defaultValue) {
        return getProperty(key, defaultValue, new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                return Ints.tryParse(input);
            }
        });
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getProperty(key, defaultValue, new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input;
            }
        });
    }
}
