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

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.exceptions.RemoteFileNotFoundException;
import com.ericsson.cifwk.taf.tools.cli.jsch.FileInfo;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLIToolException;
import com.ericsson.cifwk.taf.utils.FileStructure;
import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


public class RemoteObjectHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteObjectHandler.class);

    private final Host host;

    private User user;

    private static String fileSeparator = "/";

    /**
     * Instantiates the <code>RemoteObjectHandler</code> object with given <code>host</code> This uses the hosts default user to
     * connect to the host.
     *
     * @param host - Host remoteObjectHandler will be connecting to
     */
    public RemoteObjectHandler(Host host) {
        this(host, null);
    }

    /**
     * Instantiates the <code>RemoteObjectHandler</code> object with given <code>host</code> and <code>user</code>
     *
     * @param host - Host remoteObjectHandler will be connecting to
     * @param user - User connecting to host
     */
    public RemoteObjectHandler(Host host, User user) {
        this.host = host;
        this.user = getDefaultUser(host, user);
    }

    /**
     * Retrieves the user connecting to host
     *
     * @return String - Username
     */
    public String getUser() {
        return user.getUsername();
    }

    /**
     * Retrieves the password used to connect to host
     *
     * @return String - Password
     */
    public String getPass() {
        return user.getPassword();
    }

    @VisibleForTesting
    protected static User getDefaultUser(Host host, User user) {
        if (user != null) {
            return user;
        }

        return host.getDefaultUser();
    }

    /**
     * Retrieves an ssh public key file from the MS and stores it in the temp folder on the local machine, the location returned by System.getProperty("java.io.tmpdir")
     *
     * @return String - Path to keyFile in temp folder on local machine
     */
    public String getKeyfileFromMs() {
        return JSchRemoteObjectUtils.getKeyfileFromMs();
    }

    /**
     * Retrieves an ssh public key file from the MS and stores it in the location provided in the parameter localKeyFilePath
     *
     * @param localKeyFilePath - Path where the keyFile will be stored on local machine
     * @return boolean - returns true if file is successfully copied
     */
    public boolean getKeyfileFromMs(String localKeyFilePath) {
        Preconditions.checkArgument(localKeyFilePath != null && !localKeyFilePath.isEmpty());
        return JSchRemoteObjectUtils.getKeyfileFromMs(localKeyFilePath);
    }

    /**
     * Retrieves information from a file in a specified filePath. This is the equivalent of ls -l command.
     *
     * @param filePath - file name and path of the file on the remote machine e.g /var/tmp/receivedFile.txt
     * @return an {@link FileInfo} with informations about the file,
     * that is a wrapper over {@link com.jcraft.jsch.SftpATTRS}
     */
    public FileInfo getInfo(final String filePath) {
        return JSchRemoteObjectUtils.getInfo(filePath, host, user);
    }

    /**
     * Returns the home directory given the user name.
     *
     * @return The home directory associated to that user. A last slash will be already provided at the end of {@code String}.
     */
    public String getHomeDir() {
        return JSchRemoteObjectUtils.getHomeDir(host, user);
    }

    /**
     * Utility method to change permissions of the file, uses password authentication
     *
     * @param filePath - file name and path of the file on the remote machine e.g /var/tmp/receivedFile.txt
     * @param mod      - octal permissions e.g 777, 755
     */
    public void changeMod(final String filePath, final int mod) {
        JSchRemoteObjectUtils.changeMod(filePath, mod, host, user);
    }

    /**
     * Utility method to change permissions of the file, uses public-key authentication
     *
     * @param filePath - file name and path of the file on the remote machine e.g /var/tmp/receivedFile.txt
     * @param mod      - octal permissions e.g 777, 755
     * @param pathToPrivateKeyFile - path to key file to use for authentication
     */
    public void changeModWithSshKeyFile(final String filePath, final int mod, final String pathToPrivateKeyFile){
        JSchRemoteObjectUtils.changeModWitSshKeyFile(filePath, mod, host, user, pathToPrivateKeyFile);
    }

    /**
     * Utility method to copy a file from a local machine to the remote one. This method uses {@link com.ericsson.cifwk.taf.utils.InternalFileFinder}
     * methods to locate the file in the supplied location on the local machine
     *
     * @param localFile          - file name of local file to be copied e.g. localFile.txt
     * @param remoteFileWithPath - file name and path where file will be stored on the remote machine e.g /var/tmp/receivedFile.txt
     * @param initialLocation    - location of local file on file system e.g. C:\\Users\\user\\tmp\\
     * @return boolean - returns true if file is successfully copied
     */
    public boolean copyLocalFileToRemote(final String localFile, final String remoteFileWithPath, final String initialLocation) {
        return copyLocalFileToRemoteWithSshKeyFile(localFile, remoteFileWithPath, initialLocation, null);
    }

    public boolean copyLocalFileToRemote(final String localFile, final String remoteFileWithPath) {
        return copyLocalFileToRemoteWithSshKeyFile(localFile, remoteFileWithPath, "", null);
    }

    /**
     * Utility method to copy a file from a local machine to the remote one. This method uses {@link com.ericsson.cifwk.taf.utils.InternalFileFinder}
     * methods to locate the file in the supplied location on the local machine
     *
     * @param localFile            - file name of local file to be copied e.g. localFile.txt
     * @param remoteFileWithPath   - file name and path where file will be stored on the remote machine e.g /var/tmp/receivedFile.txt
     * @param initialLocation      - location of local file on file system e.g. C:\\Users\\user\\tmp\\
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return boolean - returns true if file is successfully copied
     */
    public boolean copyLocalFileToRemoteWithSshKeyFile(final String localFile, final String remoteFileWithPath, final String initialLocation, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(localFile, remoteFileWithPath, initialLocation);
        final String foundFile = InternalFileFinder.findFile(localFile, initialLocation);
        if (foundFile == null) {
            throw new JSchCLIToolException(String.format("File '%s' not found", localFile));
        }
        return JSchObjectCopy.putFile(foundFile, remoteFileWithPath, host, user, pathToPrivateKeyFile);
    }

    public boolean copyLocalFileToRemoteWithSshKeyFile(final String localFile, final String remoteFileWithPath, final String pathToPrivateKeyFile) {
        return copyLocalFileToRemoteWithSshKeyFile(localFile, remoteFileWithPath, "", pathToPrivateKeyFile);
    }

    /**
     * Utility method to copy a file from a remote machine to the local machine
     *
     * @param remoteFileWithPath - file name and path where file is stored on the remote machine e.g /var/tmp/remoteFile.txt
     * @param localFile          - file name and path to store the copied file on the local machine  e.g. C:\\Users\\user\\localCopyOfFile.txt
     * @return boolean - Returns true if the file is successfully copied
     */
    public boolean copyRemoteFileToLocal(final String remoteFileWithPath, final String localFile) {
        Preconditions.checkNotNull(remoteFileWithPath, localFile);
        return JSchObjectCopy.getFile(remoteFileWithPath, localFile, host, user);
    }

    /**
     * Utility method to copy a file from a remote machine to the local machine
     *
     * @param remoteFileWithPath   - file name and path where file is stored on the remote machine e.g /var/tmp/remoteFile.txt
     * @param localFile            - file name and path to store the copied file on the local machine  e.g. C:\\Users\\user\\localCopyOfFile.txt
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return boolean - Returns true if the file is successfully copied
     */
    public boolean copyRemoteFileToLocalWithSshKeyFile(final String remoteFileWithPath, final String localFile, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(remoteFileWithPath, localFile);
        return JSchObjectCopy.getFile(remoteFileWithPath, localFile, host, user, pathToPrivateKeyFile);
    }

    /**
     * Utility method to copy a file from a remote machine to the local machine
     *
     * @param dirPath - file path where directory is to be created on the remote machine e.g /var/tmp/remoteFile.txt
     */
    public void createRemoteDirectory(final String dirPath) {
        Preconditions.checkNotNull(dirPath);
        JSchRemoteObjectUtils.createRemoteDirectory(host, user, dirPath, null);
    }

    /**
     * Utility method to copy a file from a remote machine to the local machine
     *
     * @param dirPath              - file path where directory is to be created on the remote machine e.g /var/tmp/remoteFile.txt
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     */
    public void createRemoteDirectoryWithSshKeyFile(final String dirPath, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(dirPath);
        JSchRemoteObjectUtils.createRemoteDirectory(host, user, dirPath, pathToPrivateKeyFile);
    }

    /**
     * Utility method to copy a directory from a local machine to the remote machine.
     *
     * @param localDirectory  - contents of this dir are copied to remoteDir
     * @param remoteDirectory - contents are copied to this dir
     * @return boolean - returns true if file is successfully copied
     */
    public boolean copyLocalDirToRemote(final String localDirectory, final String remoteDirectory) {
        Preconditions.checkNotNull(localDirectory, remoteDirectory);
        String localZipFilePath = ensurePathEnding(Paths.get(localDirectory).getParent().toString()) + UUID.randomUUID() + ".tar";
        String remoteZipFilePath = ensurePathEnding(remoteDirectory) + UUID.randomUUID() + ".tar";
        Tar.tar(localDirectory, localZipFilePath);
        JSchObjectCopy.putFile(localZipFilePath, remoteZipFilePath, host, user);
        return JSchRemoteObjectUtils.untarRemoteArchiveAndDelete(host, user, remoteZipFilePath, remoteDirectory);
    }

    /**
     * Utility method to copy a directory from a local machine to the remote machine.
     *
     * @param localDirectory       - the directory to copy to the remote location
     * @param remoteDirectory      - the remote location to copy the directory to
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return boolean - returns true if the directory is successfully copied
     */
    public boolean copyLocalDirToRemoteWithSshKeyFile(final String localDirectory, final String remoteDirectory, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(localDirectory, remoteDirectory, pathToPrivateKeyFile);
        String localZipFilePath = ensurePathEnding(Paths.get(localDirectory).getParent().toString()) + UUID.randomUUID() + ".tar";
        String remoteZipFilePath = ensurePathEnding(remoteDirectory) + UUID.randomUUID() + ".tar";
        Tar.tar(localDirectory, localZipFilePath);
        JSchObjectCopy.putFile(localZipFilePath, remoteZipFilePath, host, user, pathToPrivateKeyFile);
        return JSchRemoteObjectUtils.untarRemoteArchiveAndDelete(host, user, remoteZipFilePath, remoteDirectory, pathToPrivateKeyFile);
    }


    /**
     * Utility method to copy a directory from a remote machine to the local machine.
     *
     * @param remoteDir - contents are copied to this dir
     * @param localDir  - contents of this dir are copied to remoteDir
     * @return boolean - returns true if file is successfully copied
     */
    public boolean copyRemoteDirToLocal(final String remoteDir, final String localDir) {
        String zipFile = JSchRemoteObjectUtils.tarRemoteDirectory(host, user, remoteDir);
        JSchObjectCopy.getFile(ensurePathEnding(remoteDir) + zipFile, ensurePathEnding(localDir) + zipFile, host, user);
        Tar.untar(ensurePathEnding(localDir) + zipFile, localDir);
        return new File(ensurePathEnding(localDir) + zipFile).delete();
    }

    /**
     * Utility method to copy a directory from a remote machine to the local machine.
     *
     * @param remoteDir            - contents are copied to this dir
     * @param localDir             - contents of this dir are copied to remoteDir
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return boolean - returns true if file is successfully copied
     */
    public boolean copyRemoteDirToLocal(final String remoteDir, final String localDir, String pathToPrivateKeyFile) {
        String zipFile = JSchRemoteObjectUtils.tarRemoteDirectory(host, user, remoteDir, pathToPrivateKeyFile);
        JSchObjectCopy.getFile(ensurePathEnding(remoteDir) + zipFile, ensurePathEnding(localDir) + zipFile, host, user, pathToPrivateKeyFile);
        Tar.untar(ensurePathEnding(localDir) + zipFile, localDir);
        return new File(ensurePathEnding(localDir) + zipFile).delete();
    }

    /**
     * Utility method to copy a file from a remote machine to a remote machine
     *
     * @param sourceHost            - Host containing remote file to be copied
     * @param remoteFileSource      - file name and path of the source file on the remote machine e.g /var/tmp/fileToCopy.txt
     * @param destinationHost       - Host remote file will be copied to
     * @param remoteFileDestination - file name and path where file will be stored on the remote machine e.g /var/tmp/recievedFile.txt
     * @return boolean - Returns true if the file is successfully copied
     * calls copyRemoteFileToRemoteViaLocal if unsuccessful
     */
    public boolean copyRemoteFileToRemote(final Host sourceHost, final String remoteFileSource, final Host destinationHost, final String remoteFileDestination) {
        Preconditions.checkNotNull(sourceHost, remoteFileSource, destinationHost, remoteFileDestination);
        if (JSchRemoteObjectUtils.copyRemoteToRemote(sourceHost, remoteFileSource, destinationHost, remoteFileDestination) ||
                copyRemoteFileToRemoteViaLocal(sourceHost, remoteFileSource, destinationHost, remoteFileDestination)) {
            return true;
        } else {
            return copyRemoteFileToRemoteWithCli(sourceHost, remoteFileSource, destinationHost, remoteFileDestination);
        }
    }

    /**
     * Utility method to copy a file from a remote machine to a remote machine
     *
     * @param sourceHost            - Host containing remote file to be copied
     * @param remoteFileSource      - file name and path of the source file on the remote machine e.g /var/tmp/fileToCopy.txt
     * @param pathToPrivateKeyFile  - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @param destinationHost       - Host remote file will be copied to
     * @param remoteFileDestination - file name and path where file will be stored on the remote machine e.g /var/tmp/recievedFile.txt
     * @param pathToPrivateKeyFile1 - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return boolean - Returns true if the file is successfully copied
     * calls copyRemoteFileToRemoteViaLocal if unsuccessful
     */
    public boolean copyRemoteFileToRemoteWithSshKeyFile(final Host sourceHost, final String remoteFileSource, final String pathToPrivateKeyFile, final Host destinationHost, final String remoteFileDestination, final String pathToPrivateKeyFile1) {
        Preconditions.checkNotNull(sourceHost, remoteFileSource, destinationHost, remoteFileDestination);
        if (JSchRemoteObjectUtils.copyRemoteToRemote(sourceHost, remoteFileSource, pathToPrivateKeyFile, destinationHost, remoteFileDestination, pathToPrivateKeyFile1))
            return true;
        else
            return copyRemoteFileToRemoteViaLocal(sourceHost, remoteFileSource, pathToPrivateKeyFile, destinationHost, remoteFileDestination, pathToPrivateKeyFile1);
    }

    private static boolean copyRemoteFileToRemoteViaLocal(Host sourceHost, String remoteFileSource, Host destinationHost, String remoteFileDestination) {
        return copyRemoteFileToRemoteViaLocal(sourceHost, remoteFileSource, null, destinationHost, remoteFileDestination, null);
    }

    private static boolean copyRemoteFileToRemoteViaLocal(Host sourceHost, String remoteFileSource, String pathToPrivateKeyFile, Host destinationHost, String remoteFileDestination, String pathToPrivateKeyFile1) {
        Preconditions.checkNotNull(sourceHost, remoteFileSource, destinationHost, remoteFileDestination);
        File tempFile, renamedFile = null;
        String tempString = "/";
        String stringTemp = "\\";
        boolean result = false;
        try {
            tempFile = File.createTempFile("tmpFile", "tmp");
            if(System.getProperty("os.name").startsWith("Windows")){
                tempString = "\\";
            }
            String tmpFileAbsolutePath = tempFile.getAbsolutePath();
            int index = tmpFileAbsolutePath.lastIndexOf(tempString);
            String  tmpFileExactName = tmpFileAbsolutePath.substring(index + 1);
            if(remoteFileSource.contains("/")){
                stringTemp  = "/";
            }
            int index1 = remoteFileSource.lastIndexOf(stringTemp);
            String remoteFileExactName = remoteFileSource.substring(index1 + 1);
            String newFileFullPath = tmpFileAbsolutePath.replace(tmpFileExactName, "");
            String newFileName  = newFileFullPath+remoteFileExactName;
            renamedFile = new File(newFileName );
            if (JSchObjectCopy.getFile(remoteFileSource, renamedFile.getAbsolutePath(), sourceHost, sourceHost.getDefaultUser(), pathToPrivateKeyFile))
                result = JSchObjectCopy.putFile(renamedFile.getAbsolutePath(), remoteFileDestination, destinationHost, destinationHost.getDefaultUser(), pathToPrivateKeyFile1);
        } catch (IOException e) {
            LOGGER.error("IO Exception while copying file from {} to {} via local", sourceHost, destinationHost);
            throw Throwables.propagate(e);
        } finally {
            if (renamedFile.exists())
                renamedFile.delete();
        }
        return result;
    }



    @VisibleForTesting
    public boolean copyRemoteFileToRemoteWithCli(Host sourceHost, String remoteFileSource, Host destinationHost, String remoteFileDestination) {
        return JSchRemoteObjectUtils.copyRemoteFileToRemoteWithCli(sourceHost, remoteFileSource, destinationHost, remoteFileDestination);
    }

    /**
     * Utility method to delete a file on a remote host
     *
     * @param filePath - file name and path of the file on the remote machine  e.g /var/tmp/fileToDelete.txt
     * @return true if file is successfully deleted
     */
    public boolean deleteRemoteFile(final String filePath) {
        Preconditions.checkNotNull(filePath);
        return JSchRemoteObjectUtils.deleteRemoteFileOrFolder(host, user, filePath, null);
    }

    /**
     * Utility method to delete a file on a remote host
     *
     * @param filePath             - file name and path of the file on the remote machine  e.g /var/tmp/fileToDelete.txt
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return true if file is successfully deleted
     */
    public boolean deleteRemoteFileWithSshKeyFile(final String filePath, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(filePath);
        return JSchRemoteObjectUtils.deleteRemoteFileOrFolder(host, user, filePath, pathToPrivateKeyFile);
    }

    /**
     * Utility method to delete a folder on a remote host
     *
     * @param folderPath - name and path of the folder on the remote machine  e.g /var/tmp/directoryToDelete
     * @return true if file is successfully deleted
     */
    public boolean deleteRemoteFolder(final String folderPath) {
        Preconditions.checkNotNull(folderPath);
        return JSchRemoteObjectUtils.deleteRemoteFileOrFolder(host, user, folderPath, null);
    }

    /**
     * Utility method to delete a folder on a remote host
     *
     * @param folderPath           - name and path of the folder on the remote machine  e.g /var/tmp/directoryToDelete
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return true if file is successfully deleted
     */
    public boolean deleteRemoteFolderWithSshKeyFile(final String folderPath, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(folderPath);
        return JSchRemoteObjectUtils.deleteRemoteFileOrFolder(host, user, folderPath, pathToPrivateKeyFile);
    }

    /**
     * Utility method to create a file on a remote host. The file is filled with random characters up to the size specified
     *
     * @param filePath - file name and path of the file on the remote machine e.g /var/tmp/createdFile.txt
     * @param fileSize - Size of file created on remote machine
     * @param type     - type of file created on remote machine
     * @return Returns true if the file is successfully copied
     */
    public boolean createRemoteFile(final String filePath, final Long fileSize, final String type) {
        Preconditions.checkNotNull(filePath, type);
        return JSchRemoteObjectUtils.createRemoteFile(host, user, filePath, fileSize, type, null);
    }

    /**
     * Utility method to create a file on a remote host. The file is filled with random characters up to the size specified
     *
     * @param filePath             - file name and path of the file on the remote machine e.g /var/tmp/createdFile.txt
     * @param fileSize             - Size of file created on remote machine
     * @param type                 - type of file created on remote machine
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return Returns true if the file is successfully copied
     */
    public boolean createRemoteFileWithSshKeyFile(String filePath, Long fileSize, String type, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(filePath, type);
        return JSchRemoteObjectUtils.createRemoteFile(host, user, filePath, fileSize, type, pathToPrivateKeyFile);
    }

    /**
     * Utility method to check if a specified file exists on a remote host
     *
     * @param filePath - file name and path of the file on the remote machine e.g /var/tmp/fileToCheck.txt
     * @return true if file exists
     */
    public boolean remoteFileExists(final String filePath) {
        Preconditions.checkNotNull(filePath);
        return JSchRemoteObjectUtils.remoteFileExists(host, user, filePath, null);
    }

    /**
     * Utility method to check if a specified file exists on a remote host
     *
     * @param filePath             - file name and path of the file on the remote machine e.g /var/tmp/fileToCheck.txt
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return true if file exists
     */
    public boolean remoteFileExistsWithSshKeyFile(final String filePath, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(filePath);
        return JSchRemoteObjectUtils.remoteFileExists(host, user, filePath, pathToPrivateKeyFile);
    }

    /**
     * Utility method to check if a file/files exist that match a pattern
     *
     * @param filePathWithWildCard - pattern to check for files that exist with name/path matching the pattern
     * @return List of files that match the pattern search
     */
    public List<String> remoteFileExistsWithWildCard(String filePathWithWildCard) {
        return JSchRemoteObjectUtils.remoteFileExistsWithWildCard(host, user, filePathWithWildCard, null);
    }

    /**
     * Utility method to check if a file/files exist that match a pattern
     *
     * @param filePathWithWildCard - pattern to check for files that exist with name/path matching the pattern
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return List of files that match the pattern search
     */
    public List<String> remoteFileExistsWithWildCardWithSshKeyFile(String filePathWithWildCard, final String pathToPrivateKeyFile) {
        return JSchRemoteObjectUtils.remoteFileExistsWithWildCard(host, user, filePathWithWildCard, pathToPrivateKeyFile);
    }

    /**
     * Utility method to check if a specified folder exists on a remote host
     *
     * @param folderPath - name and path of the folder on the remote machine  e.g /var/tmp/directoryToDelete
     * @return true if file exists
     */
    public boolean remoteFolderExists(final String folderPath) {
        Preconditions.checkNotNull(folderPath);
        return JSchRemoteObjectUtils.remoteFolderExists(host, user, folderPath, null);
    }

    /**
     * Utility method to check if a specified folder exists on a remote host
     *
     * @param folderPath           - name and path of the folder on the remote machine  e.g /var/tmp/directoryToDelete
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return true if file exists
     */
    public boolean remoteFolderExistsWithSshKeyFile(final String folderPath, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(folderPath);
        return JSchRemoteObjectUtils.remoteFolderExists(host, user, folderPath, pathToPrivateKeyFile);
    }

    /**
     * Utility method to retrieve a files file size and md5 checksum from a remote host
     *
     * @param filePath - file name and path of the file on the remote machine  e.g /var/tmp/fileToCheck.txt
     * @return FileStructure consisting of the files md5 checksum and size
     */
    public FileStructure getRemoteFileInformation(final String filePath) {
        Preconditions.checkNotNull(filePath);
        return JSchRemoteObjectUtils.getRemoteFileInformation(host, user, filePath, null);
    }

    /**
     * Utility method to retrieve a files file size and md5 checksum from a remote host
     *
     * @param filePath             - file name and path of the file on the remote machine  e.g /var/tmp/fileToCheck.txt
     * @param pathToPrivateKeyFile - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return FileStructure consisting of the files md5 checksum and size
     */
    public FileStructure getRemoteFileInformationWithSshKeyFile(final String filePath, final String pathToPrivateKeyFile) {
        Preconditions.checkNotNull(filePath);
        return JSchRemoteObjectUtils.getRemoteFileInformation(host, user, filePath, pathToPrivateKeyFile);
    }

    /**
     * Utility method to compare two files on two different remote hosts by evaluating their md5 checksum and file size
     *
     * @param sourceHost     - Remote host where file is located
     * @param sourceFile     - file name and path of the file on the remote machine  e.g /var/tmp/fileToCheck.txt
     * @param comparisonHost - Remote host where second file is located
     * @param comparisonFile - file name and path where second file is on the remote machine e.g /var/tmp/comparisonFile.txt
     * @return true if files match
     */
    public boolean compareRemoteFiles(final Host sourceHost, final String sourceFile, final Host comparisonHost, final String comparisonFile) {
        Preconditions.checkNotNull(sourceHost, sourceFile, comparisonHost, comparisonFile);
        LOGGER.trace("Comparing " + sourceFile + " on host " + sourceHost + " with " + comparisonFile + " on host " + comparisonHost);
        User sourceHostUser = sourceHost.getDefaultUser();
        if (!JSchRemoteObjectUtils.remoteFileExists(sourceHost, sourceHostUser, sourceFile, null)) {
            throw new RemoteFileNotFoundException(sourceHost, sourceFile);
        }
        User comparisonHostUser = comparisonHost.getDefaultUser();
        if (!JSchRemoteObjectUtils.remoteFileExists(comparisonHost, comparisonHostUser, comparisonFile, null)) {
            throw new RemoteFileNotFoundException(comparisonHost, comparisonFile);
        }
        final FileStructure remoteFile1Info = JSchRemoteObjectUtils.getRemoteFileInformation(sourceHost, sourceHostUser, sourceFile, null);
        final FileStructure remoteFile2Info = JSchRemoteObjectUtils.getRemoteFileInformation(comparisonHost, comparisonHostUser, comparisonFile, null);
        if (!compareMd5(remoteFile1Info, remoteFile2Info)) {
            return false;
        }
        return compareFilesize(remoteFile1Info, remoteFile2Info);
    }

    /**
     * Utility method to compare two files on two different remote hosts by evaluating their md5 checksum and file size
     *
     * @param sourceHost           - Remote host where file is located
     * @param sourceFile           - file name and path of the file on the remote machine  e.g /var/tmp/fileToCheck.txt
     * @param sourceSshKeyFile     - location of private key file on the local file system used to connect to host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @param comparisonHost       - Remote host where second file is located
     * @param comparisonFile       - file name and path where second file is on the remote machine e.g /var/tmp/comparisonFile.txt
     * @param comparisonSshKeyFile - location of private key file on the local file system used to connect to the comparison host e.g. C:\\Users\\user\\.ssh\\vm_private_key
     * @return true if files match
     */
    public boolean compareRemoteFilesWithSshKeyFile(final Host sourceHost, final String sourceFile, final String sourceSshKeyFile,
                                                    final Host comparisonHost, final String comparisonFile, final String comparisonSshKeyFile) {
        Preconditions.checkNotNull(sourceHost, sourceFile, sourceSshKeyFile, comparisonHost, comparisonFile, comparisonSshKeyFile);
        LOGGER.trace("Comparing " + sourceFile + " on host " + sourceHost + " with " + comparisonFile + " on host " + comparisonHost);
        User sourceHostUser = sourceHost.getDefaultUser();
        if (!JSchRemoteObjectUtils.remoteFileExists(sourceHost, sourceHostUser, sourceFile, sourceSshKeyFile)) {
            throw new RemoteFileNotFoundException(sourceHost, sourceFile);
        }
        User comparisonHostUser = comparisonHost.getDefaultUser();
        if (!JSchRemoteObjectUtils.remoteFileExists(comparisonHost, comparisonHostUser, comparisonFile, comparisonSshKeyFile)) {
            throw new RemoteFileNotFoundException(comparisonHost, comparisonFile);
        }
        final FileStructure remoteFile1Info = JSchRemoteObjectUtils.getRemoteFileInformation(sourceHost, sourceHostUser, sourceFile, sourceSshKeyFile);
        final FileStructure remoteFile2Info = JSchRemoteObjectUtils.getRemoteFileInformation(comparisonHost, comparisonHostUser, comparisonFile, comparisonSshKeyFile);
        if (!compareMd5(remoteFile1Info, remoteFile2Info)) {
            return false;
        }
        return compareFilesize(remoteFile1Info, remoteFile2Info);
    }

    private static boolean compareMd5(FileStructure fileInfo1, FileStructure fileInfo2) {
        return StringUtils.equals(fileInfo1.md5, fileInfo2.md5);
    }

    private static boolean compareFilesize(final FileStructure fileInfo1, final FileStructure fileInfo2) {
        return StringUtils.equals(fileInfo1.filesize, fileInfo2.filesize);
    }

    protected static String ensurePathEnding(String path) {
        return path.endsWith(fileSeparator) ? path : path + fileSeparator;
    }
}
