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
public class JSchChmodCommand extends JSchRemoteObjectCommand<Void> {

    private final int mod;
    private final String filePath;

    public JSchChmodCommand(String filePath, int mod, Host host, User user) {
        super(host, user);
        this.mod = mod;
        this.filePath = filePath;
    }

    public JSchChmodCommand(final String filePath, final int mod, final Host host, final User user, final String pathToPrivateKeyFile) {
        super(host, user, pathToPrivateKeyFile);
        this.mod = mod;
        this.filePath = filePath;
    }

    @Override
    public Void run(ChannelSftp sftpChannel) throws SftpException {
        sftpChannel.chmod(Integer.parseInt(String.valueOf(mod), 8), filePath);
        return null;
    }
}
