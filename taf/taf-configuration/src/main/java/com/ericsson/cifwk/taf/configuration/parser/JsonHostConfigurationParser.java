package com.ericsson.cifwk.taf.configuration.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Iterator;

class JsonHostConfigurationParser implements ConfigurationParser {

    private static final String IPV4 = "ipv4";
    private static final String IPV6 = "ipv6";
    private static final String STORAGE = "storage";
    private static final String INTERNAL = "internal";
    private static final String JGROUP = "jgroup";

    @Override
    public Configuration parse(InputStream is) throws ConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // validate JSON properties
            JsonNode node = mapper.readTree(is);
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            JsonNode schemaNode = JsonLoader.fromResource("/schema/hosts.properties.schema.json");
            JsonSchema schema = factory.getJsonSchema(schemaNode);
            ProcessingReport processingReport = schema.validate(node);
            if (processingReport != null && processingReport.isSuccess()) {
                for (JsonNode host : node) {
                    String hostname = host.get("hostname").asText();
                    if (!hostname.equalsIgnoreCase("null")) {
                        String prefix = "host." + hostname;
                        config.append(hostConfiguration(host, prefix));
                    }
                }
            } else {
                throw new ConfigurationException("JSON properties file is not valid. " + (processingReport != null ? processingReport.iterator().next() : ""));
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
        return config;
    }

    void processInterfaces(PropertiesConfiguration conf, JsonNode iface, String prefix, boolean setByPrivate) {
        final String ipv4Address = checkForIpvNodes(iface, IPV4);
        final String ipv6Address = checkForIpvNodes(iface, IPV6);
        if (StringUtils.isNotEmpty(ipv4Address) && !ipv4Address.equalsIgnoreCase("null"))
            if (conf.getProperty(prefix + ".ip") == null || setByPrivate) {
                conf.addProperty(prefix + ".ip", ipv4Address);
            }
        if (StringUtils.isNotEmpty(ipv6Address) && !ipv6Address.equalsIgnoreCase("null"))
            if (conf.getProperty(prefix + ".ipv6") == null) {
                conf.addProperty(prefix + ".ipv6", ipv6Address);
            }
        if (iface.has("ports")) {
            JsonNode ports = iface.get("ports");
            Iterator<String> i = ports.fieldNames();
            while (i.hasNext()) {
                String portType = i.next();
                if (conf.getProperty(prefix + ".port." + portType) == null || setByPrivate) {
                    conf.addProperty(prefix + ".port." + portType, ports.get(portType).asText());
                }
            }
        }
    }

    private String checkForIpvNodes(JsonNode iface, String type) {
        return (iface.get(type) != null) ? iface.get(type).asText() : null;
    }

    void processInternalStorageAndJgroupInterfaces(PropertiesConfiguration conf, JsonNode iface, String prefix, String interfaceType) {
        final String ipv4Address = checkForIpvNodes(iface, IPV4);
        final String prefixWithInterfaceType = String.format("%s.%s", prefix, interfaceType);
        if (StringUtils.isNotEmpty(ipv4Address) && !ipv4Address.equalsIgnoreCase("null"))
            if (conf.getProperty(String.format("%s.ip", prefixWithInterfaceType)) == null) {
                conf.addProperty(String.format("%s.ip", prefixWithInterfaceType), ipv4Address);
            }
        if (iface.has("ports")) {
            JsonNode ports = iface.get("ports");
            Iterator<String> i = ports.fieldNames();
            while (i.hasNext()) {
                String portType = i.next();
                if (conf.getProperty(String.format("%s.port.%s", prefixWithInterfaceType, portType)) == null) {
                    conf.addProperty(String.format("%s.port.%s", prefixWithInterfaceType, portType), ports.get(portType).asText());
                }
            }
        }
        if (iface.has("tunnel")) {
            conf.addProperty(String.format("%s.tunnel", prefixWithInterfaceType), iface.get("tunnel").asText());
        }
    }

    Configuration hostConfiguration(JsonNode host, String prefix) {
        PropertiesConfiguration conf = new PropertiesConfiguration();
        conf.addProperty(prefix + ".type", host.get("type").asText());
        if (host.has("ip")) {
            if (conf.getProperty(prefix + ".ip") != null) {
                conf.clearProperty(prefix + ".ip");
            }
            conf.addProperty(prefix + ".ip", host.get("ip").asText());
        }
        if (host.has("ports")) {
            JsonNode ports = host.get("ports");
            Iterator<String> i = ports.fieldNames();
            while (i.hasNext()) {
                String portType = i.next();
                if (conf.getProperty(prefix + ".port." + portType) != null) {
                    conf.clearProperty(prefix + ".port." + portType);
                }
                conf.addProperty(prefix + ".port." + portType, ports.get(portType).asText());
            }
        }
        if (host.has("offset")) {
            conf.addProperty(prefix + ".offset", host.get("offset").asText());
        }
        if (host.has("tunnel")) {
            conf.addProperty(prefix + ".tunnel", host.get("tunnel").asText());
        }
        if (host.has("group")) {
            conf.addProperty(prefix + ".group", host.get("group").asText());
        }
        if (host.has("unit")) {
            conf.addProperty(prefix + ".unit", host.get("unit").asText());
        }
        if (host.has("users")) {
            JsonNode users = host.get("users");
            for (JsonNode user : users) {
                String name = user.get("username").asText();
                conf.addProperty(prefix + ".user." + name + ".pass", user.get("password").asText());
                if (user.get("type") != null) {
                    conf.addProperty(prefix + ".user." + name + ".type", user.get("type").asText());
                }
            }
        }
        if (host.has("interfaces")) {
            for (JsonNode iface : host.get("interfaces")) {
                boolean setByPrivate = false;
                if (iface.get("type").asText().equalsIgnoreCase("public")) {
                    processInterfaces(conf, iface, prefix, setByPrivate);
                } else if (iface.get("type").asText().equalsIgnoreCase(INTERNAL)) {
                    processInterfaces(conf, iface, prefix, setByPrivate);
                    processInternalStorageAndJgroupInterfaces(conf, iface, prefix, INTERNAL);
                } else if (iface.get("type").asText().equalsIgnoreCase(STORAGE)) {
                    processInternalStorageAndJgroupInterfaces(conf, iface, prefix, STORAGE);
                } else if (iface.get("type").asText().equalsIgnoreCase(JGROUP)) {
                    processInternalStorageAndJgroupInterfaces(conf, iface, prefix, JGROUP);
                }
            }
        }
        if (host.has("nodes")) {
            JsonNode nodes = host.get("nodes");
            for (JsonNode node : nodes) {
                String nodename = node.get("hostname").asText();
                if (!nodename.equalsIgnoreCase("null")) {
                    conf.append(hostConfiguration(node, prefix + ".node." + nodename));
                }
            }
        }
        if (host.has("iloInfo")) {
            JsonNode ilo = host.get("iloInfo");
            JsonNode iloHostName = ilo.get("hostname");
            if (iloHostName != null) {
                String iloName = iloHostName.asText();
                if (!iloName.equalsIgnoreCase("null")) {
                    conf.append(hostConfiguration(ilo, prefix + ".iloInfo." + iloName));
                }
            }
        }
        return conf;
    }
}
