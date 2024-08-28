package com.ericsson.cifwk.taf.configuration.interpol;

import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.exception.HostInterpolationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by ekonsla on 18/08/2016.
 */
public class HostLookupTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private HostLookup unit = new HostLookup();

    @Test
    public void testLookupEmpty() {
        thrown.expect(HostInterpolationException.class);
        thrown.expectMessage("Host definition should come in a format: ${host:<hostname>} or  ${host:<hostname> port:<porttype>}");
        unit.lookup("");
    }

    @Test
    public void testFindParam() {
        List<String> params = new ArrayList<>();
        params.add(" port:https");
        assertThat(unit.findParam("port", params).get(), is("https"));
        assertThat(unit.findParam("port", params).get(), is("https"));
    }

    @Test
    public void testFindEmptyParam() {
        thrown.expect(HostInterpolationException.class);
        thrown.expectMessage("Parameter [port] is empty");
        List<String> params = new ArrayList<>();
        params.add(" port: ");
        unit.findParam("port", params);
    }

    @Test
    public void testGetPort() {
        TafConfiguration configuration = mock(TafConfiguration.class);
        doReturn("443").when(configuration).getString(eq("host.svc1.port.https"));
        assertThat(unit.getPort("svc1", Arrays.asList("port:https"), configuration), is(""));

        doReturn("8443").when(configuration).getString(eq("host.svc1.port.https"));
        assertThat(unit.getPort("svc1", Arrays.asList("port:https"), configuration), is(":8443"));

        doReturn("80").when(configuration).getString(eq("host.svc1.port.http"));
        assertThat(unit.getPort("svc1", Arrays.asList("port:http"), configuration), is(""));

        doReturn("8080").when(configuration).getString(eq("host.svc1.port.http"));
        assertThat(unit.getPort("svc1", Arrays.asList("port:http"), configuration), is(":8080"));

        assertThat(unit.getPort("svc1", new ArrayList<String>(), configuration), is(""));
    }

    @Test
    public void testGetPortNotDefined() {
        thrown.expect(HostInterpolationException.class);
        thrown.expectMessage("Port of type [https] is not defined for host [svc1]");
        TafConfiguration configuration = mock(TafConfiguration.class);
        assertThat(unit.getPort("svc1", Arrays.asList("port:https"), configuration), is(""));
    }

    @Test
    public void testIsDefault() {
        assertTrue(unit.isDefaultPort("https", "443"));
        assertFalse(unit.isDefaultPort("https", "8443"));
        assertTrue(unit.isDefaultPort("http", "80"));
        assertFalse(unit.isDefaultPort("http", "8080"));
        assertFalse(unit.isDefaultPort("ssh", "8080"));
        assertFalse(unit.isDefaultPort("ssh", "22"));
    }

    @Test
    public void testGetSchema() {
        assertThat(unit.getSchema(Arrays.asList("port:ssh", "useSchema:true")), is(""));
        assertThat(unit.getSchema(Arrays.asList("port:jmx", "useSchema:true")), is(""));
        assertThat(unit.getSchema(Arrays.asList("port:http", "useSchema:true")), is("http://"));
        assertThat(unit.getSchema(Arrays.asList("port:https", "useSchema:true")), is("https://"));
    }

    @Test
    public void testGetSchemaPortTypeNotDefined() {
        thrown.expect(HostInterpolationException.class);
        thrown.expectMessage("port is not defined for ");
        unit.getSchema(Arrays.asList("useSchema:true"));
    }

    @Test
    public void testUseSchema() {
        assertTrue(unit.isUseSchema(Arrays.asList("useSchema:true")));
        assertFalse(unit.isUseSchema(Arrays.asList("useSchema:false")));
        assertFalse(unit.isUseSchema(Arrays.asList("bla:bla")));
        assertFalse(unit.isUseSchema(Arrays.asList("useSchema:truefalse")));
    }
}
