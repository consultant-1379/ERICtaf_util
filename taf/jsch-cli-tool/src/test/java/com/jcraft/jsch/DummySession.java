package com.jcraft.jsch;

public class DummySession extends Session {

    public boolean isConnected = false;

    public DummySession(JSch jsch, String username, String host, int port) throws JSchException {
        super(jsch, username, host, port);
    }

    @Override
    public void connect(int timeout) {
        isConnected = true;
    }

    @Override
    public void disconnect() {
        isConnected = false;
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }
}
