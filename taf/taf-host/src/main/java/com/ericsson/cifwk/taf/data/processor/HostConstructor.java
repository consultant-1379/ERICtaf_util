package com.ericsson.cifwk.taf.data.processor;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public final class HostConstructor {

    private static final Logger logger = LoggerFactory.getLogger(HostConstructor.class);

    public static final String PORT = "port";
    public static final String TYPE = "type";
    public static final String USER = "user";
    public static final String USER_PASS = "pass";
    public static final String USER_TYPE = "type";
    public static final String IP = "ip";
    public static final String IPV6 = "ipv6";
    public static final String NODE = "node";
    public static final String ILO = "iloInfo";
    public static final String OFFSET = "offset";
    public static final String TUNNEL_OFFSET = "tunnel";
    public static final String UNIT = "unit";
    public static final String ORIGINAL = "original";
    public static final String GROUP = "group";
    public static final String NAMESPACE = "namespace";


    public static Host constructHost(String key, Map<String, Object> value) {
        Host host = new Host();
        host.setHostname(key);
        Map<Ports, String> ports = getPorts(value);
        if (ports != null) {
            host.setPort(ports);
        }
        HostType type = getType(value);
        if (type != null) {
            host.setType(type);
        }
        List<User> users = getUsers(value);
        if (users != null) {
            host.setUsers(users);
        }
        String namespace =  getNamespace(value);
        if(namespace != null) {
            host.setNamespace(namespace);
        }
        host.setIp(getIp(value));
        host.setIpv6(getIpv6(value));

        List<Host> nodes = getNodes(value);
        if (nodes != null) {
            host.setNodes(nodes);
        }

        Host ilo = getIlo(value);
        if (ilo != null) {
            host.setIlo(ilo);
        }

        String originalIp = getOriginalIp(value);
        if (originalIp != null) {
            host.setOriginalIp(originalIp);
        }
        Map<Ports, String> originalPorts = getOriginalPorts(value);
        if (originalPorts != null) {
            host.setOriginalPort(originalPorts);
        }

        host.setOffset(getOffset(value));
        host.setGroup(getGroup(value));
        host.setUnit(getUnit(value));

        String tunnelOffset = getTunnelOffset(value);
        if (tunnelOffset != null) {
            host.setTunnelPortOffset(tunnelOffset);
        }

        return host;
    }

    private static String getNamespace(Map<String, Object> namespaceValue) {
        return getAsString(namespaceValue, NAMESPACE);
    }

    private static Map<Ports, String> getPorts(Map<String, Object> hostValue) {
        final Map<Ports, String> result = new HashMap<>();
        Map<String, Object> hostPorts = getAsMap(hostValue, PORT);
        if (hostPorts.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, Object> entry : hostPorts.entrySet()) {
            Ports portEnum;
            try {
                String portName = entry.getKey();
                portEnum = Ports.valueOf(portName.toUpperCase());
                result.put(portEnum, entry.getValue().toString());
            } catch (IllegalArgumentException e) {
                logger.error("Cannot process port data: (Key: $portKey Value: $portValue)", e);
            }
        }
        return result;
    }

    protected static HostType getType(Map<String, Object> hostValue) {
        String hostType = getAsString(hostValue, TYPE);
        if (hostType != null) {
            try {
                return HostType.valueOf(hostType.toUpperCase());
            }catch(IllegalArgumentException e) {
                return HostType.UNEXPECTED;
            }
        } else {
            return HostType.UNKNOWN;
        }
    }

    private static List<User> getUsers(Map<String, Object> hostValue) {
        final List<User> result = new ArrayList<>();
        Map<String, Object> hostUsers = getAsMap(hostValue, USER);
        if (hostUsers.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, Object> entry : hostUsers.entrySet()) {
            try {
                User user = constructUser(entry.getKey(), (Map<String, Object>) entry.getValue());
                result.add(user);
            } catch (Exception e) {
                logger.error("Cannot process user data: (Key: " + entry.getKey()
                        + " Value: " + entry.getValue() + ")", e);
            }
        }
        return result;
    }

    private static User constructUser(String key, Map<String, Object> value) {
        User user = new User();
        user.setUsername(key);
        String pass = getAsString(value, USER_PASS);
        if (pass != null) {
            user.setPassword(pass);
        }
        try {
            String type = getAsString(value, USER_TYPE);
            if (type != null) {
                UserType userType = UserType.valueOf(type.toUpperCase());
                user.setType(userType);
            }
        } catch (IllegalArgumentException noType) {
            // ignore
        }
        return user;
    }

    private static String getIp(Map<String, Object> hostValue) {
        return getAsString(hostValue, IP);
    }

    private static String getIpv6(Map<String, Object> hostValue) {
        return getAsString(hostValue, IPV6);
    }

    protected static List<Host> getNodes(final Map<String, Object> hostValue) {
        final List<Host> result = new ArrayList<>();
        Map<String, Object> nodes = getAsMap(hostValue, NODE);
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            for (Map.Entry<String, Object> entry : nodes.entrySet()) {
                try {
                    Host host = constructHost(entry.getKey(), (Map) entry.getValue());
                    result.add(host);
                    result.addAll(host.getNodes());
                } catch (Exception e) {
                    logger.error("Cannot process node data: (Key: " + entry.getKey()
                            + " Value: " + entry.getValue() + ") ", e);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot construct node base on node values " + String.valueOf(nodes));
        }
        return result;
    }

    protected static Host getIlo(final Map<String, Object> hostValue) {
        Map<String, Object> iloList = getAsMap(hostValue, ILO);
        if (iloList.isEmpty()) {
            return null;
        }
        Map.Entry<String, Object> entry = iloList.entrySet().iterator().next();

        Host ilo = constructHost(entry.getKey(), (Map) entry.getValue());
        return ilo;
    }

    private static String getOffset(Map<String, Object> hostValue) {
        return getAsString(hostValue, OFFSET);
    }

    private static String getUnit(Map<String, Object> hostValue) {
        return getAsString(hostValue, UNIT);
    }

    private static String getTunnelOffset(Map<String, Object> hostValue) {
        return getAsString(hostValue, TUNNEL_OFFSET);
    }

    private static Map<Ports, String> getOriginalPorts(Map<String, Object> hostValue) {
        Map<String, Object> beforeOffset = getAsMap(hostValue, ORIGINAL);
        if (!beforeOffset.isEmpty()) {
            return getPorts(beforeOffset);
        } else {
            return Collections.emptyMap();
        }
    }

    private static String getOriginalIp(Map<String, Object> hostValue) {
        Map<String, Object> beforeOffset = getAsMap(hostValue, ORIGINAL);
        if (!beforeOffset.isEmpty()) {
            return getIp(beforeOffset);
        } else {
            return null;
        }
    }

    private static String getGroup(Map<String, Object> hostValue) {
        return getAsString(hostValue, GROUP);
    }

    private static String getAsString(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (!(value instanceof String)) {
            return null;
        }
        return (String) value;
    }

    private static Map<String, Object> getAsMap(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            return Collections.emptyMap();
        }
        Object value = map.get(key);
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        return (Map<String, Object>) value;
    }

}
