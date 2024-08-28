package com.ericsson.cifwk.taf.utils.ssh

import com.ericsson.cifwk.meta.API
import com.maverick.ssh2.Ssh2Channel
import com.maverick.ssh2.Ssh2Session
import groovy.util.logging.Log4j

/**
 * Implementation of SshRemoteCommandExecutor using J2SSH library
 *
 */
@Log4j
@API(value = API.Quality.Internal)
class J2SshCommandExecutor extends J2SshConnectionsHelper {

	String hostName
	String user
	String pass
	String key
	File keyFile
	String port = "22"

	String stdOut
	String stdErr
	int exitCode


	/**
	 * Method allowing to get session channel to run command
	 * @return
	 */
	protected Ssh2Session getSession(){
		def session = getFreeSession(hostName, user, port, pass)
		log.trace "Got session $session for $hostName with $user on port $port"
		return session
	}

	/**
	 * Method to disconnect from server
	 */
	public void disconnect(){
		disconnect(hostName, user, port, pass)
	}
	/**
	 * Method allowing to release session channel after command execution
	 */
	protected void releaseSession(Ssh2Session session){
		session.close()
	}
	/**
	 * Mechanism to read messages after command run using "preferred" and "low-level" mechanism
	 * @param stream
	 * @return
	 */
	private String simpleRead(InputStream stream){
		StringBuffer result = new StringBuffer()
		int bytesRead = 0
		def buffer = new byte[255]
		while  ((bytesRead = stream.read(buffer,0,buffer.size()-1)) > 0){
			String sshData = new String(buffer,0,bytesRead)
			result.append(sshData)
		}
		return result.readLines().join("\n")
	}

	/**
	 * Method to read an SSH stream and return String as an output
	 * @param stream
	 * @return
	 */
	protected String readStream(InputStream stream){
		String result = simpleRead(stream)
		log.trace "Stream has ${stream.available()} bytes available"
		while (stream.available() > 0)
			result += simpleRead(stream)
		log.trace "Stream has ${stream.available()} bytes available"
		log.trace "Finished reading SSH. Ended up with $result"
		return result
	}

	/**
	 * Method to execute a command on the hostName
	 *
	 * @param cmdWithArgs
	 *            - command that is to be run on the hostName
	 * @param sendOnly
	 *            - true: command output will not be read.
	 *            - false: command output will be read.
	 * @return True if the command was executed without exception on the hostName
	 */
	public boolean sendCommand(String cmdWithArgs, boolean sendOnly=false){
		try {
			log.trace "Trying to get session"
			Ssh2Session currentSession = session
			boolean sendResult = currentSession.executeCommand(cmdWithArgs)
			if (sendResult && ! sendOnly) {
				InputStream inputStream = currentSession.getInputStream()
				InputStream errorStream = currentSession.getStderrInputStream()
				log.trace "Reading input stream"
				stdOut = readStream(inputStream)
				log.trace "Reading error stream"
				stdErr = readStream(errorStream)
				exitCode = currentSession.exitCode()
				log.debug("Command: " + cmdWithArgs + " returned exit code " + exitCode)
			}
			releaseSession(currentSession)
		} catch (IOException e) {
			log.error("Failed to invoke SSH shell command",e)
		}
		log.trace "SSH executor is returning [$stdOut] and [$stdErr] with exit code: ${exitCode}"
		return exitCode == 0
	}
}
