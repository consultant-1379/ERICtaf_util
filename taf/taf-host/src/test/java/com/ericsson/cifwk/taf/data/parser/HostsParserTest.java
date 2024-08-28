package com.ericsson.cifwk.taf.data.parser;

import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.*;
import com.ericsson.cifwk.taf.data.parsers.HostsParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class HostsParserTest {
    
    @Before
    public void setUp() throws Exception {
        TafConfigurationProvider.provide().reload();
    }

    @Test
    public void verifyJsonParser() throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/taf_properties/hosts.properties.json"), "UTF-8")) {
            List<Host> hostList = HostsParser.parse(reader);
            Assert.assertEquals(11, hostList.size());

            Host sc1Host = hostList.get(1);
            Assert.assertEquals("sc1", sc1Host.getHostname());
            Assert.assertEquals("0.0.0.2", sc1Host.getIp());
            Assert.assertEquals(HostType.SC1, sc1Host.getType());
            Assert.assertEquals(1, sc1Host.getUsers().size());
            Assert.assertEquals(1, sc1Host.getPort().size());
            Assert.assertEquals(1, sc1Host.getNodes().size());

            User sc1HostUser = sc1Host.getUsers().get(0);
            Assert.assertEquals("root", sc1HostUser.getUsername());
            Assert.assertEquals("cobbler", sc1HostUser.getPassword());
            Assert.assertEquals(UserType.ADMIN, sc1HostUser.getType());

            Assert.assertEquals("22", sc1Host.getPort().get(Ports.SSH));

            Host sc1HostNode = sc1Host.getNodes().get(0);
            Assert.assertEquals("jboss1", sc1HostNode.getHostname());
            Assert.assertEquals("0.0.0.2", sc1HostNode.getIp());
            Assert.assertEquals(HostType.JBOSS, sc1HostNode.getType());
            Assert.assertEquals(2, sc1HostNode.getUsers().size());
            Assert.assertEquals(4, sc1HostNode.getPort().size());

            Assert.assertEquals("8080", sc1HostNode.getPort().get(Ports.HTTP));
            Assert.assertEquals("4447", sc1HostNode.getPort().get(Ports.RMI));
            Assert.assertEquals("9999", sc1HostNode.getPort().get(Ports.JMX));
            Assert.assertEquals("9999", sc1HostNode.getPort().get(Ports.JBOSS_MANAGEMENT));

            Host badHost = hostList.get(3);
            Assert.assertEquals("BadHost", badHost.getHostname());
            Assert.assertEquals("0.0.0.4", badHost.getIp());
            Assert.assertEquals(HostType.UNKNOWN, badHost.getType());
            Assert.assertEquals(1, badHost.getUsers().size());
            Assert.assertEquals(UserType.OPER, badHost.getUsers().get(0).getType());
            Assert.assertEquals(2, badHost.getPort().size());
            Assert.assertEquals("8080", badHost.getPort().get(Ports.HTTP));
            Assert.assertEquals("1234", badHost.getPort().get(Ports.UNKNOWN));
        }
    }
}
