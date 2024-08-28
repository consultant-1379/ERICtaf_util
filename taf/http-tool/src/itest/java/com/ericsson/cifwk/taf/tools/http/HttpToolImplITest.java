package com.ericsson.cifwk.taf.tools.http;


import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpToolImplITest {

    private EmbeddedJetty jetty;

    @Before
    public void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(new BasicHttpServlet(), "/test/*")
                .start();
    }

    @After
    public void tearDown() throws Exception {
        jetty.stop();
    }

    @Test
    public void responseShouldBeTheSameAfterCopy() {
        HttpTool firstTool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        firstTool.addCookie("Name", "Value");

        HttpTool secondTool = firstTool.copy();

        HttpResponse responseFromFirst = firstTool.get("/test/");
        HttpResponse responseFromSecond = secondTool.get("/test/");

        assertThat(responseFromFirst.getBody(), equalTo(responseFromSecond.getBody()));
        assertThat(responseFromFirst.getCookies(), equalTo(responseFromSecond.getCookies()));
        assertThat(responseFromFirst.getContentType(), equalTo(responseFromSecond.getContentType()));
        assertThat(responseFromFirst.getHeaders(), equalTo(responseFromSecond.getHeaders()));
        assertThat("Created cookie name not found", responseFromFirst.getCookies().containsKey("Name"));
        assertThat("Created cookie value not found", responseFromFirst.getCookies().containsValue("Value"));
        assertThat("Created cookie name not found", responseFromSecond.getCookies().containsKey("Name"));
        assertThat("Created cookie value not found", responseFromSecond.getCookies().containsValue("Value"));
    }

    @Test
    public void cookiesCopiedCorrectlyDuringCopy() {
        HttpTool firstTool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        firstTool.addCookie("Shared Name", "Shared Value");
        HttpTool secondTool = firstTool.copy();
        firstTool.addCookie("First Name", "First Value");
        secondTool.addCookie("Second Name", "Second Value");

        HttpResponse responseFromFirst = firstTool.get("/test/");
        HttpResponse responseFromSecond = secondTool.get("/test/");

        assertThat("Created cookie name not found", responseFromFirst.getCookies().containsKey("Shared Name"));
        assertThat("Created cookie value not found", responseFromFirst.getCookies().containsValue("Shared Value"));
        assertThat("Created cookie name not found", responseFromFirst.getCookies().containsKey("First Name"));
        assertThat("Created cookie value not found", responseFromFirst.getCookies().containsValue("First Value"));
        assertThat("Cookie name found in wrong httpTool", !responseFromFirst.getCookies().containsKey("Second Name"));
        assertThat("Cookie name found in wrong httpTool", !responseFromFirst.getCookies().containsValue("Second Value"));

        assertThat("Created cookie name not found", responseFromSecond.getCookies().containsKey("Shared Name"));
        assertThat("Created cookie value not found", responseFromSecond.getCookies().containsValue("Shared Value"));
        assertThat("Created cookie name not found", responseFromSecond.getCookies().containsKey("Second Name"));
        assertThat("Created cookie value not found", responseFromSecond.getCookies().containsValue("Second Value"));
        assertThat("Cookie name found in wrong httpTool", !responseFromSecond.getCookies().containsKey("First Name"));
        assertThat("Cookie name found in wrong httpTool", !responseFromSecond.getCookies().containsValue("First Value"));
    }

    @Test
    public void testToolCanHandleEmptyResponse() {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.head("/test/");

        assertThat(response.getBody(), nullValue());
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));

        response = tool.delete("/test/");
        assertThat(response.getBody(), equalTo("{}"));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    private HttpEndpoint getDefaultHost() {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn("127.0.0.1");
        when(mock.getHttpPort()).thenReturn(jetty.getPort());
        return mock;
    }
}
