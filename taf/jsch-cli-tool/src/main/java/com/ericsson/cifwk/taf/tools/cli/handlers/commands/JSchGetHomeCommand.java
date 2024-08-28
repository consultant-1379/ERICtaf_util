package com.ericsson.cifwk.taf.tools.cli.handlers.commands;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Created by ekonsla on 22/01/2016.
 */
@API(Internal)
public class JSchGetHomeCommand extends JSchRemoteObjectCommand<String> {

    public JSchGetHomeCommand(Host host, User user) {
        super(host, user);
    }

    @Override
    protected String run(ChannelSftp sftpChannel) throws SftpException {
        return sftpChannel.getHome() + "/";
    }
}
