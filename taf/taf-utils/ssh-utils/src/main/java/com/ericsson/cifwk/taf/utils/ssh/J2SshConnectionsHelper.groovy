package com.ericsson.cifwk.taf.utils.ssh

import com.ericsson.cifwk.meta.API
import com.maverick.ssh2.Ssh2Context
import com.sshtools.net.SocketTransport
import com.maverick.ssh.SshClient
import com.maverick.ssh.SshConnector
import com.maverick.ssh2.Ssh2Client
import com.maverick.ssh2.Ssh2PasswordAuthentication
import com.maverick.ssh2.Ssh2Session
import groovy.transform.PackageScope
import groovy.transform.WithWriteLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Helper class to put connection mechanism delivered by J2SSH library separate to command handling
 *
 */
@API(value = API.Quality.Internal)
@PackageScope
class J2SshConnectionsHelper {

    public static final String CONNECTION_FAILURE = "Connection failed"
    public static final String AUTHENTICATION_FAILURE = "Authentication failed"
    static int socketTimeout = 5000

    /**
     * Map allowing to reuse connections to server with structure Host -> User -> Port -> Password
     */
    private static Map<String,List<SshClient>> currentConnections = [:]
    private static Logger logger = LoggerFactory.getLogger(J2SshConnectionsHelper)

    private static String getConnectionKey(String ip, String user, String port, String pass){
        return "$ip:$user:$port:$pass"
    }
    /**
     * Method to update current connections pool with new one
     * @param ip
     * @param user
     * @param port
     * @param pass
     * @param connection
     */
    private static void updateCurrentConnections(String ip, String user, String port, String pass, SshClient connection){
        String key = getConnectionKey(ip, user, port, pass)
        List openConnections =	currentConnections[key]
        if (openConnections == null){
            openConnections = []
        }
        openConnections << connection
        currentConnections[key] = openConnections
        if (logger.debugEnabled)logger.debug "Connections updated: $currentConnections"
    }

    /**
     * Method to search trough map of current connections and return one for re-use or create new one
     * @param ip
     * @param user
     * @param port
     * @param pass
     * @return
     */
    private static Ssh2Client searchCurrentConnections(String ip, String user, String port, String pass){
        if (logger.traceEnabled)logger.trace "Looking for connection $ip:$user:$port:$pass"
        if (logger.traceEnabled) logger.trace "All available connections: $currentConnections"
        List<SshClient> foundConnections = currentConnections[getConnectionKey(ip, user, port, pass)]
        if (foundConnections == null) {
            logger.debug "Creating new connections to $ip on port $port for user $user with pass $pass"
            SshClient connection = new Ssh2Client()
            updateCurrentConnections(ip, user, port, pass, connection)
            return connection
        } else{
            if (logger.traceEnabled)logger.trace "Re-using connection $foundConnections"
            return foundConnections.last()
        }
    }
    /**
     * Method to find connection if already created or create new one. Method is Thread-safe
     * @param ip
     * @param user
     * @param port
     * @param pass
     * @return
     */
    @WithWriteLock
    protected static SshClient findConnection(String ip, String user, String port, String pass){
        SshClient foundConnection = searchCurrentConnections(ip, user, port, pass)
        logger.trace "Got connection $foundConnection with status connection ${foundConnection.connected} and authenticated ${foundConnection.authenticated}"
        openSshConnection(foundConnection, ip, port)
        authenticate(foundConnection, user, pass)
        return foundConnection
    }

    /**
     * Method to get connection to server
     * Connection is either re-used from pool of previously made connections or created
     * @param ip
     * @param user
     * @param port
     * @param pass
     * @return
     */
    public SshClient getConnection(String ip, String user, String port, String pass){
        SshClient foundConnection
        foundConnection = findConnection(ip,user,port,pass)
        if (logger.traceEnabled) logger.trace "Current connection: $foundConnection"
        return foundConnection
    }

    protected SshClient getNewConnection(String ip, String user, String port, String pass){
        if (logger.debugEnabled) logger.debug "Opening new connection to server $ip"
        updateCurrentConnections(ip, user, port, pass, new Ssh2Client())
        return getConnection(ip, user, port, pass)
    }

    /**
     * Method to carry out the authenticate the user on the host
     *
     * @param user
     *            - username to log on to the host
     * @param pass
     *            - user password to log on to the hist
     * @return True if the user was authenticated sucessfulle
     */
    protected static boolean authenticate(SshClient ssh, String user, String pass){
        if (! ssh.authenticated){
            try {
                Ssh2PasswordAuthentication pac = new Ssh2PasswordAuthentication()
                pac.username = user
                pac.password = pass
                int result = ssh.authenticate(pac)
                SshAuthenticationState currentState = SshAuthenticationState.find {it.toInteger() == result }
                logger.debug "Authentication finished with status $currentState"
            } catch (Exception e){
                logger.error "Authentication error",e
            }
        }
        return ssh.authenticated
    }

    /**
     * Method to open an SSH connection to the remote host
     *
     * @param host
     *            - String representation of the host (IP address)
     * @return True if the connection to the host was established
     * @throws NullPointerException  if IP or PORT are empty
     */
    protected static boolean openSshConnection(SshClient ssh, String ip, String port) throws NullPointerException{
        if (! (ip && port)) {
            throw new IllegalArgumentException("Cannot get SSH connection without IP or port to connect")
        }
        if (! ssh.connected){
            logger.debug "Connecting to $ip on port $port"
            try {
                SshConnector con = SshConnector.createInstance()
                Ssh2Context context = (Ssh2Context) con.getContext(SshConnector.SSH2)
                context.setSocketTimeout(socketTimeout)
                SocketTransport t = new SocketTransport(ip, port.toInteger())
                t.setTcpNoDelay(true)
                ssh = con.connect(t, ssh.getUsername())
            }catch (IOException e) {
                logger.error "Failed to open SSH connection due to exception: ",e
            }
        }
        return ssh.connected
    }

    /**
     * Method to get command session to host. New connections is always created. If server disallows to create new connections,
     * another try of creating it is taken after delay. Timeout for creating connection is 10 seconds, when method throws exception
     * @param hostName
     * @param user
     * @param port
     * @param pass
     * @return
     */
    public Ssh2Session getFreeSession(String hostName, String user, String port, String pass){
        def connection = getConnection(hostName, user, port, pass)
        def session
        try {
            session = connection.openSessionChannel()
        } catch (Exception e) {
            logger.debug "Cannot create a new session " + e.message + " Will try with new connection."
            try {
                connection = getNewConnection(hostName, user, port, pass)
                session = connection.openSessionChannel()
            } catch(Exception finalException){
                logger.error("Cannot open session",finalException)
            }
        }
        if (! session) throw new RuntimeException(" Cannot create command session for host wihin 10 seconds. Please look in debug information for details")
        if(logger.traceEnabled)logger.trace "Got session $session"
        return session
    }

    @WithWriteLock
    private static void closeAndRemoveConnections(String hostName, String user, String port, String pass){
        List<SshClient> connections = currentConnections[getConnectionKey(hostName, user, port, pass)]
        connections.each {
            if (it.connected)
                it.disconnect()
        }
        connections = null
        currentConnections.remove(getConnectionKey(hostName, user, port, pass))
    }
    /**
     * Method to disconnect SSH connection
     * @param hostName
     * @param user
     * @param port
     * @param pass
     */
    @WithWriteLock
    public void disconnect(String hostName, String user, String port, String pass){
        logger.debug "Disconnecting $hostName"
        try {
            closeAndRemoveConnections(hostName, user, port, pass)
        } catch (Throwable e) {
            logger.debug "Disconnecting failed due to error: $e"
            logger.trace "Error content",e
        }
    }
}
