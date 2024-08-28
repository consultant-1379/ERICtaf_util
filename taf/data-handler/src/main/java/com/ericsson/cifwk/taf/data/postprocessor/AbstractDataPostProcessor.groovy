package com.ericsson.cifwk.taf.data.postprocessor

import com.ericsson.cifwk.taf.data.parser.JsonParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Helper class for Data Post Processors
 * @author erafkos, evicovc
 */
abstract class AbstractDataPostProcessor {

    private static Logger logger = LoggerFactory.getLogger(AbstractDataPostProcessor)

    private static ConfigSlurper propertiesParser = new ConfigSlurper()

    /**
     * Closure to filter out incorrect lines:
     * - does not contain any property after prefix and name
     * - does not contain value
     */
    private static Closure findIncorrectLines = { remove, prefix, key, value ->
        boolean evaluationResult = (key.toString().split(/\./).size() > 2 && value)
        if (!evaluationResult) {
            logger.warn "Property line $key=$value is incorrect for $prefix configuration " + ((!remove) ? "removing" : "")
        }
        return evaluationResult || !remove
    }

    /**
     * Get property values as tree of maps
     * @param attributes
     * @param prefix
     * @return
     */
    protected static Map getMappedValues(Map attributes, String prefix, boolean removeIncorrectLines = true) {
        logger.trace "Looking at map $attributes"
        Map attrs = attributes.findAll { key, value ->
            key.toString().startsWith("$prefix.") && !key.toString().startsWith("${prefix}.json.")
        }.findAll(findIncorrectLines.curry(removeIncorrectLines, prefix))
        logger.trace "Attributes filtered for prefix $prefix: $attrs"

        Map result = propertiesParser.parse(attrs as Properties)[prefix]
        attrs = attributes.findAll{ key, value ->
            key.toString().startsWith("${prefix}.json.")
        }.findAll(findIncorrectLines.curry(removeIncorrectLines, prefix))
        logger.trace "Attributes filtered for prefix ${prefix}.json: $attrs"
        mergeJsonAttrs(attrs, result)
        logger.trace "Mapped values $result"
        return result
    }

    private static void mergeJsonAttrs(Map attrs, Map result) {
        if (attrs.isEmpty()) {
            return
        }
        logger.trace "Attrs $attrs"
        attrs.each { String key, String value ->
            List<ConfigObject> configs = JsonParser.parse(value);

            String hostname;
            for (ConfigObject config : configs) {
                hostname = config.get('hostname');

                if (config.get('ports') != null) {
                    config.put('port', convertPorts((LinkedHashMap) config.get('ports')));
                    config.remove('ports');
                }
                if (config.get('nodes') != null) {
                    ConfigObject nodes = convertNodes((List) config.get('nodes'));
                    config.put('node', nodes);
                    config.remove('nodes');
                }
                if (config.get('users') != null) {
                    ConfigObject users = convertUsers((List) config.get('users'));
                    config.put('user', users)
                    config.remove('users');
                }
                result.put(hostname, config);
                config.remove('hostname');
            }
        }
        logger.trace "result $result"
    }

    private static LinkedHashMap convertPorts(LinkedHashMap ports) {
        for (String key : ports.keySet()) {
            if (ports[key] instanceof Double) {
                ports[key] = ((Double) ports[key]).intValue();
            }
            ports[key] = ports[key].toString();
        }
        return ports;
    }

    private static ConfigObject convertNodes(List<LinkedHashMap> nodes) {
        ConfigObject nodesObj = new ConfigObject();
        String nodeName;
        for (LinkedHashMap node : nodes) {
            ConfigObject nodeObj = new ConfigObject();

            for (String key : node.keySet()) {
                logger.trace ("json 2 props $key : ${node.get(key)}")
                nodeObj.put(key, node.get(key));
            }

            nodeName = nodeObj.get("hostname");

            if (nodeObj.get('users') != null) {
                ConfigObject users = convertUsers((List) nodeObj.get('users'));
                nodeObj.put('user', users);
                nodeObj.remove('users');
            }
            nodeObj.remove("hostname");

            nodesObj.put(nodeName, nodeObj);
        }
        return nodesObj;
    }

    private static ConfigObject convertUsers(List users) {
        ConfigObject usersObj = new ConfigObject();
        String userName;
        for (LinkedHashMap user : users) {
            userName = user.get("username");
            user.remove("username");

            if (user.get("password")) {
                user.put("pass", user.get("password"));
                user.remove("password");
            }

            usersObj.put(userName, user);
        }
        return usersObj;
    }
}
