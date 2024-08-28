package com.ericsson.cifwk.taf.tools.http;

import static com.ericsson.cifwk.taf.tools.http.BasicHttpServlet.DEFAULT_COOKIE_NAME;
import static com.ericsson.cifwk.taf.tools.http.BasicHttpServlet.DEFAULT_COOKIE_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.ericsson.cifwk.taf.tools.http.impl.HttpToolListeners;
import com.ericsson.de.tools.http.DnsResolver;
import com.ericsson.de.tools.http.impl.HttpToolImpl;
import com.google.common.io.CharStreams;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mortbay.util.IO;
import org.mortbay.util.ajax.JSON;

public class HttpToolTest {

    private EmbeddedJetty jetty;
    private static final Integer HTTPS_PORT = 44_443;
    private final static String KEYSTORE_LOCATION = "target/itest-classes/keystore.jks";
    private final static String KEYSTORE_PASS = "password";
    private final static String TRUSTSTORE_LOCATION = "target/itest-classes/truststore.jks";
    private final static String TRUSTSTORE_PASS = "password";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private HttpToolListener httpToolListener;

    @Before
    public void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(new BasicHttpServlet(), "/test/*")
                .start(25);
        httpToolListener = mock(HttpToolListener.class);
        HttpToolListeners.addListener(httpToolListener);
    }

    @After
    public void tearDown() throws Exception {
        HttpToolListeners.removeAllListeners();
        jetty.stop();
    }

    @Test
    public void testGet() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(), equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testGetIpv4() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Host\":\"127.0.0.1:"));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testGetIpv6() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .useIpv6IfProvided(true)
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Host\":\"[::1]:"));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testGetHostname() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .useHostnameIfProvided(true)
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Host\":\"localhost:"));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMisformattedUrl() throws Exception {
        HttpEndpoint host = mock(HttpEndpoint.class);
        when(host.getIpv6()).thenReturn("[::1]");
        when(host.getHttpPort()).thenReturn(jetty.getPort());

        HttpTool tool = HttpToolBuilder.newBuilder(host).useIpv6IfProvided(true).build();
        tool.get("/test/");
    }

    @Test
    public void testGetWithCustomResponseHandler() throws Exception {
        HttpToolImpl tool = (HttpToolImpl) HttpToolBuilder.newBuilder(getDefaultHost()).build();
        tool.setResponseHandler(new ResponseHandler() {
            @Override
            public HttpResponse handle(org.apache.http.HttpResponse response) throws IOException {
                HttpEntity responseEntity = response.getEntity();
                return new HttpTestResponse(EntityUtils.toByteArray(responseEntity), ContentType.APPLICATION_XML, HttpStatus.BAD_REQUEST);
            }
        });
        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(), equalTo(ContentType.APPLICATION_XML));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.BAD_REQUEST));
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testGetWithoutSlash() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.get("test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(), equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testGetParams() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .queryParam("param1", "value1")
                .queryParam("param2", "value2")
                .get("/test/");

        Map<String, String[]> result = parseJson(response);

        assertThat(result, hasEntry("param1", new String[]{"value1"}));
        assertThat(result, hasEntry("param2", new String[]{"value2"}));
    }

    @Test
    public void testPost() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .contentType(ContentType.TEXT_PLAIN)
                .body("Some data")
                .post("/test/");

        assertThat(response.getBody(), containsString("Some data"));
    }

    @Test
    public void testPostForm() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .followRedirect(true)
                .build();

        HttpResponse response = tool.request()
                .body("field1", "value1")
                .body("field2", "value2")
                .post("/test/");

        assertThat(response.getBody(), containsString("field1=value1"));
        assertThat(response.getBody(), containsString("field2=value2"));
    }

    @Test
    public void testPostFormMultipart1() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .followRedirect(true)
                .build();

        HttpResponse response = tool.request()
                .body("field1", "value1")
                .body("field2", "value2")
                .post("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Content-Type\":\"application/x-www-form-urlencoded\""));
    }

    @Test
    public void testPostFormMultipart2() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .followRedirect(true)
                .build();

        ByteInputStream inputStream = new ByteInputStream("value3".getBytes(), 6);
        HttpResponse response = tool.request()
                .body("field1", "value1")
                .body("field2", "value2")
                .body("field3", inputStream)
                .post("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Content-Type\":\"multipart/form-data; boundary="));
    }

    @Test
    public void testPostFormMultipart3() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .followRedirect(true)
                .build();

        HttpResponse response = tool.request()
                .contentType(ContentType.MULTIPART_FORM_DATA)
                .body("field1", "value1")
                .body("field2", "value2")
                .post("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Content-Type\":\"multipart/form-data; boundary="));
    }


    @Test
    public void testPostNoBody() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .post("/test/");

        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testGetHeaders() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        final String HEADER_NAME = "Referer";
        final String HEADER_VALUE = "http://en.wikipedia.org/wiki/Main_Page";

        HttpResponse response = tool.request()
                .header(HEADER_NAME, HEADER_VALUE)
                .get("/test" + BasicHttpServlet.HEADERS);

        Map<String, String> result = parseJson(response);

        assertThat(result, hasEntry(HEADER_NAME, HEADER_VALUE));
    }

    @Test
    public void testGetWithAdditionalCookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);
        HttpResponse httpResponse = tool.get("/test" + BasicHttpServlet.COOKIES);

        Map<String, String> result = parseJson(httpResponse);
        assertThat(result, hasEntry(COOKIE_NAME, COOKIE_VALUE));
    }

    @Test
    public void testGetWithMultipleCookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);

        tool.get("/test" + BasicHttpServlet.COOKIES);
        HttpResponse httpResponse = tool.get("/test" + BasicHttpServlet.COOKIES);

        Map<String, String> result = parseJson(httpResponse);
        assertThat(result, hasEntry(BasicHttpServlet.DEFAULT_COOKIE_NAME, BasicHttpServlet.DEFAULT_COOKIE_VALUE));
        assertThat(result, hasEntry(COOKIE_NAME, COOKIE_VALUE));
    }

    @Test
    public void testGetWithHostnameCookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .useHostnameIfProvided(true)
                .build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);

        HttpResponse firstResponse = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> firstResult = parseJson(firstResponse);
        assertThat(firstResult, hasEntry(COOKIE_NAME, COOKIE_VALUE));

        HttpResponse second = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> secondResult = parseJson(second);
        assertThat(secondResult, hasEntry(DEFAULT_COOKIE_NAME, DEFAULT_COOKIE_VALUE));
        assertThat(secondResult, hasEntry(COOKIE_NAME, COOKIE_VALUE));
    }

    @Test
    public void testGetWithIpv6Cookies() throws Exception {
        final String COOKIE_NAME = "Session_id";
        final String COOKIE_VALUE = "1";
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .useIpv6IfProvided(true)
                .build();
        tool.addCookie(COOKIE_NAME, COOKIE_VALUE);

        HttpResponse firstResponse = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> firstResult = parseJson(firstResponse);
        assertThat(firstResult, hasEntry(COOKIE_NAME, COOKIE_VALUE));

        HttpResponse second = tool.get("/test" + BasicHttpServlet.COOKIES);
        Map<String, String> secondResult = parseJson(second);
        assertThat(secondResult, hasEntry(DEFAULT_COOKIE_NAME, DEFAULT_COOKIE_VALUE));
        assertThat(secondResult, hasEntry(COOKIE_NAME, COOKIE_VALUE));
    }


    @Test
    public void testUploadFile() throws Exception {
        final String CONTENT = "This is the test";

        File file = tempFolder.newFile("temp.txt");
        BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(file.toURI()), Charset.defaultCharset());
        writer.append(CONTENT);
        writer.flush();

        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .contentType(ContentType.APPLICATION_OCTET_STREAM)
                .file(file)
                .post("/test/");

        assertThat(response.getBody(), equalTo('"' + CONTENT + '"'));
    }

    @Test
    public void testDownloadTextFile() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .queryParam("type", "text")
                .get("/test/download");
        assertThat(response.getBody(), containsString("TEXT FILE CONTENT"));
        assertThat(response.getContentType(), equalTo(ContentType.TEXT_PLAIN));

        InputStream content = response.getContent();
        StringWriter writer = new StringWriter();
        CharStreams.copy(new InputStreamReader(content), writer);
        assertThat(writer.toString(), containsString("TEXT FILE CONTENT"));

        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testDownloadBinaryFile() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .queryParam("type", "binary")
                .get("/test/download");
        assertThat(response.getContentType(), equalTo(ContentType.APPLICATION_OCTET_STREAM));

        InputStream content = response.getContent();
        assertThat(IO.readBytes(content), equalTo(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));

        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testDelete() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.delete("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(), equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testHead() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.head("/test/");

        assertThat(response.getBody(), nullValue());
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testPut() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse response = tool.request()
                .contentType(ContentType.TEXT_PLAIN)
                .body("Some data")
                .post("/test/");

        assertThat(response.getBody(), containsString("Some data"));
    }

    @Test
    public void testSslGet() throws Exception {
        int port = jetty.restartWithHTTPSSupport(
                KEYSTORE_LOCATION,
                KEYSTORE_PASS,
                TRUSTSTORE_LOCATION,
                TRUSTSTORE_PASS,
                true);

        HttpTool tool = HttpToolBuilder.newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .trustSslCertificates(true)
                .setSslKeyAndCert(
                        "target/itest-classes/clientprivatekey.pem",
                        "target/itest-classes/clientcert.pem")
                .build();
        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(), equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultTimeout() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .timeout(2)
                .build();
        tool.request().get("/test" + BasicHttpServlet.TIMEOUT);
    }

    @Test(expected = IllegalStateException.class)
    public void testTimeoutExpected() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        tool.request()
                .timeout(2)
                .get("/test" + BasicHttpServlet.TIMEOUT);
    }

    @Test
    public void testTimeoutUnexpected() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost()).build();
        HttpResponse httpResponse = tool.request()
                .timeout(5)
                .get("/test" + BasicHttpServlet.TIMEOUT);

        assertThat(httpResponse.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testNoHttpsPortDefined() throws Exception {
        HttpTool tool = HttpToolBuilder.newBuilder(getDefaultHost())
                .followRedirect(false)
                .useHttpsIfProvided(true)
                .trustSslCertificates(true)
                .setSslKeyAndCert(
                        "target/itest-classes/clientprivatekey.pem",
                        "target/itest-classes/clientcert.pem")
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.OVERRIDE_HTTPS);

        int portUsed = Integer.parseInt(response.getCookies().get("port"));
        assertThat(portUsed, not(equalTo(HTTPS_PORT)));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
        verify(httpToolListener).onRequest(Mockito.any(RequestEvent.class));
    }

    @Test
    public void testCustomDnsResolver() throws Exception {
        final String CUSTOM = "custom.local";

        HttpEndpoint host = mock(HttpEndpoint.class);
        when(host.getHostname()).thenReturn(CUSTOM);
        when(host.getHttpPort()).thenReturn(jetty.getPort());


        HttpTool tool = HttpToolBuilder.newBuilder(host)
                .setDnsResolver(new DnsResolver() {
                    @Override
                    public InetAddress[] resolve(String host) throws UnknownHostException {
                        if (CUSTOM.equals(host)) {
                            return new InetAddress[]{InetAddress.getByName("127.0.0.1")};
                        }

                        return new InetAddress[0];
                    }
                })
                .useHostnameIfProvided(true)
                .build();
        HttpResponse response = tool.get("/test" + BasicHttpServlet.HEADERS);

        assertThat(response.getBody(), containsString("\"Host\":\"custom.local:"));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    private HttpEndpoint getDefaultHost() {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getHostname()).thenReturn("localhost");
        when(mock.getIp()).thenReturn("127.0.0.1");
        when(mock.getIpv6()).thenReturn("::1");
        when(mock.getHttpPort()).thenReturn(jetty.getPort());
        when(mock.getHttpsPort()).thenReturn(null);
        return mock;
    }

    private HttpEndpoint getHTTPSHost(int port) {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn("127.0.0.1");
        when(mock.getHttpsPort()).thenReturn(port);
        return mock;
    }

    @SuppressWarnings("unchecked")
    private <T, D> Map<T, D> parseJson(HttpResponse response) {
        return (Map<T, D>) JSON.parse(response.getBody());
    }

    private class HttpTestResponse implements HttpResponse {

        private byte[] content;
        private String contentType;
        private HttpStatus httpStatus;

        public HttpTestResponse(byte[] content, String contentType, HttpStatus httpStatus) {
            this.content = content;
            this.contentType = contentType;
            this.httpStatus = httpStatus;
            Charset defaultCharset = Charset.forName("utf-8");
        }

        @Override
        public Map<String, String> getHeaders() {
            return null;
        }

        @Override
        public HttpStatus getResponseCode() {
            return httpStatus;
        }

        @Override
        public long getResponseTimeToEntityMillis() {
            return 0;
        }

        @Override
        public long getResponseTimeMillis() {
            return 0;
        }

        @Override
        public String getBody() {
            return new String(content, Charset.forName("utf-8"));
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public Map<String, String> getCookies() {
            return null;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getStatusLine() {
            return null;
        }

        @Override
        public InputStream getContent() {
            return new ByteArrayInputStream(content);
        }
    }
}
