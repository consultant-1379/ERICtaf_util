package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.configuration.TafHost;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static com.ericsson.cifwk.taf.guice.TafHostFilter.isValueSet;
import static com.ericsson.cifwk.taf.guice.TafHostInjector.asString;
import static com.ericsson.cifwk.taf.guice.TafHostValidationUtils.getTafHostAnnotation;
import static org.assertj.core.api.Assertions.assertThat;

public class TafHostInjectorTest {

    @TafHost(hostname = "hostname1", type = "type1", group = "group1")
    private Host host;

    @TafHost
    private Host undefinedHost;

    private TafHost hostAnnotation;

    private TafHost undefinedHostAnnotation;

    @Before
    public void setUp() throws NoSuchFieldException {
        Field field = TafHostInjectorTest.class.getDeclaredField("host");
        hostAnnotation = getTafHostAnnotation(field);

        field = TafHostInjectorTest.class.getDeclaredField("undefinedHost");
        undefinedHostAnnotation = getTafHostAnnotation(field);
    }

    @Test
    public void tafHostIsValueSet() {
        assertThat(isValueSet(hostAnnotation.hostname())).isTrue();
        assertThat(isValueSet(undefinedHostAnnotation.hostname())).isFalse();
    }

    @Test
    public void hostAnnotationToString() {
        String expected = "(hostname: 'hostname1', group: 'group1', type: 'type1')";
        assertThat(asString(hostAnnotation)).isEqualTo(expected);
    }

    @Test
    public void hostToString() {
        String expected = "(hostname: 'hostname1', group: 'group1', type: 'sc1')";
        Host host = Host.builder().withName("hostname1").withType(HostType.SC1).build();
        host.setGroup("group1");
        assertThat(asString(host)).isEqualTo(expected);
    }
}