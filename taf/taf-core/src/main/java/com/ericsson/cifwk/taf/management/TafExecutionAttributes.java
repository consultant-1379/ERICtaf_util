package com.ericsson.cifwk.taf.management;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple wrapper on top of Map for holding test runtime attributes.
 */
public final class TafExecutionAttributes {

    private final Map<String, Object> parameters = new HashMap<>();

    public void setAttribute(String key, Object value) {
        parameters.put(key, value);
    }

    public <T> T getAttribute(String key) {
        return (T) parameters.get(key);
    }

}
