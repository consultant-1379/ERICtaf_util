package com.ericsson.cifwk.taf.tools.cli.handlers.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.tools.cli.jsch.JSchSession.JSCH_SESSION_CONNECT_TIMEOUT_PROPERTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLITool;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Helper class to put connection mechanism delivered by JSch library separate to command handling
 */
public final class JSchConnectionsHelper {

    static int CONNECT_TIMEOUT_FIVE_SECOND = (int) TimeUnit.MILLISECONDS.toSeconds(TafConfigurationProvider
            .provide().getInt(
            JSCH_SESSION_CONNECT_TIMEOUT_PROPERTY,
            JSchCLITool
                    .DEFAULT_TIMEOUT_SEC));
    static final String STRICT_HOST_KEY_CHECKING_NEGATIVE_VALUE = "no";
    static final String STRICT_HOST_KEY_CHECKING_KEY = "StrictHostKeyChecking";

    private final static Logger LOGGER = LoggerFactory.getLogger(JSchConnectionsHelper.class);
    private final static String preferredAuthentications = "publickey,password,keyboard-interactive";

    static int getRemoteFileWithSCP(final String remoteFile, final String localFile, final Session session,
                                    final String command) throws JSchException, IOException {
        LOGGER.debug("scp get file {}, to location {}", localFile, remoteFile);

        String prefix = null;
        if (new File(localFile).isDirectory()) {
            prefix = localFile + File.separator;
        }
        final Channel scpChannel = session.openChannel("exec");
        ((ChannelExec) scpChannel).setCommand(command);

        final OutputStream outPutStream = scpChannel.getOutputStream();
        final InputStream inputStream = scpChannel.getInputStream();
        scpChannel.connect();

        byte[] buffer = writeStream(outPutStream);

        writeFile(localFile, prefix, scpChannel, outPutStream, inputStream, buffer);
        return scpChannel.getExitStatus();
    }

    private static void writeFile(final String localFile, String prefix, final Channel scpChannel,
                                  final OutputStream outPutStream, final InputStream inputStream, byte[] buffer) throws IOException {
        FileOutputStream fileOutputStream = null;
        while (true) {
            final int check = checkAck(inputStream);
            if (check != 'C') {
                break;
            }
            inputStream.read(buffer, 0, 5);
            long filesize = 0L;
            filesize = readStream(inputStream, buffer, filesize);
            String file = writeFile(inputStream, buffer);
            buffer[0] = 0;
            outPutStream.write(buffer, 0, 1);
            outPutStream.flush();

            fileOutputStream = new FileOutputStream(prefix == null ? localFile : prefix + file);
            writeOutputStream(fileOutputStream, inputStream, buffer, filesize);
            fileOutputStream.close();
            if (checkAck(inputStream) != 0) {
                scpChannel.getExitStatus();
            }
            buffer[0] = 0;
            outPutStream.write(buffer, 0, 1);
            outPutStream.flush();
        }
    }

    private static long writeOutputStream(final FileOutputStream fileOutputStream, final InputStream inputStream, byte[] buffer, long filesize)
            throws IOException {
        int foo;
        while (true) {
            if (buffer.length < filesize) {
                foo = buffer.length;
            } else {
                foo = (int) filesize;
            }
            foo = inputStream.read(buffer, 0, foo);
            if (foo < 0) {
                break;
            }
            fileOutputStream.write(buffer, 0, foo);
            filesize -= foo;
            if (filesize == 0L) {
                break;
            }
        }
        return filesize;
    }

    private static long readStream(final InputStream inputStream, byte[] buffer, long filesize) throws IOException {
        while (true) {
            if (inputStream.read(buffer, 0, 1) < 0) {
                break;
            }
            if (buffer[0] == ' ') {
                break;
            }
            filesize = filesize * 10L + buffer[0] - '0';
        }
        return filesize;
    }

    private static byte[] writeStream(final OutputStream outPutStream) throws IOException {
        byte[] buffer = new byte[1024];
        buffer[0] = 0;
        outPutStream.write(buffer, 0, 1);
        outPutStream.flush();
        return buffer;
    }

    private static String writeFile(InputStream inputStream, byte[] buffer) throws IOException {
        String file;
        for (int i = 0; ; i++) {
            inputStream.read(buffer, i, 1);
            if (buffer[i] == (byte) 0x0a) {
                file = new String(buffer, 0, i);
                break;
            }
        }
        return file;
    }

    static int putRemoteFileWithSCP(String localFile, String remoteFile, Host host, User user, String pathToPrivateKeyFile) throws IOException {
        LOGGER.debug("Putting file {}, to location {} using scp", localFile, remoteFile);
        final JSch jsch = new JSch();
        Session session;
        Channel scpChannel;
        FileInputStream fileInputStream;
        OutputStream outPutStream;
        InputStream inputStream;
        String command;
        try {
            if (pathToPrivateKeyFile != null)
                jsch.addIdentity(pathToPrivateKeyFile);
            session = jsch.getSession(user.getUsername(), host.getIp(), host.getPort(Ports.SSH));
            setUpSession(session, user.getPassword());
            command = "scp -p -t " + remoteFile;
            scpChannel = session.openChannel("exec");
            ((ChannelExec) scpChannel).setCommand(command);
            outPutStream = scpChannel.getOutputStream();
            inputStream = scpChannel.getInputStream();
            scpChannel.connect();
        } catch (JSchException e) {
            LOGGER.error("Unable to connect to host {}@{}", user, host);
            return -1;
        }
        final File file = new File(localFile);
        generateCommand(localFile, true, scpChannel, outPutStream, inputStream, file);

        fileInputStream = new FileInputStream(localFile);
        writeOutToFile(fileInputStream, outPutStream);
        cleanStreamsAndCloseSession(session, fileInputStream, inputStream, scpChannel, outPutStream);
        int status = scpChannel.getExitStatus();
        LOGGER.debug("Exiting with status {}", status);
        return status;
    }

    private static void generateCommand(final String localFile, final boolean timestamp, final Channel scpChannel, final OutputStream outPutStream,
                                        final InputStream inputStream, final File file) throws IOException {
        String command;
        if (timestamp) {
            command = "T " + (file.lastModified() / 1000) + " 0";
            command += (" " + (file.lastModified() / 1000) + " 0\n");
            outPutStream.write(command.getBytes());
            outPutStream.flush();
            if (checkAck(inputStream) != 0) {
                scpChannel.getExitStatus();
            }
        }
        long fileSize = file.length();
        command = "C0644 " + fileSize + " ";
        if (localFile.lastIndexOf('/') > 0) {
            command += localFile.substring(localFile.lastIndexOf('/') + 1);
        } else {
            command += localFile;
        }
        command += "\n";
        outPutStream.write(command.getBytes());
        outPutStream.flush();
        if (checkAck(inputStream) != 0) {
            scpChannel.getExitStatus();
        }
    }

    private static void writeOutToFile(FileInputStream fileInputStream, OutputStream outPutStream) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int length = fileInputStream.read(buffer, 0, buffer.length);
            if (length <= 0) {
                break;
            }
            for (int i = 0; i < length; i++) {
                LOGGER.trace("writing  :{}", buffer[i]);
            }
            outPutStream.write(buffer, 0, length);
        }
    }

    private static void cleanStreamsAndCloseSession(Session session, FileInputStream fileInputStream, InputStream inputStream, Channel scpChannel, OutputStream outPutStream)
            throws IOException {
        if (fileInputStream != null)
            fileInputStream.close();
        if (inputStream != null)
            inputStream.close();
        outPutStream.flush();
        outPutStream.close();
        scpChannel.disconnect();
        session.disconnect();
    }

    private static int checkAck(InputStream inputStream) throws IOException {
        int check = inputStream.read();
        if (check == 0 || check == -1) return check;
        if (check == 1 || check == 2) {
            StringBuilder sb = new StringBuilder();
            int input;
            do {
                input = inputStream.read();
                sb.append((char) input);
            }
            while (input != '\n');
        }
        return check;
    }

    public static Session getSession(Host host, JSch jsch) {
        return getSessionInternal(host, host.getUser(), host.getPass(), jsch, null);
    }

    public static Session getSession(Host host, JSch jsch, String pathToPrivateKeyFile) {
        return getSessionInternal(host, host.getUser(), host.getPass(), jsch, pathToPrivateKeyFile);
    }

    @API(Internal)
    public static Session getSession(Host host, User user, JSch jsch) {
        return getSessionInternal(host, user.getUsername(), user.getPassword(), jsch, null);
    }

    @API(Internal)
    public static Session getSession(Host host, User user, JSch jsch, String pathToPrivateKeyFile) {
        return getSessionInternal(host, user.getUsername(), user.getPassword(), jsch, pathToPrivateKeyFile);
    }

    private static Session getSessionInternal(Host host, String user, String pass, JSch jsch, String pathToPrivateKeyFile) {
        String ip = host.getIp();
        int port = getPort(host);
        Session session = null;
        try {
            if (pathToPrivateKeyFile != null)
                jsch.addIdentity(pathToPrivateKeyFile);
            session = jsch.getSession(user, ip, port);
            setUpSession(session, pass);
        } catch (JSchException e) {
            LOGGER.error("Unable to connect to host {}@{}:{} due to the following error: {}", user, ip, port, e);
            return session;
        }
        return session;
    }

    public static int getPort(Host host) {
        String port = host.getPort().get(Ports.SSH);
        try {
            if (StringUtils.isBlank(port)) {
                LOGGER.error("SSH port is not set");
                throw new RuntimeException("SSH port is not set");
            }
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            LOGGER.error("SSH port {} is malformed", port);
            throw new RuntimeException("SSH port is malformed", e);
        }
    }

    private static void setUpSession(final Session session, final String password) throws JSchException {
        session.setPassword(password);
        session.setConfig(STRICT_HOST_KEY_CHECKING_KEY, STRICT_HOST_KEY_CHECKING_NEGATIVE_VALUE);
        session.setConfig("PreferredAuthentications", preferredAuthentications);
        session.setDaemonThread(true);
        session.connect(CONNECT_TIMEOUT_FIVE_SECOND);
    }
}
