package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import expectj.AbstractSpawnable;
import expectj.Spawnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Spawnable for controlling an SSH session using ExpectJ.
 */
public class SshSpawn extends AbstractSpawnable implements Spawnable {
    /**
     * Use this to write data to the remote host.
     */
    private OutputStream stdin;

    /**
     * Use this to read data from the remote host STDOUT.
     */
    private InputStream stdout;

    /**
     * Use this to read data from the remote host STDERR.
     */
    private InputStream stderr;

    /**
     * The JSch Channel of type "shell"
     */
    private Channel m_channel = null;

    /**
     * Takes control over an existing SSH channel.
     *
     * @param channel The channel we should control.  If this channel isn't
     *                already connected, {@link Channel#connect()} will be called.
     * @throws IOException If connecting the channel fails.
     */
    public SshSpawn(Channel channel) throws IOException {
        if (!channel.isConnected()) {
            try {
                channel.connect();
            } catch (JSchException e) {
                throw new IOException("Failed connecting the channel", e);
            }
        }

        this.m_channel = channel;
        stdin = m_channel.getOutputStream();
        stdout = m_channel.getInputStream();
        stderr = m_channel.getExtInputStream();
    }

    public void start() throws IOException {
        // We've probably been created by the SshSpawn(Channel) constructor,
        // or start() has already been called.  No need to do anything
        // anyway.
        return;
    }

    public InputStream getStdout() {
        return stdout;
    }

    public OutputStream getStdin() {
        return stdin;
    }

    public InputStream getStderr() {
        return stderr;
    }

    public boolean isClosed() {
        if (m_channel != null) {
            if (m_channel.isClosed()) {
                // We've been disconnected, shut down
                stop();
            }
        }
        return m_channel == null;
    }

    public int getExitValue() {
        return 0;
    }

    public void stop() {
        if (m_channel == null) {
            return;
        }
        m_channel.disconnect();
        m_channel = null;
        stdin = null;
        stdout = null;
        stderr = null;
    }
}
