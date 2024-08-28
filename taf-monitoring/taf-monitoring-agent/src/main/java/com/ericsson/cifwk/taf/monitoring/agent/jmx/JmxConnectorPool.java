package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

public class JmxConnectorPool extends GenericKeyedObjectPool<JMXServiceURL, JMXConnector> {

    public JmxConnectorPool(){
        this(new JmxMonitorConnectorFactory(),new GenericKeyedObjectPool.Config());
    }
    public JmxConnectorPool(JmxMonitorConnectorFactory jmxMonitorConnectorFactory, GenericKeyedObjectPool.Config config){
        super(jmxMonitorConnectorFactory, config);
    }

}
