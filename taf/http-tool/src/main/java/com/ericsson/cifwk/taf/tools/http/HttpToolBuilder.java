package com.ericsson.cifwk.taf.tools.http;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.de.tools.http.BasicHttpToolBuilder;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder to configure and create HttpTool object
 */
@API(Stable)
public final class HttpToolBuilder extends BasicHttpToolBuilder<HttpToolBuilder> {

    private boolean useHttpsIfProvided = false;
    private boolean useHostnameIfProvided = false;
    private boolean useIpv6IfProvided = false;

    private HttpEndpoint restHost;

    private static final Logger LOG = LoggerFactory.getLogger(HttpToolBuilder.class);
    private Integer httpsPort;

    HttpToolBuilder(HttpEndpoint restHost) {
        super("");
        this.restHost = restHost;
    }

    HttpToolBuilder(String hostname) {
        super(hostname);
    }

    /**
     * Creates new HttpToolBuilder for HttpTool configuration and creation
     *
     * @param restHost
     *         Host which will be used as base for all HTTP requests of HTTP
     *         tool. Requires HTTP or HTTPS port to be set up
     */
    public static HttpToolBuilder newBuilder(HttpEndpoint restHost) {
        return new HttpToolBuilder(restHost);
    }

    /**
     * Creates new HttpToolBuilder for HttpTool configuration and creation
     *
     * @param restHost
     *         Host which will be used as base for all HTTP requests of HTTP
     *         tool. Requires HTTP or HTTPS port to be set up
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.31)
    @Deprecated// Please use signature with HttpEndpoint
    public static HttpToolBuilder newBuilder(Host restHost) {
        return new HttpToolBuilder(restHost);
    }

    /**
     * Creates new HttpToolBuilder for HttpTool configuration and creation
     *
     * @param hostname
     *         Host which will be used as base for all HTTP requests of HTTP
     *         tool. Requires HTTP or HTTPS port to be set up
     */
    public static HttpToolBuilder newBuilder(String hostname) {
        return new HttpToolBuilder(hostname);
    }

    /**
     * @param port
     *         set HTTP ports to builder.
     * @return Builder to continue configuration
     */
    public HttpToolBuilder withHttpPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @param port
     *         set HTTPS ports to builder.
     * @return Builder to continue configuration
     */
    public HttpToolBuilder withHttpsPort(int port) {
        this.httpsPort = port;
        return this;
    }

    /**
     * @param useHttpsIfProvided
     *         If set to true and Host contains HTTPS port, https connection
     *         will be made
     * @return Builder to continue configuration
     */
    public HttpToolBuilder useHttpsIfProvided(boolean useHttpsIfProvided) {
        checkArgument(!useIpv6IfProvided, "Only one of useHostnameIfProvided or " +
                "useIpv6IfProvided could be used");
        this.useHttpsIfProvided = useHttpsIfProvided;


        return this;
    }

    /**
     * @param useIpv6IfProvided
     *         If set to true and Host contains IPv6 address, IPv6 connection
     *         will be made
     * @return Builder to continue configuration
     */
    public HttpToolBuilder useIpv6IfProvided(boolean useIpv6IfProvided) {
        checkArgument(!useHostnameIfProvided, "Only one of useHostnameIfProvided or " +
                "useIpv6IfProvided could be used");
        this.useIpv6IfProvided = useIpv6IfProvided;

        return this;
    }

    /**
     * @param useHostnameIfProvided
     *         If set to true and Host contains Hostname part, connection by Hostname
     *         will be made
     * @return Builder to continue configuration
     */
    public HttpToolBuilder useHostnameIfProvided(boolean useHostnameIfProvided) {
        this.useHostnameIfProvided = useHostnameIfProvided;

        return this;
    }


    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder timeout(int sec) {
        super.timeout(sec);

        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder trustSslCertificates(boolean trustSslCertificates) {
        super.trustSslCertificates(trustSslCertificates);

        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder doNotVerifyHostname(boolean doNotVerifyHostname) {
        super.doNotVerifyHostname(doNotVerifyHostname);

        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder followRedirect(boolean followRedirect) {
        super.followRedirect(followRedirect);
        return this;
    }

    /**
     * Use SSL key and certificate for authentication
     *
     * @param pathToKey
     *         filesystem path to private key in .pem format
     * @param pathToCert
     *         filesystem path to certificate in .pem format
     * @return Builder to continue configuration
     * @deprecated As of TAF 2.1.1,Replaced by
     * {@link #addCertToKeyStore(String pathToKey, String... pathToCerts)}
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.6)
    @Deprecated
    public HttpToolBuilder setSslKeyAndCert(String pathToKey, String pathToCert) {

        ArrayList<String> keyStoreCerts = new ArrayList<>();
        keyStoreCerts.add(pathToCert);

        sslKeyStore.addKeyStoreCert(pathToKey, keyStoreCerts);

        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder setTrustStore(String pathToTrustStore,
                                         String storePasswd) {

        super.setTrustStore(pathToTrustStore, storePasswd);
        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder addCertToTrustStore(String pathToCertificates) {

        super.addCertToTrustStore(pathToCertificates);
        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder setKeyStore(String pathToKeyStore,
                                       String storePasswd, String keyPasswd) {
        super.setKeyStore(pathToKeyStore, storePasswd, keyPasswd);
        return this;
    }

    /**
     * Sets custom DNS resolver
     * <p>
     * Possibility to override DNS resolution mechanism
     *
     * @see com.ericsson.de.tools.http.DnsResolver#IPV6_ONLY
     */
    public HttpToolBuilder setDnsResolver(DnsResolver dnsResolver) {
        super.setDnsResolver(dnsResolver);
        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpToolBuilder addCertToKeyStore(String pathToKey,
                                             String... pathToCerts) {
        super.addCertToKeyStore(pathToKey, pathToCerts);
        return this;
    }

    @Override
    public HttpTool build() {
        String host = getHost();
        ProtocolDescriptor protocolDescriptor = getProtocolDescriptor();

        this.protocol = protocolDescriptor.protocol;
        this.hostname = host;
        this.port = protocolDescriptor.port;

        return super.build();
    }

    private ProtocolDescriptor getProtocolDescriptor() {
        Integer httpPort;
        Integer httpsPort;
        if (restHost != null) {
            httpPort = restHost.getHttpPort();
            httpsPort = restHost.getHttpsPort();
        } else {
            httpPort = this.port;
            httpsPort = this.httpsPort;
        }
        checkArgument(httpPort != null || httpsPort != null, "No ports are specified in builder");

        if (useHttpsIfProvided && httpsPort != null) {
            return new ProtocolDescriptor("https", httpsPort);
        } else {
            checkArgument(httpPort != null, "No ports are specified in host");
            warnIf(useHttpsIfProvided, "Using HTTP port as no HTTPS port was defined");
            return new ProtocolDescriptor("http", httpPort);
        }
    }

    private String getHost() {
        if (!Strings.isNullOrEmpty(hostname)) {
            return hostname;
        }
        if (useHostnameIfProvided && restHost.getHostname() != null) {
            return restHost.getHostname();
        }
        if (useIpv6IfProvided && restHost.getIpv6() != null) {
            return "[" + restHost.getIpv6() + "]";
        } else {
            warnIf(useHostnameIfProvided, "Using IPv4 as Host does not contain hostname");
            warnIf(useIpv6IfProvided, "Using IPv4 as Host does not contain IPv6 address");
            checkArgument(restHost.getIp() != null, "Host does not contain IPv4 address");
            return restHost.getIp();
        }
    }

    private void warnIf(boolean condition, String message) {
        if (condition) {
            LOG.warn(message);
        }
    }

    private static class ProtocolDescriptor {

        private String protocol;

        private Integer port;

        public ProtocolDescriptor(String protocol, Integer port) {
            this.protocol = protocol;
            this.port = port;
        }

        public String getProtocol() {
            return protocol;
        }

        public Integer getPort() {
            return port;
        }
    }
}
