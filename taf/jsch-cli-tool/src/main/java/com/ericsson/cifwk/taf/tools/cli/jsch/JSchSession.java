package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.tools.TargetHost;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * This class wrap {@link com.jcraft.jsch.Session} in order to be able to keep {@link com.jcraft.jsch.Session} in pool
 */
@API(API.Quality.Internal)
public class JSchSession {

    public static final String JSCH_SESSION_CONNECT_TIMEOUT_PROPERTY = "jsch.session.connect.timeout";
    private static final Logger LOG = LoggerFactory.getLogger(JSchSession.class);

    private static final String LOCALHOST = "127.0.0.1";
    private final int DEFAULT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(TafConfigurationProvider.provide().getInt(
            JSCH_SESSION_CONNECT_TIMEOUT_PROPERTY,
            JSchCLITool
            .DEFAULT_TIMEOUT_SEC));

    String id;
    Session session;
    String host;
    int port;
    String user;

    public JSchSession(String id) {
        this.id = id;
    }


    public void open(String host, int port, String user, String password) {
        open(host, port, user, password, null);

    }

    public void open(String host, int port, String user, String password, Path pathToPrivateKeyFile) {
        String privateKeyFile = pathToPrivateKeyFile != null ? pathToPrivateKeyFile.toString() : null;
        open(host, port, user, password, privateKeyFile, null);
    }

    public void open(String host, int port, String user, String password, String privateKeyFile, TargetHost tunnelHost) {
        String portAsString = String.valueOf(port);

        try {
            JSchException connectException = null;
            int timeout = JSchCLITool.DEFAULT_TIMEOUT_SEC;

            for (int i = 1; i <= JSchCLITool.CONNECTION_RETRY_THEN_ERROR; i++) {
                try {
                    session = createSession(host, port, user, password, privateKeyFile, tunnelHost);
                    timeout = timeout * i;
                    if (timeout > JSchCLITool.MAX_RETRY_TIMEOUT_SEC) timeout = JSchCLITool.MAX_RETRY_TIMEOUT_SEC;
                    if (session.isConnected()) break;
                } catch (JSchException e) {
                    if (!e.getMessage().contains("timeout") && !e.getMessage().contains("timed out")) throw e;
                    String msg = MessageFormat.format("Retry the opening session ssh://{2}@{0}:{1}, reason: throws {3}({4})", host, portAsString, user, e.getClass().getName(), e.getMessage());
                    LOG.error(msg);
                    connectException = e;
                }
            }
            if (session == null || !session.isConnected() ) {
                if (connectException != null) throw connectException;
                String msg = MessageFormat.format("Can''t open session ssh://{2}@{0}:{1}", host, portAsString, user);
                throw new JSchCLIToolException(msg);
            }
        } catch (JSchException e) {
            String msg = MessageFormat.format("Can''t open session ssh://{2}@{0}:{1}, throws {3}({4})", host, portAsString, user, e.getClass().getName(), e.getMessage());
            throw new JSchCLIToolException(msg, e);
        }
    }

    private Session createSession(String host, int port, String user, String password, String privateKeyFilePath, TargetHost tunnelHost) throws JSchException {
        this.host = host;
        this.port = port;
        this.user = user;

        JSch jsch = new JSch();
        Session session = getConfiguredSession(host, port, user, password, jsch, privateKeyFilePath);
        session.connect(DEFAULT_TIMEOUT);
        if (tunnelHost != null) {
            int allocatedPort = session.setPortForwardingL(0, tunnelHost.getHost(), tunnelHost.getPort());
            session = getConfiguredSession(LOCALHOST, allocatedPort, tunnelHost.getUsername(), tunnelHost.getPassword(), jsch, privateKeyFilePath);
            session.setHostKeyAlias(tunnelHost.getHost());
            session.connect(DEFAULT_TIMEOUT);
        }

        return session;
    }

    private Session getConfiguredSession(String host, int port, String user, String password, JSch jsch, String privateKeyFilePath) throws JSchException {
        final String authPreferences = setIdentityAngGetAuthPreferences(privateKeyFilePath, jsch);
        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", authPreferences);
        session.setPassword(password);
        session.setDaemonThread(true);
        return session;
    }

    private String setIdentityAngGetAuthPreferences(String privateKeyFilePath, JSch jsch) throws JSchException {
        final String authPreferences;
        if (privateKeyFilePath != null) {
            jsch.addIdentity(privateKeyFilePath);
            authPreferences = "publickey,password,keyboard-interactive";
        } else {
            authPreferences = "password,publickey,keyboard-interactive";
        }
        return authPreferences;
    }

    public void close() {
        if (session != null) session.disconnect();
    }

}
