package com.ericsson.cifwk.taf.tools.cli;

import java.nio.file.Path;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.de.tools.cli.SshShellBuilder;

/**
 * This class allows to build a {@link TafCliToolShell} instance
 */
@API(API.Quality.Experimental)
public class TafSshShellBuilder {

    private final SshShellBuilder builder;

    /**
     * Create {@link TafSshShellBuilder} with target host using {@link Host} object
     */
    public TafSshShellBuilder(Host host) {
        this.builder = new SshShellBuilder(host.getIp());
        withUsername(host.getUser());
        withPassword(host.getPass());
        withPort(host.getPort(Ports.SSH));
    }

    /**
     * Create {@link TafSshShellBuilder} with target host using host address
     */
    public TafSshShellBuilder(String targetHost) {
        this.builder = new SshShellBuilder(targetHost);
    }

    public TafSshShellBuilder withDefaultTimeout(Long timeout) {
        builder.withDefaultTimeout(timeout);
        return this;
    }

    /**
     * Add user using {@link User} object
     * <p/>
     *
     * @param user
     * @return {@link TafSshShellBuilder}
     */
    public TafSshShellBuilder withUser(User user) {
        builder.withUsername(user.getUsername());
        builder.withPassword(user.getPassword());
        return this;
    }

    /**
     * Add username
     * <p/>
     *
     * @param username
     * @return {@link TafSshShellBuilder}
     */
    public TafSshShellBuilder withUsername(String username) {
        builder.withUsername(username);
        return this;
    }

    /**
     * Add user password
     * <p/>
     *
     * @param password
     * @return {@link TafSshShellBuilder}
     */
    public TafSshShellBuilder withPassword(String password) {
        builder.withPassword(password);
        return this;
    }

    /**
     * Add ssh key file for public key authentication
     * @param keyFile
     * @return {@link TafSshShellBuilder}
     */
    public TafSshShellBuilder withKeyFile(Path keyFile) {
        builder.withKeyFile(keyFile);
        return this;
    }

    /**
     * Add host connection port. If value not set then default port value is {@value com.ericsson.de.tools.cli.AbstractGenericSshCliToolBuilder#DEFAULT_PORT)
     * <p>
     * Here are two possible usage variations:<br/>
     * <ol>
     * <li>Override connection port which is defined in {@link Host targetHost} object if used <code>TafSshShellBuilder({@link Host})<code/> constructor</li>
     * <li>Set connection port if used <code>TafSshShellBuilder({@link String} targetHost)<code/> constructor (Required)</li>
     * </ol>
     * </p>
     *
     * @param port
     * @return {@link TafSshShellBuilder }
     */
    public TafSshShellBuilder withPort(Integer port) {
        builder.withPort(port);
        return this;
    }

    /**
     * Build the specified {@link com.ericsson.de.tools.cli.CliToolShell} instance
     */
    public TafCliToolShell build() {
        return new TafSshCliTool(builder.build());
    }
}
