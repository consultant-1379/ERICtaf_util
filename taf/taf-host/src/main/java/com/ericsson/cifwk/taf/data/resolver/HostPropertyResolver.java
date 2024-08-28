package com.ericsson.cifwk.taf.data.resolver;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.parsers.PropertyParser;
import com.ericsson.cifwk.taf.data.processor.HostConstructor;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.HOSTS_PREFIX;
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.HTTP_HOSTS_PREFIX;
import static com.google.common.collect.Lists.newArrayList;

public class HostPropertyResolver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HostPropertyResolver.class);

    /**
     * Extract the host specific info from the map and store in host object where no Host type has been specified
     * @param attributes the map of attributes
     * @return List of host objects
     */
    public static List<Host> findHostProperty(Map attributes) {
        List<Host> hostList = newArrayList();
        Map hostsOnly = getMappedValues(attributes, HOSTS_PREFIX);
        Map httpConfigHostsOnly = getMappedValues(attributes, HTTP_HOSTS_PREFIX);
        hostsOnly.putAll(httpConfigHostsOnly);
    
        Iterator iterator = hostsOnly.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> next = (Map.Entry<Object, Object>) iterator.next();
            
            try {
                hostList.addAll(constructHosts((String) next.getKey(), (Map) next.getValue()));
            } catch (Exception e) {
                LOGGER.error("Cannot construct host");
                LOGGER.trace("Details: ", e);
            }
        }
        return hostList;
    }
    
    protected static Map getMappedValues(Map attributes, final String prefix) {
        LOGGER.trace("Looking at map {}", attributes);
    
        Map attrs = Maps.filterKeys(attributes, new Predicate() {
            @Override
            public boolean apply(Object key) {
                return key.toString().startsWith(prefix) && !key.toString().startsWith(prefix + ".json.");
            }
        });
        LOGGER.trace("Attributes filtered for prefix {}: {}", prefix, attrs);
        
        Map result = PropertyParser.parse(attrs);
        if (result.containsKey(prefix)) {
            result = (Map) result.get(prefix);
        }
        attrs = Maps.filterKeys(attributes, new Predicate() {
            @Override
            public boolean apply(Object key) {
                return key.toString().startsWith(prefix + ".json.");
            }
        });
        LOGGER.trace("Attributes filtered for prefix ${prefix}.json: $attrs");
        mergeJsonAttrs(attrs, result);
        LOGGER.trace("Mapped values {}", result);
        return result;
    }

    private static List<Host> constructHosts(String key, Map value) {
        Host host = constructHost(key, value);
        List<Host> hosts = newArrayList();
        hosts.add(host);
        hosts.addAll(host.getNodes());
        return hosts;
    }

    private static Host constructHost(String key, Map value) {
        LOGGER.trace("Constructing host with {} and values {}", key, value);
        Host host = HostConstructor.constructHost(key, value);
        LOGGER.trace("Returning host {}", host);
        return host;
    }
    
    private static void mergeJsonAttrs(Map attrs, Map result) {
        if (attrs.isEmpty()) {
            return;
        }
        LOGGER.trace("Attrs {}", attrs);
    }
}
