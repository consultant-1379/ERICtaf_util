package com.ericsson.cifwk.taf.tools;

import com.ericsson.cifwk.meta.API;

/**
 * Class representing SSH connection properties
 * <br>
 * Properties pathToSshPrivateKeyFile and enforceHostKeyStrictChecking are optional
 * <br/>
 * */
@API(API.Quality.Experimental)
public class TargetHost {

    private final String username;
    private final String password;
    private final String host;
    private final Integer port;
    private final String pathToSshPrivateKeyFile;
    private final boolean enforceStrictHostKeyChecking;

    public TargetHost(String username, String password, String host, Integer port) {
        this(username, password, host, port, null);
    }

    public TargetHost(String username, String password, String host, Integer port, String pathToSshPrivateKeyFile) {
        this(username, password, host, port, pathToSshPrivateKeyFile, false);
    }

    public TargetHost(String username, String password, String host, Integer port, boolean enforceStrictHostKeyChecking) {
        this(username, password, host, port, null, enforceStrictHostKeyChecking);
    }

    public TargetHost(String username, String password, String host, Integer port, String pathToSshPrivateKeyFile, boolean enforceStrictHostKeyChecking) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.pathToSshPrivateKeyFile = pathToSshPrivateKeyFile;
        this.enforceStrictHostKeyChecking = enforceStrictHostKeyChecking;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPathToSshPrivateKeyFile() {
        return pathToSshPrivateKeyFile;
    }

    public boolean isEnforceStrictHostKeyChecking() {
        return enforceStrictHostKeyChecking;
    }

    @Override
    public String toString() {
        return "TargetHost{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", pathToSshPrivateKeyFile='" + pathToSshPrivateKeyFile + '\'' +
                ", enforceStrictHostKeyChecking='" + enforceStrictHostKeyChecking + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TargetHost that = (TargetHost) o;

        if (enforceStrictHostKeyChecking != that.enforceStrictHostKeyChecking) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (port != null ? !port.equals(that.port) : that.port != null) return false;
        return pathToSshPrivateKeyFile != null ? pathToSshPrivateKeyFile.equals(that.pathToSshPrivateKeyFile) : that.pathToSshPrivateKeyFile == null;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (pathToSshPrivateKeyFile != null ? pathToSshPrivateKeyFile.hashCode() : 0);
        return result;
    }
}
