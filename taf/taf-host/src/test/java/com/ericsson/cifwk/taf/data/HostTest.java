package com.ericsson.cifwk.taf.data;

import com.ericsson.cifwk.taf.data.exception.UserNotFoundException;
import com.ericsson.cifwk.taf.data.resolver.HostPropertyResolver;
import com.ericsson.cifwk.taf.tools.http.HttpEndpoint;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.fail;

public class HostTest {

    @Test
    public void testUsers() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put("host.t1.type", "sc1");
        map.put("host.t1.user.root.type", "admin");
        map.put("host.t1.user.oper.type", "oper");
        map.put("host.t1.user.web1.type", "web");
        map.put("host.t1.user.web2.type", "web");

        Host host = HostPropertyResolver.findHostProperty(map).iterator().next();

        assertEquals(host.getUsers().size(), 4);
        assertEquals(host.getUsers(UserType.WEB).size(), 2);

        host.addUser("web3", "nopass", UserType.WEB);
        assertEquals(host.getUsers(UserType.WEB).size(), 3);
        ArrayList<User> actual = Lists.newArrayList(host.getUsersIterator(UserType.WEB));
        Assert.assertThat(actual, equalTo(host.getUsers(UserType.WEB)));
    }

    @Test
    public void testDefaultAdminUser() {
        Map<String, String> map = new LinkedHashMap<>(6);
        map.put("host.t1.type", "sc1");
        map.put("host.t1.user.root.type", "admin");
        map.put("host.t1.user.oper.type", "oper");
        map.put("host.t1.user.web1.type", "web");
        map.put("host.t1.user.web2.type", "web");
        map.put("host.t1.user.litp-admin.type", "admin");

        final String targetAdminUser = "litp-admin";
        List<Host> hosts = HostPropertyResolver.findHostProperty(map);
        assertThat(hosts).isNotEmpty();

        Optional<Host> foundHost = Iterables.tryFind(hosts, new Predicate<Host>() {
            @Override
            public boolean apply(Host host) {
                boolean foundUser = false;
                for(User user : host.getUsers()) {
                    foundUser = Objects.equals(user.getUsername(), targetAdminUser);
                    if (foundUser) {
                        break;
                    }
                }
                return foundUser;
            }
        });
        assertThat(foundHost.isPresent()).isTrue();
    }

    @Test(expected = UserNotFoundException.class)
    public void testNoDefaultAdminUser() {
        Map<String, String> map = new LinkedHashMap<>(4);
        map.put("host.t1.type", "sc1");
        map.put("host.t1.user.oper.type", "oper");
        map.put("host.t1.user.web1.type", "web");
        map.put("host.t1.user.web2.type", "web");

        Host host = HostPropertyResolver.findHostProperty(map).iterator().next();

        host.getFirstAdminUser();
        fail();
    }

    @Test(expected = UserNotFoundException.class)
    public void testNoUsers() {
        Map<String, String> map = new LinkedHashMap<>(1);
        map.put("host.t1.type", "sc1");

        Host host = HostPropertyResolver.findHostProperty(map).iterator().next();

        host.getFirstAdminUser();
        fail();
    }

    @Test
    public void testGetSpecificUser() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put("host.t1.type", "sc1");
        map.put("host.t1.user.root.type", "admin");
        map.put("host.t1.user.oper.type", "oper");
        map.put("host.t1.user.web1.type", "web");
        map.put("host.t1.user.web2.type", "web");

        Host host = HostPropertyResolver.findHostProperty(map).iterator().next();

        assertTrue(host.getUser("root").getType().toString().equalsIgnoreCase("admin"));
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetSpecificUserThatDoesntExist() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put("host.t1.type", "sc1");
        map.put("host.t1.user.root.type", "admin");
        map.put("host.t1.user.oper.type", "oper");
        map.put("host.t1.user.web1.type", "web");
        map.put("host.t1.user.web2.type", "web");

        Host host = HostPropertyResolver.findHostProperty(map).iterator().next();

        host.getUser("myUser");
        fail();
    }

    @Test
    public void testUserCreation() throws Exception {
        User admin = new User("root", "secret1", UserType.ADMIN);
        User user = new User("user", "secret2", UserType.OPER);

        Host host = Host.builder()
                .withUser(admin)
                .withUser(user)
                .build();

        Assert.assertThat(host.getUser("root"), equalTo(admin));
        Assert.assertThat(host.getUser(UserType.OPER), equalTo("user"));
        Assert.assertThat(host.getDefaultUser(), equalTo(admin));
    }

    @Test
    public void shouldBuildHost_BasicAttributes() {
        Host.HostBuilder builder = Host.builder();
        User admin = new User("root", "secret", UserType.ADMIN);
        Host node = builder.withName("node").build();

        Host host = builder
                .withName("myHost")
                .withIp("127.0.0.1")
                .withNode(node)
                .withType(HostType.GATEWAY)
                .withUser(admin)
                .build();

        Assert.assertThat(host.getHostname(), equalTo("myHost"));
        Assert.assertThat(host.getIp(), equalTo("127.0.0.1"));
        Assert.assertThat(host.getType(), equalTo(HostType.GATEWAY));
        Assert.assertThat(host.getUser("root"), equalTo(admin));
        Assert.assertThat(host.getNodes().iterator().next(), equalTo(node));
    }

    @Test
    public void shouldBuildHost_DefaultPorts() throws Exception {
        Host host = Host.builder()
                .withName("ports")
                .withJmsPort()
                .withJbossManagementPort()
                .withHttpPort()
                .withHttpsPort()
                .withRmiPort()
                .withAmqpPort()
                .withSshPort()
                .build();

        assertEquals(host.getPort(Ports.JMS), new Integer(5540));
        assertEquals(host.getPort(Ports.JBOSS_MANAGEMENT), new Integer(9999));
        assertEquals(host.getPort(Ports.HTTP), new Integer(80));
        assertEquals(host.getPort(Ports.HTTPS), new Integer(443));
        assertEquals(host.getPort(Ports.RMI), new Integer(5520));
        assertEquals(host.getPort(Ports.AMQP), new Integer(5672));
        assertEquals(host.getPort(Ports.SSH), new Integer(22));
    }

    @Test
    public void shouldBuildHost_CustomPorts() throws Exception {
        Host host = Host.builder()
                .withName("ports")
                .withJmsPort(1001)
                .withJbossManagementPort(1002)
                .withHttpPort(1003)
                .withHttpsPort(1004)
                .withRmiPort(1005)
                .withAmqpPort(1006)
                .withSshPort(1007)
                .withJmxPort(1008)
                .withUnknownPort(1009)
                .build();

        assertEquals(host.getPort(Ports.JMS), new Integer(1001));
        assertEquals(host.getPort(Ports.JBOSS_MANAGEMENT), new Integer(1002));
        assertEquals(host.getPort(Ports.HTTP), new Integer(1003));
        assertEquals(host.getPort(Ports.HTTPS), new Integer(1004));
        assertEquals(host.getPort(Ports.RMI), new Integer(1005));
        assertEquals(host.getPort(Ports.AMQP), new Integer(1006));
        assertEquals(host.getPort(Ports.SSH), new Integer(1007));
        assertEquals(host.getPort(Ports.JMX), new Integer(1008));
        assertEquals(host.getPort(Ports.UNKNOWN), new Integer(1009));
    }

    @Test
    public void shouldBuildHostThatFollowsHttpHostInterfaceContract() throws Exception {
        HttpEndpoint host = Host.builder()
                .withName("expectedHostname")
                .withHttpPort(1003)
                .withHttpsPort(1004)
                .withIp("127.0.0.1")
                .withIpv6("[::1]")
                .build();

        assertEquals(host.getHostname(), "expectedHostname");
        assertEquals(host.getIp(), "127.0.0.1");
        assertEquals(host.getIpv6(), "[::1]");
        assertEquals(host.getHttpsPort().intValue(), 1004);
        assertEquals(host.getHttpPort().intValue(), 1003);
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertThat(Host.builder().withName("name").withType(HostType.DB).build().toString(), equalTo("db name"));
        Assert.assertThat(Host.builder().withIp("127.0.0.1").build().toString(), equalTo("null 127.0.0.1"));
        Assert.assertThat(Host.builder().withName("name").withIp("127.0.0.1").build().toString(), equalTo("null name"));
    }

    @Test
    public void testEquals() throws Exception {
        Host host = prepareHost();

        assertThat(host).isNotNull();
        assertThat(host).isEqualTo(host);
        assertThat(host).isNotEqualTo(Host.builder().build());
    }

    private Host prepareHost() {
        Host host = new Host();
        host.setHostname("name");
        host.setIp("127.0.0.1");
        return host;
    }

    @Test
    public void testHashCode() throws Exception {
        Host host1 = prepareHost();
        Host host2 = prepareHost();

        Assert.assertThat(host1.hashCode(), equalTo(host2.hashCode()));
    }

}
