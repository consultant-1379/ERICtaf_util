package com.ericsson.cifwk.taf.data.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.util.Collections;
import java.util.List;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.network.EnmNetworkHost;
import com.ericsson.cifwk.taf.data.network.NetworkHost;
import com.ericsson.cifwk.taf.data.network.NetworkInterface;
import com.google.common.collect.Lists;

@API(Internal)
public class EnmNetworkHostImpl implements EnmNetworkHost {
    private String type;
    private String unit;
    private String group;
    private List<NetworkHost> nodes = Lists.newArrayList();
    private List<NetworkInterface> interfaces = Lists.newArrayList();
    private List<User> users = Lists.newArrayList();
    private NetworkHost ilo;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public List<NetworkHost> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public List<NetworkInterface> getNetworkInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    @Override
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    @Override
    public NetworkHost getIlo() {
        return ilo;
    }

    public EnmNetworkHostImpl setType(String type) {
        this.type = type;
        return this;
    }

    public EnmNetworkHostImpl setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public EnmNetworkHostImpl setGroup(String group) {
        this.group = group;
        return this;
    }

    public EnmNetworkHostImpl addNode(NetworkHost node) {
        this.nodes.add(node);
        return this;
    }

    public EnmNetworkHostImpl addNodes(List<? extends NetworkHost> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public EnmNetworkHostImpl addInterfaces(List<NetworkInterface> interfaces) {
        this.interfaces.addAll(interfaces);
        return this;
    }

    public EnmNetworkHostImpl addUsers(List<User> users) {
        this.users.addAll(users);
        return this;
    }

    public EnmNetworkHostImpl addInterface(NetworkInterface networkInterface) {
        this.interfaces.add(networkInterface);
        return this;
    }

    public EnmNetworkHostImpl addUser(User user) {
        this.users.add(user);
        return this;
    }

    public EnmNetworkHostImpl setIlo(NetworkHost ilo) {
        this.ilo = ilo;
        return this;
    }
}
