package com.ericsson.cifwk.taf.tms;

import com.google.common.base.Throwables;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 06.01.2016
 */
public class HttpClientFactory {

    private HttpClientFactory() {
        // hiding constructor
    }

    protected static CloseableHttpClient create() {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        SSLContext trustAll = getTrustAllSslContext();
        clientBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(trustAll, ALLOW_ALL_HOSTNAME_VERIFIER));
        return clientBuilder.build();
    }

    private static SSLContext getTrustAllSslContext() {
        try {
            return SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw Throwables.propagate(e);
        }
    }

    private static class TrustAllStrategy implements TrustStrategy {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    }

}
