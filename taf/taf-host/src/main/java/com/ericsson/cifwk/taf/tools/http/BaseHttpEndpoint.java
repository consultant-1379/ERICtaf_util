package com.ericsson.cifwk.taf.tools.http;

import com.ericsson.cifwk.meta.API;

import java.util.Objects;

/**
 * Default HttpHost Object
 */
@API(API.Quality.Internal)
class BaseHttpEndpoint implements HttpEndpoint {

    private String hostName;
    private String ip;
    private String ipv6;
    private Integer httpPort;
    private Integer httpsPort;

    public BaseHttpEndpoint(String hostName, String ip, String ipv6, Integer httpPort, Integer httpsPort) {
        this.hostName = hostName;
        this.ip = ip;
        this.ipv6 = ipv6;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
    }

    @Override
    public String getHostname() {
        return hostName;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getIpv6() {
        return ipv6;
    }

    @Override
    public Integer getHttpPort() {
        return httpPort;
    }

    @Override
    public Integer getHttpsPort() {
        return httpsPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseHttpEndpoint that = (BaseHttpEndpoint) o;
        return Objects.equals(hostName, that.hostName) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(ipv6, that.ipv6) &&
                Objects.equals(httpPort, that.httpPort) &&
                Objects.equals(httpsPort, that.httpsPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, ip, ipv6, httpPort, httpsPort);
    }
}
