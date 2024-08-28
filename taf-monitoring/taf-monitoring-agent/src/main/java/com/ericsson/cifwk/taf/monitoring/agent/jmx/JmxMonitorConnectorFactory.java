package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

public class JmxMonitorConnectorFactory extends BaseKeyedPoolableObjectFactory<JMXServiceURL,JMXConnector>{

    @Override
    public JMXConnector makeObject(JMXServiceURL url) throws IOException {
        return JMXConnectorFactory.connect(url);
    }

    public void destroyObject(JMXServiceURL url, JMXConnector jmxConnector) throws IOException {
        jmxConnector.close();
    }
}
