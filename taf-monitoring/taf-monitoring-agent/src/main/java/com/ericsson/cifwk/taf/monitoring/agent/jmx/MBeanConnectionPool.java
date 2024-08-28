package com.ericsson.cifwk.taf.monitoring.agent.jmx;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanConnectionPool extends GenericKeyedObjectPool<ConnectionDescription, JMXConnection> {

    public MBeanConnectionPool(){
        this(new MBeanConnectionFactory(),new GenericKeyedObjectPool.Config());
    }
    public MBeanConnectionPool(MBeanConnectionFactory factory, Config config) {
        super(factory, config);
    }

    Logger log = LoggerFactory.getLogger(MBeanConnectionPool.class);

    public void close(){
        try {
            super.close();
        } catch (Exception e) {
            log.error("Failed to close MBeanConnectionPool", e);
        }
        try {
            MBeanConnectionFactory.getJmxConnectorPool().close();
        } catch (Exception e) {
            log.error("Failed to close MBeanConnectionPool via JMX connector pool", e);
        }
    }


}
