package com.ericsson.cifwk.taf.configuration;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Test;

public class HostPropertyParserTest {
    @After
    public void tearDown() throws Exception {
        TafDataHandler.getConfiguration().clear();
    }

    @Test
    public void buildHostPropertyTree() throws Exception {
        ImmutableMap<Object, Object> props = ImmutableMap.builder()
                .put("host.ms1.ip", "10.43.251.3")
                .put("host.ms1.type", "ms")
                .put("host.ms1.port.ssh", "22")
                .put("host.ms1.node.netsim1.tunnel", "1")
                .put("host.ms1.node.netsim1.ip", "ieatnetsimv5040-03.athtem.eei.ericsson.se")
                .put("host.ms1.node.netsim1.type", "netsim")
                .put("host.ms1.node.netsim1.port.ssh", "22")
                .put("host.ms1.node.netsim2.tunnel", "2")
                .put("host.ms1.node.netsim2.ip", "10.140.24.144")
                .put("host.ms1.node.netsim2.type", "netsim")
                .put("host.ms1.node.netsim2.port.ssh", "22")

                .put("host.ms2.ip", "10.43.251.4")
                .put("host.ms2.type", "ms")

                .put("nothost", "randomProperty")
                .build();

        Map<String, Object> tree = HostPropertyParser.buildHostPropertyTree(props);

        assertThat(tree).hasSize(2);
        Map ms1 = (Map) tree.get("ms1");
        assertThat(ms1).hasSize(4);

        assertThat(ms1.get("ip")).isEqualTo("10.43.251.3");

        assertThat(ms1.get("type")).isEqualTo("ms");

        Map port = (Map) ms1.get("port");
        assertThat(port).hasSize(1);
        assertThat(port.get("ssh")).isEqualTo("22");

        Map node = (Map) ms1.get("node");
        assertThat(node).hasSize(2);

        Map netsim1 = (Map) node.get("netsim1");
        assertThat(netsim1).hasSize(4);

        assertThat(netsim1.get("ip")).isEqualTo("ieatnetsimv5040-03.athtem.eei.ericsson.se");
        assertThat(netsim1.get("type")).isEqualTo("netsim");
        assertThat(netsim1.get("tunnel")).isEqualTo("1");

        Map netsim1_port = (Map) netsim1.get("port");
        assertThat(netsim1_port).hasSize(1);
        assertThat(netsim1_port.get("ssh")).isEqualTo("22");

        Map netsim2 = (Map) node.get("netsim2");
        assertThat(netsim2).hasSize(4);

        assertThat(netsim2.get("ip")).isEqualTo("10.140.24.144");
        assertThat(netsim2.get("type")).isEqualTo("netsim");
        assertThat(netsim2.get("tunnel")).isEqualTo("2");

        Map netsim2_port = (Map) netsim2.get("port");
        assertThat(netsim2_port).hasSize(1);
        assertThat(netsim2_port.get("ssh")).isEqualTo("22");

        Map ms2 = (Map) tree.get("ms2");
        assertThat(ms2).hasSize(2);
        assertThat(ms2.get("ip")).isEqualTo("10.43.251.4");
        assertThat(ms2.get("type")).isEqualTo("ms");
    }

    @Test
    public void ignoreWrongProperties() throws Exception {
        HashMap<Object, Object> props = Maps.newHashMap();
        props.put("nothost.ms1.ip", "ignored");
        props.put("host.ms1.ip", "10.43.251.3");
        props.put("host.ms1.", "wrong");
        props.put("host.ms1.ip.type", "wrong");
        props.put("host......", "wrong");
        props.put("host.", "wrong");
        props.put("host", "wrong");
        props.put(null, null);
        props.put("host.ms1.type", "ms");

        Map<String, Object> tree = HostPropertyParser.buildHostPropertyTree(props);

        assertThat(tree).hasSize(1);
        Map ms1 = (Map) tree.get("ms1");
        assertThat(ms1).hasSize(2);
        assertThat(ms1.get("ip")).isEqualTo("10.43.251.3");
        assertThat(ms1.get("type")).isEqualTo("ms");
    }
}