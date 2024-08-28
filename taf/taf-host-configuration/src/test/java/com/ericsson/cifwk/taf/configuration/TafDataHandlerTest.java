package com.ericsson.cifwk.taf.configuration;

import static org.assertj.core.api.Java6Assertions.assertThat;

import static com.ericsson.cifwk.taf.data.HostType.ILO;
import static com.ericsson.cifwk.taf.data.HostType.UNKNOWN;
import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.data.network.EnmNetworkHost;
import com.ericsson.cifwk.taf.data.network.NetworkHost;
import com.ericsson.cifwk.taf.data.network.NetworkInterface;
import com.ericsson.cifwk.taf.data.network.Port;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class TafDataHandlerTest {
    public static final String HOST_IP = "141.137.173.140";
    public static final String NODE_IP = "tafexe1";

    @Before
    public void setUp() throws Exception {
        TafDataHandler.getConfiguration().clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMoreThanOne() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost2.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost2.group", "thegroup");

        TafDataHandler.findHost().withGroup("thegroup").get();
    }

    @Test
    public void testGetAttributeReturnsCorrectClass()  {
        TafDataHandler.setAttribute("integer.value", 300);
        Integer attribute = TafDataHandler.getAttribute("integer.value", 60);
        assertThat(attribute.getClass()).isEqualTo(Integer.class);

        TafDataHandler.setAttribute("integer.value.as.string", "300");
        Integer attributeString = TafDataHandler.getAttribute("integer.value.as.string", 60);
        assertThat(attributeString.getClass()).isEqualTo(Integer.class);

        TafDataHandler.setAttribute("integer.value.default.as.string", 300);
        String attributeDefaultString = TafDataHandler.getAttribute("integer.value.default.as.string", "60");
        assertThat(attributeDefaultString.getClass()).isEqualTo(String.class);
    }


    @Test
    public void getOne() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost2.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost2.group", "thegroup");

        Host thehost = TafDataHandler.findHost().withHostname("thehost").get();
        assertThat(thehost.getHostname()).isEqualTo("thehost");
    }

    @Test
    public void allMatchers() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost.node.thenode.name", NODE_IP);
        TafDataHandler.setAttribute("host.thehost.node.thenode.type", "http");
        TafDataHandler.setAttribute("host.thehost.node.thenode.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost.node.thenode.ip", NODE_IP);

        Host thehost = TafDataHandler.findHost().withHostname("thehost").get();

        Host thenode = TafDataHandler.findHost()
                .withHostname("thenode")
                .withGroup("thegroup")
                .withType(HostType.HTTP)
                .withParent(thehost)
                .withParentHostName("thehost")
                .get();

        assertThat(thenode.getHostname()).isEqualTo("thenode");
    }

    @Test
    public void allMatchersENMHost() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost.node.thenode.name", NODE_IP);
        TafDataHandler.setAttribute("host.thehost.node.thenode.type", "http");
        TafDataHandler.setAttribute("host.thehost.node.thenode.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost.node.thenode.ip", NODE_IP);

        EnmNetworkHost thehost = TafDataHandler.findEnmHost().withHostname("thehost").get();

        EnmNetworkHost thenode = TafDataHandler.findEnmHost()
                .withHostname("thenode")
                .withGroup("thegroup")
                .withType("http")
                .withParent(thehost)
                .withParentHostName("thehost")
                .get();

        assertThat(thenode.getType()).isEqualTo("http");
    }

    @Test
    public void getCustomPredicate() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");

        Host thehost = TafDataHandler.findHost().withPredicate(
                new Predicate<HostFilter>() {
                    @Override
                    public boolean apply(HostFilter input) {
                        return "thehost".equals(input.getHostName());
                    }
                }
        ).get();

        assertThat(thehost.getHostname()).isEqualTo("thehost");
    }

    @Test
    public void getFirst() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost2.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost2.group", "thegroup");

        Host thehost = TafDataHandler.findHost().withGroup("thegroup").getFirst();

        assertThat(thehost).isNotNull();
    }

    @Test
    public void getAll() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.group", "thegroup");
        TafDataHandler.setAttribute("host.thehost2.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost2.group", "thegroup");

        List<Host> thegroup = TafDataHandler.findHost().withGroup("thegroup").getAll();
        assertThat(thegroup).hasSize(2);
    }

    @Test
    public void getAllNotExisting() throws Exception {
        List<Host> thegroup = TafDataHandler.findHost().withGroup("notexising").getAll();
        assertThat(thegroup).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNotExisting() throws Exception {
        TafDataHandler.findHost()
                .withHostname("notexisting")
                .get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getFirstNotExisting() throws Exception {
        TafDataHandler.findHost()
                .withHostname("notexisting")
                .getFirst();
    }

    @Test
    public void getNotExistingNullable() throws Exception {
        Host thehost = TafDataHandler.findHost()
                .withHostname("notexisting")
                .nullable()
                .get();

        assertThat(thehost).isNull();
    }

    @Test
    public void getFirstNotExistingNullable() throws Exception {
        Host thehost = TafDataHandler.findHost()
                .withHostname("notexisting")
                .nullable()
                .getFirst();

        assertThat(thehost).isNull();
    }


    @Test
    public void parseAllPropertiesHost() throws Exception {
        TafDataHandler.setAttribute("host.sc1.ip", HOST_IP);
        TafDataHandler.setAttribute("host.sc1.ipv6", "2001:1b70:82a1:103::194");
        TafDataHandler.setAttribute("host.sc1.unit", "6");
        TafDataHandler.setAttribute("host.sc1.port.ssh", "22");
        TafDataHandler.setAttribute("host.sc1.group", "thegroup");
        TafDataHandler.setAttribute("host.sc1.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.user.root.type", "admin");

        TafDataHandler.setAttribute("host.sc1.iloInfo.UnitTestIlo.ip", "10.0.0.2");
        TafDataHandler.setAttribute("host.sc1.iloInfo.UnitTestIlo.type", "ILO");
        TafDataHandler.setAttribute("host.sc1.iloInfo.UnitTestIlo.port.ssh", "22");

        TafDataHandler.setAttribute("host.sc1.node.jboss2.ip", "0.0.0.3");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.type", "jboss");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.root.type", "admin");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.guest.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.guest.type", "oper");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.port.rmi", "4441");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.port.jmx", "4447");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.port.jboss_management", "9999");
        TafDataHandler.setAttribute("host.sc1.node.http1.ip", "0.0.0.5");
        TafDataHandler.setAttribute("host.sc1.node.http1.type", "http");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.guest.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.guest.type", "oper");
        TafDataHandler.setAttribute("host.sc1.node.http1.port.rmi", "4441");
        TafDataHandler.setAttribute("host.sc1.node.http1.port.jmx", "4447");
        TafDataHandler.setAttribute("host.sc1.node.http1.port.jboss_management", "9999");

        List<Host> allHosts = TafDataHandler.getAllHosts();
        assertThat(allHosts).hasSize(3);

        Host host = TafDataHandler.findHost().withHostname("sc1").get();
        assertThat(host.getIp()).isEqualTo(HOST_IP);
        assertThat(host.getIpv6()).isEqualTo("2001:1b70:82a1:103::194");
        assertThat(host.getUnit()).isEqualTo("6");
        assertThat(host.getType()).isEqualTo(UNKNOWN);
        assertThat(host.getPort(Ports.SSH)).isEqualTo(22);
        assertThat(host.getGroup()).isEqualTo("thegroup");
        assertThat(host.getUsers()).hasSize(1);
        assertThat(host.getUser()).isEqualTo("root");
        assertThat(host.getPass()).isEqualTo("shroot");
        assertThat(host.getNodes()).hasSize(2);
        assertThat(host.getOffset()).isNull();
        assertThat(host.getOriginalIp()).isNull();
        assertThat(host.getOriginalPort()).isEmpty();
        assertThat(host.getTunnelPortOffset()).isNull();
        assertThat(host.getParentName()).isNull();

        Host ilo = host.getIlo();
        assertThat(ilo.getIp()).isEqualTo("10.0.0.2");
        assertThat(ilo.getType()).isEqualTo(ILO);
        assertThat(ilo.getPort(Ports.SSH)).isEqualTo(22);

        Host jboss2Node = TafDataHandler.findHost().withHostname("jboss2").get();
        assertThat(jboss2Node.getParentName()).isEqualTo("sc1");
        assertThat(jboss2Node.getIp()).isEqualTo("0.0.0.3");
        assertThat(jboss2Node.getType()).isEqualTo(HostType.JBOSS);
        assertThat(jboss2Node.getPort(Ports.RMI)).isEqualTo(4441);
        assertThat(jboss2Node.getPort(Ports.JMX)).isEqualTo(4447);
        assertThat(jboss2Node.getPort(Ports.JBOSS_MANAGEMENT)).isEqualTo(9999);
        assertThat(jboss2Node.getUsers()).hasSize(2);
        assertThat(jboss2Node.getUser(UserType.ADMIN)).isEqualTo("root");
        assertThat(jboss2Node.getPass(UserType.ADMIN)).isEqualTo("shroot");
        assertThat(jboss2Node.getUser(UserType.OPER)).isEqualTo("guest");
        assertThat(jboss2Node.getPass(UserType.OPER)).isEqualTo("shroot");

        Host http1 = TafDataHandler.findHost().withHostname("http1").get();
        assertThat(http1.getParentName()).isEqualTo("sc1");
        assertThat(http1.getIp()).isEqualTo("0.0.0.5");
        assertThat(http1.getType()).isEqualTo(HostType.HTTP);
        assertThat(http1.getPort(Ports.RMI)).isEqualTo(4441);
        assertThat(http1.getPort(Ports.JMX)).isEqualTo(4447);
        assertThat(http1.getPort(Ports.JBOSS_MANAGEMENT)).isEqualTo(9999);
        assertThat(http1.getUsers()).hasSize(2);
        assertThat(http1.getUser(UserType.ADMIN)).isEqualTo("root");
        assertThat(http1.getPass(UserType.ADMIN)).isEqualTo("shroot");
        assertThat(http1.getUser(UserType.OPER)).isEqualTo("guest");
        assertThat(http1.getPass(UserType.OPER)).isEqualTo("shroot");
    }

    @Test
    public void parseAllPropertiesNetworkHost() throws Exception {
        TafDataHandler.setAttribute("host.sc1.ip", HOST_IP);
        TafDataHandler.setAttribute("host.sc1.ipv6", "2001:1b70:82a1:103::194");
        TafDataHandler.setAttribute("host.sc1.unit", "6");
        TafDataHandler.setAttribute("host.sc1.port.ssh", "22");
        TafDataHandler.setAttribute("host.sc1.group", "thegroup");
        TafDataHandler.setAttribute("host.sc1.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.user.root.type", "admin");

        TafDataHandler.setAttribute("host.sc1.iloInfo.UnitTestIlo.ip", "10.0.0.2");
        TafDataHandler.setAttribute("host.sc1.iloInfo.UnitTestIlo.type", "ILO");
        TafDataHandler.setAttribute("host.sc1.iloInfo.UnitTestIlo.port.ssh", "22");

        TafDataHandler.setAttribute("host.sc1.node.jboss2.ip", "0.0.0.3");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.type", "jboss");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.root.type", "admin");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.guest.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.user.guest.type", "oper");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.port.rmi", "4441");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.port.jmx", "4447");
        TafDataHandler.setAttribute("host.sc1.node.jboss2.port.jboss_management", "9999");
        TafDataHandler.setAttribute("host.sc1.node.http1.ip", "0.0.0.5");
        TafDataHandler.setAttribute("host.sc1.node.http1.type", "http");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.root.type", "admin");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.guest.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.node.http1.user.guest.type", "oper");
        TafDataHandler.setAttribute("host.sc1.node.http1.port.rmi", "4441");
        TafDataHandler.setAttribute("host.sc1.node.http1.port.jmx", "4447");
        TafDataHandler.setAttribute("host.sc1.node.http1.port.jboss_management", "9999");

        List<EnmNetworkHost> allHosts = TafDataHandler.getAllEnmHosts();
        assertThat(allHosts).hasSize(3);

        EnmNetworkHost host = TafDataHandler.findEnmHost().withHostname("sc1").get();
        NetworkInterface networkInterface = host.getNetworkInterfaces().get(0);
        assertThat(networkInterface.getHostname()).isEqualTo("sc1");
        assertThat(networkInterface.getIpv4()).isEqualTo(HOST_IP);
        assertThat(networkInterface.getIpv6()).isEqualTo("2001:1b70:82a1:103::194");
        assertThat(networkInterface.getIpv6()).isEqualTo("2001:1b70:82a1:103::194");
        assertThat(networkInterface.getPorts()).hasSize(1);
        assertThat(networkInterface.getPorts().get(0).getPortType()).isEqualTo("ssh");
        assertThat(networkInterface.getPorts().get(0).getPort()).isEqualTo(22);
        assertThat(host.getUnit()).isEqualTo("6");
        assertThat(host.getType()).isEqualTo("unknown");
        assertThat(host.getGroup()).isEqualTo("thegroup");
        assertThat(host.getUsers()).hasSize(1);
        assertThat(host.getUsers().get(0).getUsername()).isEqualTo("root");
        assertThat(host.getUsers().get(0).getPassword()).isEqualTo("shroot");
        assertThat(host.getUsers().get(0).getType()).isEqualTo(UserType.ADMIN);
        assertThat(host.getNodes()).hasSize(2);

        NetworkHost ilo = host.getIlo();
        NetworkInterface iloInterface = ilo.getNetworkInterfaces().get(0);
        assertThat(iloInterface.getIpv4()).isEqualTo("10.0.0.2");
        assertThat(iloInterface.getType()).isEqualTo("ilo");
        assertThat(iloInterface.getPorts()).hasSize(1);
        assertThat(iloInterface.getPorts().get(0).getPortType()).isEqualTo("ssh");
        assertThat(iloInterface.getPorts().get(0).getPort()).isEqualTo(22);

        EnmNetworkHost jboss2Node = TafDataHandler.findEnmHost().withHostname("jboss2").get();
        assertThat(jboss2Node.getNetworkInterfaces()).hasSize(1);
        assertThat(jboss2Node.getNetworkInterfaces().get(0).getIpv4()).isEqualTo("0.0.0.3");
        assertThat(jboss2Node.getType()).isEqualTo("jboss");
        assertThat(getPort(jboss2Node, "rmi")).isEqualTo(4441);
        assertThat(getPort(jboss2Node, "jmx")).isEqualTo(4447);
        assertThat(getPort(jboss2Node, "jboss_management")).isEqualTo(9999);
        assertThat(jboss2Node.getUsers()).hasSize(2);
        assertThat(getUser(jboss2Node, UserType.ADMIN).getUsername()).isEqualTo("root");
        assertThat(getUser(jboss2Node, UserType.ADMIN).getPassword()).isEqualTo("shroot");
        assertThat(getUser(jboss2Node, UserType.OPER).getUsername()).isEqualTo("guest");
        assertThat(getUser(jboss2Node, UserType.OPER).getPassword()).isEqualTo("shroot");

        EnmNetworkHost http1 = TafDataHandler.findEnmHost().withHostname("http1").get();
        assertThat(http1.getNetworkInterfaces()).hasSize(1);
        assertThat(http1.getNetworkInterfaces().get(0).getIpv4()).isEqualTo("0.0.0.5");
        assertThat(http1.getType()).isEqualTo("http");
        assertThat(getPort(http1, "rmi")).isEqualTo(4441);
        assertThat(getPort(http1, "jmx")).isEqualTo(4447);
        assertThat(getPort(http1, "jboss_management")).isEqualTo(9999);
        assertThat(http1.getUsers()).hasSize(2);
        assertThat(getUser(http1, UserType.ADMIN).getUsername()).isEqualTo("root");
        assertThat(getUser(http1, UserType.ADMIN).getPassword()).isEqualTo("shroot");
        assertThat(getUser(http1, UserType.OPER).getUsername()).isEqualTo("guest");
        assertThat(getUser(http1, UserType.OPER).getPassword()).isEqualTo("shroot");
    }

    private User getUser(EnmNetworkHost host, UserType type) {
        for (User user : host.getUsers()) {
            if (type.equals(user.getType())) {
                return user;
            }
        }
        return null;
    }

    private Integer getPort(EnmNetworkHost host, String type) {
        assertThat(host.getNetworkInterfaces()).hasSize(1);
        for (Port port : host.getNetworkInterfaces().get(0).getPorts()) {
            if (type.equalsIgnoreCase(port.getPortType())) {
                return port.getPort();
            }
        }
        return null;
    }


    @Test
    public void parseDefaultProperties() throws Exception {
        TafDataHandler.setAttribute("host.sc1.ip", HOST_IP);
        TafDataHandler.setAttribute("host.sc1.user.root.pass", "shroot");

        Host host = TafDataHandler.findHost().withHostname("sc1").get();
        assertThat(host.getType()).isEqualTo(HostType.UNKNOWN);
        assertThat(getOnlyElement(host.getUsers()).getType()).isEqualTo(UserType.CUSTOM);
    }

    @Test
    public void parseUnknownProperties() throws Exception {
        TafDataHandler.setAttribute("host.sc1.ip", HOST_IP);
        TafDataHandler.setAttribute("host.sc1.customtest", HOST_IP);
        TafDataHandler.setAttribute("host.sc1.customtest.randomtest", HOST_IP);
        TafDataHandler.setAttribute("host.sc1.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.sc1.user.root.customtest", "random");
        TafDataHandler.setAttribute("host.sc1.user.root.customtest.randomtest", "random");

        Host host = TafDataHandler.findHost().withHostname("sc1").get();
        assertThat(host.getType()).isEqualTo(HostType.UNKNOWN);
        assertThat(getOnlyElement(host.getUsers()).getType()).isEqualTo(UserType.CUSTOM);
    }

    @Test
    public void testRuntimeOverride() throws Exception {
        TafDataHandler.setAttribute("host.h1.ip", "initial_h1");
        TafDataHandler.setAttribute("host.h1.node.n1.ip", "initial_n1");

        Host unitTest = TafDataHandler.findHost().withHostname("h1").get();
        Host n1 = TafDataHandler.findHost().withHostname("n1").get();
        assertThat(unitTest.getIp()).isEqualTo("initial_h1");
        assertThat(n1.getIp()).isEqualTo("initial_n1");

        TafDataHandler.setAttribute("host.h1.ip", "ovedrrider_h1");
        TafDataHandler.setAttribute("host.h1.node.n1.ip", "ovedrrider_n1");

        unitTest = TafDataHandler.getHostByName("h1");
        n1 = TafDataHandler.getHostByName("n1");
        assertThat(unitTest.getIp()).isEqualTo("ovedrrider_h1");
        assertThat(n1.getIp()).isEqualTo("ovedrrider_n1");
    }

    @Test
    public void testEquals() throws Exception {
        TafDataHandler.setAttribute("host.thehost.ip", "0.0.0.0");
        TafDataHandler.setAttribute("host.thehost.port.ssh", "22");
        TafDataHandler.setAttribute("host.thehost.user.root.pass", "shroot");
        TafDataHandler.setAttribute("host.thehost.user.root.type", "admin");
        TafDataHandler.setAttribute("host.thehost.node.thenode.name", NODE_IP);
        TafDataHandler.setAttribute("host.thehost.node.thenode.ip", NODE_IP);
        TafDataHandler.setAttribute("host.thehost.node.thenode.port.ssh", "22");


        List<Host> allHosts = TafDataHandler.getAllHosts();
        Host theNode1 = findHost(allHosts, "thenode");

        Host theNode2 = TafDataHandler.findHost().withHostname("thenode").get();

        Host theNode3 = TafDataHandler.findHost().withHostname("thenode").get();

        Host thehost = findHost(allHosts, "thehost");
        Host theNode4 = getOnlyElement(thehost.getNodes());

        assertThat(theNode2).isEqualTo(theNode1);
        assertThat(theNode3).isEqualTo(theNode1);
        assertThat(theNode4).isEqualTo(theNode1);
    }

    @Test
    public void handleNullAndEmpty() throws Exception {
        TafDataHandler.setAttribute("host.ms1.ip", null);
        TafDataHandler.setAttribute("host.ms1.unit", "");
        TafDataHandler.setAttribute("host.ms1.type", null);

        Host ms1 = TafDataHandler.findHost().withHostname("ms1").get();

        assertThat(ms1.getIp()).isNull();
        assertThat(ms1.getUnit()).isEmpty();
        assertThat(ms1.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    public void getAttribute() throws Exception {
        TafDataHandler.setAttribute("theAttribute", 2);
        Integer attr = TafDataHandler.getAttribute("theAttribute");
        assertThat(attr).isEqualTo(2);
    }

    @Test(expected = ClassCastException.class)
    public void getAttributeDifferentType() throws Exception {
        TafDataHandler.setAttribute("theAttribute", 2);
        String attr = TafDataHandler.getAttribute("theAttribute");
    }

    @Test
    public void getNotExistingAttribute() throws Exception {
        String notExisting = TafDataHandler.getAttribute("notExisting");
        assertThat(notExisting).isNull();
    }

    @Test
    public void nullAttribute() throws Exception {
        TafDataHandler.setAttribute("theAttribute", null);
        Integer attr = TafDataHandler.getAttribute("theAttribute");
        assertThat(attr).isNull();
    }

    @Test
    public void configurationDoesNotReturnNullValues() throws Exception {
        TafDataHandler.setAttribute("host.ms1.ip", null);
        TafDataHandler.setAttribute("host.ms1.unit", "");
        TafDataHandler.setAttribute("host.ms1.type", null);

        Properties properties = TafDataHandler.getConfiguration().getProperties();
        ImmutableList<Object> hosts = FluentIterable.from(properties.keySet()).filter(new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return ("" + input).startsWith("host.");
            }
        }).toList();

        assertThat(hosts).containsExactly("host.ms1.unit");
    }

    private Host findHost(List<Host> allHosts, final String name) {
        return Iterables.find(allHosts, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return name.equals(input.getHostname());
            }
        });
    }

}
