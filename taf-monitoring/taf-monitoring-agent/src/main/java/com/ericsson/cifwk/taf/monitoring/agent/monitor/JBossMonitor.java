package com.ericsson.cifwk.taf.monitoring.agent.monitor;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;

import javax.management.JMException;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

public class JBossMonitor extends JMXMonitor{

    public JBossMonitor(Host host, String beanName) throws IOException, JMException {
        super(host, beanName);
    }

    @Override
    protected JMXServiceURL buildUrl(Host host) throws MalformedURLException {
        final String address;
        if (host.getIp() != null && !"".equals(host.getIp())) {
            address = host.getIp();
        } else {
            address = host.getHostname();
        }
        return new JMXServiceURL("service:jmx:remoting-jms://" + address + ":" + host.getPort().get(Ports.JMX));
    }
}
