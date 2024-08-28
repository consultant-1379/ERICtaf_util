package com.ericsson.cifwk.taf.tools.http;

import com.ericsson.cifwk.meta.API;

/**
 * HttpEndpoint interface to be used in HttpTool implementations.
 */
@API(API.Quality.Stable)
public interface HttpEndpoint {

    String HOSTS_PREFIX = "host";
    String HTTP_HOSTS_PREFIX = "http_config.host";
    String PORT = "port";
    String TYPE = "type";
    String IP = "ip";
    String NODE = "node";
    String TUNNEL_STARTED_PROPERTY = "tunneled";
    String ORIGINAL = "original";

    String getHostname();

    String getIp();

    String getIpv6();

    Integer getHttpPort();

    Integer getHttpsPort();

    /**
     * Builds a configured HttpHost Object.
     */
    final class Builder {

        private String hostName;
        private String ip;
        private String ipv6;
        private Integer httpPort;
        private Integer httpsPort;

        /**
         * Define hostname for HttpHost object.
         */
        public Builder withHostname(String hostName) {
            if (hostName == null) {
                throw new NullPointerException("hostname == null");
            }
            this.hostName = hostName;
            return this;
        }

        /**
         * Define ip for HttpHost object.
         */
        public Builder withIp(String ip) {
            if (ip == null) {
                throw new NullPointerException("ip == null");
            }
            this.ip = ip;
            return this;
        }

        /**
         * Define ipv6 for HttpHost object.
         */
        public Builder withIpv6(String ipv6) {
            if (ipv6 == null) {
                throw new NullPointerException("ipv6 == null");
            }
            this.ipv6 = ipv6;
            return this;
        }

        /**
         * Define HTTP port for HttpHost object.
         */
        public Builder withHttpPort(int port) {
            this.httpPort = port;
            return this;
        }

        /**
         * Define HTTPS port for HttpHost object.
         */
        public Builder withHttpsPort(int port) {
            this.httpsPort = port;
            return this;
        }

        /**
         * Assemble HttpHost object.
         *
         * @return HttpHost pojo
         */
        public HttpEndpoint build() {
            return new BaseHttpEndpoint(hostName, ip, ipv6, httpPort, httpsPort);
        }
    }
}
