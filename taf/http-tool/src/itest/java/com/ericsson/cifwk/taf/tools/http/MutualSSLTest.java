package com.ericsson.cifwk.taf.tools.http;

import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.ericsson.cifwk.taf.tools.http.constants.ContentType;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.ericsson.cifwk.taf.tools.http.impl.HttpToolListeners;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * 
 * For CIP-4577:As a developer I would like to be able to perform mutual SSL authentication.
 *  
 * 
 */

public class MutualSSLTest {

    private EmbeddedJetty jetty;
    private final static String CERT_LOCATION = "target/itest-classes/mutualSSL/";
    private final static String KEYSTORE_PASS = "password";
    private final static String KEYSTORE_KEYPASS = "keypass";
    private final static String TRUSTSTORE_PASS = "password";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private HttpToolListener httpToolListener;

    @Before
    public void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(new BasicHttpServlet(), "/test/*")
                .start(50);
        httpToolListener = mock(HttpToolListener.class);
        HttpToolListeners.addListener(httpToolListener);
    }

    @After
    public void tearDown() throws Exception {
        HttpToolListeners.removeAllListeners();
        jetty.stop();
    }

    /*
     * serverkeystoretaf.jks: Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     */
    @Test
    public void testOneWaySSL() throws Exception {   //Todo
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, false);

        HttpTool tool = HttpToolBuilder.newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true).trustSslCertificates(true).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    /*
     * serverkeystoretaf.jks: Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientcert.pem Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     */
    @Test
    public void testSslNormalFlow_trustallCerts() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setSslKeyAndCert(CERT_LOCATION + "clientprivatekey.pem",
                        CERT_LOCATION + "clientcert.pem")
                .trustSslCertificates(true).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    /*
     * serverkeystoretaf.jks: Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientcert.pem Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     * clienttruststoretaf.jks Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     */
    @Test
    public void testSslNormalFlow_validateCertsWithTrustStore()
            throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setSslKeyAndCert(CERT_LOCATION + "clientprivatekey.pem",
                        CERT_LOCATION + "clientcert.pem")
                .setTrustStore(CERT_LOCATION + "clienttruststoretaf.jks",
                        TRUSTSTORE_PASS).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    /*
     * serverkeystoretaf.jks: Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientcert.pem Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     * tafserver.crt Issuer: CN=127.0.0.1 Subject: CN=127.0.0.1
     */
    @Test
    public void testSslNormalFlow_validateCertsWithTrustStoreCerts()
            throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setSslKeyAndCert(CERT_LOCATION + "clientprivatekey.pem",
                        CERT_LOCATION + "clientcert.pem")
                .addCertToTrustStore(CERT_LOCATION + "tafserver.crt")
                .addCertToTrustStore(CERT_LOCATION + "RootCA.crt").build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    /*
     * serverkeystoretaf.jks: Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientkeystore.jks Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     * clienttruststoretaf.jks Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     */
    @Test
    public void testSslNormalFlow_withKeyStore() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setKeyStore(CERT_LOCATION + "clientkeystore.jks",
                        KEYSTORE_PASS, KEYSTORE_KEYPASS)
                .setTrustStore(CERT_LOCATION + "clienttruststoretaf.jks",
                        TRUSTSTORE_PASS).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testIfTrustOptionNotSpecified() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, true);

        try {
            HttpTool tool = HttpToolBuilder
                    .newBuilder(getHTTPSHost(port))
                    .useHttpsIfProvided(true)
                    .setSslKeyAndCert(CERT_LOCATION + "clientprivatekey.pem",
                            CERT_LOCATION + "clientcert.pem").build();

            HttpResponse response = tool.get("/test/");
            fail();
        } catch (IllegalStateException e) {

            assertThat(
                    e.getMessage(),
                    containsString("trustStore or certificates needs to be provided or set the trustSslCertificates flag"));
        }
    }

    /*
     * keystore.jks: Owner: CN=localhost Issuer: CN=localhost
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientcert.pem Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     * clienttruststore.jks Owner: CN=localhost Issuer: CN=localhost Hostname
     * that is been connected: 127.0.0.1
     */
    @Test
    public void testhostnameverifier() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION + "keystore.jks",
                KEYSTORE_PASS, CERT_LOCATION + "servertruststore.jks",
                TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .addCertToKeyStore(CERT_LOCATION + "clientprivatekey.pem",
                        CERT_LOCATION + "clientcert.pem")
                .setTrustStore(CERT_LOCATION + "clienttruststore.jks",
                        TRUSTSTORE_PASS).build();
        try {
            HttpResponse response = tool.get("/test/");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getCause().toString(),
                    containsString("Host name '127.0.0.1' does not match the certificate subject"));
        }

    }

    /*
     * keystore.jks: Owner: CN=localhost Issuer: CN=localhost
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientcert.pem Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     * clienttruststore.jks Owner: CN=localhost Issuer: CN=localhost Hostname
     * that is been connected: 127.0.0.1
     */
    @Test
    public void testhostnameverifierDisabled() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION + "keystore.jks",
                KEYSTORE_PASS, CERT_LOCATION + "servertruststore.jks",
                TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .addCertToKeyStore(CERT_LOCATION + "clientprivatekey.pem",
                        CERT_LOCATION + "clientcert.pem")
                .setTrustStore(CERT_LOCATION + "clienttruststore.jks",
                        TRUSTSTORE_PASS).doNotVerifyHostname(true).build();

        HttpResponse response = tool.get("/test/");
        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));

    }

    /*
     * Certificate from Server needs to be signed by CN=127.0.0.1,If not fake.
     * In this case,Certificate from Server is signed by TestCA.
     * 
     * serverkeystoreTestCA.jks: Owner: CN=TestCA Issuer: CN=TestCA
     * servertruststore.jks Owner: CN=localhost Issuer: CN=localhost
     * clientkeystore.jks Issuer: CN=localhost Subject: CN=localhost CA:TRUE
     * clienttruststoretaf.jks Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     */
    @Test
    public void testfakeServer() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoreTestCA.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setKeyStore(CERT_LOCATION + "clientkeystore.jks",
                        KEYSTORE_PASS, KEYSTORE_KEYPASS)
                .setTrustStore(CERT_LOCATION + "clienttruststoretaf.jks",
                        TRUSTSTORE_PASS).build();
        try {
            HttpResponse response = tool.get("/test/");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getCause().toString(),
                    containsString("unable to find valid certification path"));
        }
    }

    /*
     * Client needs to be signed by testCA,If not fake. In this
     * case,clientcert.pem is signed by localHost. serverkeystoretaf.jks: Owner:
     * CN=127.0.0.1 Issuer: CN=127.0.0.1 servertruststoreTestCA.jks Owner:
     * CN=TestCA Issuer: CN=TestCA clientkeystore.jks Issuer: CN=localhost
     * Subject: CN=localhost CA:TRUE clienttruststoretaf.jks Owner: CN=127.0.0.1
     * Issuer: CN=127.0.0.1
     */
    @Test
    public void testfakeClient_withKeyStore() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststoreTestCA.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setKeyStore(CERT_LOCATION + "clientkeystore.jks",
                        KEYSTORE_PASS, KEYSTORE_KEYPASS)
                .setTrustStore(CERT_LOCATION + "clienttruststoretaf.jks",
                        TRUSTSTORE_PASS).build();

        try {
            HttpResponse response = tool.get("/test/");
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Connection was aborted"));
        }
    }

    /*
     * serverkeystoretaf.jks: Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     * servertruststore_2alias.jks Alias name:firstclient Owner: CN=firstclient
     * Issuer: CN=firstclient Alias name:secondclient Owner: CN=secondclient
     * Issuer: CN=secondclientclientkeystore_2aliassamepass.jks Alias
     * name:firstclient Owner: CN=firstclient Issuer: CN=firstclient Alias
     * name:secondclient Owner: CN=secondclient Issuer: CN=secondclien
     * clienttruststoretaf.jks Owner: CN=127.0.0.1 Issuer: CN=127.0.0.1
     */
    @Test
    public void testMultipleKeysinKeyStore() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore_2alias.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setKeyStore(
                        CERT_LOCATION + "clientkeystore_2aliassamepass.jks",
                        KEYSTORE_PASS, "firstClient") // If there are 2 alias
                // with different
                // keypass,it may not
                // work,could be a bug
                // with apache
                // SSLContextBuilder.
                .setTrustStore(CERT_LOCATION + "clienttruststoretaf.jks",
                        TRUSTSTORE_PASS).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    /*
     * serverkeystore_ServerSBSCA_SubCA_RootCA.jks contains
     * ServerSBSCA,SubCA,RootCA certificates
     */
    @Test
    public void testCertificateChainByRootCA() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                        + "serverkeystore_ServerSBSCA_SubCA_RootCA.jks", "ServerSBSCA",
                CERT_LOCATION + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setKeyStore(CERT_LOCATION + "clientkeystore.jks",
                        KEYSTORE_PASS, KEYSTORE_KEYPASS)
                .setTrustStore(CERT_LOCATION + "RootCA.jks", TRUSTSTORE_PASS)
                .doNotVerifyHostname(true).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    /*
     * serverkeystore_ServerSBSCA_SubCA_RootCA.jks contains
     * ServerSBSCA,SubCA,RootCA certificates
     */
    @Test
    public void testCertificateChainBySubCA() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                        + "serverkeystore_ServerSBSCA_SubCA_RootCA.jks", "ServerSBSCA",
                CERT_LOCATION + "servertruststore.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .setKeyStore(CERT_LOCATION + "clientkeystore.jks",
                        KEYSTORE_PASS, KEYSTORE_KEYPASS)
                .setTrustStore(CERT_LOCATION + "SubCA.jks", TRUSTSTORE_PASS)
                .doNotVerifyHostname(true).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testCertificateChainwithaddCertstoKeystore() throws Exception {
        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "RootCA.jks", TRUSTSTORE_PASS, true);

        HttpTool tool = HttpToolBuilder
                .newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true)
                .addCertToKeyStore(CERT_LOCATION + "ServerSBSCA.key",
                        CERT_LOCATION + "ServerSBSCA.crt",
                        CERT_LOCATION + "SubCA.crt")
                .setTrustStore(CERT_LOCATION + "clienttruststoretaf.jks",
                        TRUSTSTORE_PASS).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getBody(), containsString("{}"));
        assertThat(response.getContentType(),
                equalTo(ContentType.APPLICATION_JSON));
        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void testSSLPortAlreadyInUseMechanism() throws Exception {
        EmbeddedJetty blockingPortJetty = EmbeddedJetty.build()
                .withServlet(new BasicHttpServlet(), "/test/*")
                .start(50);

        int port = jetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, false);

        blockingPortJetty.restartWithHTTPSSupport(CERT_LOCATION
                + "serverkeystoretaf.jks", KEYSTORE_PASS, CERT_LOCATION
                + "servertruststore.jks", TRUSTSTORE_PASS, false);

        HttpTool tool = HttpToolBuilder.newBuilder(getHTTPSHost(port))
                .useHttpsIfProvided(true).trustSslCertificates(true).build();

        HttpResponse response = tool.get("/test/");

        assertThat(response.getResponseCode(), equalTo(HttpStatus.OK));
    }

    private HttpEndpoint getHTTPSHost(int port) {
        HttpEndpoint mock = mock(HttpEndpoint.class);
        when(mock.getIp()).thenReturn("127.0.0.1");
        when(mock.getHttpsPort()).thenReturn(port);
        return mock;
    }
}
