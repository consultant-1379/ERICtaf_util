package com.ericsson.cifwk.taf.utils.ssh;

import com.ericsson.cifwk.taf.utils.InternalStringUtils;
import com.sshtools.sftp.SftpClient;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.ChannelOpenException;
import com.maverick.ssh.SshException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by erafkos on 31/08/2016.
 */
public class J2SshFileCopy extends J2SshConnectionsHelper {

    private static Logger LOGGER = LoggerFactory.getLogger(J2SshFileCopy.class);

    private static SftpClient getClient(String ip, String user, String port, String pass)
    throws IOException, SshException, SftpStatusException, ChannelOpenException {
        SftpClient result;
        try {
            result = new SftpClient(findConnection(ip, user, port, pass));
        } catch(Exception e){
            LOGGER.debug("Re-using connection failed, due to exception $e. Trying with new connection");
            result = new SftpClient(new J2SshFileCopy().getNewConnection(ip, user, port, pass));
        }
        return result;
    }

    public static boolean putFile(String localFile, String remoteFile, String ip, String user, String pass, String port) {
        SftpClient sftp = null;
        try {
            sftp = getClient(ip, user, port, pass);
            LOGGER.debug("Usig client {}", sftp);
            String[] splitToFolders = remoteFile.split("/");
            sftp.mkdirs(InternalStringUtils.join(ArrayUtils.remove(splitToFolders, splitToFolders.length - 1), "/"));
            LOGGER.debug("Copying {} to {}:{}", localFile, ip, remoteFile);
            sftp.put(localFile, remoteFile, null);
            sftp.quit();
        } catch (Exception e) {
            LOGGER.error("Transfer of {} to {}:{} failed with exception {}", localFile, ip, remoteFile, e);
        } finally {
            if (sftp != null) {
                try {
                    sftp.quit();
                } catch (SshException e) {
                    LOGGER.warn("sftp failed to quit");
                }
            }
        }
        return true;
    }

    public static boolean getFile(String remoteFile,String localFile, String ip, String user, String pass, String port) {
        SftpClient sftp = null;
        boolean failed = false;
        try {
            sftp =  getClient(ip, user, port, pass);
            LOGGER.debug("Using client {}",  sftp);
            LOGGER.debug("Copying {}:{} to {}", ip, remoteFile, localFile);
            sftp.get(remoteFile, localFile, null);
        } catch (Exception e){
            LOGGER.error("Transfer failed with exception", e);
            return false;
        } finally {
            quit(sftp);
        }
        return true;
    }

    private static void quit(SftpClient sftp) {
        try {
            sftp.quit();
        } catch (SshException e) {
            LOGGER.warn("sftp failed to quit");
        }
    }
}
