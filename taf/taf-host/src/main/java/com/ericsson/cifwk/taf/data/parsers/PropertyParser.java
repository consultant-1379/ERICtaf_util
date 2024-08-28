package com.ericsson.cifwk.taf.data.parsers;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.collect.Maps.newHashMap;

public class PropertyParser {
    
    public static Map parse(Map mapProperties) {
        Properties properties = new Properties();
        properties.putAll(mapProperties);
        return parse(properties);
    }
    
    public static Map parse(Properties properties) {
        Map config = new HashMap();
        for (Object key : properties.keySet()) {
            List<String> tokens = Splitter.on(CharMatcher.anyOf(".")).omitEmptyStrings().splitToList(key.toString());
            Map current = config;
            Map last = null;
            String lastToken = null;
            for (String token : tokens) {
                last = current;
                lastToken = token;
                
                if (current.containsKey(token)) {
                    current = (Map) current.get(token);
                } else {
                    current.put(token, newHashMap());
                    current = (Map) current.get(token);
                }
            }
            last.put(lastToken, properties.get(key));
        }
        return config;
    }
}
