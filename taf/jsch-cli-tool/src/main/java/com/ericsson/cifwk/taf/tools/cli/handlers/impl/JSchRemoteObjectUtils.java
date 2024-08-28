/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.cifwk.taf.tools.cli.handlers.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.handlers.commands.JSchChmodCommand;
import com.ericsson.cifwk.taf.tools.cli.handlers.commands.JSchGetHomeCommand;
import com.ericsson.cifwk.taf.tools.cli.handlers.commands.JSchGetInfoCommand;
import com.ericsson.cifwk.taf.tools.cli.jsch.FileInfo;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLIToolException;
import com.ericsson.cifwk.taf.utils.CommandExecutionOutput;
import com.ericsson.cifwk.taf.utils.FileStructure;
import com.ericsson.de.tools.cli.CliIntermediateResult;
import com.ericsson.de.tools.cli.CliToolShell;
import com.ericsson.de.tools.cli.CliTools;
import com.ericsson.de.tools.cli.WaitConditions;
import com.google.common.base.Preconditions;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.tools.cli.CLICommandHelperConstants.GENERIC_PROMPT_PATTERN;
import static com.ericsson.cifwk.taf.tools.cli.CLICommandHelperConstants.PASSWORD_PATTERN;
import static com.ericsson.cifwk.taf.tools.cli.handlers.impl.JSchConnectionsHelper.STRICT_HOST_KEY_CHECKING_KEY;
import static com.ericsson.cifwk.taf.tools.cli.handlers.impl.JSchConnectionsHelper.STRICT_HOST_KEY_CHECKING_NEGATIVE_VALUE;

class JSchRemoteObjectUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSchRemoteObjectUtils.class);

    private static final String SHELL_COMMAND_PREFIX = "bash -c";
    private static final String WHITESPACES_REGEX = "\\s";
    private static final String UNIX_RESET_LS_ALIAS = "alias ls=ls";
    private static final String UNIX_DIR_LIST_COMMAND = "ls";
    private static final String UNIX_MD5_COMMAND = "md5sum";
    private static final String UNIX_RM_COMMAND = "rm -rf";
    private static final String UNIX_CREATE_DIR_COMMAND = "mkdir -p";
    private static final String UNIX_SCP_COMMAND = "scp";
    private static final String UNIX_TAR_COMMAND = "tar";
    private static final String UNIX_CD_COMMAND = "cd";
    private static final String STRICT_HOST_KEY_CHECKING = STRICT_HOST_KEY_CHECKING_KEY + "=" + STRICT_HOST_KEY_CHECKING_NEGATIVE_VALUE;
    private static final int SIZE_POSITION = 4;
    private static final int MD5_POSITION = 0;
    private static final String REMOTE_KEYFILE_PATH = "/root/.ssh/vm_private_key";
    private static String fileSeparator = System.getProperty("file.separator");

    static String getHomeDir(Host host, User user) throws JSchCLIToolException {
        return new JSchGetHomeCommand(host, user).execute();
    }

    static FileInfo getInfo(String filePath, Host host, User user) throws JSchCLIToolException {
        return new JSchGetInfoCommand(filePath, host, user).execute();
    }

    static void changeMod(String filePath, int mod, Host host, User user) throws JSchCLIToolException {
        new JSchChmodCommand(filePath, mod, host, user).execute();
    }

    static void changeModWitSshKeyFile(final String filePath, final int mod, final Host host, final User user, final String pathToPrivateKeyFile) {
        new JSchChmodCommand(filePath, mod, host, user, pathToPrivateKeyFile).execute();
    }
    static boolean remoteFileExists(Host host, User user, String filePath, String pathToPrivateKeyFile) {
        LOGGER.debug("Checking does " + filePath + " on host " + host.getIp() + " exist");
        String command = String.format("%s \"[ -f %s %s\"", SHELL_COMMAND_PREFIX, filePath, "] && echo \'file exists\' || echo \'does not exist\'");
        CommandExecutionOutput result = connectToHostAndExecuteCommand(host, user, command, pathToPrivateKeyFile);
        if (StringUtils.isBlank(result.getStdOut()) || !StringUtils.isBlank(result.getStdErr()))
            return false;
        return result.getStdOut().contains("file exists");
    }

    static List<String> remoteFileExistsWithWildCard(Host host, User user, String filePathWithWildCard, String pathToPrivateKeyFile) {
        LOGGER.debug("Checking do files exist matching pattern " + filePathWithWildCard + " on host " + host.getIp());
        String command = String.format("%s; %s \"%s -1 %s\"", UNIX_RESET_LS_ALIAS, SHELL_COMMAND_PREFIX, UNIX_DIR_LIST_COMMAND, filePathWithWildCard);
        LOGGER.debug("Executing command: " +command);
        CommandExecutionOutput result = connectToHostAndExecuteCommand(host, user, command, pathToPrivateKeyFile);
        String completeOutput = result.getStdOut();
        int exitCode = result.getExitCode();
        LOGGER.debug("Output is: " + completeOutput);
        List<String> filesMatchingPattern = new ArrayList<>();
        if (exitCode == 0) {
            for (String output : completeOutput.split("\\n")) {
                filesMatchingPattern.add(output.trim());
            }
        }
        return filesMatchingPattern;
    }

    static boolean remoteFolderExists(Host host, User user, String filePath, String pathToPrivateKeyFile) {
        LOGGER.debug("Checking does " + filePath + " on host " + host.getIp() + " exist");
        String command = String.format("%s \"[ -d %s %s\"", SHELL_COMMAND_PREFIX, filePath, "] && echo \'file exists\' || echo \'does not exist\'");
        CommandExecutionOutput result = connectToHostAndExecuteCommand(host, user, command, pathToPrivateKeyFile);
        return result.getStdOut().contains("file exists");
    }

    static FileStructure getRemoteFileInformation(Host host, User user, String filePath, String pathToPrivateKeyFile) {
        LOGGER.debug("Getting remote file information for " + filePath + " on host " + host.getHostname());
        String shellCommand = String.format("%s \"%s -l %s\"", SHELL_COMMAND_PREFIX, UNIX_DIR_LIST_COMMAND, filePath);
        final CommandExecutionOutput cmdResult = connectToHostAndExecuteCommand(host, user, shellCommand, pathToPrivateKeyFile);
        final String fileInfo = cmdResult.getStdOut();
        final FileStructure filestructure = new FileStructure();
        filestructure.filesize = fileInfo.split(WHITESPACES_REGEX)[SIZE_POSITION];
        LOGGER.trace("filesize is:" + filestructure.filesize);

        shellCommand = String.format("%s \"%s %s\"", SHELL_COMMAND_PREFIX, UNIX_MD5_COMMAND, filePath);
        final String md5Output;
        md5Output = connectToHostAndExecuteCommand(host, user, shellCommand, pathToPrivateKeyFile).getStdOut();
        filestructure.md5 = StringUtils.isBlank(md5Output) ? null : md5Output.split(WHITESPACES_REGEX)[MD5_POSITION];

        LOGGER.trace("md5 is:" + filestructure.md5);
        LOGGER.trace("Inside method on host " + host.getHostname() + " file: " + filePath);
        LOGGER.trace("Returning " + filestructure);
        return filestructure;
    }

    static boolean copyRemoteToRemote(final Host sourceHost, final String remoteFileSource, final Host destinationHost,
                                      final String remoteFileDestination) {
        String destinationPwd = destinationHost.getPass();
        String shellCommand = String.format("%s \'%s -o %s %s %s@%s:%s\'", SHELL_COMMAND_PREFIX, UNIX_SCP_COMMAND, STRICT_HOST_KEY_CHECKING, remoteFileSource,
                destinationHost.getUser(), destinationHost.getIp(), remoteFileDestination);
        String refCommand = shellCommand+"/"+destinationPwd;
        CommandExecutionOutput out = connectToHostAndExecuteCommandWithParams(sourceHost, sourceHost.getDefaultUser(), refCommand, null);
        if(out.getStdErr().contains("Permission denied")){
            return false;
        }
        return remoteFileExists(destinationHost, destinationHost.getDefaultUser(), remoteFileDestination, null);
    }


    static boolean copyRemoteToRemote(final Host sourceHost, final String remoteFileSource, String pathToPrivateKeyFile, final Host destinationHost,
                                      final String remoteFileDestination, String pathToPrivateKeyFile1) {
        String shellCommand = String.format("%s \"%s -i %s -o %s %s %s@%s:%s\"", SHELL_COMMAND_PREFIX, UNIX_SCP_COMMAND, pathToPrivateKeyFile1, STRICT_HOST_KEY_CHECKING,
                remoteFileSource, destinationHost.getUser(), destinationHost.getIp(), remoteFileDestination);

        connectToHostAndExecuteCommand(sourceHost, sourceHost.getDefaultUser(), shellCommand, pathToPrivateKeyFile);
        return remoteFileExists(destinationHost, destinationHost.getDefaultUser(), remoteFileDestination, pathToPrivateKeyFile1);
    }

    static String tarRemoteDirectory(Host host, User user, String dirPath) {
        String tarfileName = RandomStringUtils.randomAlphanumeric(8) + ".tar";
        String zipCommand = String.format("%s \"%s %s && %s -cf %s .\"", SHELL_COMMAND_PREFIX, UNIX_CD_COMMAND, dirPath, UNIX_TAR_COMMAND, tarfileName);
        connectToHostAndExecuteCommand(host, user, zipCommand, null);
        return tarfileName;
    }

    static String tarRemoteDirectory(Host host, User user, String dirPath, String sshKeyFile) {
        String tarfileName = RandomStringUtils.randomAlphanumeric(8) + ".tar";
        String zipCommand = String.format("%s \"%s %s && %s -cf %s .\"", SHELL_COMMAND_PREFIX, UNIX_CD_COMMAND, dirPath, UNIX_TAR_COMMAND, tarfileName);
        connectToHostAndExecuteCommand(host, user, zipCommand, sshKeyFile);
        return tarfileName;
    }

    static boolean untarRemoteArchiveAndDelete(Host host, User user, String filePath, String toDir) {
        String shellCommand = String.format("%s \"%s -xf %s -C %s\"", SHELL_COMMAND_PREFIX, UNIX_TAR_COMMAND, filePath, toDir);
        connectToHostAndExecuteCommand(host, user, shellCommand, null);
        return deleteRemoteFileOrFolder(host, user, filePath, null);
    }

    static boolean untarRemoteArchiveAndDelete(Host host, User user, String filePath, String toDir, String sshKeyFile) {
        String shellCommand = String.format("%s \"%s -xf %s -C %s\"", SHELL_COMMAND_PREFIX, UNIX_TAR_COMMAND, filePath, toDir);
        connectToHostAndExecuteCommand(host, user, shellCommand, sshKeyFile);
        return deleteRemoteFileOrFolder(host, user, filePath, sshKeyFile);
    }

    static boolean createRemoteFile(Host host, User user, String filePath, Long fileSize, String fileType, boolean createDir, String pathToPrivateKeyFile) {
        LOGGER.trace("Creating remote file " + filePath + " on host " + host.getHostname());
        if (createDir) {
            createRemoteDirectoryForFile(host, user, filePath, pathToPrivateKeyFile);
        }
        final String shellCommand = String.format("%s \"dd if=/dev/zero of=%s bs=%s%s count=1\"", SHELL_COMMAND_PREFIX, filePath, fileSize, fileType);
        String cmdResult = connectToHostAndExecuteCommand(host, user, shellCommand, pathToPrivateKeyFile).getStdErr();
        if (!StringUtils.isBlank(cmdResult)) {
            LOGGER.warn("Could Not Create Remote File. Error = " + cmdResult);
        }
        return remoteFileExists(host, user, filePath, pathToPrivateKeyFile);
    }

    static boolean createRemoteFile(Host host, User user, String filePath, Long fileSize, String fileType, final String pathToPrivateKeyFile) {
        return createRemoteFile(host, user, filePath, fileSize, fileType, true, pathToPrivateKeyFile);
    }

    public static String createRemoteDirectory(Host host, String dirPath, String pathToPrivateKeyFile) {
        return createRemoteDirectory(host, host.getDefaultUser(), dirPath, pathToPrivateKeyFile);
    }

    @API(Internal)
    public static String createRemoteDirectory(Host host, User user, String dirPath, String pathToPrivateKeyFile) {
        final String shellCommand = String.format("%s \"%s %s\"", SHELL_COMMAND_PREFIX, UNIX_CREATE_DIR_COMMAND, dirPath);
        CommandExecutionOutput result = connectToHostAndExecuteCommand(host, user, shellCommand, pathToPrivateKeyFile);
            Preconditions.checkState(remoteFolderExists(host, user, dirPath, pathToPrivateKeyFile), String.format("Failed to create remote directory, the following may give some idea why \n %s",
                    result.getStdOut()));
        return result.getStdOut();
    }

    private static String createRemoteDirectoryForFile(Host host, User user, String filePath, String pathToPrivateKeyFile) {
        final String directoryName = getDirectoryNameFromFilePath(filePath);
        return createRemoteDirectory(host, user, directoryName, pathToPrivateKeyFile);
    }

    private static String getDirectoryNameFromFilePath(final String filePath) {
        final String[] splitFilePath = filePath.split("/");
        if (splitFilePath.length < 2) {
            return filePath;
        }
        String directory = "";
        int index = 0;
        for (String string : splitFilePath) {
            if (index != 0 && index != splitFilePath.length - 1) {
                directory = directory + "/" + string;
            }
            index++;
        }
        return directory;
    }

    static boolean deleteRemoteFileOrFolder(Host host, User user, String path, final String pathToPrivateKeyFile) {
        final String directoryToBeDeleted = getDirectoryNameFromFilePath(path);
        LOGGER.debug("Deleting remote Folder " + directoryToBeDeleted + " on host " + host.getHostname());
        final String shellCommand = String.format("%s \"%s %s\"", SHELL_COMMAND_PREFIX, UNIX_RM_COMMAND, path);
        connectToHostAndExecuteCommand(host, user, shellCommand, pathToPrivateKeyFile);
        return !remoteFolderExists(host, user, path, pathToPrivateKeyFile);
    }

    private static CommandExecutionOutput connectToHostAndExecuteCommand(Host host, User user, String command, String pathToPrivateKeyFile) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec execChannel = null;
        String stOut = "";
        String stErr = "";
        int exitCode = -1;
        InputStream inputStream = null;
        InputStream errStream = null;
        try {
            session = JSchConnectionsHelper.getSession(host, user, jsch, pathToPrivateKeyFile);
            execChannel = (ChannelExec) session.openChannel("exec");
            execChannel.setCommand(command);
            inputStream = execChannel.getInputStream();
            errStream = execChannel.getErrStream();
            execChannel.connect();
            byte[] tmp = new byte[1024];
            while (true) {
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    stOut = new String(tmp, 0, i);
                }
                while (errStream.available() > 0) {
                    int i = errStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    stErr = new String(tmp, 0, i);
                }
                if (execChannel.isClosed())
                    break;
                try {
                    Thread.sleep(500);
                } catch (Exception ee) {
                    LOGGER.error("Thread interrupted: {}", ee);
                }
            }
            exitCode = execChannel.getExitStatus();
        } catch (Exception e) {
            LOGGER.error("Unable to connect to host: {}@{}", user, host, e);
        } finally {
            closeResources(session, execChannel, inputStream, errStream);
        }
        return new CommandExecutionOutput(stErr, stOut, exitCode);
    }

    private static CommandExecutionOutput connectToHostAndExecuteCommandWithParams(Host host, User user, String command, String pathToPrivateKeyFile) {
        int exitCode = -1;
        String stOut = "";
        String stErr = "";
        String username = user.getUsername();
        String password = user.getPassword();
        String hostip = host.getIp();
        CliToolShell cliToolShell = CliTools.sshShell(hostip).withUsername(username).withPassword(password).build();
        try {
            int index = command.lastIndexOf("/");
            String  destPwd = command.substring(index + 1);
            String tempString = "/"+destPwd;
            String commandNew = command.replace(tempString, "");
            CliIntermediateResult result=cliToolShell.writeLine(commandNew, WaitConditions.substring("password"));
            cliToolShell.writeLine(destPwd);
            String ins = result.getOutput();
            if (!ins.isEmpty()){
                exitCode = 0;
            }
            // exitCode = execChannel.getExitStatus();
        } catch (Exception e) {
            LOGGER.error("Unable to connect to host: {}@{}", user, host, e);
        } finally {
            cliToolShell.close();
        }
        return new CommandExecutionOutput(stErr, stOut, exitCode);

    }

    private static void closeResources(Session session, ChannelExec execChannel, InputStream inputStream, InputStream errStream) {
        try {
            if(inputStream != null)
                inputStream.close();
            if(errStream != null)
                errStream.close();
            if(execChannel != null && (execChannel.isConnected() || !execChannel.isClosed()))
                    execChannel.disconnect();
            if (session.isConnected()) {
                session.disconnect();
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred closing InputStream: {}", e);
        }
    }

    static String getKeyfileFromMs() {
        String localTmpDir = System.getProperty("java.io.tmpdir");
        if (!localTmpDir.endsWith(fileSeparator)) {
            localTmpDir = localTmpDir + fileSeparator;
        }
        String localKeyFilePath = localTmpDir + "vm_private_key";
        RemoteObjectHandler remoteObjectHandler = new RemoteObjectHandler(DataHandler.getHostByName("ms1"));
        remoteObjectHandler.copyRemoteFileToLocal(REMOTE_KEYFILE_PATH, localKeyFilePath);
        return localKeyFilePath;
    }

    static boolean getKeyfileFromMs(String localKeyFilePath) {
        RemoteObjectHandler remoteObjectHandler = new RemoteObjectHandler(DataHandler.getHostByName("ms1"));
        return remoteObjectHandler.copyRemoteFileToLocal(REMOTE_KEYFILE_PATH, localKeyFilePath);
    }

    static boolean copyRemoteFileToRemoteWithCli(Host sourceHost, String remoteFileSource, Host destinationHost, String remoteFileDestination) {
        String shellCommand = String.format("%s \' %s -o %s %s %s@%s:%s\'", SHELL_COMMAND_PREFIX, UNIX_SCP_COMMAND, STRICT_HOST_KEY_CHECKING, remoteFileSource,
                destinationHost.getUser(), destinationHost.getIp(), remoteFileDestination);
        return openShellAndExecuteCommand(sourceHost, destinationHost, shellCommand) != -1;
    }

    protected static int openShellAndExecuteCommand(final Host sourceHost, final Host destinationHost, String shellCommand) {
        CLI cli = new CLI(sourceHost);
        Shell shell = cli.openShell();
        try {
            shell.writeln(shellCommand);
            String cmdResult = shell.expect(PASSWORD_PATTERN);
            Matcher m = PASSWORD_PATTERN.matcher(cmdResult);
            if (m.find()) {
                shell.writeln(destinationHost.getPass());
                shell.expect(GENERIC_PROMPT_PATTERN);
            }
            shell.writeln("exit");
            return shell.getExitValue();
        }finally{
            if (shell != null)
                shell.disconnect();
        }
    }

}
