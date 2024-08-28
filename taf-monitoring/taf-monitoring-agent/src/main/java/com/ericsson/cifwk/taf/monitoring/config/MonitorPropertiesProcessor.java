package com.ericsson.cifwk.taf.monitoring.config;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.MemMonitor;
import com.ericsson.cifwk.taf.monitoring.agent.monitor.MonitorType;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonitorPropertiesProcessor {

    private static final String PREFIX = "monitor";

    private static final Logger logger = LoggerFactory.getLogger(MonitorPropertiesProcessor.class);


    private MonitorPropertiesProcessor() {
    }

    public static List<MonitorConfig> process(TafConfiguration properties) {
        List<String> monitorNames = getMonitorNames(filterMonitorProperties(properties));
        List<MonitorConfig> monitorConfigs = new ArrayList<>();
        for (String monitorName : monitorNames) {
            monitorConfigs.add(new MonitorConfig(monitorName, getType(monitorName, properties), getHosts(monitorName, properties), getAddress(monitorName, properties)));
        }
        return monitorConfigs;
    }

    private static Map<String, Object> filterMonitorProperties(TafConfiguration properties) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.getProperties().entrySet()) {
            if (entry.getKey().toString().startsWith(PREFIX + ".")) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return result;
    }

    private static List<String> getMonitorNames(Map<String, Object> properties) {
        Set<String> monitorNames = new HashSet<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            try {
                monitorNames.add(entry.getKey().split("\\.")[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.debug("MonitorNames array index out of bounds {}", e);
            }
        }
        return new ArrayList<>(monitorNames);
    }

    private static MonitorType getType(String monitorName, TafConfiguration properties) {
        return MonitorType.valueOf(properties.getProperty(String.format("%s.%s.type", PREFIX, monitorName), String.class).toUpperCase());
    }

    private static String[] getHosts(String monitorName, TafConfiguration properties) {
        try {
            Object[] hosts = properties.getProperty(String.format("%s.%s.hosts", PREFIX, monitorName), ArrayList.class).toArray();
            return Arrays.asList(hosts).toArray(new String[hosts.length]);
        } catch (NullPointerException e) {
            logger.debug("List of hosts are null {}", e);
            return new String[0];
        }
    }

    private static String getAddress(String monitorName, TafConfiguration properties) {
        return properties.getProperty(String.format("%s.%s.address", PREFIX, monitorName), String.class);
    }
}
