package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MBeanConnectionFactory extends BaseKeyedPoolableObjectFactory<ConnectionDescription, JMXConnection> {

    private static final JmxConnectorPool jmxConnectorPool = new JmxConnectorPool();
    Logger log = LoggerFactory.getLogger(MBeanConnectionFactory.class);

    @Override
    public JMXConnection makeObject(ConnectionDescription key) throws Exception {
        log.info("Creating JMXConnection");
        final JMXConnector jmxc = (JMXConnector) jmxConnectorPool.borrowObject(key.getUrl());
        final MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        final ObjectName objectName = new ObjectName(key.getBeanName());
        final List mBeanProxies = new LinkedList();
        mBeanProxies.add(new SimpleMBean(mbsc,objectName));
        final JMXConnection jmxBean = new JMXConnection(jmxc, mBeanProxies);
        jmxConnectorPool.returnObject(key.getUrl(),jmxc);
        return jmxBean;
    }

    public void destoryObject(ConnectionDescription key, JMXConnection jmxConnection) throws IOException {
        jmxConnection.close();
    }

    public static JmxConnectorPool getJmxConnectorPool() {
        return jmxConnectorPool;
    }


}
