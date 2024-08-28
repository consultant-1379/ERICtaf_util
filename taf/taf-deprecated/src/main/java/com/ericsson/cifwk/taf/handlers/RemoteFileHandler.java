package com.ericsson.cifwk.taf.handlers;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.exceptions.RemoteFileNotFoundException;
import com.ericsson.cifwk.taf.utils.FileStructure;
import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import com.ericsson.cifwk.taf.utils.RemoteFilesUtils;
import com.ericsson.cifwk.taf.utils.ssh.J2SshFileCopy;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler}
 */
@API(API.Quality.Deprecated)
@API.Since(2.17)
@Deprecated
public class RemoteFileHandler {

    private static Logger logger = LoggerFactory
            .getLogger(RemoteFileHandler.class);
    private final Host host;
    private String user;
    private String pass;

    /**
     * Instantiates the <code>RemoteFileHandler</code> object with given
     * <code>host</code>
     * This uses the default user from the host object to connect to the host with.
     *
     * @param host host
     */
    public RemoteFileHandler(final Host host) {
        this.host = host;
        setUser(null);
    }

    /**
     * Instantiates the <code>RemoteFileHandler</code> object with given
     * <code>host</code> and <code>user</code>
     * This uses the user passed to connect to the host with.
     *
     * @param host host
     * @param user user
     */
    public RemoteFileHandler(final Host host, final User user) {
        this(host);
        setUser(user);
    }
    
    /**
     * Get the user for this object
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#getUser()}
     *
     * @return user
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public String getUser() {
        return user;
    }

    /**
     * Get the password for this object
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#getPass()}
     *
     * @return password
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public String getPass() {
        return pass;
    }

    /**
     * Set the user/pass to the appropriate values.
     */
    private void setUser(User userObj) {
        if(userObj == null){
            this.user = host.getUser();
            this.pass = host.getPass();
        } else {
            this.user = userObj.getUsername();
            this.pass = userObj.getPassword();
        }
    }

    /**
     * Static Utility method to compare 2 files on 2 different remote hosts by
     * evaluating their md5 checksum and file size
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#compareRemoteFiles(com.ericsson.cifwk.taf.data.Host, String, com.ericsson.cifwk.taf.data.Host, String)}
     *
     * @return true if files match, false otherwise
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public static boolean compareRemoteFiles(final Host host1,
            final String filePath1, final Host host2, final String filePath2) {

        if (logger.isTraceEnabled()) {
            logger.trace("Comparing " + filePath1 + " on host " + host1
                    + " with " + filePath2 + " on host " + host2);
        }
        if (!RemoteFilesUtils.remoteFileExists(host1.getIp(), host1.getPort()
                .get(Ports.SSH), host1.getUser(), host1
                .getPass(), filePath1)) {
            throw new RemoteFileNotFoundException(host1, filePath1);
        }
        if (!RemoteFilesUtils.remoteFileExists(host2.getIp(), host2.getPort()
                .get(Ports.SSH), host2.getUser(), host2
                .getPass(), filePath2)) {
            throw new RemoteFileNotFoundException(host2, filePath2);
        }

        FileStructure remoteFile1Info = RemoteFilesUtils
                .getRemoteFileInformation(host1.getIp(),
                        host1.getPort().get(Ports.SSH),
                        host1.getUser(),
                        host1.getPass(), filePath1);
        FileStructure remoteFile2Info = RemoteFilesUtils
                .getRemoteFileInformation(host2.getIp(),
                        host2.getPort().get(Ports.SSH),
                        host2.getUser(),
                        host2.getPass(), filePath2);

        if (!compareMd5(remoteFile1Info, remoteFile2Info)) {
            return false;
        }

        return compareFilesize(remoteFile1Info, remoteFile2Info);
    }

    /**
     * Private method to compare the md5 field of 2 filestructures
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#compareMd5(com.ericsson.cifwk.taf.utils.FileStructure, com.ericsson.cifwk.taf.utils.FileStructure)}
     *
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    private static boolean compareMd5(FileStructure fileInfo1,
            FileStructure fileInfo2) {
        return StringUtils.equals(fileInfo1.md5, fileInfo2.md5);
    }

    /**
     * Private method to compare the filesize field of 2 filestructures
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#compareFilesize(com.ericsson.cifwk.taf.utils.FileStructure, com.ericsson.cifwk.taf.utils.FileStructure)}
     *
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    private static boolean compareFilesize(FileStructure fileInfo1,
            FileStructure fileInfo2) {
        return StringUtils.equals(fileInfo1.filesize, fileInfo2.filesize);
    }

    /**
     * Utility method to check if a specified file exists on a remote host
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#remoteFileExists(String, String)}
     *
     *
     * @return true if file exists, false otherwise
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean remoteFileExists(final String filePath) {
        return RemoteFilesUtils.remoteFileExists(host.getIp(), host.getPort()
                .get(Ports.SSH), user, pass, filePath);
    }

    /**
     * Utility method to copy a file from a remote machine to the local machine
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#copyRemoteFileToLocal(String, String)}
     *
     * @exception An
     *                SFTP failed to start exception may occur if the user is
     *                sending multiple files This is due to the server having a
     *                limited number of Sessions in the ssh configuration. By
     *                default this value is 10. To change this value on the
     *                server please see link below
     *                http://confluence-oss.lmera.ericsson
     *                .se/display/TAF/RemoteFileHandler
     * 
     * @param remoteFile
     *            - file name and path of the file on the remote machine
     * @param localFile
     *            - file name and path to store the file on the local machine
     * @return boolean - did the operation succeed or not.
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean copyRemoteFileToLocal(final String remoteFile,
            final String localFile) {
        return J2SshFileCopy.getFile(remoteFile, localFile, host.getIp(), user,
                pass, host.getPort().get(Ports.SSH));
    }

    /**
     * Utility method to copy a file from a local machine to the remote machine
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#copyLocalFileToRemote(String,
     *      String, String)}
     *
     * @exception An
     *                SFTP failed to start exception may occur if the user is
     *                sending multiple files This is due to the server having a
     *                limited number of Sessions in the ssh configuration. By
     *                default this value is 10. To change this value on the
     *                server please see link below
     *                http://confluence-oss.lmera.ericsson
     *                .se/display/TAF/RemoteFileHandler
     * 
     * @param localFile
     *            - file name
     * @param remoteFile
     *            - file name and path of the file on the remote machine
     * @param initialLocation
     *            - location on filesystem to start the search
     * @return boolean - did the operation succeed or not.
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean copyLocalFileToRemote(final String localFile,
            final String remoteFile, final String initialLocation) {
        String foundFile = InternalFileFinder.findFile(localFile, initialLocation);
        if (foundFile == null) {
            throw new RuntimeException(String.format("File '%s' not found", localFile));
        }
        return J2SshFileCopy.putFile(foundFile, remoteFile, host.getIp(), user, pass, host.getPort().get(Ports.SSH));
    }

    /**
     * @see com.ericsson.cifwk.taf.handlers.RemoteFileHandler#copyLocalFileToRemote(String,
     *      String, String)
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#copyLocalFileToRemote(String, String)}
     *
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean copyLocalFileToRemote(final String localFile,
            final String remoteFile) {
        return copyLocalFileToRemote(localFile, remoteFile, "");
    }

    /**
     * Utility method to create a file on a remote host. The file is filled with
     * random characters up to the size specified
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#createRemoteFile(String, Long, String)}
     *
     * @return true if file is successfully created, false otherwise
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean createRemoteFile(final String filePath, final Long fileSize,
            final String sizeType) {
        return RemoteFilesUtils.createRemoteFile(host.getIp(), host.getPort()
                .get(Ports.SSH), user, pass, filePath, fileSize, sizeType);
    }

    /**
     * Utility method to delete a file on a remote host
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#deleteRemoteFile(String)}
     *
     * @return true if file is successfully deleted, false otherwise
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean deleteRemoteFile(final String filePath) {
        return RemoteFilesUtils.deleteRemoteFile(host.getIp(), host.getPort()
                .get(Ports.SSH), user, pass, filePath);
    }

    /**
     * Utility method to copy a file on a remote host
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#copyLocalFileToRemote(String, String)}
     *
     * @param sourceFilePath
     *            source remote location
     * @param targetFilePath
     *            target remote location
     * @return true if file is successfully copied, false otherwise
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public boolean copyRemoteFile(final String sourceFilePath,
            final String targetFilePath) {
        return RemoteFilesUtils.copyRemoteFile(host.getIp(), host.getPort()
                .get(Ports.SSH), user, pass, sourceFilePath, targetFilePath);
    }

    /**
     * Utility method to retrieve file information from a remote host
     *
     * @deprecated use {@link com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler#getRemoteFileInformation(String)}
     *
     * @return FileStructure object consisting of the files md5 checksum and
     *         size
     */
    @API(API.Quality.Deprecated)
    @API.Since(2.17)
    @Deprecated
    public FileStructure getRemoteFileInformation(final String filePath) {
        return RemoteFilesUtils.getRemoteFileInformation(host.getIp(), host
                .getPort().get(Ports.SSH), user, pass, filePath);
    }

}