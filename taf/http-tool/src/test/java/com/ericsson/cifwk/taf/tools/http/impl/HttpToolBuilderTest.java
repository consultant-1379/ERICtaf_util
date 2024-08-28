package com.ericsson.cifwk.taf.tools.http.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import com.ericsson.cifwk.taf.tools.http.HttpEndpoint;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.cifwk.taf.tools.http.RequestBuilder;
import com.ericsson.de.tools.http.impl.HttpToolImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpToolBuilderTest {

    public static final Integer HTTP_PORT = 8080;
    public static final Integer HTTPS_PORT = 8443;
    public static final String IPV4 = "127.0.0.1";
    public static final String HOSTNAME = "localhost";
    public static final String IPV6 = "::1";

    @Mock
    private RequestBuilder request;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionUseHttpsIfProvided() {
        HttpToolBuilder
                .newBuilder(getHostNoPortDefined())
                .useHttpsIfProvided(true)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDontUseHttpsIfProvided() {
        HttpToolBuilder
                .newBuilder(getHostHttpsOnly(HTTPS_PORT))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfBothHostnameAndIpv6Provided() throws Exception {
        HttpToolBuilder.newBuilder(getHost(HTTP_PORT, HTTPS_PORT))
                .useHostnameIfProvided(true)
                .useIpv6IfProvided(true)
                .build();
    }

    @Test
    public void shouldUseIpv6IfProvided() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(getCompleteHost())
                .useIpv6IfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getHost(), is("[" + IPV6 + "]"));
    }

    @Test
    public void shouldUseHostnameIfProvided() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(getCompleteHost())
                .useHostnameIfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getHost(), is(HOSTNAME));
    }

    @Test
    public void shouldUseHostnameFromConstructorIfProvided() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(HOSTNAME)
                .withHttpPort(80)
                .useHostnameIfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getHost(), is(HOSTNAME));
    }

    @Test
    public void shouldFallbackToIPv4ifIPv6isMissing() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(getHostHttpOnly(HTTP_PORT))
                .useHostnameIfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getHost(), is(IPV4));
    }

    @Test
    public void shouldFallbackToIPv4ifHostnameIsMissing() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(getHostHttpOnly(HTTP_PORT))
                .useIpv6IfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getHost(), is(IPV4));
    }

    @Test
    public void shouldUseHttpUseHttpsIfProvided() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(getHostHttpOnly(HTTP_PORT))
                .useHttpsIfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getPort(), is(HTTP_PORT));
    }

    @Test
    public void shouldUseHttpPorts() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(HOSTNAME)
                .withHttpPort(HTTP_PORT)
                .useHttpsIfProvided(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getPort(), is(HTTP_PORT));
    }

    @Test
    public void shouldUseHttpsPorts() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(HOSTNAME)
                .withHttpPort(HTTP_PORT)
                .withHttpsPort(HTTPS_PORT)
                .useHttpsIfProvided(true)
                .trustSslCertificates(true)
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getPort(), is(HTTPS_PORT));
    }

    @Test
    public void shouldUseHttpDontUseHttpsIfProvided() throws MalformedURLException {
        HttpToolImpl httpTool = (HttpToolImpl) HttpToolBuilder
                .newBuilder(getHost(HTTP_PORT, HTTPS_PORT))
                .build();

        assertThat(new URL(httpTool.getBaseUrl()).getPort(), is(HTTP_PORT));
    }

    @Test
    public void shouldUseHttps() throws MalformedURLException {
        HttpToolImpl httpTool = spy((HttpToolImpl) HttpToolBuilder
                .newBuilder(getHost(HTTP_PORT, HTTPS_PORT))
                .useHttpsIfProvided(true)
                .trustSslCertificates(true)
                .build());

        assertThat(new URL(httpTool.getBaseUrl()).getPort(), is(HTTPS_PORT));
    }

    private HttpEndpoint getHost(int httpPortNumber, int httpsPortNumber) {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn(IPV4);
        when(mock.getHttpPort()).thenReturn(httpPortNumber);
        when(mock.getHttpsPort()).thenReturn(httpsPortNumber);
        return mock;
    }

    private HttpEndpoint getHostHttpOnly(int portNumber) {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn(IPV4);
        when(mock.getHttpPort()).thenReturn(portNumber);
        when(mock.getHttpsPort()).thenReturn(null);
        return mock;
    }

    private HttpEndpoint getHostHttpsOnly(int portNumber) {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn(IPV4);
        when(mock.getHttpsPort()).thenReturn(portNumber);
        when(mock.getHttpPort()).thenReturn(null);
        return mock;
    }

    private HttpEndpoint getHostNoPortDefined() {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getHttpPort()).thenReturn(null);
        when(mock.getHttpsPort()).thenReturn(null);
        when(mock.getIp()).thenReturn(IPV4);
        return mock;
    }

    private HttpEndpoint getCompleteHost() {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn(IPV4);
        when(mock.getIpv6()).thenReturn(IPV6);
        when(mock.getHostname()).thenReturn(HOSTNAME);
        when(mock.getHttpPort()).thenReturn(80);
        when(mock.getHttpsPort()).thenReturn(443);
        return mock;
    }
}
