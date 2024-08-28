package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.Map;

/**
 * Command line builder facility
 */
interface CommandLineBuilder {

    void start();

    void end();

    void startCommand(String command);

    void endCommand();

    void beforeParameters(Map<NetSimCommandEmitter.AttributeKey, Object> attributeMap);

    void addParameter(NetSimCommandEmitter.AttributeKey key, Object value, boolean hasNext);

    String build();
}
