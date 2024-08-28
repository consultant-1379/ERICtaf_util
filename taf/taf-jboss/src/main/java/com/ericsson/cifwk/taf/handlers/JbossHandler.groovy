package com.ericsson.cifwk.taf.handlers
import com.ericsson.cifwk.taf.data.Ports
import com.ericsson.cifwk.taf.utils.FileUtils
import com.ericsson.cifwk.taf.utils.ssh.J2SshFileCopy
import groovy.transform.InheritConstructors
import groovy.util.logging.Log4j

import java.nio.charset.MalformedInputException
/**
 * Class to handle interactions with JBOSS
 *
 */
@Log4j
@InheritConstructors
class JbossHandler extends AsDeploymentInfo {

	private static String TEMP_FILES = "/tmp/jbossfiles"
	private static final int REMOVE_DRIVE_ID_FOR_WINDOWS = 3
	private static final String OS_ID_WINDOWS = "\\"
	private static int ENABLED_POS = 8
	private static String ENABLED_STATE = "true"
	private static String NOTFOUND = "not found"


	/**
	 * Copy file to jboss server
	 * @param fileToCopy
	 * @return
	 */
	private String copyFile(File fileToCopy, String newName = null){
		String destDir
		log.trace "New File Name = $newName"
		if(newName)
			destDir = "$TEMP_FILES/$newName"
		else
			destDir = "$TEMP_FILES/${fileToCopy.name}"
		log.trace "Destination DIR  = "+destDir
		J2SshFileCopy.putFile(fileToCopy.absolutePath, destDir, server.ip, server.user, server.pass, server.port[Ports.SSH])
		return destDir
	}


	/**
	 * Get relative path for file for deployment if using local client. For Windows, it sub strings to get
	 * String after the first few characters, omitting "<HardDrive>:\" from the filepath.
	 * @param filePath
	 * @return
	 */
	private String getLocalRelativePath(String filePath){
		if(filePath.contains(OS_ID_WINDOWS)){
			return FileUtils.topDirFromCurrent + filePath.substring(REMOVE_DRIVE_ID_FOR_WINDOWS)
		}
		return FileUtils.topDirFromCurrent + filePath
	}

	/**
	 * Get relative path for file for deployment if using remote client
	 * This Method requires a nix O.S. (unix, linux). Windows O.S. will not be supported.
	 * @param filePath
	 * @return
	 */
	private String getRemoteRelativePath(String filePath){
		String pwd = commandService.sshConnection.simpleExec("pwd")
		String path=""
		(pwd.split("/").size()-1).times {path += "../"}
		return path + filePath
	}

	/**
	 * Run command to deploy file on JBOSS node. If 'activate' is specified as false, file will not be started after deployment. If forceDeploy option is used, deployment will be forced regardless of current state
	 * @param fileToDeploy
	 * @return
	 */
	public boolean deployFile(File fileToDeploy, boolean activate=true, boolean forceDeploy=false, String newFileName = null){
		String filePath
		String commandString
		if (commandService.useRemote()){
			String remoteFilePath = copyFile(fileToDeploy, newFileName)
			filePath = getRemoteRelativePath(remoteFilePath)
		} else {
			filePath = getLocalRelativePath(fileToDeploy.absolutePath)
		}
		if (forceDeploy)
			commandString = "deploy $filePath --force"
		else
			commandString = "deploy $filePath" + ((activate)?"": " --disabled")

		boolean status = commandService.execute(commandString)
		log.debug "File deployment completed with status $status. Response: $commandService.response"
		return status
	}

	/**
	 * Execute command to activate previously deployed file
	 * @param fileName
	 * @return
	 */
	boolean activateDeployedFile(String fileName){
		boolean status = commandService.execute("deploy --name=$fileName")
		log.debug "File activaton executed with status: $status. Response: $commandService.response"
		return status
	}

	/**
	 * Run a command to undeploy file from JBOSS node. If second argument is true, file is removed from JBOSS filesystem as well
	 * @param fileName
	 * @return
	 */
	boolean undeployFile(String fileName, boolean removeContent=false){
		boolean status = commandService.execute("undeploy $fileName" + ((removeContent)?"": " --keep-content"))
		log.debug "File undeployment executed with status: $status. Response: $commandService.response"
		return status
	}


	/**
	 * Run a command to check for ENABLED state for a deployed file.
	 * @param fileName
	 * @return True if file is enabled, File Not Found Exception if file not found
	 * @throws FileNotFoundException if ear file not found.
	 * @throws MalformedInputException if invalid enabled status found
	 */
	boolean isDeployedFileEnabled(String fileName){
		Boolean status = commandService.execute("deployment-info --name=$fileName")
		log.debug "File Deployment Status executed with status: $status. Response: $commandService.response"
		if (commandService.response.contains(NOTFOUND))
			throw new FileNotFoundException("File Not Found: $fileName")
		try{
			return commandService.response.split()[ENABLED_POS].contains(ENABLED_STATE)
		}catch (Exception e){
			throw new MalformedInputException("MalFormed Status Response Received")
		}
	}

	/**
	 * Run a command to check Status states for a deployed file.
	 * @param fileName
	 * @return ENABLED, PERSISTENT and Status states
	 */
	public deploymentStates(String fileName){
		return [isEnabled(fileName),isPersistent(fileName),getStatus(fileName)]
	}
}
