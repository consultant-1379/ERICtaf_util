package com.ericsson.cifwk.taf.data;

import com.ericsson.cifwk.taf.data.resolver.HostPropertyResolver;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class HostLoadingTest {

    private static final String FM_SERV_HOST = "fmserv_1";
    private static final String SVC1_HOST = "svc1";
    private static final String STR1_HOST = "str1";

    List<Host> hosts;

    @Before
    public void setup() throws Exception {
        URL url = Resources.getResource("taf_properties/hosts.properties");
        String text = Resources.toString(url, Charsets.UTF_8);
        Map<String, String> map = Splitter.on("\n").withKeyValueSeparator(" = ").split(text);
        Map<String, String> trimmed = Maps.transformValues(map, new Function<String, String>() {
            @Override
            public String apply(String input) {
                return CharMatcher.BREAKING_WHITESPACE.trimFrom(input);
            }
        });
        hosts = HostPropertyResolver.findHostProperty(trimmed);
    }

    @Test
    public void testLoadHosts() {
        Host host = Iterables.tryFind(hosts, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return input.getHostname().equals(FM_SERV_HOST);
            }
        }).get();

        assertThat(host.getParentName(), equalTo(SVC1_HOST));
        assertThat(host.getTunnelPortOffset(), nullValue());
    }

    @Test
    public void testCreateStreamingHost() {
        Host streamingHost1 = Iterables.tryFind(hosts, new Predicate<Host>() {
            @Override
            public boolean apply(Host input) {
                return input.getHostname().equals(STR1_HOST);
            }
        }).get();

        assertThat(streamingHost1.getPort(Ports.SSH), equalTo(22));
        assertThat(streamingHost1.getIp(), equalTo("192.168.0.243"));
        assertThat(streamingHost1.getDefaultUser().getUsername(), equalTo("litp-admin"));
    }
}
