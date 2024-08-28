package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.taf.configuration.configurations.TafProfileConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;

import java.util.Iterator;
import java.util.Properties;

public class TafConfigurationUtils {

    public static final int NOT_INCLUDE_INMEMORY = 1;

    public static String trace(Configuration configuration) {
        //
        StringBuilder trace = new StringBuilder();
        trace.append(nameFor(configuration));
        //
        if (configuration instanceof CompositeConfiguration) {
            CompositeConfiguration c = (CompositeConfiguration) configuration;
            for (int i = 0; i < c.getNumberOfConfigurations(); i++) {
                Configuration _c = c.getConfiguration(i);
                if (_c == c.getInMemoryConfiguration() && _c.isEmpty()) {
                    continue;
                }
                trace.append("\n").append(trace(c.getConfiguration(i)));
            }
        } else {
            Iterator<String> itr = configuration.getKeys();
            while (itr.hasNext()) {
                String key = itr.next();
                trace.append("\n    ").append(key).append(" = ").append(configuration.getProperty(key));
            }

        }
        return trace.toString();
    }

    public static String nameFor(Configuration configuration) {
        String classFullName = configuration.getClass().getName();
        int lastIndexOf = classFullName.lastIndexOf('.');
        String classShortName = classFullName.substring(lastIndexOf < 0 ? 0 : lastIndexOf + 1);
        StringBuilder name = new StringBuilder(classShortName);
        if (configuration.isEmpty()) {
            name.append("[EMPTY]");
        }
        if (configuration instanceof TafProfileConfiguration) {
            name.append("[profile:").append(((TafProfileConfiguration) configuration).getProfile()).append("]");
        }
        if (configuration instanceof CompositeConfiguration) {
            int numberOfConfiguration = ((CompositeConfiguration) configuration).getNumberOfConfigurations() - NOT_INCLUDE_INMEMORY;
            name.append("[numberOfConfigurations:").append(numberOfConfiguration).append("]");
        }
        if (configuration instanceof FileConfiguration) {
            name.append("[file:").append(((FileConfiguration) configuration).getFileName()).append("]");
        }
        return name.toString();
    }

    public static String getSource(Configuration conf, String key) {
        if (!conf.containsKey(key)) return "";
        if (conf instanceof CompositeConfiguration) {
            for (int i = 0; i < ((CompositeConfiguration) conf).getNumberOfConfigurations(); i++) {
                if (((CompositeConfiguration) conf).getConfiguration(i).containsKey(key)) {
                    return nameFor(conf);
                }
            }
        }
        return nameFor(conf);
    }

    public static Properties getProperties(Configuration configuration) {
        Properties properties = new Properties();
        Iterator<String> i = configuration.getKeys();
        while (i.hasNext()) {
            String key = i.next();
            Object property = configuration.getProperty(key);
            if (property != null) {
                properties.put(key, property);
            }
        }
        return properties;
    }
}
