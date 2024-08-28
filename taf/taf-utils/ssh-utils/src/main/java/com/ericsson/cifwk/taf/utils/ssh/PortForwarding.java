package com.ericsson.cifwk.taf.utils.ssh;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Created by ekonsla on 05/07/2016.
 */
@API(Internal)
public class PortForwarding {

    private String lport;
    private String host;
    private String rport;

    public PortForwarding(String lport, String host, String rport) {
        this.lport = lport;
        this.host = host;
        this.rport = rport;
    }

    public PortForwarding(String[] forwarding) {
        this(forwarding[0], forwarding[1], forwarding[2]);
    }

    /**
     * forwardingString in a format "lport:host:rport"
     */
    public PortForwarding(String forwardingString) {
        this(forwardingString.split(":"));
    }

    public String getLport() {
        return lport;
    }

    public String getHost() {
        return host;
    }

    public String getRport() {
        return rport;
    }
}
