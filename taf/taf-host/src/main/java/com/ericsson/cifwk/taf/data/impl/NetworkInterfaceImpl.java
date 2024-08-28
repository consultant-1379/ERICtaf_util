package com.ericsson.cifwk.taf.data.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.util.Collections;
import java.util.List;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.network.NetworkInterface;
import com.ericsson.cifwk.taf.data.network.Port;
import com.google.common.collect.Lists;

@API(Internal)
public class NetworkInterfaceImpl implements NetworkInterface {
    private String type;
    private String hostname;
    private List<Port> ports = Lists.newArrayList();
    private String ipv4;
    private String ipv6;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public List<Port> getPorts() {
        return Collections.unmodifiableList(ports);
    }

    @Override
    public String getIpv4() {
        return ipv4;
    }

    @Override
    public String getIpv6() {
        return ipv6;
    }

    public NetworkInterfaceImpl setType(String type) {
        this.type = type;
        return this;
    }

    public NetworkInterfaceImpl setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public NetworkInterfaceImpl addPort(String portType, Integer port) {
        Port portImpl = new Port();
        portImpl.setPort(port);
        portImpl.setPortType(portType);
        this.ports.add(portImpl);
        return this;
    }

    public NetworkInterfaceImpl addPorts(List<Port> ports) {
        this.ports.addAll(ports);
        return this;
    }

    public NetworkInterfaceImpl setIpv4(String ipv4) {
        this.ipv4 = ipv4;
        return this;
    }

    public NetworkInterfaceImpl setIpv6(String ipv6) {
        this.ipv6 = ipv6;
        return this;
    }
}
