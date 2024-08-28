package com.ericsson.cifwk.taf.configuration.processor;

import com.ericsson.cifwk.meta.API;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Default HTTP Client Factory for internal usage in TAF Configuration
 * (reading remote configuration over HTTP).
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 08.06.2017
 */
@API(API.Quality.Internal)
public class HttpClientFactory {

    public static final HttpClientFactory HTTP_CLIENT_FACTORY = new HttpClientFactory();

    public CloseableHttpClient create() {
        try {
            return createInternally();
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Could not create HTTP Tool: ", e);
        }
    }

    private CloseableHttpClient createInternally() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        SSLContextBuilder contextBuilder = SSLContexts.custom();
        contextBuilder.loadTrustMaterial(null, new TrustAllStrategy());
        X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                contextBuilder.build(), new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null,
                hostnameVerifier);
        clientBuilder.setSSLSocketFactory(socketFactory);
        return clientBuilder.build();
    }

    private static class TrustAllStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return true;
        }
    }

}
