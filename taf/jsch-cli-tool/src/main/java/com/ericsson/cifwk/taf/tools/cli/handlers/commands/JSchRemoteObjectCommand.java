package com.ericsson.cifwk.taf.tools.cli.handlers.commands;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.JSchConnectionsHelper;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLIToolException;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Created by ekonsla on 22/01/2016.
 */
@API(Internal)
public abstract class JSchRemoteObjectCommand<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSchRemoteObjectCommand.class);

    private final Host host;

    private String pathToPrivateKeyFile;
    private User user;

    public JSchRemoteObjectCommand(Host host, User user) {
        this.host = host;
        this.user = user;
    }

    public JSchRemoteObjectCommand(Host host, User user, String pathToPrivateKeyFile) {
        this.host = host;
        this.user = user;
        this.pathToPrivateKeyFile = pathToPrivateKeyFile;
    }

    public T execute() {
        final JSch jsch = new JSch();
        Session session = null;
        try {
            session = JSchConnectionsHelper.getSession(host, user, jsch, pathToPrivateKeyFile);
            final ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            return run(sftpChannel);
        } catch (Exception e) {
            LOGGER.error("Unable to connect to host: {}@{}", user, host, e);
            throw new JSchCLIToolException(e);
        } finally {
            if (session != null && session.isConnected())
                session.disconnect();
        }
    }

    protected abstract T run (ChannelSftp sftpChannel) throws SftpException;
}
