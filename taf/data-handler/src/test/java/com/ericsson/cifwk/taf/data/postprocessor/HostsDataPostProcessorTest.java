package com.ericsson.cifwk.taf.data.postprocessor;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.Ignore;
import org.junit.Test;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HostsDataPostProcessorTest {

    @Test
    public void verifyProcessing() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(7);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.type", "MS");
        map.put("host.UnitTest.namespace","namespace");
        map.put("host.UnitTest.user.u1.pass", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.node.n1.ip", "10.0.0.2");
        map.put("host.UnitTest.node.n1.offset", "7");
        map.put("host.UnitTest.node.n1.port.rmi", "4441");

        List<Host> result = HostsDataPostProcessor.process(map);
        assertEquals(2, result.size());

        Host unitTestHost = Iterables.get(result, 0);

        assertEquals("UnitTest", unitTestHost.getHostname());
        assertEquals(HostType.MS, unitTestHost.getType());
        assertEquals("10.0.0.1", unitTestHost.getIp());
        assertEquals("namespace",unitTestHost.getNamespace());
        assertEquals("u1", unitTestHost.getUser());
        assertEquals("p1", unitTestHost.getPass());
        assertEquals(UserType.ADMIN, DefaultGroovyMethods.first(unitTestHost.getUsers()).getType());
        assertEquals("10.0.0.2", DefaultGroovyMethods.first(unitTestHost.getNodes()).getIp());
        assertEquals("7", DefaultGroovyMethods.first(unitTestHost.getNodes()).getOffset());
        assertNotNull(DefaultGroovyMethods.first(unitTestHost.getNodes()).getPort());
        assertEquals("4441", DefaultGroovyMethods.first(unitTestHost.getNodes()).getPort().get(Ports.RMI));
    }

    @Test
    public void verifyIloProcessed() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(7);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.type", "MS");
        map.put("host.UnitTest.user.u1.pass", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.iloInfo.UnitTestIlo.ip", "10.0.0.2");
        map.put("host.UnitTest.iloInfo.UnitTestIlo.type", "ILO");
        map.put("host.UnitTest.iloInfo.UnitTestIlo.port.ssh", "22");

        List<Host> result = HostsDataPostProcessor.process(map);
        assertEquals(1, result.size());

        Host unitTestHost = Iterables.get(result, 0);

        Host ilo = unitTestHost.getIlo();

        assertEquals("UnitTestIlo", ilo.getHostname());
        assertEquals(HostType.ILO, ilo.getType());
        assertEquals("10.0.0.2", ilo.getIp());
    }

    @Test
    public void verifyWrongUser() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(5);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.type", "ms");
        map.put("host.UnitTest.user.u1", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.node.n1.type", "jboss");
        List<Host> result = HostsDataPostProcessor.process(map);

        Host unitTestHost = Iterables.get(result, 0);

        assertEquals(0, unitTestHost.getUsers().size());
        assertNotNull(unitTestHost.getNodes());
    }

    @Test
    public void verifyWrongNode() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(4);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.user.u1.pass", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.node", "10.0.0.2");
        List<Host> result = HostsDataPostProcessor.process(map);
        assertEquals(1, result.size());
        Host unitTestHost = DefaultGroovyMethods.first(result);
        assertEquals("UnitTest", unitTestHost.getHostname());
        assertEquals(HostType.UNKNOWN, unitTestHost.getType());
        assertEquals("10.0.0.1", unitTestHost.getIp());
        assertNotNull(unitTestHost.getUsers());
        assertEquals((0), unitTestHost.getNodes().size());
    }

    @Test
    public void setTunnelAttrs() {
        DataHandler.setAttribute("host.UnitTest.ip", "10.0.0.1");
        DataHandler.setAttribute("host.UnitTest.type", "MS");
        DataHandler.setAttribute("host.UnitTest.user.u1.pass", "p1");
        DataHandler.setAttribute("host.UnitTest.user.u1.type", "admin");
        DataHandler.setAttribute("host.UnitTest.node.n1.ip", "10.0.0.2");
        DataHandler.setAttribute("host.UnitTest.node.n1.offset", "7");
        DataHandler.setAttribute("host.UnitTest.node.n1.port.rmi", "4441");
        DataHandler.setAttribute("host.UnitTest.node.n1.tunnel", "1");
        DataHandler.setAttribute("host.UnitTest.node.n2.ip", "22");
        DataHandler.setAttribute("host.UnitTest.node.n3.tunnel", "7");
        DataHandler.setAttribute("host.UnitTest.node.n3.ip", "11.11");
        DataHandler.setAttribute("host.UnitTest.node.n3.tunneled", true);

        verifyHostsWithTunnelEnabledWillNotReEnable("n3");
        verifyTunnelingCannotBeEnabledForHost();
    }

    public void verifyTunnelingCannotBeEnabledForHost() {
        List<Host> result = DataHandler.getHosts();
        Host p1 = Iterables.find(result, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return input.getHostname().equals("UnitTest");
            }
        });
        assertNull(p1.getTunnelPortOffset());
        Host n1 = Iterables.find(result, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return input.getHostname().equals("n1");
            }
        });

        assertEquals("1", n1.getTunnelPortOffset());
        p1.setTunnelPortOffset("2");
        assertEquals("2", p1.getTunnelPortOffset());
        assertFalse(p1.isTunneled());
    }

    public void verifyHostsWithTunnelEnabledWillNotReEnable(final String hostname) {
        List<Host> result = DataHandler.getHosts();
        Host n3 = Iterables.find(result, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return input.getHostname().equals(hostname);
            }
        });

        assertTrue(n3.isTunneled());
    }

    /**
     * Test case below is not unit by any meaning, it is created to proof functionality is working at all until proper env will be used
     */
    @Ignore
    @Test
    public void delme() {
        DataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        DataHandler.setAttribute("host.b1.type", "ms");
        DataHandler.setAttribute("host.b1.port.ssh", "22");
        DataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        DataHandler.setAttribute("host.b1.user.root.type", "admin");
        DataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dr.tunnel", "5");
        DataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        assertTrue(DataHandler.getHostByName("dr").isTunneled());
    }

    @Test
    public void shouldReturnHostFromProperties() {
        DataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        DataHandler.setAttribute("host.b1.type", "ms");
        DataHandler.setAttribute("host.b1.port.ssh", "22");
        DataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        DataHandler.setAttribute("host.b1.user.root.type", "admin");
        DataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        DataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        assertNotNull(DataHandler.getHostByType(HostType.JBOSS));
        assertNotNull(DataHandler.getHostByType(HostType.MS));
    }

    @Test
    public void shouldReturnHostFromPropertiesBasedOnName() {
        DataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        DataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        DataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        DataHandler.setAttribute("host.b1.type", "ms");
        DataHandler.setAttribute("host.b1.port.ssh", "22");
        DataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        DataHandler.setAttribute("host.b1.user.root.type", "admin");
        DataHandler.setAttribute("host.b1.node.dra.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dra.port.ssh", "22");
        DataHandler.setAttribute("host.b1.node.dra.type", "jboss");
        assertEquals("dr", DataHandler.getHostByName("dr").getHostname());
        assertEquals("dra", DataHandler.getHostByName("dra").getHostname());
        assertEquals("b1", DataHandler.getHostByName("b1").getHostname());
    }

    @Test
    public void shouldGetGroupAndUnit() {
        DataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        DataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        DataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        DataHandler.setAttribute("host.b1.type", "ms");
        DataHandler.setAttribute("host.b1.port.ssh", "22");
        DataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        DataHandler.setAttribute("host.b1.user.root.type", "admin");
        DataHandler.setAttribute("host.b1.node.dr.group", "some");
        DataHandler.setAttribute("host.b1.node.dr.unit", "1");
        DataHandler.setAttribute("host.b1.node.dra.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dra.port.ssh", "22");
        DataHandler.setAttribute("host.b1.node.dra.type", "jboss");
        Host dr = DataHandler.getHostByName("dr");
        Host dra = DataHandler.getHostByName("dra");
        assertEquals("some", dr.getGroup());
        assertEquals("1", dr.getUnit());
        assertNull(dra.getGroup());
        assertNull(dra.getUnit());
    }

    @Test
    public void shouldGetHostByGroup() {
        DataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        DataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        DataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        DataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        DataHandler.setAttribute("host.b1.type", "ms");
        DataHandler.setAttribute("host.b1.port.ssh", "22");
        DataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        DataHandler.setAttribute("host.b1.user.root.type", "admin");
        DataHandler.setAttribute("host.b1.node.dr.group", "some");
        DataHandler.setAttribute("host.b1.node.dr.unit", "1");
        List<Host> hosts = DataHandler.getAllHostsByGroup("some");
        assertTrue(hosts.size() == 1);
        Host dr = getHostByName(hosts, "dr");
        assertNotNull(dr);
    }

    @Test
    public void shouldReturnNull() {
        assertNull(DataHandler.getHostByName("notexistinghost"));
        assertNull(DataHandler.getHostByType(HostType.CIFWK));
    }

    @Test
    public void processIsolated() throws Exception {
        String hostDefs = "host.ms1.ip=192.168.0.42\n"
                + "host.ms1.user.root.pass=12shroot\n"
                + "host.ms1.user.root.type=admin\n"
                + "host.ms1.port.ssh=22\n"
                + "host.ms1.type=ms\n"
                + " \n"
                + "host.sc-1.ip=192.168.0.43\n"
                + "host.sc-1.user.root.pass=litpc0b6lEr\n"
                + "host.sc-1.user.root.type=admin\n"
                + "host.sc-1.type=sc1\n"
                + "host.sc-1.port.ssh=22\n"
                + "host.sc-1.node.FMPMMS.ip=192.168.0.69\n"
                + "host.sc-1.node.FMPMMS.type=jboss\n"
                + "host.sc-1.node.FMPMMS.user.root.pass=shroot\n"
                + "host.sc-1.node.FMPMMS.user.root.type=admin\n"
                + "host.sc-1.node.FMPMMS.user.guest.type=oper\n"
                + "host.sc-1.node.FMPMMS.user.guest.pass=guestp\n"
                + "host.sc-1.node.FMPMMS.port.http=8080\n"
                + "host.sc-1.node.FMPMMS.port.rmi=4447\n"
                + "host.sc-1.node.FMPMMS.port.jmx=9998\n"
                + "host.sc-1.node.FMPMMS.port.jboss_management=9999";

        Properties properties = new Properties();
        properties.load(new StringReader(hostDefs));
        List<Host> hosts = HostsDataPostProcessor.processIsolated(properties);
        assertEquals(2, hosts.size());

        Host ms1 = getHostByName(hosts, "ms1");
        assertNotNull(ms1);
        assertEquals("192.168.0.42", ms1.getIp());
        assertEquals("MS", ms1.getType().toString());
        User user = ms1.getUsers(UserType.ADMIN).get(0);
        assertEquals("root", user.getUsername());
        assertEquals("12shroot", user.getPassword());

        Host sc1 = getHostByName(hosts, "sc-1");
        assertNotNull(sc1);
        assertEquals("192.168.0.43", sc1.getIp());
        assertEquals("SC1", sc1.getType().toString());
        user = sc1.getUsers(UserType.ADMIN).get(0);
        assertEquals("root", user.getUsername());
        assertEquals("litpc0b6lEr", user.getPassword());

        List<Host> sc1Nodes = sc1.getNodes();
        assertEquals(1, sc1Nodes.size());
        Host sc1Node = sc1Nodes.get(0);

        assertEquals("FMPMMS", sc1Node.getHostname());
        assertEquals("192.168.0.69", sc1Node.getIp());
        assertEquals("JBOSS", sc1Node.getType().toString());

        User rootUser = sc1Node.getUsers(UserType.ADMIN).get(0);
        assertEquals("root", rootUser.getUsername());
        assertEquals("shroot", rootUser.getPassword());

        User operUser = sc1Node.getUsers(UserType.OPER).get(0);
        assertEquals("guest", operUser.getUsername());
        assertEquals("guestp", operUser.getPassword());

        assertEquals(Integer.valueOf(8080), sc1Node.getPort(Ports.HTTP));
        assertEquals(Integer.valueOf(4447), sc1Node.getPort(Ports.RMI));
        assertEquals(Integer.valueOf(9998), sc1Node.getPort(Ports.JMX));
        assertEquals(Integer.valueOf(9999), sc1Node.getPort(Ports.JBOSS_MANAGEMENT));
    }

    @Test
    public void verifyFilterHostByKeyValue() {
        Properties props = new Properties();
        props.put("host.masterservice.type", "rc");
        props.put("host.masterservice.ip", "10.45.17.210");
        props.put("host.master.type", "rc");
        props.put("host.ossmaster.type", "rcom");
        props.put("host.atrcxb1609.type", "uas");
        Properties filteredprops = HostsDataPostProcessor.filterHostByKeyValue(props, "type", "rc");

        assertEquals(2, filteredprops.size());
    }

    private Host getHostByName(List<Host> hosts, String hostName) {
        for (Host host : hosts) {
            if (hostName.equals(host.getHostname())) {
                return host;
            }
        }

        return null;
    }

    @Test
    public void verifyHostFormationWithInternalAttributesSet() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(10);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.internal.ip", "192.168.0.1");
        map.put("host.UnitTest.type", "MS");
        map.put("host.UnitTest.user.u1.pass", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.node.n1.ip", "10.0.0.2");
        map.put("host.UnitTest.node.n1.offset", "7");
        map.put("host.UnitTest.node.n1.port.rmi", "4441");
        map.put("host.UnitTest.node.n1.internal.ip", "192.168.0.2");
        map.put("host.UnitTest.node.n1.internal.port.rmi", "4449");
        List<Host> result = HostsDataPostProcessor.process(map);
        assertEquals(2, result.size());

        Host unitTestHost = Iterables.get(result, 0);

        assertEquals("UnitTest", unitTestHost.getHostname());
        assertEquals(HostType.MS, unitTestHost.getType());
        assertEquals("10.0.0.1", unitTestHost.getIp());
        assertEquals("u1", unitTestHost.getUser());
        assertEquals("p1", unitTestHost.getPass());
        assertEquals(UserType.ADMIN, DefaultGroovyMethods.first(unitTestHost.getUsers()).getType());
        assertEquals("10.0.0.2", DefaultGroovyMethods.first(unitTestHost.getNodes()).getIp());
        assertEquals("7", DefaultGroovyMethods.first(unitTestHost.getNodes()).getOffset());
        assertNotNull(DefaultGroovyMethods.first(unitTestHost.getNodes()).getPort());
        assertEquals("4441", DefaultGroovyMethods.first(unitTestHost.getNodes()).getPort().get(Ports.RMI));
    }

}
