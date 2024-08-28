package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.TargetHost;
import com.ericsson.de.tools.cli.CliHostHopper;
import com.ericsson.de.tools.cli.CliToolShell;

/**
 * This class allows to establish ssh connection to the specified host
 */
@API(API.Quality.Experimental)
public class TafCliHostHopper extends CliHostHopper {

    TafCliHostHopper(CliToolShell cliTool) {
        super(cliTool);
    }

    /**
     * Allows user to ssh to a {@link Host}
     *
     * @param host {@link Host} information in which connection will be made
     */
    public void hop(Host host) {
        hop(host, host.getDefaultUser(), false, null);
    }

    /**
     * Allows user to ssh to a {@link Host} with specified {@link User}
     *
     * @param host {@link Host} information in which connection will be made
     * @param user {@link User} object which will contain username and password login
     *             credentials
     */
    public void hop(Host host, User user) {
        hop(host, user, false, null);
    }

    /**
     * Allows user to ssh to a {@link Host} with specified {@link User} information
     *
     * @param host                         {@link Host} information in which connection will be made
     * @param user                         {@link User} object which will contain username and password login
     *                                     credentials
     * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
     *                                     Set to true to turn StrictHostChecking on
     */
    public void hop(Host host, User user, boolean enforceStrictHostKeyChecking) {
        hop(host, user, enforceStrictHostKeyChecking, null);
    }

    /**
     * Allows user to ssh to a specified {@link Host}
     *
     * @param host                         {@link Host} information in which connection will be made
     * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
     *                                     Set to true to turn StrictHostChecking on
     */
    public void hop(Host host, boolean enforceStrictHostKeyChecking) {
        hop(host, host.getDefaultUser(), enforceStrictHostKeyChecking, null);
    }

    /**
     * Perform an ssh to a specified {@link Host} with full path to ssh private key specified
     *
     * @param host                    Host information in which connnection will be made using default user
     * @param pathToSshPrivateKeyFile Absolute path to location of private key file on server
     */
    public void hop(Host host, String pathToSshPrivateKeyFile) {
        hop(host, host.getDefaultUser(), false, pathToSshPrivateKeyFile);
    }

    /**
     * Perform an ssh to a specified {@link Host} with full path to ssh private key specified
     *
     * @param host                    {@link Host} information in which connection will be made
     * @param user                    {@link User} object which will contain username login credentials
     * @param pathToSshPrivateKeyFile Absolute path to location of private key file on server
     */
    public void hop(Host host, User user, String pathToSshPrivateKeyFile) {
        hop(host, user, false, pathToSshPrivateKeyFile);
    }

    /**
     * Perform an ssh to a {@link Host} with full path to ssh private key specified
     *
     * @param host                         {@link Host} information in which connection will be made
     * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
     *                                     Set to true to turn StrictHostChecking on
     * @param pathToSshPrivateKeyFile      Absolute path to location of private key file on server
     */
    public void hop(Host host, boolean enforceStrictHostKeyChecking, String pathToSshPrivateKeyFile) {
        hop(host, host.getDefaultUser(), enforceStrictHostKeyChecking, pathToSshPrivateKeyFile);
    }

    /**
     * Perform an ssh to a {@link Host} with full path to ssh private key specified
     *
     * @param host                         {@link Host} information in which connection will be made using default user
     * @param user                         {@link User} object which will contain username login credentials
     * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
     *                                     Set to true to turn StrictHostChecking on
     * @param pathToSshPrivateKeyFile      Absolute path to location of private key file on server
     */
    public void hop(Host host, User user, boolean enforceStrictHostKeyChecking, String pathToSshPrivateKeyFile) {
        TargetHost targetHost = createTargetHost(host, user, enforceStrictHostKeyChecking, pathToSshPrivateKeyFile);
        hop(targetHost);
    }

    private TargetHost createTargetHost(Host host, User user, boolean enforceStrictHostKeyChecking, String pathToSshPrivateKeyFile) {
        return new TargetHost(user.getUsername(), user.getPassword(), host.getIp(), defaultPort(host), pathToSshPrivateKeyFile, enforceStrictHostKeyChecking);
    }

    private int defaultPort(Host host) {
        Integer port = host.getPort(Ports.SSH);
        return port != null ? port : com.ericsson.de.tools.cli.SshShellBuilder.DEFAULT_PORT;
    }
}
