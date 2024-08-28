package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.TargetHost;
import com.ericsson.de.tools.cli.SimpleExecutorBuilder;
import com.ericsson.de.tools.cli.CliTool;

/**
 * This class allows to build a {@link CliTool} instance
 */
@API(API.Quality.Experimental)
public class TafSimpleExecutorBuilder {

    private final SimpleExecutorBuilder builder;

    /**
     * Create {@link SimpleExecutorBuilder} with target host using {@link Host}
     */
    public TafSimpleExecutorBuilder(Host host) {
        this.builder = new SimpleExecutorBuilder(host.getIp());
        withUsername(host.getUser());
        withPassword(host.getPass());
        withPort(host.getPort(Ports.SSH));
    }

    /**
     * Create {@link SimpleExecutorBuilder} with target host using host address
     */
    public TafSimpleExecutorBuilder(String targetHost) {
        this.builder = new SimpleExecutorBuilder(targetHost);
    }

    public TafSimpleExecutorBuilder withDefaultTimeout(Long timeout) {
        builder.withDefaultTimeout(timeout);
        return this;
    }

    /**
     * Add user using {@link User} object
     * <p/>
     *
     * @param user
     * @return {@link TafSimpleExecutorBuilder}
     */
    public TafSimpleExecutorBuilder withUser(User user) {
        builder.withUsername(user.getUsername());
        builder.withPassword(user.getPassword());
        return this;
    }

    /**
     * Establish connection via tunnel using {@link TargetHost} object. Can be used only once otherwise value will be reassigned
     * <p>
     * Example:<br/>
     * <code>
     * {@link TafSimpleExecutorBuilder} builder = {@link com.ericsson.de.tools.cli.CliTools}.simpleExecutor({@link Host});<br/>
     * builder.withTunnelHost({@link TargetHost});<br/>
     * builder.build();
     * </code>
     * <br/
     * Result
     * <p/>
     *
     * @param tunnelNode
     * @return {@link TafSimpleExecutorBuilder}
     */
    public TafSimpleExecutorBuilder withTunnelHost(TargetHost tunnelNode) {
        builder.withTunnelHost(tunnelNode);
        return this;
    }

    /**
     * Add username
     * <p/>
     *
     * @param username
     * @return {@link TafSimpleExecutorBuilder}
     */
    public TafSimpleExecutorBuilder withUsername(String username) {
        builder.withUsername(username);
        return this;
    }

    /**
     * Add user password
     * <p/>
     *
     * @param password
     * @return {@link TafSimpleExecutorBuilder}
     */
    public TafSimpleExecutorBuilder withPassword(String password) {
        builder.withPassword(password);
        return this;
    }

    /**
     * Provide connection to a host with full path to ssh private key file. Must be used when necessary authentication with private key. If this method will be used then the username is unnecessary
     *
     * @param pathToSshPrivateKeyFile full path to ssh private key file
     * @return {@link TafSimpleExecutorBuilder}
     */
    public TafSimpleExecutorBuilder withKeyFile(String pathToSshPrivateKeyFile) {
        builder.withKeyFile(pathToSshPrivateKeyFile);
        return this;
    }

    /**
     * Enforce strict host key checking
     *
     * @param enforceStrictHostKeyChecking default value is false
     * @return {@link TafSimpleExecutorBuilder}
     */
    public TafSimpleExecutorBuilder withStrictHostKeyChecking(boolean enforceStrictHostKeyChecking) {
        builder.withStrictHostKeyChecking(enforceStrictHostKeyChecking);
        return this;
    }

    /**
     * Add host connection port. If value not set then default port value is {{@value com.ericsson.de.tools.cli.AbstractCliToolBuilder#DEFAULT_TIMEOUT})
     * <p>
     * Here are two possible usage variations:<br/>
     * <ol>
     * <li>Override connection port which is defined in {@link Host targetHost} object if used <code>TafSshShellBuilder({@link Host})<code/> constructor</li>
     * <li>Set connection port if used <code>TafSshShellBuilder({@link String} targetHost)<code/> constructor (Required)</li>
     * </ol>
     * </p>
     *
     * @param port
     * @return {@link TafSimpleExecutorBuilder }
     */
    public TafSimpleExecutorBuilder withPort(Integer port) {
        builder.withPort(port);
        return this;
    }

    /**
     * Build the specified {@link CliTool} instance
     */
    public CliTool build() {
        return builder.build();
    }
}
