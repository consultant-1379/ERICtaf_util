package com.ericsson.cifwk.diagmon.util.instr.outputhandlers.taf;

import com.ericsson.cifwk.diagmon.util.instr.config.ConfigTreeNode;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static  org.mockito.Mockito.when;

public class ConfigTest {

    Config config;
    ConfigTreeNode node;
    ConfigTreeNode parent;

    @Before
    public void setUp() throws Exception {
        node = mock(ConfigTreeNode.class);
        parent = mock(ConfigTreeNode.class);
        when(node.getParent()).thenReturn(parent);
        config = new Config(node);
    }

    @Test
    public void shouldReturnLocalhostAsMonitoredHost() throws Exception {
        when(parent.getChildren()).thenReturn(Collections.EMPTY_LIST);
        String host = config.getMonitoredHost();
        String expected = InetAddress.getLocalHost().getHostName().split("\\.")[0];
        assertEquals(expected, host);
    }

    @Test
    public void shouldReturnHostName_when_ipServiceHostName_isin_DDCXML() throws Exception {
        ConfigTreeNode ipService = mock(ConfigTreeNode.class);
        when(ipService.baseName()).thenReturn("ipService");
        when(ipService.getAttribute("host")).thenReturn(InetAddress.getLocalHost().getCanonicalHostName());
        when(parent.getChildren()).thenReturn(Arrays.asList(ipService));
        String host = config.getMonitoredHost();
        String expected = InetAddress.getLocalHost().getHostName().split("\\.")[0];
        assertEquals(expected, host);
    }

    @Test
    public void shouldReturnHostName_when_ipServiceHostIP_isin_DDCXML() throws Exception {
        ConfigTreeNode ipService = mock(ConfigTreeNode.class);
        when(ipService.baseName()).thenReturn("ipService");
        when(ipService.getAttribute("host")).thenReturn(InetAddress.getLocalHost().getHostAddress());
        when(parent.getChildren()).thenReturn(Arrays.asList(ipService));
        String host = config.getMonitoredHost();
        String expected = InetAddress.getLocalHost().getHostName().split("\\.")[0];
        assertEquals(expected, host);
    }

}
