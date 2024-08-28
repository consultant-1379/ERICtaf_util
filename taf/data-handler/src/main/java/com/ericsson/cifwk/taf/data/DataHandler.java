package com.ericsson.cifwk.taf.data;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.postprocessor.HostsDataPostProcessor;
import com.ericsson.cifwk.taf.data.processor.HostConstructor;
import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provide a single point of entry to various sources of properties:
 *   Properties Files,
 *   System Properties,
 *   Runtime Properties
 */
public class DataHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(DataHandler.class);

    private static TafConfiguration configuration = TafConfigurationProvider.provide();

    public static TafConfiguration getConfiguration() {
        return configuration;
    }

    public static Host getSpecificNode(Host host, HostType type) {
        if (HostsDataPostProcessor.getSpecificNode(HostsDataPostProcessor.getHostByType(host.getType()), type) == null) {
            return null;
        }

        Properties props = HostsDataPostProcessor.filterHost(configuration.getProperties(), type.toString(), host.getHostname());
        try {
            return HostsDataPostProcessor.process(props).get(1);
        } catch (IndexOutOfBoundsException e) {
            String msg = String.format("No node found for host %s of type %s", host, type);
            LOGGER.error(msg, e);
            return null;
        }
    }

    /**
     * Method to return the first instance of a specific type.
     *
     * @param type
     * @return the first matching host object
     */
    public static Host getHostByType(HostType type) {
        Properties properties = HostsDataPostProcessor.filterHostByKeyValue(configuration.getProperties(), HostConstructor.TYPE, type.toString());
        if (properties.isEmpty()) {
            return null;
        }
        String propertyKey = (String) properties.keySet().iterator().next();
        List<Host> processedHosts = HostsDataPostProcessor.process(getPropertiesForName(propertyKey));
        for (Host host : processedHosts) {
            if (host.getType().equals(type)) {
                return host;
            }
        }
        return null;
    }

    private static Properties getPropertiesForName(String propertyKey) {
        Properties hostProps = new Properties();
        String name = HostsDataPostProcessor.getNodeNameFromProperty(propertyKey);
        if (name != null) {
            hostProps.putAll(HostsDataPostProcessor.filterByNodeName(configuration.getProperties(), name));
        } else {
            name = HostsDataPostProcessor.getHostNameFromProperty(propertyKey);
            hostProps.putAll(HostsDataPostProcessor.filerHostByName(configuration.getProperties(), name));
        }

        return hostProps;
    }

    /**
     * Get all hosts with specified group
     *
     * @param group
     * @return
     */
    public static List<Host> getAllHostsByGroup(String group) {
        Properties hostProperties = HostsDataPostProcessor.filterHostByKeyValue(configuration.getProperties(), HostConstructor.GROUP, group);
        LOGGER.trace("Hosts in groups {}: {}", group, hostProperties);
        Properties hostProps = new Properties();
        for (Object prop : hostProperties.keySet()) {
            String key = (String) prop;
            Properties nextHost = getPropertiesForName(key);
            if (HostsDataPostProcessor.getNodeNameFromProperty(key) != null && !isTunneled(hostProps, key)) {
                nextHost = removeParent(nextHost);
            }
            hostProps.putAll(nextHost);
        }
        return HostsDataPostProcessor.process(hostProps);
    }

    static boolean isTunneled(Properties props, String name) {
        for (Object propKey : props.keySet()) {
            String key = (String) propKey;
            if (key.contains(name + ".tunnel")) {
                return true;
            }
        }
        return false;
    }

    static Properties removeParent(Properties props) {
        Properties result = new Properties();
        for (Map.Entry<Object, Object> property : props.entrySet()) {
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            result.setProperty("host." + key.split(HostConstructor.NODE + ".")[1], value);
        }
        return result;
    }

    /**
     * Method to return a list of all the hosts of a specific type.
     *
     * @param type
     * @return a list of host of a particular type
     */
    public static List<Host> getAllHostsByType(HostType type) {
        Properties hostProperties = HostsDataPostProcessor.filterHostByKeyValue(configuration.getProperties(), HostConstructor.TYPE, type.toString());
        Properties hostProps = new Properties();
        for (Object prop : hostProperties.keySet()) {
            String key = (String) prop;
            hostProps.putAll(getPropertiesForName(key));
        }
        List<Host> hostList = HostsDataPostProcessor.process(hostProps, type);
        List<Host> hostListByType = new ArrayList<>();
        for (Host host : hostList) {
            if (host.getType().equals(type)) {
                hostListByType.add(host);
            }
        }
        return hostListByType;
    }

    /**
     * Method to return first host having particular hostname
     *
     * @param name
     * @return
     */
    public static Host getHostByName(String name) {
        Properties props = HostsDataPostProcessor.filterByNodeName(configuration.getProperties(), name);
        boolean isNode = (props.size() > 0);
        if (!isNode) {
            props = HostsDataPostProcessor.filerHostByName(configuration.getProperties(), name);
        }
        List<Host> processed = HostsDataPostProcessor.process(props);
        for (Host host : processed) {
            if (host.getHostname().equals(name)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Get all the host objects from all data sources and return in a list
     *
     * @return a list of Host objects.
     */
    public static List<Host> getHosts() {
        return HostsDataPostProcessor.process(configuration.getProperties());
    }

    /**
     * Loop through all of the DataReconcilers, finding the requested attribute from the most prioritized data source.
     * @param key the requested attribute name
     * @return a string representation of the attribute requested
     */
    public static synchronized Object getAttribute(Object key) throws NullPointerException {
        Object property = null;
        String k = key.toString();
        if (configuration.containsKey(k)) {
            String source = configuration.getSource(k);
            property = configuration.getProperty(k);
            if (LOGGER.isTraceEnabled()) LOGGER.trace("For attribute {}: {} using {} configuration", key, property, source);
        } else {
            if (LOGGER.isTraceEnabled()) LOGGER.trace("Can't found attribute {} in configuration", key);
        }
        return property;
    }


    /**
     * Create a map of all the properties from all the data sources
     *
     * @return a map of properties
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.29)
    @Deprecated
    public static synchronized Map getAttributes() {
        Properties properties = configuration.getProperties();
        Map result = new HashMap();
        result.putAll(properties);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("All attributes result is: {}", Joiner.on('\n').withKeyValueSeparator(" -> ").join(result));
        }
        return result;
    }

    /**
     * Dynamically set a runtime attribute
     *
     * @param key   - the attribute key
     * @param value - the attribute value
     * @return void
     */
    public static void setAttribute(Object key, Object value) throws NullPointerException {
        if (key == null) {
            throw new IllegalArgumentException("Cannot set attribute with null key");
        }
        configuration.setProperty(key.toString(), value);
    }

    /**
     * Method to remove object from runtime data
     *
     * @param key
     */
    public static void unsetAttribute(Object key) throws NullPointerException {
        if (key == null) {
            throw new IllegalArgumentException("Cannot unset attribute with null key");
        }
        configuration.clearProperty(key.toString());
    }

    public static void unsetAttributes() {
        configuration.clear();
    }
}
