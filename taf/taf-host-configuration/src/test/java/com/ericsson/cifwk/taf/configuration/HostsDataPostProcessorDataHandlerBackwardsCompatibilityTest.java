package com.ericsson.cifwk.taf.configuration;

import static com.ericsson.cifwk.taf.configuration.GenericHostBuilder.toHost;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Lists.transform;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.assertj.core.util.Strings;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Ported from com.ericsson.cifwk.taf.data.postprocessor.HostsDataPostProcessorTest
 * Validates same behavior as DataHandlerTest, also ignores same exceptions
 */
public class HostsDataPostProcessorDataHandlerBackwardsCompatibilityTest {
    @BeforeClass
    public static void setUp() {
        System.setProperty("sysProp", "1");
        System.setProperty("taf.profiles", "ported_data_handler_host_properties");
        TafDataHandler.getConfiguration().reload();
    }

    @AfterClass
    public static void tearDownClass() {
        System.setProperty("taf.profiles", "");
        TafDataHandler.getConfiguration().reload();
    }

    @After
    public void tearDown() throws Exception {
        TafDataHandler.getConfiguration().clear();
    }

    @Test
    public void verifyProcessing() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(7);
        map.put("host.UnitTest.ip", "10.0.0.1");
        map.put("host.UnitTest.type", "MS");
        map.put("host.UnitTest.user.u1.pass", "p1");
        map.put("host.UnitTest.user.u1.type", "admin");
        map.put("host.UnitTest.node.n1.ip", "10.0.0.2");
        map.put("host.UnitTest.node.n1.offset", "7");
        map.put("host.UnitTest.node.n1.port.rmi", "4441");

        List<Host> result = transform(HostPropertyParser.propsToHosts(map), toHost());
        assertEquals(2, result.size());

        Host unitTestHost = result.get(0);

        assertEquals("UnitTest", unitTestHost.getHostname());
        assertEquals(HostType.MS, unitTestHost.getType());
        assertEquals("10.0.0.1", unitTestHost.getIp());
        assertEquals("u1", unitTestHost.getUser());
        assertEquals("p1", unitTestHost.getPass());
        assertEquals(UserType.ADMIN, get(unitTestHost.getUsers(), 0).getType());
        assertEquals("10.0.0.2", get(unitTestHost.getNodes(), 0).getIp());
        assertEquals("7", get(unitTestHost.getNodes(), 0).getOffset());
        assertNotNull(get(unitTestHost.getNodes(), 0).getPort());
        assertEquals("4441", get(unitTestHost.getNodes(), 0).getPort().get(Ports.RMI));
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

        List<Host> result = transform(HostPropertyParser.propsToHosts(map), toHost());
        assertEquals(1, result.size());

        Host unitTestHost = get(result, 0);

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
        List<Host> result = transform(HostPropertyParser.propsToHosts(map), toHost());

        Host unitTestHost = get(result, 0);

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
        List<Host> result = transform(HostPropertyParser.propsToHosts(map), toHost());
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
        TafDataHandler.setAttribute("host.UnitTest.ip", "10.0.0.1");
        TafDataHandler.setAttribute("host.UnitTest.type", "MS");
        TafDataHandler.setAttribute("host.UnitTest.user.u1.pass", "p1");
        TafDataHandler.setAttribute("host.UnitTest.user.u1.type", "admin");
        TafDataHandler.setAttribute("host.UnitTest.node.n1.ip", "10.0.0.2");
        TafDataHandler.setAttribute("host.UnitTest.node.n1.offset", "7");
        TafDataHandler.setAttribute("host.UnitTest.node.n1.port.rmi", "4441");
        TafDataHandler.setAttribute("host.UnitTest.node.n1.tunnel", "1");
        TafDataHandler.setAttribute("host.UnitTest.node.n2.ip", "22");
        TafDataHandler.setAttribute("host.UnitTest.node.n3.tunnel", "7");
        TafDataHandler.setAttribute("host.UnitTest.node.n3.ip", "11.11");
        TafDataHandler.setAttribute("host.UnitTest.node.n3.tunneled", true);

        verifyHostsWithTunnelEnabledWillNotReEnable("n3");
        verifyTunnelingCannotBeEnabledForHost();
    }

    public void verifyTunnelingCannotBeEnabledForHost() {
        List<Host> result = TafDataHandler.getAllHosts();
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
        Assert.assertFalse(p1.isTunneled());
    }

    public void verifyHostsWithTunnelEnabledWillNotReEnable(final String hostname) {
        List<Host> result = TafDataHandler.getAllHosts();
        Host n3 = Iterables.find(result, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return input.getHostname().equals(hostname);
            }
        });

        Assert.assertTrue(n3.isTunneled());
    }

    /**
     * HostTunnelHelper#isTunneled() actually just reads property, which returns false on any exception
     */
    public boolean isHostReallyTunneled(Host n3) {
        return "127.0.0.1".equals(n3.getIp()) && !Strings.isNullOrEmpty(n3.getTunnelPortOffset());
    }


    @Test
    public void shouldReturnHostFromProperties() {
        TafDataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        TafDataHandler.setAttribute("host.b1.type", "ms");
        TafDataHandler.setAttribute("host.b1.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        TafDataHandler.setAttribute("host.b1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        TafDataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        assertNotNull(TafDataHandler.getHostByName("b1"));
        assertNotNull(TafDataHandler.getHostByName("dr"));
    }

    @Test
    public void shouldReturnHostFromPropertiesBasedOnName() {
        TafDataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        TafDataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        TafDataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        TafDataHandler.setAttribute("host.b1.type", "ms");
        TafDataHandler.setAttribute("host.b1.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        TafDataHandler.setAttribute("host.b1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.b1.node.dra.ip", "10.45.237.137");
        TafDataHandler.setAttribute("host.b1.node.dra.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.node.dra.type", "jboss");
        assertEquals("dr", TafDataHandler.getHostByName("dr").getHostname());
        assertEquals("dra", TafDataHandler.getHostByName("dra").getHostname());
        assertEquals("b1", TafDataHandler.getHostByName("b1").getHostname());
    }

    @Test
    public void shouldGetGroupAndUnit() {
        TafDataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        TafDataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        TafDataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        TafDataHandler.setAttribute("host.b1.type", "ms");
        TafDataHandler.setAttribute("host.b1.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        TafDataHandler.setAttribute("host.b1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.b1.node.dr.group", "some");
        TafDataHandler.setAttribute("host.b1.node.dr.unit", "1");
        TafDataHandler.setAttribute("host.b1.node.dra.ip", "10.45.237.137");
        TafDataHandler.setAttribute("host.b1.node.dra.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.node.dra.type", "jboss");
        Host dr = TafDataHandler.getHostByName("dr");
        Host dra = TafDataHandler.getHostByName("dra");
        assertEquals("some", dr.getGroup());
        assertEquals("1", dr.getUnit());
        assertNull(dra.getGroup());
        assertNull(dra.getUnit());
    }

    @Test
    public void shouldGetHostByGroup() {
        TafDataHandler.setAttribute("host.b1.node.dr.ip", "10.45.237.137");
        TafDataHandler.setAttribute("host.b1.node.dr.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.node.dr.type", "jboss");
        TafDataHandler.setAttribute("host.b1.ip", "10.45.237.136");
        TafDataHandler.setAttribute("host.b1.type", "ms");
        TafDataHandler.setAttribute("host.b1.port.ssh", "22");
        TafDataHandler.setAttribute("host.b1.user.root.pass", "12shroot");
        TafDataHandler.setAttribute("host.b1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.b1.node.dr.group", "some");
        TafDataHandler.setAttribute("host.b1.node.dr.unit", "1");
        List<Host> hosts = TafDataHandler.getAllHostsByGroup("some");
        assertTrue(hosts.size() == 1);
        Host dr = getHostByName(hosts, "dr");
        assertNotNull(dr);
    }

    @Test
    public void shouldReturnNull() {
        assertNull(TafDataHandler.getHostByName("notexistinghost"));
        assertNull(TafDataHandler.getHostByType(HostType.CIFWK));
    }

    @Test
    public void processIsolated() throws Exception {
        TafDataHandler.setAttribute("host.ms1.ip", "192.168.0.42");
        TafDataHandler.setAttribute("host.ms1.user.root.pass", "12shroot");
        TafDataHandler.setAttribute("host.ms1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.ms1.port.ssh", "22");
        TafDataHandler.setAttribute("host.ms1.type", "ms");
        TafDataHandler.setAttribute("host.sc-1.ip", "192.168.0.43");
        TafDataHandler.setAttribute("host.sc-1.user.root.pass", "litpc0b6lEr");
        TafDataHandler.setAttribute("host.sc-1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.sc-1.type", "sc1");
        TafDataHandler.setAttribute("host.sc-1.port.ssh", "22");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.ip", "192.168.0.69");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.type", "jboss");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.user.root.type", "admin");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.user.guest.type", "oper");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.user.guest.pass", "guestp");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.port.http", "8080");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.port.rmi", "4447");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.port.jmx", "9998");
        TafDataHandler.setAttribute("host.sc-1.node.FMPMMS.port.jboss_management", "9999");

        List<Host> hosts = TafDataHandler.getAllHosts();

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
        TafDataHandler.setAttribute("host.masterservice.type", "rc");
        TafDataHandler.setAttribute("host.masterservice.ip", "10.45.17.210");
        TafDataHandler.setAttribute("host.master.type", "rc");
        TafDataHandler.setAttribute("host.ossmaster.type", "rcom");
        TafDataHandler.setAttribute("host.atrcxb1609.type", "uas");

        List<Host> rcTypeHosts = TafDataHandler.findHost().withType("rc").getAll();

        assertEquals(2, rcTypeHosts.size());
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
        List<Host> result = transform(HostPropertyParser.propsToHosts(map), toHost());
        assertEquals(2, result.size());

        Host unitTestHost = result.get(0);

        assertEquals("UnitTest", unitTestHost.getHostname());
        assertEquals(HostType.MS, unitTestHost.getType());
        assertEquals("10.0.0.1", unitTestHost.getIp());
        assertEquals("u1", unitTestHost.getUser());
        assertEquals("p1", unitTestHost.getPass());
        assertEquals(UserType.ADMIN, get(unitTestHost.getUsers(), 0).getType());
        assertEquals("10.0.0.2", get(unitTestHost.getNodes(), 0).getIp());
        assertEquals("7", get(unitTestHost.getNodes(), 0).getOffset());
        assertNotNull(get(unitTestHost.getNodes(), 0).getPort());
        assertEquals("4441", get(unitTestHost.getNodes(), 0).getPort().get(Ports.RMI));
    }

}
