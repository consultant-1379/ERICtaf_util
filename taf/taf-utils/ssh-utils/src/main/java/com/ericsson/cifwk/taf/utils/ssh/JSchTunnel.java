package com.ericsson.cifwk.taf.utils.ssh;

import com.ericsson.cifwk.meta.API;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Created by ekonsla on 30/06/2016.
 */
@API(Internal)
public class JSchTunnel {

    private static final Logger log = LoggerFactory.getLogger(JSchTunnel.class);

    private String host;
    private String port;
    private String user;
    private String pass;
    private static Map<String, Session> currentSessions = new HashMap<>();

    public JSchTunnel(String sshHostname, String sshPort, String sshUser, String sshPass) {
        host = sshHostname;
        port = sshPort;
        user = sshUser;
        pass = sshPass;
    }

    /**
     * Method to start tunnel
     */
    public void startTunnel(String localPort, String remotePort, String remoteIp) throws JSchException {
        Session session = getForwardingSession();
        session.setPortForwardingL(Integer.parseInt(localPort), remoteIp, Integer.parseInt(remotePort));
    }

    /**
     * Method to stop tunnel
     */
    public void stopTunnel(String localPort) throws JSchException {
        Session session = getForwardingSession();
        session.delPortForwardingL(Integer.parseInt(localPort));
    }

    /**
     * Method to get ports forwarded
     */
    public Collection<PortForwarding> getLocalForwardings() {
        return getLocalForwardingsForSession(getForwardingSession());
    }

    private Session getForwardingSession() {
        return findSession(user, host, port, pass);
    }

    private static Collection<PortForwarding> getLocalForwardingsForSession(Session session) {
        Collection<PortForwarding> forwardings = new ArrayList<>();
        try {
            String[] forwardingStrings = session.getPortForwardingL();
            for (String forwardingString : forwardingStrings) {
                forwardings.add(new PortForwarding(forwardingString));
            }
        } catch (JSchException e) {
            log.error("Unable to get local forwardings due to exception: ", e);
        }
        return forwardings;
    }

    /**
     * Method to disconnect SSH connection
     */
    public synchronized void disconnect(String hostName, String user, String port, String pass) {
        log.debug("Disconnecting {}", hostName);
        try {
            closeAndRemoveConnections(hostName, user, port, pass);
        } catch (Throwable e) {
            log.debug("Disconnecting failed due to error: ", e);
            log.trace("Error content", e);
        }
    }

    /**
     * Method to disconnect the connection
     */
    public void disconnect() {
        disconnect(host, user, port, pass);
    }

    public static void stopAllTunnels(Session session) {
        Collection<PortForwarding> forwardings = getLocalForwardingsForSession(session);
        for (PortForwarding forwarding : forwardings) {
            try {
                session.delPortForwardingL(Integer.parseInt(forwarding.getLport()));
            } catch (JSchException e) {
                log.error("Failed to delete forwarding due to exception:", e);
            }
        }
    }

    public static void disconnectAllSessions() {
        for (Session session : currentSessions.values()) {
            stopAllTunnels(session);
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        currentSessions.clear();
    }

    private static String getConnectionKey(String ip, String user, String port, String pass) {
        return ip + "." + user + "." + port + "." + pass;
    }

    private synchronized static Session findSession(String ip, String user, String port, String pass) {
        Session foundSession = searchOrCreateConnection(ip, user, port, pass);
        openSshConnection(foundSession);
        return foundSession;
    }

    private static Session searchOrCreateConnection(String user, String host, String port, String pass) {
        Session foundSession = currentSessions.get(getConnectionKey(host, user, port, pass));
        if (foundSession == null) {
            try {
                Session session = createSession(user, host, port, pass);
                currentSessions.put(getConnectionKey(host, user, port, pass), session);
                return session;
            } catch (JSchException e) {
                log.error("Failed to create session due to exception:", e);
            }
        } else {
            return foundSession;
        }
        return null;
    }

    private static boolean openSshConnection(Session session) {
        if (!session.isConnected()) {
            try {
                session.connect();
            } catch (JSchException e) {
                log.error("Failed to open SSH connection due to exception: ", e);
            }
        }
        return session.isConnected();
    }

    private static Session createSession(String user, String host, String port, String pass) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, Integer.parseInt(port));
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(pass);
        return session;
    }

    private synchronized static void closeAndRemoveConnections(String hostName, String user, String port, String pass) {
        Session session = currentSessions.get(getConnectionKey(hostName, user, port, pass));
        if (session.isConnected()) {
            session.disconnect();
        }
        currentSessions.remove(getConnectionKey(hostName, user, port, pass));
    }
}
