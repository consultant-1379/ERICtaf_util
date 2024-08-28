package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.taf.spi.TafPlugin;
import com.ericsson.cifwk.taf.utils.ssh.JSchTunnel;

/**
 * Created by ekonsla on 06/07/2016.
 */
public class JSchTafPlugin implements TafPlugin {

    @Override
    public void init() {

    }

    @Override
    public void shutdown() {
        JSchTunnel.disconnectAllSessions();
    }
}
