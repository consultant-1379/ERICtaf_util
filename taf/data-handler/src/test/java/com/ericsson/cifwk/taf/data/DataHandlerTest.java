package com.ericsson.cifwk.taf.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import groovy.util.logging.Log4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Log4j
public class DataHandlerTest {

    @Before
    public void setUp() throws Exception {
        System.setProperty("sysProp", "1");
    }

    @After
    public void tearDown() throws Exception {
        DataHandler.unsetAttributes();
    }

    @Test
    public void testGetAttributes() {
        Map attributes = DataHandler.getAttributes();
        assertNotNull(attributes);
    }

    @Test
    public void testGetAttribute() {
        String attribute = "test.data.handler.field";
        String data = "abcde";
        DataHandler.setAttribute(attribute, data);
        assertEquals(data, DataHandler.getAttribute(attribute));
    }

    @Test
    public void testGetHosts() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(2);
        map.put("host.testhost.user.user1.pass", "pass1");
        map.put("host.testhost.ip", "10.1.1.1");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            DataHandler.setAttribute(entry.getKey(), entry.getValue());
        }

        List<Host> hosts = DataHandler.getHosts();
        assertNotNull(hosts);
    }

    @Test
    public void testGetAllHostsByType() {
        List<Host> hosts = DataHandler.getAllHostsByType(HostType.NETSIM);
        int size = hosts.size();
        assertEquals(3, size);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(11);
        map.put("host.ms1.ip", "10.43.251.3");
        map.put("host.ms1.port.ssh", "22");
        map.put("host.ms1.type", "ms");
        map.put("host.ms1.node.netsim1.tunnel", "1");
        map.put("host.ms1.node.netsim1.ip", "ieatnetsimv5040-03.athtem.eei.ericsson.se");
        map.put("host.ms1.node.netsim1.type", "netsim");
        map.put("host.ms1.node.netsim1.port.ssh", "22");
        map.put("host.ms1.node.netsim2.tunnel", "2");
        map.put("host.ms1.node.netsim2.ip", "10.140.24.144");
        map.put("host.ms1.node.netsim2.type", "netsim");
        map.put("host.ms1.node.netsim2.port.ssh", "22");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            DataHandler.setAttribute(entry.getKey(), entry.getValue());
        }

        hosts = DataHandler.getAllHostsByType(HostType.NETSIM);
        assertEquals(5, hosts.size());
    }

    @Test
    public void testGetHostByType() {
        Host host_rc = DataHandler.getHostByType(HostType.RC);
        assertNotNull(host_rc);
        assertEquals(HostType.RC, host_rc.getType());
        assertEquals("masterservice", host_rc.getHostname());

        Host host_uas = DataHandler.getHostByType(HostType.UAS);
        assertNotNull(host_uas);
        assertEquals(HostType.UAS, host_uas.getType());
        assertEquals("atrcxb1601", host_uas.getHostname());
    }

    @Test
    public void testGetHostByName() {
        Host host_masterservice = DataHandler.getHostByName("masterservice");
        assertNotNull(host_masterservice);
        assertEquals(HostType.RC, host_masterservice.getType());
        assertEquals("masterservice", host_masterservice.getHostname());

        Host host_atrcxb1601 = DataHandler.getHostByName("atrcxb1601");
        assertNotNull(host_atrcxb1601);
        assertEquals(HostType.UAS, host_atrcxb1601.getType());
        assertEquals("atrcxb1601", host_atrcxb1601.getHostname());
    }

    @Test
    public void checkPropertyOverriding() {
        assertEquals("1", DataHandler.getAttribute("sysProp"));
        assertEquals("1", DataHandler.getAttributes().get("sysProp"));
        System.setProperty("sysProp", "2");
        assertEquals("2", DataHandler.getAttribute("sysProp"));
        assertEquals("2", DataHandler.getAttributes().get("sysProp"));
        DataHandler.setAttribute("sysProp", 3);
        assertEquals(3, DataHandler.getAttribute("sysProp"));
        assertEquals(3, DataHandler.getAttributes().get("sysProp"));
    }

    @Ignore
    @Test
    public void checkHostPropertiesFile() {
        Host host = DataHandler.getHostByName("sc1");
        assertNotNull(host);
        assertEquals(host.getNodes().size(), 1);
    }

    @Test
    public void testGetSpecificNode() {
        Host host = DataHandler.getAllHostsByType(HostType.SC1).get(0);
        Host node = DataHandler.getSpecificNode(host, HostType.CIFWK);
        Assert.assertNull(node);

        LinkedHashMap<String, String> map = new LinkedHashMap<>(18);
        map.put("host.sc1.node.jboss2.ip", "0.0.0.3");
        map.put("host.sc1.node.jboss2.type", "jboss");
        map.put("host.sc1.node.jboss2.user.root.pass", "shroot");
        map.put("host.sc1.node.jboss2.user.root.type", "admin");
        map.put("host.sc1.node.jboss2.user.guest.pass", "shroot");
        map.put("host.sc1.node.jboss2.user.guest.type", "oper");
        map.put("host.sc1.node.jboss2.port.rmi", "4441");
        map.put("host.sc1.node.jboss2.port.jmx", "4447");
        map.put("host.sc1.node.jboss2.port.jboss_management", "9999");
        map.put("host.sc1.node.http1.ip", "0.0.0.5");
        map.put("host.sc1.node.http1.type", "http");
        map.put("host.sc1.node.http1.user.root.pass", "shroot");
        map.put("host.sc1.node.http1.user.root.type", "admin");
        map.put("host.sc1.node.http1.user.guest.pass", "shroot");
        map.put("host.sc1.node.http1.user.guest.type", "oper");
        map.put("host.sc1.node.http1.port.rmi", "4441");
        map.put("host.sc1.node.http1.port.jmx", "4447");
        map.put("host.sc1.node.http1.port.jboss_management", "9999");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            DataHandler.setAttribute(entry.getKey(), entry.getValue());
        }

        Host jbossNode = DataHandler.getSpecificNode(host, HostType.JBOSS);
        assertEquals("jboss1", jbossNode.getHostname());
        assertEquals("0.0.0.2", jbossNode.getIp());
        assertEquals(new Integer(8080), jbossNode.getPort(Ports.HTTP));

        Host hostWithNode = DataHandler.getAllHostsByType(HostType.SC1).get(0);
        Host httpNode = DataHandler.getSpecificNode(hostWithNode, HostType.HTTP);
        assertEquals("http1", httpNode.getHostname());
        assertEquals("0.0.0.5", httpNode.getIp());
        assertEquals(new Integer(4447), httpNode.getPort(Ports.JMX));
    }

    @Test
    public void testIpv6() throws Exception {
        Map<String, String> attrs = new ImmutableMap.Builder<String, String>()
                .put("host.svc1.ip", "141.137.210.212")
                .put("host.svc1.ipv6", "2001:db8::1")
                .put("host.svc1.node.fmserv_1.ip", "10.247.246.37")
                .put("host.svc1.node.fmserv_1.ipv6", "2607:f0d0:1002:51::4")
                .put("host.svc1.node.pmserv_1.ip", "10.247.246.15")
                .put("host.svc1.node.pmserv_1.ipv6", "fe80::200:f8ff:fe21:67cf")
                .build();

        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            DataHandler.setAttribute(attr.getKey(), attr.getValue());
        }

        Host svc1 = DataHandler.getHostByName("svc1");
        assertEquals(attrs.get("host.svc1.ip"), svc1.getIp());
        assertEquals(attrs.get("host.svc1.ipv6"), svc1.getIpv6());

        Host fmserv1 = DataHandler.getHostByName("fmserv_1");
        assertEquals(attrs.get("host.svc1.node.fmserv_1.ip"), fmserv1.getIp());
        assertEquals(attrs.get("host.svc1.node.fmserv_1.ipv6"), fmserv1.getIpv6());

        Host pmserv1 = DataHandler.getHostByName("pmserv_1");
        assertEquals(attrs.get("host.svc1.node.pmserv_1.ip"), pmserv1.getIp());
        assertEquals(attrs.get("host.svc1.node.pmserv_1.ipv6"), pmserv1.getIpv6());
    }

    @Test
    public void testGetHostsByGroup() {
        List<Host> hosts_haproxy = DataHandler.getAllHostsByGroup("haproxy");
        assertEquals(1, hosts_haproxy.size());

        List<Host> hosts_netsim = DataHandler.getAllHostsByGroup("netsim");
        assertEquals(2, hosts_netsim.size());
    }

    @Test
    public void testJgroupInterfscePopulated() {
        assertNotNull("Jgroup info was not loaded by DataHandler", DataHandler.getConfiguration().getProperties().getProperty("host.svc1.node.pmserv_1.jgroup.ip"));
    }

}
