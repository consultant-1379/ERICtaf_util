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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

class JSchObjectCopy {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSchObjectCopy.class);

    static boolean putFile(final String localFile, final String remoteFile, final Host host, User user) {
        return putFile(localFile, remoteFile, host, user, null);
    }

    static boolean putFile(final String localFile, final String remoteFile, final Host host, User user, String pathToPrivateKeyFile) {
        JSch jsch = new JSch();
        Session session = null;
        boolean result = true;
        ChannelSftp sftpChannel = null;
        int scpResult;
        try {
            session = JSchConnectionsHelper.getSession(host, user, jsch, pathToPrivateKeyFile);
            if(session.isConnected()) {
                sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();
            }
            if (sftpChannel != null && sftpChannel.isConnected()) {
                createFoldersIfNotPresent(sftpChannel, remoteFile);
                sftpChannel.put(localFile, remoteFile);
            } else {
                LOGGER.debug("Error connecting to server with sFTP, trying scp connection");
                scpResult = JSchConnectionsHelper.putRemoteFileWithSCP(localFile, remoteFile, host, user, pathToPrivateKeyFile);
                return scpResult != -1;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to connect to host: {}@{}", user, host, e);
            result = false;
        } finally {
            if(sftpChannel != null && (sftpChannel.isConnected() || !sftpChannel.isClosed())) {
                    sftpChannel.disconnect();
                    sftpChannel.quit();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return result;
    }

    static boolean getFile(String remoteFile, String localFile, Host host, User user) {
        return getFile(remoteFile, localFile, host, user, null);
    }

    static boolean getFile(String remoteFile, String localFile, Host host, User user, String pathToPrivateKeyFile) {
        final JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;
        boolean result = true;
        BufferedInputStream inputStream;
        try {
            session = JSchConnectionsHelper.getSession(host, user, jsch, pathToPrivateKeyFile);
            if(session.isConnected()) {
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
            }
            if (channel != null && channel.isConnected()) {
                inputStream = new BufferedInputStream(channel.get(remoteFile));
                final File newFile = new File(localFile);
                writeFile(new byte[1024], inputStream, newFile);
                inputStream.close();
            } else {
                LOGGER.debug("Error connecting to server with sFTP, trying an scp connection");
                JSchConnectionsHelper.getRemoteFileWithSCP(remoteFile, localFile, session, "scp -f " + remoteFile);
            }
        } catch (Exception e) {
            LOGGER.error("Error retrieving file: ", e);
            result = false;
        } finally {
            if(channel != null) {
                channel.disconnect();
                channel.quit();
                session.disconnect();
            }
        }
        return result;
    }

    private static void writeFile(final byte[] buffer, final BufferedInputStream inputStream, final File newFile) throws FileNotFoundException {
        final OutputStream outputStream = new FileOutputStream(newFile);
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        int readCount;
        try {
            while ((readCount = inputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, readCount);
            }
            inputStream.close();
            bufferedOutputStream.close();
        } catch (IOException e) {
            LOGGER.error("Error writing to file: ", e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                LOGGER.error("Error closing stream: ", e);
            }
        }
    }

    private static void createFoldersIfNotPresent(ChannelSftp channelSftp, String remoteFile) {
        LOGGER.debug("Creating all folders in {} if they don't exit", remoteFile);
        int finalSeparatorLocation = remoteFile.lastIndexOf("/");
        String folderPath = remoteFile.substring(0, finalSeparatorLocation);
        LOGGER.debug("Checking if {} exists", folderPath);
        try {
            String originalLocation = channelSftp.pwd();
            try {
                channelSftp.cd(folderPath);
                LOGGER.debug("Folder structure exists so no creation needed");
                channelSftp.cd(originalLocation);
            } catch (SftpException e) {
                LOGGER.debug("Folder structure does not exist so creating");
                channelSftp.mkdir(folderPath);
                channelSftp.cd(folderPath);
                LOGGER.debug("Folder structure created successfully");
                channelSftp.cd(originalLocation);
            }
        }catch (SftpException e) {
            LOGGER.debug("Could not create complete folder structure. Attempting to create recursively");
            createFoldersRecursivelyIfNotPresent(channelSftp, folderPath);
        }
    }

    private static void createFoldersRecursivelyIfNotPresent(ChannelSftp channelSftp, String remoteFolder) {
        try {
            String separator = "/";
            String[] folders = remoteFolder.split(separator);
            String originalLocation = channelSftp.pwd();
            if (folders.length > 1) {
                if (folders[0].length() == 0) {
                    folders[1] = separator + folders[1];
                }
                for (String folder : folders) {
                    if (folder.length() > 0) {
                        try {
                            channelSftp.cd(folder);
                        } catch (SftpException e) {
                            LOGGER.debug("Could not cd to folder {} due to {}. Attempting to create folder", folder, e.getLocalizedMessage());
                            channelSftp.mkdir(folder);
                            channelSftp.cd(folder);
                            LOGGER.debug("Folder {} created successfully");
                        }
                    }
                }
                channelSftp.cd(originalLocation);
            }
        } catch (SftpException e) {
            LOGGER.warn("Could not create folder structure for {} due to {}", remoteFolder, e);
        }
    }
}
