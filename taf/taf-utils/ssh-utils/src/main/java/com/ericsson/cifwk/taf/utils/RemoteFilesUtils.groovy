package com.ericsson.cifwk.taf.utils

import com.ericsson.cifwk.meta.API
import com.ericsson.cifwk.meta.API.Quality
import com.ericsson.cifwk.meta.API.Since
import com.ericsson.cifwk.taf.utils.ssh.J2SshCommandExecutor
import groovy.transform.WithWriteLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Please use RemoteObjectHandler instead
 */
@Deprecated
@API(Quality.Deprecated)
@Since(2.30d)
class RemoteFilesUtils {

	private static final Logger logger = LoggerFactory.getLogger(RemoteFilesUtils)

	private static J2SshCommandExecutor sshCommandExecutor
	private static final String SHELL_COMMAND_PREFIX = 'bash -c'
	protected static final String WHITESPACES_REGEX = '\\s'
	public static final String UNIX_DIR_LIST_COMMAND = 'ls'
	public static final String UNIX_MD5_COMMAND = 'md5sum'
	public static final String UNIX_RM_COMMAND = 'rm -rf'
	public static final String UNIX_COPY_COMMAND = 'cp'
	public static final String UNIX_CREATE_DIR_COMMAND = "mkdir -p"
	public static final int SIZE_POSITION=4
	public static final int MD5_POSITION=0

	/**
	 * Utility method to check if a specified file exists on a remote host
	 * @param hostname
	 * @param port
	 * @param user
	 * @param password
	 * @param filePath
	 * @return true if file exists, false otherwise
	 */
	static boolean remoteFileExists(String hostname, String port, String user, String pass, String filePath) {
		logger.debug("Checking does $filePath on host $hostname exist")
		String shellCommand = "${SHELL_COMMAND_PREFIX} \"$UNIX_DIR_LIST_COMMAND ${filePath} \""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)
		return InternalStringUtils.isBlank(cmdResult.stdErr)
	}

	/**
	 * Utility method to create a file on a remote host. The file is filled with random characters up to the size specified
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param filePath
	 * @param fileSize
	 * @param sizeType
	 * @param createDir - defaults to true, create directory path if it doesn't exist.
	 * @return true if file is successfully created, false otherwise
	 */
	public static boolean createRemoteFile(String hostname, String port, String user, String pass, String filePath, Long fileSize, String sizeType, boolean createDir = true) {
		logger.trace("Creating remote file $filePath on host $hostname")
		if(createDir) {
			createRemoteDirectory(hostname, port, user, pass, filePath)
		}
		String shellCommand = "${SHELL_COMMAND_PREFIX} \"dd if=/dev/zero of=${filePath} status=noxfer bs=${fileSize}${sizeType} count=1\""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)
		if (!InternalStringUtils.isBlank(cmdResult.stdErr)) {
			logger.warn "Could Not Create Remote File. Error = " + sshCommandExecutor.stdErr
		}
		return remoteFileExists(hostname, port, user, pass, filePath)
	}


	/**
	 * Create the Remote Directory for a Given Filepath
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param directoryName
	 * @return
	 */
	public static String createRemoteDirectory(String hostname, String port, String user, String pass, String filePath){
		String[] splitFilePath = filePath.split("/")
		String directoryName = getDirectoryNameFromFilePath(filePath)
		String shellCommand = "${SHELL_COMMAND_PREFIX} \"$UNIX_CREATE_DIR_COMMAND $directoryName\""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)
		return cmdResult.stdOut
	}

	/**
	 * Get the Name of the Directory from a Given File Path
	 * @param filePath
	 * @return String - Directory Name for a Filepath
	 */
	private static String getDirectoryNameFromFilePath(String filePath){
		String[] splitFilePath = filePath.split("/")
		String directoryName = splitFilePath[0]
		if(splitFilePath.size() > 2){
			splitFilePath.eachWithIndex { String temp, int idx ->
				if(idx != 0 && idx != (splitFilePath.size() - 1))
					directoryName += "/"+temp
			}
		}
		return directoryName
	}

	/**
	 * Utility method to delete a file on a remote host
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param filePath
	 * @return true if file is successfully deleted, false otherwise
	 */
	public static boolean deleteRemoteFile(String hostname, String port, String user, String pass, String filePath) {
		logger.debug("Deleting remote file ${filePath} on host ${hostname}")
		String shellCommand = "${SHELL_COMMAND_PREFIX} \"$UNIX_RM_COMMAND  ${filePath} \""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)

		return InternalStringUtils.isBlank(cmdResult.stdErr)
	}

	/**
	 * Utility method to delete a folder on a remote host
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param filePath
	 * @return true if file is successfully deleted, false otherwise
	 */
	public static boolean deleteRemoteFolder(String hostname, String port, String user, String pass, String filePath) {
		String directoryToBeDeleted = getDirectoryNameFromFilePath(filePath)
		logger.debug("Deleting remote Folder ${directoryToBeDeleted} on host ${hostname}")
		String shellCommand = "${SHELL_COMMAND_PREFIX} \"$UNIX_RM_COMMAND  ${directoryToBeDeleted} \""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)

		return InternalStringUtils.isBlank(cmdResult.stdErr)
	}

	/**
	 * PRIVATE method to create a connection to the host, execute the command and return the result
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param command
	 * @return an instance of CommandExecutionOutput that contains the result of the command execution
	 */
	@WithWriteLock
	private static CommandExecutionOutput connectToHostAndExecuteCommand(String hostname, String port, String user, String pass, String command){
		sshCommandExecutor = new J2SshCommandExecutor()
		sshCommandExecutor.hostName = hostname
		sshCommandExecutor.port = port
		sshCommandExecutor.user = user
		sshCommandExecutor.pass = pass

		boolean result = sshCommandExecutor.sendCommand(command)
		int resultCode = (result) ? 0 : 1;
		return new CommandExecutionOutput(sshCommandExecutor.stdErr, sshCommandExecutor.stdOut, resultCode)
	}

	/**
	 * Utility method to copy a file on a remote host
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param filePath1
	 * @param filePath2
	 * @param createFilePath2 - defaults to true, create the directory path of the new file if it doesn't alreday exist.
	 * @return true if file is successfully copied, false otherwise
	 */
	public static boolean copyRemoteFile(String hostname, String port, String user, String pass, String filePath1, String filePath2, boolean createfilePath2 = true) {
		logger.debug("Copying $filePath1 on host $hostname to $filePath2")
		if(createfilePath2)
			createRemoteDirectory(hostname, port, user, pass, filePath2)
		String shellCommand = SHELL_COMMAND_PREFIX + " \"$UNIX_COPY_COMMAND " + filePath1 + " " + filePath2 + "\""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)
		if (!InternalStringUtils.isBlank(cmdResult.stdErr)) {
			logger.warn "Could not Copy File from $filePath1 to $filePath2 due to "+sshCommandExecutor.stdErr
			return false
		}
		return true
	}

	/**
	 * Utility method to retrieve file information from a remote host
	 * @param hostname
	 * @param port
	 * @param user
	 * @param pass
	 * @param filePath
	 * @return FileStructure object consisting of the files md5 checksum and size
	 */
	public static FileStructure getRemoteFileInformation(String hostname, String port, String user, String pass, String filePath) {
		logger.debug("Getting remote file information for $filePath on host $hostname")

		String shellCommand = "$SHELL_COMMAND_PREFIX \"$UNIX_DIR_LIST_COMMAND -l $filePath \""
		CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)
		if (InternalStringUtils.isBlank(cmdResult.stdOut) || !InternalStringUtils.isBlank(cmdResult.stdErr)) {
			return null;
		}

		String fileInfo = cmdResult.stdOut
		FileStructure filestructure = new FileStructure()
		filestructure.filesize = fileInfo.split(WHITESPACES_REGEX)[SIZE_POSITION]
		logger.trace("filesize is:"+filestructure.filesize)

		shellCommand = "$SHELL_COMMAND_PREFIX \"$UNIX_MD5_COMMAND $filePath \""
		CommandExecutionOutput md5CmdResult = connectToHostAndExecuteCommand(hostname, port, user, pass, shellCommand)
		String md5Output = md5CmdResult.stdOut
		filestructure.md5 = InternalStringUtils.isBlank(md5CmdResult.stdOut) ? null : md5Output.split(WHITESPACES_REGEX)[MD5_POSITION]

		if(logger.isTraceEnabled()){
			logger.trace("md5 is:"+filestructure.md5)
			logger.trace("Inside method on host " + hostname + " file: " + filePath)
			logger.trace("Returning " + filestructure)
		}
		return filestructure
	}
}