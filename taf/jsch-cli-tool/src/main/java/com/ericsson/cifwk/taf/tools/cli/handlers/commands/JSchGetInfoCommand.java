package com.ericsson.cifwk.taf.tools.cli.handlers.commands;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.jsch.FileInfo;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Created by ekonsla on 22/01/2016.
 */
@API(Internal)
public class JSchGetInfoCommand extends JSchRemoteObjectCommand<FileInfo> {

    private final String filePath;

    public JSchGetInfoCommand(String filePath, Host host, User user) {
        super(host, user);
        this.filePath = filePath;
    }

    @Override
    public FileInfo run(ChannelSftp sftpChannel) throws SftpException {
        return new FileInfo(sftpChannel.lstat(filePath));
    }
}
