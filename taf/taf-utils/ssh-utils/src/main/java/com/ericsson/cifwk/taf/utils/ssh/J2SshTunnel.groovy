package com.ericsson.cifwk.taf.utils.ssh

import com.ericsson.cifwk.meta.API
import com.sshtools.net.ForwardingClient
import com.maverick.ssh.SshException
import groovy.util.logging.Log4j
/**
 * Class to provide functionality allowing creating tunnels using SSH local forwarding
 *
 * @deprecated this class moved into the specific testware (taf-tor-operators).
 */

@Deprecated
@API(API.Quality.Deprecated)
@API.Since(2.32d)
@Log4j
class J2SshTunnel extends J2SshConnectionsHelper{
    /**
     * Host used as entry point to SSH tunnel (localhost)
     */
    public static final String FORWARDING_HOST = "127.0.0.1"
    /**
     * Exception message when local port cannot be used for tunneling
     */
    public static final String WRONG_LOCAL_PORT = "Permission denied"
    /**
     * Exception message where local port is already used and cannot be used for tunneling
     */
    public static final String ALREADY_USED_LOCAL_PORT = "Address already in use"

    private String host
    private String port
    private String user
    private String pass

    /**
     * Constructor requiring SSH host configuration
     * @param sshHostname
     * @param sshPort
     * @param sshUser
     * @param sshPass
     */
    J2SshTunnel(String sshHostname, String sshPort, String sshUser, String sshPass){
        host = sshHostname
        port = sshPort
        user = sshUser
        pass = sshPass
    }

    /**
     * Convenience method to create configuration name
     * @param localPort
     * @param remotePort
     * @param remoteIp
     * @return
     */
    private static String getTunnelConfigurationId(String localPort, String remotePort, String remoteIp){
        return "local_${localPort}_to_${remoteIp}:${remotePort}"
    }

    /**
     * Convenience method to get forwarding client for SshClient
     * @return
     */
    private ForwardingClient getForwardingClient(){
        return new ForwardingClient(getConnection(host,user,port,pass))
    }

    /**
     * Method to start tunnel
     * @param localPort
     * @param remotePort
     * @param remoteIp
     * @throws SshException
     */
    public void startTunnel(String localPort, String remotePort, String remoteIp)throws SshException {
        String configurationName = getTunnelConfigurationId(localPort, remotePort, remoteIp)
        log.debug "Creating configuration with name $configurationName"
        try {
            getForwardingClient().startLocalForwarding(FORWARDING_HOST,localPort.toInteger(),remoteIp,remotePort.toInteger())
        } catch (SshException e){
            log.error "Configuration is wrong or duplicated: $e"
            log.trace "Details: ",e
            throw e
        }
    }
    /**
     * Method to close tunnel
     * @param remotePort
     * @param remoteIp
     */
    public void stopTunnel(String remotePort, String remoteIp){
        try {
            getForwardingClient().stopLocalForwarding(remoteIp, remotePort)
        } catch (SshException e){
            log.debug "Configuration is wrong: $e"
            log.trace("Details:",e)
        }
    }

    /**
     * Method to disconnect the connection
     *
     */
    public void disconnect()  {
        disconnect(host, user, port, pass);
    }
}
