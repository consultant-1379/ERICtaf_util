package com.ericsson.cifwk.taf.tools.http.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.ericsson.cifwk.taf.tools.http.HttpEndpoint;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpToolImplTest {

    private static HttpTool httpTool;

    @BeforeClass
    public static void createHttpTool() {
        HttpEndpoint host = mock(HttpEndpoint.class);
        when(host.getIp()).thenReturn("127.0.0.1");
        when(host.getHttpPort()).thenReturn(80);
        when(host.getHostname()).thenReturn("Hostname");

        HttpToolBuilder httpToolBuilder = HttpToolBuilder.newBuilder(host);
        httpTool = httpToolBuilder.build();
        httpTool.addCookie("name", "value");
    }

    @Test
    public void shouldReturnCopyOfHttpTool() {
        HttpTool copiedHttpTool = httpTool.copy();
        assertThat("Http Tools do not match based on .equals()", (httpTool.equals(copiedHttpTool)));
    }

    @Test
    public void shouldReturnCookies() {
        Map<String, String> cookies = httpTool.getCookies();
        assertThat("get cookies method returned incorrect value", cookies.get("name").equals("value"));
    }
}
