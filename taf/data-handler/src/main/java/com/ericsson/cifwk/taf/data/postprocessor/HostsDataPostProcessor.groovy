/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.data.postprocessor

import com.ericsson.cifwk.taf.data.DataHandler
import com.ericsson.cifwk.taf.data.Host
import com.ericsson.cifwk.taf.data.HostType
import com.ericsson.cifwk.taf.data.processor.HostConstructor
import groovy.transform.WithWriteLock
import org.apache.log4j.Logger

import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.HOSTS_PREFIX
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.HTTP_HOSTS_PREFIX
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.NODE
/**
 * Class which processes the properties to extract all the host specific attributes
 * @author erobemu , ethomev
 */
class HostsDataPostProcessor extends AbstractDataPostProcessor {

    private static Logger logger = Logger.getLogger(HostsDataPostProcessor)
    private static List<Host> hostList = []

    /**
     * PUBLIC. Method to return a specific host of a specific type
     * @param host
     * @param type
     * @return the matching host object
     */
    public static Host getSpecificNode(Host host, HostType type) {
        DataHandler.getHosts()
        for (Host node : host.getNodes()) {
            if (node.getType().equals(type))
                return node
        }
    }

    /**
     * PUBLIC. Method to return the first instance of a specific type.
     * @param type
     * @return the first matching host object
     */
    public static Host getHostByType(HostType type) {
        DataHandler.getHosts()
        for (Host host : hostList) {
            if (host.getType().equals(type))
                return host
        }
    }

    /**
     * Method returns host properties containing as key or value strings provided as additional arguments in invocation
     * @param props
     * @param filter
     * @return
     */
    public static Properties filterHost(Properties props, String... filter){
        return props.findAll { String key,val ->
            key.startsWith(HOSTS_PREFIX)
        }.findAll { String key, val ->
            return filter.findAll { filterString ->
                key.toLowerCase().contains(filterString.toLowerCase()) || val.toString().toLowerCase().contains(filterString.toLowerCase())
            }.size() == filter.size()
        }
    }

    /**
     * Method returns properties for host name specified as second argument. All nodes properties are removed
     * @param props
     * @param hostName
     * @return
     */
    public static Properties filerHostByName(Properties props, String hostName){
        return filterHost(props,"${HOSTS_PREFIX}.${hostName}.").findAll { String key, val ->
            ! key.contains(".${NODE}.")
        }
    }

    /**
     * Method returns host properties matching both keyFilter for keys and valueFilter for values
     * @param props
     * @param keyFilter
     * @param valueFilter
     * @return
     */
    public static Properties filterHostByKeyValue(Properties props, String keyFilter, String valueFilter ){
        return props.findAll { String key,val ->
            key.startsWith("${HOSTS_PREFIX}") && key.toLowerCase().endsWith("."+keyFilter.toLowerCase()) && val.toString().toLowerCase().equals(valueFilter.toLowerCase())
        }
    }


    /**
     * Method to return host name from key of property line
     * @param propertyLine
     * @return
     */
    public static String getHostNameFromProperty(String propertyLine){
        return propertyLine.split("\\.")[1]
    }

    /**
     * Method to return node name from key of property line
     * @param propertyLine
     * @return
     */
    public static String getNodeNameFromProperty(String propertyLine){
        boolean nodeFound = false
        return propertyLine.split("\\.").find {
            if (nodeFound) {
                return it
            }
            else {
                nodeFound = (it == NODE)
                return false
            }
        }
    }

    /**
     * Method returns filtered properties for node name specififed as second argument
     * @param props
     * @param nodeName
     * @return
     */
    public static Properties filterByNodeName (Properties props, String nodeName){
        return props.findAll { String key,val ->
            key.contains(".${NODE}.${nodeName}.")
        }
    }

    /**
     * PUBLIC. Method to return a specific host
     * @param name
     * @return the host with the hostname
     */
    public static Host getHostByName(String name) {
        DataHandler.getHosts()
        for (Host host : hostList) {
            if (host.getHostname().equals(name))
                return host
        }
    }

    /**
     * Extract the host specific info from the map and store in host objects
     * @param attributes
     * @param hostType
     * @return List of host objects
     */
    @WithWriteLock
    public static List<Host> process(Map attributes, HostType hostType) {
        hostList = []
        Map hostsOnly = getMappedValues(attributes, HOSTS_PREFIX)
        Map httpConfigHostsOnly = getMappedValues(attributes, HTTP_HOSTS_PREFIX)
        hostsOnly.putAll(httpConfigHostsOnly)
        hostsOnly.each { key, value ->
            try {
                logger.debug("Constructing host with key: $key value: $value")
                if (hostType != null) {
                    constructHostByType(key, value, hostType)
                } else {
                    constructHost(key, value)
                }
            } catch (Exception e) {
                logger.error("Cannot construct host with key: $key value: $value due to $e")
                logger.trace "Details: ", e
            }
        }
        return hostList
    }

    /**
     * Overloaded method to extract the host specific info from the map and store in host object
     * where no Host type has been specified
     * @param attributes
     * @return List of host objects
     */
    public static List<Host> process(Map attributes) {
        return process(attributes, null)
    }

    /**
     * <p>Does the same as {@link #process(java.util.Map)} but doesn't populate the processing results to
     * the internal host pool, and returns the host hierarchy instead of semi-flat collection (node hosts are not added
     * to the result set, they're only represented in the parent host objects as nodes).</p>
     * <p>Can be used to parse properties into the list of {@link Host} objects.</p>
     * @param attributes
     * @return
     */
    public static List<Host> processIsolated(Map attributes) {
        Map hostsOnly = getMappedValues(attributes, HOSTS_PREFIX)
        Map httpConfigHostsOnly = getMappedValues(attributes, HTTP_HOSTS_PREFIX)
        hostsOnly.putAll(httpConfigHostsOnly)
        List<Host> results = new ArrayList<Host>()
        for (String hostName : hostsOnly.keySet()) {
            results.add(HostConstructor.constructHost(hostName, (Map) hostsOnly.get(hostName)))
        }

        return results
    }

    /**
     * Method to create each Host object, populate the attributes and add the Host object to the list of hosts
     * @param key - the attribute key
     * @param value - the attribute value
     * @return host - newly constructed host object
     */
    private static Host constructHost(String key, Map value) {
        logger.trace("Constructing host with $key and values $value")
        Host host = HostConstructor.constructHost(key, value)
        hostList.add(host)
        hostList.addAll(host.getNodes())
        logger.trace("Returning host ${host.dump()}")
        return host
    }

    /**
     * Method to create each Host object by type, populate the attributes and add the Host object to the list of hosts
     * @param key - the attribute key
     * @param value - the attribute value
     * @param hostType - the host type
     * @return host - newly constructed host object
     */
    private static Host constructHostByType(String key, Map value, HostType hostType) {
        logger.trace("Constructing host with $key and values $value")
        Host host = HostConstructor.constructHost(key, value)

        if (host.getType().equals(hostType)) {
            hostList.add(host)
            logger.trace("Returning host ${host.dump()}")
        }
        if (host.hasNodes()) {
            for (Host subHost : host.getNodes()) {
                if (subHost.getType().equals(hostType)) {
                    hostList.add(subHost)
                }
            }
        }
        return host
    }

}
