package com.ericsson.cifwk.taf.configuration;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(Internal)
enum HostPropertyParser {
    IP {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String ip = str(hostProperty.getValue());
            builder.withIp(ip);
        }
    },

    IPV6 {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String ip = str(hostProperty.getValue());
            builder.withIpv6(ip);
        }
    },

    TYPE {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String hostType = str(hostProperty.getValue());
            builder.withType(hostType);
        }
    },

    GROUP {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String group = str(hostProperty.getValue());
            builder.withGroup(group);
        }
    },

    UNIT {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String unit = str(hostProperty.getValue());
            builder.withUnit(unit);
        }
    },

    OFFSET {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String offset = str(hostProperty.getValue());
            builder.withOffset(offset);
        }
    },

    ILOINFO {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            List<GenericHostBuilder> nodes = parseHosts(checkMap(hostProperty.getValue()));
            builder.withIlo(getOnlyElement(nodes));
        }
    },

    USER {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            Map<String, Object> usersProperties = checkMap(hostProperty.getValue());
            for (Entry<String, Object> user : usersProperties.entrySet()) {
                try {
                    Map<String, Object> userProperties = checkMap(user.getValue());
                    String username = user.getKey();
                    String password = str(userProperties.get("pass"));
                    UserType type = UserType.getByName(str(userProperties.get("type")));

                    builder.withUser(new User(username, password, type));
                } catch (Exception e) {
                    LOGGER.error("Invalid user " + usersProperties, e);
                }
            }
        }
    },

    PORT {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            Map<String, Object> portsProperties = checkMap(hostProperty.getValue());
            for (Entry<String, Object> port : portsProperties.entrySet()) {
                Integer value = Integer.valueOf(str(port.getValue()));
                builder.withPort(port.getKey(), value);
            }
        }
    },

    NODE {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            List<GenericHostBuilder> nodes = parseHosts(checkMap(hostProperty.getValue()));
            builder.withNodes(nodes);
        }
    },


    /**
     * Address runtime overriding in HostTunnelHelper#overrideProperties()
     */
    ORIGINAL {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            Map<String, Object> fixHostFormatInconsistency = ImmutableMap.of(hostProperty.getKey(), hostProperty.getValue());
            List<GenericHostBuilder> nodes = parseHosts(fixHostFormatInconsistency);
            builder.withOriginalIp(getOnlyElement(nodes).getIp());
            builder.withOriginalPort(getOnlyElement(nodes).getPorts());
        }
    },

    TUNNEL {
        @Override
        void parse(GenericHostBuilder builder, Entry<String, Object> hostProperty) {
            String tunnelPortOffset = str(hostProperty.getValue());
            builder.withTunnelPortOffset(tunnelPortOffset);
        }
    },

    UNKNOWN {
        void parse(GenericHostBuilder hostBuilder, Entry<String, Object> hostProperty) {
            LOGGER.warn("Property `{}.{}` unrecognized, therefore ignored", hostProperty.getKey(), hostProperty.getValue());
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(HostPropertyParser.class);

    abstract void parse(GenericHostBuilder hostBuilder, Entry<String, Object> hostProperty);

    public static HostPropertyParser getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    static List<GenericHostBuilder> propsToHosts(Map<? extends Object, ? extends Object> properties) {
        Map<String, Object> hostPropertyTree = buildHostPropertyTree(properties);
        return parseHosts(hostPropertyTree);
    }

    @VisibleForTesting
    static Map<String, Object> buildHostPropertyTree(Map<?, ?> properties) {
        Map<String, Object> hostPropertyTree = Maps.newHashMap();
        for (Entry<? extends Object, ? extends Object> property : properties.entrySet()) {
            try {
                String key = str(property.getKey());
                if (key.startsWith("host.")) {
                    String childKey = split(key)[1];
                    put(hostPropertyTree, childKey, property.getValue());
                }
            } catch (Exception e) {
                LOGGER.error("Invalid Host Property `{}.{}`", property.getKey(), property.getValue(), e);
            }
        }
        return hostPropertyTree;
    }

    private static List<GenericHostBuilder> parseHosts(Map<String, Object> hostPropertyTree) {
        List<GenericHostBuilder> hosts = Lists.newArrayList();
        for (Entry<String, Object> nameHostProperties : hostPropertyTree.entrySet()) {

            GenericHostBuilder builder = GenericHostBuilder.build()
                    .withName(nameHostProperties.getKey());

            Map<String, Object> hostProperties = checkMap(nameHostProperties.getValue());
            for (Entry<String, Object> hostProperty : hostProperties.entrySet()) {
                try {
                    String key = hostProperty.getKey();
                    HostPropertyParser.getByName(key).parse(builder, hostProperty);
                } catch (Exception e) {
                    LOGGER.error("Unable to parse property `{}.{}`", hostProperty.getKey(), hostProperty.getValue(), e);
                }
            }
            hosts.add(builder);
            hosts.addAll(builder.nodes);
        }

        return hosts;
    }

    private static void put(Map<String, Object> hostPropertyTree, String key, Object value) {
        if (!key.contains(".")) {
            hostPropertyTree.put(key, value);
        } else {
            String[] split = split(key);
            String parent = split[0];
            String childKey = split[1];
            if (!hostPropertyTree.containsKey(parent)) {
                hostPropertyTree.put(parent, Maps.newHashMap());
            }

            Object nextParent = hostPropertyTree.get(parent);

            if (nextParent instanceof Map) {
                put((Map<String, Object>) nextParent, childKey, value);
            } else {
                LOGGER.error("Invalid parent `{}` for Host Property `{}.{}`. Expected map, got object `{}`.",
                        parent, key, value, nextParent);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> checkMap(Object shouldBeMap) {
        checkArgument(shouldBeMap instanceof Map, "Expected map, got " + shouldBeMap);
        return (Map<String, Object>) shouldBeMap;
    }

    /**
     * Splits String on first dot only
     */
    private static String[] split(String key) {
        checkArgument(key.contains("."));
        checkArgument(key.lastIndexOf(".") + 1 != key.length());
        return key.split("\\.", 2);
    }

    private static String str(Object value) {
        return "" + value;
    }
}
