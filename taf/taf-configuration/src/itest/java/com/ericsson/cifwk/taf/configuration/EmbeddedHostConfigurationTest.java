package com.ericsson.cifwk.taf.configuration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by ekonsla on 19/08/2016.
 */
public class EmbeddedHostConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testHttpsNonDefaultPortUseSchema() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("non.default.https.host.port.use.schema"), is("https://10.43.251.3:8443/login"));
    }

    @Test
    public void testHttpNonDefaultPortUseSchema() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("non.default.http.host.port.use.schema"), is("http://10.43.251.3:8080/login"));
    }

    @Test
    public void testHttpsDefaultPort() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("default.https.host.port"), is("10.43.251.3/login"));
    }

    @Test
    public void testHttpDefaultPort() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("default.http.host.port"), is("10.43.251.3/login"));
    }


    @Test
    public void testHttpsNonDefaultPort() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("non.default.https.host.port"), is("10.43.251.3:8443/login"));
    }

    @Test
    public void testHttpNonDefaultPort() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("non.default.http.host.port"), is("10.43.251.3:8080/login"));
    }

    @Test
    public void testNonHttp() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("non.http.host.port"), is("10.43.251.3:22/login"));
    }

    @Test
    public void testNonHttpUseSchema() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("non.http.host.port.use.schema"), is("10.43.251.3:22/login"));
    }

    @Test
    public void testHostNotFound() throws Exception {
        thrown.expectMessage("Host [fifth] is  not found");
        TafConfiguration configuration = TafConfigurationProvider.provide();
        configuration.getString("not.found.host");
    }

    @Test
    public void testNoSchema() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        assertThat(configuration.getString("just.host"), is("10.43.251.3/login"));
    }

    @Test
    public void testHostNoPortDefined() throws Exception {
        thrown.expectMessage("Port of type [ssh] is not defined for host [third]");
        TafConfiguration configuration = TafConfigurationProvider.provide();
        configuration.getString("not.defined.port");
    }

    @Test
    public void testEmptyHost() throws Exception {
        thrown.expectMessage("Host definition should come in a format: ${host:<hostname>} or  ${host:<hostname> port:<porttype>}");
        TafConfiguration configuration = TafConfigurationProvider.provide();
        configuration.getString("empty.host");
    }
}
