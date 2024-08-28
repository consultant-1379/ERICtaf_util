package com.ericsson.cifwk.taf.data;

import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.HOSTS_PREFIX;
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.IP;
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.NODE;
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.ORIGINAL;
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.PORT;
import static com.ericsson.cifwk.taf.tools.http.HttpEndpoint.TUNNEL_STARTED_PROPERTY;
import static java.lang.String.format;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.exception.IncorrectHostConfigurationException;
import com.ericsson.cifwk.taf.data.resolver.HostPropertyResolver;
import com.ericsson.cifwk.taf.utils.ssh.JSchTunnel;
import com.ericsson.cifwk.taf.utils.ssh.PortForwarding;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Implicit tunneling should be replaced with explicit hopping using the
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">CLI Tool</a>
 */
@API(API.Quality.Deprecated)
@API.Since(2.37)
@Deprecated
public abstract class HostTunnelHelper {
    private static final Logger log = LoggerFactory.getLogger(HostTunnelHelper.class);

    private static TafConfiguration configuration = TafConfigurationProvider.provide();
    /**
     * Start of the dynamic range of ports
     */
    public static final int PORT_START_RANGE = 10_000;
    public static final int PORT_MAX_RANGE = 65_534;

    private static final String FORWARDING_HOST = "127.0.0.1";
    /**
     * Name of the host or node available via getHostname and setHostname
     */
    private String hostname;

    /**
     * IPv4 address of the host or node available via getIp and setIp
     */
    private String ip;

    /**
     * IPv6 address of the host or node available via getIp and setIp
     */
    private String namespace;

    /**
     * Namespace of the host or node available via getNamespace and setNamespace
     */
    String ipv6;

    /**
     * Port mappings for the host or node available via getPort and setPort
     */
    @SerializedName("ports")
    private Map<Ports, String> port;

    /**
     * Map to keep original port values after tunnel is opened
     */
    @SerializedName("originalPorts")
    private Map<Ports, String> originalPort;

    /**
     * Original IP kept for reference after tunnel is opened
     */
    private String originalIp;

    /**
     * List of tunnel port prefixes already in use
     */
    private static final List<String> offsetsInUse = new ArrayList();

    /**
     * Instance of J2SSH tunnel when instantatiated
     */
    private JSchTunnel tunnelInstance;

    /**
     * Private instance of prefix for intrnal use
     */
    @SerializedName("tunnel")
    private String tunnelOffset;
    /**
     * Variable to keep name of the parent host when found once
     */
    private String parentHost;

    public void setTunnelPortOffset(String prefix) {
        try {
            tunnelOffset = prefix;
            if (!isTunneled()) {
                log.warn("Implicit tunneling has been deprecated in TAF version 2.37 and will be removed in 5 releases");
                useOffset(prefix);
                log.debug("Starting tunnel for " + hostname);
                startTunnel();
            } else {
                log.trace(format("Tunnel for %s started already", hostname));
            }
        } catch (Exception e) {
            log.error("Tunnel for {} will not be started due to error: {}", hostname, e);
            log.trace("Details: {}", e);
        }
    }

    /**
     * Starts tunneling for host
     *
     * @throws IncorrectHostConfigurationException if host is not a node or port prefix is not set
     */
    public void startTunnel() throws IncorrectHostConfigurationException {
        checkTunelPrerequisites();
        log.debug("Starting tunnel for {} using remote ip {}, {} as SSH host and 127.0.0.1 as start of the tunnel",
            hostname, ip, getParentName());
        Map<Ports, String> newPorts = new HashMap();
        originalPort = new HashMap<>();
        for (Map.Entry<Ports, String> portEntry : port.entrySet()) {
            Ports portName = portEntry.getKey();
            String portValue = portEntry.getValue();
            log.trace("Setting tunnel for port {}", portName);
            boolean needToStart = true;
            int occupiedPortCounter = 0;
            while (needToStart) {
                String localPort = getLocalPort(portValue, occupiedPortCounter);
                try {
                    getTunnel().startTunnel(localPort, portValue, ip);
                    newPorts.put(portName, localPort);
                    originalPort.put(portName, portValue);
                    needToStart = false;
                    log.trace("Tunnel started on port {}", portValue);
                } catch (JSchException e) {
                    if (isTunnelAlreadySetup(localPort, portValue)) {
                        newPorts.put(portName, getPortAlreadySetUp(portValue));
                        originalPort.put(portName, portValue);
                        needToStart = false;
                    } else {
                        log.trace("Cannot use port {}. Trying next one", getLocalPort(portValue, occupiedPortCounter));
                        occupiedPortCounter++;
                    }
                }
            }
        }
        port = newPorts;
        log.debug("Tunnel for {} started. Overriding properties with ports {}", hostname, port);
        originalIp = ip;
        ip = FORWARDING_HOST;
        overrideProperties();
    }

    /**
     * Stops tunneling for host
     *
     * @throws IncorrectHostConfigurationException if host is not a node or port prefix is not set
     */
    public void stopTunnel() throws IncorrectHostConfigurationException {
        checkTunelPrerequisites();
        log.debug("Stopping SSH tunnel for {}", hostname);
        for (PortForwarding forwarding : getTunnel().getLocalForwardings()) {
            try {
                getTunnel().stopTunnel(forwarding.getLport());
            } catch (JSchException e) {
                log.error("Cannot stop SSH tunnel for {}", hostname);
            }
        }
        restoreProperties();
    }


    /**
     * Restarts the tunneling for host
     *
     * @throws IncorrectHostConfigurationException if host is not a node or port prefix is not set
     */
    public void reStartTunnel() throws IncorrectHostConfigurationException {
        checkTunelPrerequisites();
        log.trace("Restarting SSH tunnel for {}", hostname);

        stopTunnel();

        getTunnel().disconnect();

        for (Map.Entry<Ports, String> p : port.entrySet()) {
            log.trace("Starting tunnel for port {}", p.getKey());
            try {
                getTunnel().startTunnel(p.getValue(), originalPort.get(p.getKey()), originalIp);
            } catch (JSchException e) {
                log.debug("The configuration name already exists");
            }
        }
    }

    /**
     * Provides information if tunnel for node is enabled
     *
     * @return boolean
     */
    public boolean isTunneled() {
        boolean result = false;
        try {
            result = configuration.getProperty(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, TUNNEL_STARTED_PROPERTY), Boolean.class);
        } catch (Exception e) {
            log.debug("Cannot get tunnel status for {} due to {}", hostname, e);
            log.trace("Details: {}", e);
        }
        return result;
    }

    /**
     * Get tunnel port prefix value
     *
     * @return String
     */
    public String getTunnelPortOffset() {
        return tunnelOffset;
    }

    /**
     * Get the tunnel object so ports can be forwarded
     *
     * @return JSchTunnel
     */
    protected JSchTunnel getTunnel() {
        if (tunnelInstance == null) {
            Map properties = Maps.filterKeys(configuration.getProperties(), new Predicate<Object>() {
                @Override
                public boolean apply(Object input) {
                    String key = (String) input;
                    return key.startsWith(Joiner.on(".").join(HOSTS_PREFIX, getParentName())) && !key.contains("." + NODE);
                }
            });
            if(isPhysical()){
                log.debug("Creating tunnel using LMS");
                Map LMSproperties = Maps.filterKeys(configuration.getProperties(), new Predicate<Object>() {
                    @Override
                    public boolean apply(Object input) {
                        String key = (String) input;
                        return key.startsWith(Joiner.on(".").join(HOSTS_PREFIX, "ms1")) && !key.contains("." + NODE);
                    }
                });
                Host ms = HostPropertyResolver.findHostProperty(LMSproperties).get(0);
                log.warn("Parent is LMS: {}", ms);
                tunnelInstance = new JSchTunnel(ms.getIp(), ms.getPort().get(Ports.SSH), ms.getUser(), ms.getPass());
            }
            else{
                log.debug("Creating tunnel using SSH host {}", getParentName());
                log.trace("Props: {}", properties);
                Host parent = HostPropertyResolver.findHostProperty(properties).get(0);
                log.warn("Parent: {}", parent);
                tunnelInstance = new JSchTunnel(parent.getIp(), parent.getPort().get(Ports.SSH), parent.getUser(), parent.getPass());
            }

        }
        return tunnelInstance;
    }

    private boolean isPhysical(){
        Map properties = Maps.filterKeys(configuration.getProperties(), new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                String key = (String) input;
                return key.startsWith(Joiner.on(".").join(HOSTS_PREFIX, "ms1")) && !key.contains("." + NODE);
            }
        });
        System.out.println("The MS1 properties are " + properties);
        Host ms = HostPropertyResolver.findHostProperty(properties).get(0);
        return !(ms == null || Strings.isNullOrEmpty(ms.getIp()) || ms.getIp().equalsIgnoreCase("0.0.0.0"));
    }

    protected String getLocalPort(String remotePort) {
        return getLocalPort(remotePort, 0);
    }

    /**
     * Method to create local port for forwarding; Port is always in range over 10000
     *
     * @param remotePort
     * @param additinalOffset optional parameter used if iterating trough ports
     * @return String
     */
    protected String getLocalPort(String remotePort, int additinalOffset) {
        int localPort = Integer.parseInt(remotePort);
        if (localPort < PORT_START_RANGE) {
            localPort += PORT_START_RANGE;
        }
        localPort += Integer.parseInt(getTunnelPortOffset()) + additinalOffset;
        if (localPort > PORT_MAX_RANGE)
            localPort = Integer.parseInt(getLocalPort(remotePort, localPort - PORT_MAX_RANGE));
        return String.valueOf(localPort);
    }

    protected boolean isTunnelAlreadySetup(final String lport, final String rport) {
        Collection<PortForwarding> filtered = Collections2.filter(getTunnel().getLocalForwardings(), new Predicate<PortForwarding>() {
            @Override
            public boolean apply(PortForwarding forwarding) {
                return forwarding.getHost().equals(ip) && forwarding.getRport().equals(rport) && forwarding.getLport().equals(lport);
            }
        });
        return !filtered.isEmpty();
    }

    /**
     * Method to fetch local port from already created configurations
     *
     * @param portValue
     * @return String
     */
    protected String getPortAlreadySetUp(final String portValue) {
        Collection<PortForwarding> filtered = Collections2.filter(getTunnel().getLocalForwardings(), new Predicate<PortForwarding>() {
            @Override
            public boolean apply(PortForwarding forwarding) {
                return forwarding.getHost().equals(ip) && forwarding.getRport().equals(portValue);
            }
        });

        return Iterables.getLast(filtered).getLport();
    }

    /**
     * Method to lock the tunnel port prefix
     *
     * @param offsetNo
     * @return
     * @throws IncorrectHostConfigurationException if prefix is already in use
     */
    private static synchronized boolean useOffset(String offsetNo) throws IncorrectHostConfigurationException {
        return offsetsInUse.add(offsetNo);
    }

    /**
     * Get parent for the host
     *
     * @return
     * @throws IncorrectHostConfigurationException if host is not a node and parent name cannot be found
     */
    public String getParentName() throws IncorrectHostConfigurationException {
        if (Strings.isNullOrEmpty(parentHost)) {
            log.trace("Looking for parent of {}", hostname);
            try {
                parentHost = retrieveParentName(configuration);
                log.trace("Got parent {} for {}", parentHost, hostname);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.trace("Problem getting parent node. ", e);
                String msg = format("Cannot find parent host for node %s based on name and IP: %s", hostname, ip);
                throw new IncorrectHostConfigurationException(msg);
            }
        }
        return parentHost;
    }

    /**
     * Set the parent host name
     * @param parentName Name of the parent host
     * @return Parent host name
     */
    public String setParentName(String parentName){
        return parentHost = parentName;
    }

    /**
     * Retirievese parent name from configuration
     *
     * @param configuration
     * @return
     */
    private String retrieveParentName(TafConfiguration configuration) {
        String parentName = null;
        for (Map.Entry property : configuration.getProperties().entrySet()) {
            if (!(property.getKey() instanceof String) || (!(property.getValue() instanceof String))) {
                continue;
            }
            String key = (String) property.getKey();
            String value = (String) property.getValue();
            if (key.endsWith("." + NODE + "." + hostname + "." + IP) && value.equals(ip)) {
                parentName = key.split("\\.")[1];
                break;
            }
        }
        return parentName;
    }

    /**
     * Method to check if tunneling can be enabled for node
     *
     * @throws IncorrectHostConfigurationException
     */
    private void checkTunelPrerequisites() throws IncorrectHostConfigurationException {
        if (Strings.isNullOrEmpty(getParentName())) {
            throw new IncorrectHostConfigurationException("There is no SSH host defined for this node or host is not correct");
        }
        if ((Strings.isNullOrEmpty(getTunnelPortOffset())) || (!isNumber(getTunnelPortOffset()))) {
            throw new IncorrectHostConfigurationException("Tunnel port offset is not defined (correctly)");
        }
    }

    /**
     * Get attributes from DataHandler so they will be overriden
     *
     * @return Map
     */
    private Map<Object, Object> getAttributesToModify() {
        log.trace("Using finder for attributes to modify: {}.{}.{}.{}.{}", HOSTS_PREFIX, getParentName(), NODE, hostname, PORT);
        return Maps.filterKeys(configuration.getProperties(), new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                String key = (String) input;
                return key.startsWith(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, PORT));
            }
        });
    }

    /**
     * Override DataHandler properties so next time host will be constructed with tunneling
     */
    private void overrideProperties() {
        for (Map.Entry attribute : getAttributesToModify().entrySet()) {
            String key = (String) attribute.getKey();
            String value = (String) attribute.getValue();
            String portName = key.split("." + PORT + ".")[1].toUpperCase();
            log.trace("Setting {}", key);
            configuration.setProperty(key.replace(PORT, ORIGINAL + "." + PORT), value);
            configuration.setProperty(key, port.get(Ports.valueOf(portName)));
            log.trace("{} set to: ", TafConfigurationProvider.provide().getProperty(key));
        }
        configuration.setProperty(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, ORIGINAL, IP), originalIp);
        configuration.setProperty(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, IP), ip);
        configuration.setProperty(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, TUNNEL_STARTED_PROPERTY), true);
        if (log.isTraceEnabled()) {
            log.trace("Properties after change:");
            Map<Object, Object> properties = Maps.filterKeys(configuration.getProperties(), new Predicate<Object>() {
                @Override
                public boolean apply(Object input) {
                    String key = (String) input;
                    return key.startsWith(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname));
                }
            });
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                log.trace("Attr:{}: {}", property.getKey(), property.getValue());
            }
        }
    }

    /**
     * Restore attributes to original after tunneling is disabled
     */
    private void restoreProperties() {
        for (Object key : getAttributesToModify().keySet()) {
            configuration.clearProperty((String) key);
        }
        configuration.clearProperty(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, ORIGINAL, IP));
        configuration.clearProperty(Joiner.on(".").join(HOSTS_PREFIX, getParentName(), NODE, hostname, TUNNEL_STARTED_PROPERTY));
    }

    /**
     * PRIVATE Util method to check if string is a number
     *
     * @param str
     * @return
     */
    private static boolean isNumber(String str) {
        try {
            new BigDecimal(str.trim());
            return true;
        } catch (NumberFormatException var2) {
            return false;
        }
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getIpv6() {
        return ipv6;
    }

    public Map<Ports, String> getPort() {
        return port;
    }

    public Map<Ports, String> getOriginalPort() {
        return originalPort;
    }

    public String getOriginalIp() {
        return originalIp;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(Map<Ports, String> port) {
        this.port = port;
    }

    public void setOriginalIp(String originalIp) {
        this.originalIp = originalIp;
    }

    public void setOriginalPort(Map<Ports, String> originalPort) {
        this.originalPort = originalPort;
    }
}
