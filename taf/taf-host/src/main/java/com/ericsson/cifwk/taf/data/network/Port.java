package com.ericsson.cifwk.taf.data.network;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

@API(Quality.Experimental)
public class Port {

    private String portType;

    private Integer port;

    public Port() {
    }

    public String getPortType() {
        return portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Port{" +
                "portType=" + portType +
                ", port=" + port +
                '}';
    }
}
