package com.ericsson.cifwk.taf.data;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.exception.UserNotFoundException;
import com.ericsson.cifwk.taf.tools.http.HttpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Class to instantiate the host object
 * The structure of the properties file is detailed in the following webpage: https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/index.html#_host_properties_file_structure_and_example
 */
@API(Stable)
public class Host extends HostTunnelHelper implements HttpEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(Host.class);

    private List<User> users = new CopyOnWriteArrayList<>();
    private HostType type;
    private List<Host> nodes = new ArrayList<>();
    private Host ilo;
    private String offset;
    private String group;
    private String unit;


    /**
     * Returns builder for host object
     */
    public static HostBuilder builder() {
        return new HostBuilder();
    }

    public List<User> getUsers() {
        return users;
    }

    /**
     * Set the list of users for this Host object
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    public HostType getType() {
        return type;
    }

    public void setType(HostType type) {
        this.type = type;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Method to return a list of nodes has been deprecated
     *
     * @deprecated use Datahandler.getHostByName() to get specific node
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public List<Host> getNodes() {
        return nodes;
    }

    public void setNodes(List<Host> nodes) {
        this.nodes = nodes;
    }

    /**
     * Method to return the ilo for Host
     */
    public Host getIlo() {
        return ilo;
    }

    public void setIlo(Host ilo) {
        this.ilo = ilo;
    }

    /**
     * Method to return a boolean to see if a host has nodes or not
     *
     * @return boolean value to represent of nodes are present or not on the host
     */
    public boolean hasNodes() {
        return this.nodes != null && !this.nodes.isEmpty();
    }

    /**
     * Gets username of first user in list
     *
     * @return user name representing first user in list
     */
    public String getUser() {
        return getDefaultUser().getUsername();
    }

    /**
     * Set the default user's username
     */
    public void setUser(String username) {
        getDefaultUser().setUsername(username);
    }

    /**
     * Gets password of first user in list
     *
     * @return password representing first user in list
     */
    public String getPass() {
        return getDefaultUser().getPassword();
    }

    /**
     * Set the default user's password
     */
    public void setPass(String password) {
        getDefaultUser().setPassword(password);
    }

    /**
     * Get the user which has the username passed
     *
     * @throws UserNotFoundException
     *         in case no user is found matching the criteria
     */
    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }

        throw new UserNotFoundException(username + " was not found in the list of users");
    }

    /**
     * Gets default user in list
     *
     * @return user object
     */
    public User getDefaultUser() {
        if (users.isEmpty()) {
            logger.info("Default user for " + getHostname() + " does not exist, creating empty.");
            users.add(new User());
        }

        return users.iterator().next();
    }

    /**
     * Gets the first admin user in the list.
     * If there is no admin users in the list it throws an exception
     *
     * @return user object representing first admin user in list
     */
    public User getFirstAdminUser() {
        for (User user : users) {
            if (user.getType().equals(UserType.ADMIN)) {
                return user;
            }
        }
        throw new UserNotFoundException("No user of type admin was found");
    }

    /**
     * Method to get users of selected type
     */
    public List<User> getUsers(final UserType type) {
        ArrayList<User> users= new ArrayList<>();
        for (User user : this.users) {
            if (user.getType().equals(type)) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Method to get iterator of users of selected type
     */
    public Iterator<User> getUsersIterator(UserType type) {
        return getUsers(type).iterator();
    }

    /**
     * Method to get username of first user specified by type; can return null
     *
     * @return username or null if user is not available
     */
    public String getUser(UserType type) {
        List<User> match = getUsers(type);
        if (match.isEmpty()) {
            return null;
        } else {
            return match.iterator().next().getUsername();
        }
    }

    /**
     * Method to get password of first user specified by type; can return null
     *
     * @return password or null
     */
    public String getPass(UserType type) {
        List<User> match = getUsers(type);
        if (match.isEmpty()) {
            return null;
        } else {
            return match.iterator().next().getPassword();
        }
    }

    /**
     * Add user to host
     */
    public void addUser(String username, String password, UserType type) {
        users.add(new User(username, password, type));
    }

    /**
     * Returns a port number by a port type
     *
     * @param portType
     *         port type
     * @return port number corresponding to the port type <code>port</code>, or <code>null</code> if it's undefined
     */
    public Integer getPort(Ports portType) {
        String portStr = getPort().get(portType);
        if (portStr == null) {
            return null;
        }

        return Integer.parseInt(portStr);
    }

    /**
     * Method to return a string representation of the host
     *
     * @return String representation of the host's name if that name is valid, if not return a string
     * representation of the IP address
     */
    public String toString() {
        final String hostname = getHostname();
        if (hostname != null) {
            return String.valueOf(type) + " " + hostname;
        } else {
            return String.valueOf(type) + " " + getIp();
        }
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || !getClass().equals(that.getClass())) {
            return false;
        }

        Host host = (Host) that;

        return Objects.equals(this.getHostname(), host.getHostname())
                && Objects.equals(this.getIp(), host.getIp())
                && Objects.equals(this.users, host.users)
                && Objects.equals(this.type, host.type)
                && Objects.equals(this.nodes, host.nodes)
                && Objects.equals(this.offset, host.offset)
                && Objects.equals(this.group, host.group)
                && Objects.equals(this.unit, host.unit);
    }

    @Override
    public int hashCode() {
        int result = users != null ? users.hashCode() : 0;
        result = 31 * result + (getHostname() != null ? getHostname().hashCode() : 0);
        result = 31 * result + (getIp() != null ? getIp().hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (nodes != null ? nodes.hashCode() : 0);
        result = 31 * result + (offset != null ? offset.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }

    @Override
    @API(Internal)
    public Integer getHttpPort() {
        return getPort(Ports.HTTP);
    }

    @Override
    @API(Internal)
    public Integer getHttpsPort() {
        return getPort(Ports.HTTPS);
    }

    public static final class HostBuilder {

        private Map<Ports, String> ports = new HashMap<>();
        private String ip;
        private Host ilo;
        private String ipv6;
        private String hostName;
        private HostType type;
        private String namespace;
        private List<Host> nodes = new ArrayList<>();
        private List<User> users = new ArrayList<>();

        /**
         * @deprecated use {@link com.ericsson.cifwk.taf.data.Host#builder()()} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public HostBuilder() {
        }

        public HostBuilder withIp(String ip) {
            this.ip = ip;
            return this;
        }

        public HostBuilder withIpv6(String ipv6) {
            this.ipv6 = ipv6;
            return this;
        }

        public HostBuilder withName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public HostBuilder withPort(Ports type, int port) {
            ports.put(type, Integer.toString(port));
            return this;
        }

        public HostBuilder withHttpPort(int port) {
            return withPort(Ports.HTTP, port);
        }

        public HostBuilder withHttpsPort(int port) {
            return withPort(Ports.HTTPS, port);
        }

        public HostBuilder withSshPort(int port) {
            return withPort(Ports.SSH, port);
        }

        public HostBuilder withJmxPort(int port) {
            return withPort(Ports.JMX, port);
        }

        public HostBuilder withJmsPort(int port) {
            return withPort(Ports.JMS, port);
        }

        public HostBuilder withJbossManagementPort(int port) {
            return withPort(Ports.JBOSS_MANAGEMENT, port);
        }

        public HostBuilder withRmiPort(int port) {
            return withPort(Ports.RMI, port);
        }

        public HostBuilder withAmqpPort(int port) {
            return withPort(Ports.AMQP, port);
        }

        public HostBuilder withUnknownPort(int port) {
            return withPort(Ports.UNKNOWN, port);
        }

        public HostBuilder withHttpPort() {
            return withPort(Ports.HTTP, 80);
        }

        public HostBuilder withHttpsPort() {
            return withPort(Ports.HTTPS, 443);
        }

        public HostBuilder withSshPort() {
            return withPort(Ports.SSH, 22);
        }

        public HostBuilder withJmsPort() {
            return withJmsPort(5540);
        }

        public HostBuilder withJbossManagementPort() {
            return withPort(Ports.JBOSS_MANAGEMENT, 9999);
        }

        public HostBuilder withRmiPort() {
            return withRmiPort(5520);
        }

        public HostBuilder withAmqpPort() {
            return withAmqpPort(5672);
        }

        public HostBuilder withType(HostType type) {
            this.type = type;
            return this;
        }

        public HostBuilder withNode(Host node) {
            nodes.add(node);
            return this;
        }

        public HostBuilder withIlo(Host ilo) {
            this.ilo = ilo;
            return this;
        }

        public HostBuilder withUser(User user) {
            users.add(user);
            return this;
        }

        public Host build() {
            Host host = new Host();
            host.setHostname(hostName);
            host.setIp(ip);
            host.setIlo(ilo);
            host.setIpv6(ipv6);
            host.setPort(ports);
            host.setType(type);
            host.setNodes(nodes);
            host.setUsers(users);
            host.setNamespace(namespace);

            return host;
        }

        /**
         * @deprecated use {@link #withPort(com.ericsson.cifwk.taf.data.Ports, int)} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public void setPorts(Map<Ports, String> ports) {
            this.ports = ports;
        }

        /**
         * @deprecated use {@link #withIp(java.lang.String)()} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public void setIp(String ip) {
            this.ip = ip;
        }

        /**
         * @deprecated use {@link #withName(java.lang.String)()} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        /**
         * @deprecated use {@link #withType(com.ericsson.cifwk.taf.data.HostType)()} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public void setType(HostType type) {
            this.type = type;
        }

        /**
         * @deprecated use {@link #withNode(com.ericsson.cifwk.taf.data.Host)} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public void setNodes(List<Host> nodes) {
            this.nodes = nodes;
        }

        /**
         * @deprecated use {@link #withUser(com.ericsson.cifwk.taf.data.User)} instead.
         */
        @API(API.Quality.Deprecated)
        @API.Since(2.20)
        @Deprecated
        public void setUsers(List<User> users) {
            this.users = users;
        }
    }
}
