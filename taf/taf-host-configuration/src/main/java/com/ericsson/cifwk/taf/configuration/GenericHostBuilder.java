package com.ericsson.cifwk.taf.configuration;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.impl.EnmNetworkHostImpl;
import com.ericsson.cifwk.taf.data.impl.NetworkInterfaceImpl;
import com.ericsson.cifwk.taf.data.network.EnmNetworkHost;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

/**
 * Generic bean that represents properties for one host.
 * Could be built to {@link Host} or {@link EnmNetworkHost}
 */
@API(Internal)
class GenericHostBuilder implements HostFilter {
    private Host host;
    private Map<String, String> ports = new HashMap<>();
    private String ip;
    private GenericHostBuilder ilo;
    private String ipv6;
    private String hostName;
    private String type = "unknown";
    private List<User> users = new ArrayList<>();
    private String group;
    private String offset;
    private String unit;
    private String originalIp;
    private Map<String, String> originalPort = Maps.newHashMap();
    private String tunnelPortOffset = null;
    List<GenericHostBuilder> nodes = new ArrayList<>();
    GenericHostBuilder parent;

    private GenericHostBuilder() {
    }

    static GenericHostBuilder build() {
        return new GenericHostBuilder();
    }


    GenericHostBuilder withIp(String ip) {
        this.ip = ip;
        return this;
    }

    GenericHostBuilder withIpv6(String ipv6) {
        this.ipv6 = ipv6;
        return this;
    }

    GenericHostBuilder withName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    GenericHostBuilder withPort(String type, int port) {
        ports.put(type, Integer.toString(port));
        return this;
    }

    GenericHostBuilder withType(String type) {
        this.type = type;
        return this;
    }

    GenericHostBuilder withNode(GenericHostBuilder node) {
        node.withParent(this);
        nodes.add(node);
        return this;
    }

    GenericHostBuilder withNodes(List<GenericHostBuilder> nodes) {
        for (GenericHostBuilder node : nodes) {
            withNode(node);
        }
        return this;
    }

    GenericHostBuilder withIlo(GenericHostBuilder ilo) {
        this.ilo = ilo;
        return this;
    }

    GenericHostBuilder withUser(User user) {
        users.add(user);
        return this;
    }

    GenericHostBuilder withGroup(String group) {
        this.group = group;
        return this;
    }

    GenericHostBuilder withOffset(String offset) {
        this.offset = offset;
        return this;
    }

    GenericHostBuilder withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    GenericHostBuilder withParent(GenericHostBuilder parent) {
        this.parent = parent;
        return this;
    }

    GenericHostBuilder withOriginalIp(String originalIp) {
        this.originalIp = originalIp;
        return this;
    }

    GenericHostBuilder withOriginalPort(Map<String, String> originalPort) {
        this.originalPort = originalPort;
        return this;
    }

    GenericHostBuilder withTunnelPortOffset(String tunnelPortOffset) {
        this.tunnelPortOffset = tunnelPortOffset;
        return this;
    }

    synchronized Host buildHost() {
        if (host != null) {
            return host;
        }

        host = new Host();
        host.setHostname(hostName);
        host.setIp(ip);
        if (ilo != null) {
            host.setIlo(ilo.buildHost());
        }
        host.setIpv6(ipv6);
        host.setPort(getPorts(ports));
        host.setType(HostType.getByName(type));
        host.setNodes(buildNodesAsHosts());
        host.setUsers(users);
        host.setGroup(group);
        host.setUnit(unit);
        host.setOffset(offset);
        if (parent != null) {
            host.setParentName(parent.getHostName());
        }
        host.setOriginalIp(originalIp);
        host.setOriginalPort(getPorts(originalPort));
        if (tunnelPortOffset != null) {
            host.setTunnelPortOffset(tunnelPortOffset);
        }

        return host;
    }

    EnmNetworkHost buildEnmHost() {
        //TODO currently Host builder only support single public interface
        String interfaceType = "ilo".equalsIgnoreCase(type) ? "ilo" : "public";
        NetworkInterfaceImpl networkInterface = new NetworkInterfaceImpl()
                .setHostname(hostName)
                .setIpv4(ip)
                .setIpv6(ipv6)
                .setType(interfaceType);

        for (Map.Entry<String, String> port : ports.entrySet()) {
            int portValue = Integer.valueOf(port.getValue());
            networkInterface.addPort(port.getKey(), portValue);
        }

        EnmNetworkHostImpl host = new EnmNetworkHostImpl()
                .addInterface(networkInterface)
                .setType(type)
                .setUnit(unit)
                .setGroup(group)
                .addNodes(buildNodesAsEnmNetworkHosts())
                .addUsers(users);

        if (ilo!= null) {
            host.setIlo(ilo.buildEnmHost());
        }

        return host;
    }

    private Map<Ports, String> getPorts(Map<String, String> ports) {
        Map<Ports, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : ports.entrySet()) {
            Ports type = Ports.getByName(entry.getKey());
            result.put(type, entry.getValue());
        }

        return result;
    }

    private List<Host> buildNodesAsHosts() {
        return FluentIterable.from(nodes).transform(toHost()).toList();
    }

    private List<EnmNetworkHost> buildNodesAsEnmNetworkHosts() {
        return FluentIterable.from(nodes).transform(toEnmHost()).toList();
    }

    static Function<GenericHostBuilder, Host> toHost() {
        return new Function<GenericHostBuilder, Host>() {
            @Override
            public Host apply(GenericHostBuilder input) {
                return input.buildHost();
            }
        };
    }

    static Function<GenericHostBuilder, EnmNetworkHost> toEnmHost() {
        return new Function<GenericHostBuilder, EnmNetworkHost>() {
            @Override
            public EnmNetworkHost apply(GenericHostBuilder input) {
                return input.buildEnmHost();
            }
        };
    }

    @Override
    public String toString() {
        return "GenericHostBuilder{" +
                "host=" + host +
                ", ports=" + ports +
                ", ip='" + ip + '\'' +
                ", ilo=" + ilo +
                ", ipv6='" + ipv6 + '\'' +
                ", hostName='" + hostName + '\'' +
                ", type='" + type + '\'' +
                ", nodes=" + nodes +
                ", users=" + users +
                ", group='" + group + '\'' +
                ", offset='" + offset + '\'' +
                ", unit='" + unit + '\'' +
                ", parentName='" + parent.getHostName() + '\'' +
                ", originalIp='" + originalIp + '\'' +
                ", originalPort=" + originalPort +
                ", tunnelPortOffset='" + tunnelPortOffset + '\'' +
                '}';
    }

    public Map<String, String> getPorts() {
        return ports;
    }

    public String getIp() {
        return ip;
    }

    public HostFilter getIlo() {
        return ilo;
    }

    public String getIpv6() {
        return ipv6;
    }

    public String getHostName() {
        return hostName;
    }

    public String getType() {
        return type;
    }

    public List<? extends HostFilter> getNodes() {
        return nodes;
    }

    public List<User> getUsers() {
        return users;
    }

    public String getGroup() {
        return group;
    }

    public String getUnit() {
        return unit;
    }

    public String getParentName() {
        return parent.getHostName();
    }

    boolean isEqualTo(Host host) {
        return Objects.equals(ip, host.getIp()) &&
                Objects.equals(ipv6, host.getIpv6()) &&
                Objects.equals(hostName, host.getHostname()) &&
                Objects.equals(HostType.getByName(type), host.getType()) &&
                Objects.equals(group, host.getGroup()) &&
                Objects.equals(unit, host.getUnit());
    }

    boolean isEqualTo(EnmNetworkHost host) {
        return host.getNetworkInterfaces().size() == 1 &&
                Objects.equals(ip, host.getNetworkInterfaces().get(0).getIpv4()) &&
                Objects.equals(ipv6, host.getNetworkInterfaces().get(0).getIpv6()) &&
                Objects.equals(hostName, host.getNetworkInterfaces().get(0).getHostname()) &&
                Objects.equals(type, host.getType()) &&
                Objects.equals(group, host.getGroup()) &&
                Objects.equals(unit, host.getUnit());
    }

}
