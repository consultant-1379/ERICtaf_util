package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.monitoring.agent.jmx.ConnectionDescription;
import com.ericsson.cifwk.taf.monitoring.agent.jmx.JMXConnection;
import com.ericsson.cifwk.taf.monitoring.agent.jmx.MBeanConnectionPool;
import com.ericsson.cifwk.taf.monitoring.agent.jmx.SimpleMBean;
import com.ericsson.taf.graphite.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public class JMXMonitor implements Monitor {

    private static final Logger log = LoggerFactory.getLogger(JMXMonitor.class);
    private final SimpleMBean mBean;

    @Override
    public Map<String, Number> getSample() {
        return mBean.toMap();
    }

    public JMXMonitor(Host host, String beanName) throws IOException, JMException {
        mBean = getMBean(host, beanName);
    }

    private static MBeanConnectionPool mBeanConnectionPool = new MBeanConnectionPool();

    /**
     * Operator which gets an MBean, when the MBean is not needed the close connections should be called
     *
     * @param host
     * @param beanName
     * @return
     * @throws javax.management.MalformedObjectNameException
     */
    public SimpleMBean getMBean(final Host host, final String beanName) throws MalformedURLException {
        SimpleMBean simpleMBean = null;
        final JMXServiceURL url = buildUrl(host);
        try {
            final JMXConnection jmxBean = mBeanConnectionPool.borrowObject(new ConnectionDescription(url, beanName));
            simpleMBean = (SimpleMBean) jmxBean.getMbeanProxy();
        } catch (Exception e) {
            log.debug("Could not connect to {}", e);
        }
        return simpleMBean;
    }

    @Override
    public void close() {
        try {
            mBeanConnectionPool.close();
        } catch (Exception e) {
            log.debug("Could not close connection \n {}", e);
        }
    }

    protected JMXServiceURL buildUrl(Host host) throws MalformedURLException {
        final String address;
        if (!"".equals(host.getIp()) && host.getIp() != null) {
            address = host.getIp();
        } else {
            address = host.getHostname();
        }
        return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + address + ":" + host.getPort().get(Ports.JMX) + "/jmxrmi");
    }


}
