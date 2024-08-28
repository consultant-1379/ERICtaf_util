package com.ericsson.cifwk.taf.handlers.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.ericsson.cifwk.taf.utils.ssh.J2SshCommandExecutor

/**
 * Utility to execute JBOSS CLI command using SSH 
 *
 */
class JbossRemoteClient {

	private static Logger logger = LoggerFactory.getLogger(JbossRemoteClient)
	
	/**
	 * Execute JBOSS CLI command on specified server to a specified node. Requires to establish SSH connection first
	 * @param jbossHome
	 * @param command
	 * @param remoteNode
	 * @param sshUser
	 * @param sshPass
	 * @param sshPort
	 * @param user
	 * @param password
	 * @param remotePort
	 * @throws ConnectionException if JBoss Controller is unavailable
	 * @return
	 */
	static String executeCommand(String jbossHome, String command, String remoteNode, String sshHost, String sshUser, String sshPass, String sshPort, String user, String password, String remotePort, int jbossOffset=0){
		J2SshCommandExecutor ssh = new J2SshCommandExecutor(hostName:sshHost,user:sshUser,pass:sshPass,port:sshPort)
		String jbossCommand = "$jbossHome/bin/jboss-cli.sh --commands=\"connect $remoteNode:$remotePort,$command\" --user=$user --password=$password"
		logger.debug "Executing command $jbossCommand"
		ssh.sendCommand(jbossCommand)
		logger.debug "Jboss execution status $ssh.exitCode reponse: $ssh.stdOut"
		if (ssh.stdOut.contains("The controller is not available")){
			logger.debug "Could not connect to Controller for JBoss Instance $remoteNode"
			throw new ConnectException("Could not connect to Controller for JBoss Instance $remoteNode")
		}
		assert ssh.exitCode == 0 : ssh.stdOut
		return ssh.stdOut
	}
}
