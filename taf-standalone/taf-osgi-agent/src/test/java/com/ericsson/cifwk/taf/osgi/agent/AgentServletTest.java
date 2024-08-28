package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static com.ericsson.cifwk.taf.osgi.agent.TestUtils.readResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class AgentServletTest {

    private static EmbeddedJetty jetty;
    private static String basePath;
    private static DefaultHttpClient httpClient;

    @BeforeClass
    public static void beforeClass() throws Exception {
        BundleContext bundleContext = mock(BundleContext.class);
        jetty = EmbeddedJetty
                .build()
                .withServlet(new AgentServlet(bundleContext), Activator.AGENT_PATH + "/*")
                .start();
        basePath = "http://localhost:" + jetty.getPort() + Activator.AGENT_PATH;
        httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
    }

    @Test
    public void testGet() throws Exception {
        HttpResponse response = httpClient.execute(new HttpGet(basePath));
        int statusCode = response.getStatusLine().getStatusCode();

        assertEquals(HttpServletResponse.SC_OK, statusCode);
    }

    @Test
    public void testSimpleExpression() throws Exception {
        String source = readResource("scripts/AddOperator.groovy");
        String className = "scripts.AddOperator";
        registerClass(source, className);

        HttpPost post = new HttpPost(basePath + "/classes/" + className);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(GroovyInvocation.METHOD_PARAM, "add"));
        parameters.add(new BasicNameValuePair(GroovyInvocation.ARG_COUNT_PARAM, "2"));
        parameters.add(new BasicNameValuePair(GroovyInvocation.ARG_PARAM + "0", "3"));
        parameters.add(new BasicNameValuePair(GroovyInvocation.ARG_PARAM + "1", "4"));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        verifyAgentResult(post, 200, "7");
    }

    @Test
    public void testInvocationException() throws Exception {
        String source = "package scripts\n" +
                "\n" +
                "import com.ericsson.cifwk.taf.osgi.agent.ApiOperator\n" +
                "\n" +
                "class ExceptionOperator implements ApiOperator {\n" +
                "\n" +
                "    public String execute() {\n" +
                "        throw new RuntimeException(\"Oops\")\n" +
                "    }\n" +
                "\n" +
                "}";
        String className = "scripts.ExceptionOperator";
        registerClass(source, className);

        HttpPost post = new HttpPost(basePath + "/classes/" + className);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(GroovyInvocation.METHOD_PARAM, "execute"));
        parameters.add(new BasicNameValuePair(GroovyInvocation.ARG_COUNT_PARAM, "0"));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        HttpResponse response = httpClient.execute(post);
        assertEquals(400, response.getStatusLine().getStatusCode());
        String body = EntityUtils.toString(response.getEntity());
        assertThat(body, new StringContains("couldn't invoke method"));
        assertThat(body, new StringContains("Oops"));
    }

    @Test
    public void testMissingClass() throws Exception {
        HttpPost post = new HttpPost(basePath + "/classes/com.example.NoSuchClass");
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(GroovyInvocation.METHOD_PARAM, "method"));
        parameters.add(new BasicNameValuePair(GroovyInvocation.ARG_COUNT_PARAM, "0"));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        verifyAgentResult(post, 400);
    }

    private void registerClass(String source, String className) throws Exception {
        HttpPost post = new HttpPost(basePath + "/classes");
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair(GroovyInvocation.SOURCE_PARAM, source));
        post.setEntity(new UrlEncodedFormEntity(parameters));

        verifyAgentResult(post, HttpServletResponse.SC_OK, className);
    }

    private void verifyAgentResult(HttpUriRequest request, int expectedStatusCode)
            throws Exception {
        verifyAgentResult(request, expectedStatusCode, null);
    }

    private void verifyAgentResult(HttpUriRequest request, int expectedStatusCode, String expectedBody)
            throws Exception {
        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());

        assertEquals(body, expectedStatusCode, statusCode);
        if (expectedBody != null) {
            assertEquals(expectedBody, body);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        jetty.stop();
    }

}
