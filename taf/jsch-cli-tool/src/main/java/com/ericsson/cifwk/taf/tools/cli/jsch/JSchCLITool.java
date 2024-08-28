package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.taf.tools.TargetHost;
import com.ericsson.cifwk.taf.tools.cli.CLITool;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchShell.ChannelType;
import com.google.common.annotations.VisibleForTesting;
import com.jcraft.jsch.JSch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @see CLITool
 */
public class JSchCLITool implements CLITool {

    public static final int DEFAULT_TIMEOUT_SEC = CLITool.DEFAULT_TIMEOUT_SEC;

    public static final int CONNECTION_RETRY_THEN_ERROR = 5;

    public static final int MAX_RETRY_TIMEOUT_SEC = 600;

    private static final Logger LOGGER = LoggerFactory.getLogger("com.jcraft.jsch.JSch");

    static {
        JSch.setLogger(new com.jcraft.jsch.Logger() {
            @Override
            public boolean isEnabled(int level) {
                switch (level) {
                    case com.jcraft.jsch.Logger.DEBUG:
                        return LOGGER.isDebugEnabled();
                    case com.jcraft.jsch.Logger.INFO:
                        return LOGGER.isInfoEnabled();
                    case com.jcraft.jsch.Logger.WARN:
                        return LOGGER.isWarnEnabled();
                    case com.jcraft.jsch.Logger.ERROR:
                        return LOGGER.isErrorEnabled();
                    case com.jcraft.jsch.Logger.FATAL:
                        return LOGGER.isErrorEnabled();
                    default:
                        return LOGGER.isErrorEnabled();
                }
            }

            @Override
            public void log(int level, String message) {
                switch (level) {
                    case com.jcraft.jsch.Logger.DEBUG:
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(message);
                        }
                        break;
                    case com.jcraft.jsch.Logger.INFO:
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info(message);
                        }
                        break;
                    case com.jcraft.jsch.Logger.WARN:
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn(message);
                        }
                        break;
                    case com.jcraft.jsch.Logger.ERROR:
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(message);
                        }
                        break;
                    case com.jcraft.jsch.Logger.FATAL:
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(message);
                        }
                        break;
                    default:
                        LOGGER.error(message);
                }
            }
        });
    }

    JSchShellPool shellPool;

    @VisibleForTesting
    JSchSessionPool sessionPool;

    public JSchCLITool(JSchSessionPool sessionPool, JSchShellPool shellPool) {
        this.shellPool = shellPool;
        this.sessionPool = sessionPool;
    }

    @Override
    public String openShell(String host, int port, String user, String password, Terminal terminal) {
        return open(host, port, user, password, terminal, ChannelType.SHELL, "");
    }

    @Override
    public String openShell(final String host, final int port, final String user, final Path keyFile, final Terminal terminal) {
        return open(host, port, user, null, terminal, ChannelType.SHELL, keyFile);
    }

    @Override
    public String executeCommand(String host, int port, String user, String password, Terminal terminal, TargetHost tunnelHost, String... commands) {
        return open(host, port, user, password, terminal, ChannelType.EXEC, null, tunnelHost, commands);
    }

    @Override
    public String executeCommand(final String host, final int port, final String user, final Path keyFile, final Terminal terminal,
            final TargetHost tunnelHost, final String... commands) {
        return open(host, port, user, null, terminal, ChannelType.EXEC, keyFile, tunnelHost, commands);
    }

    public String executeCommand(String host, int port, String user, String password, Terminal terminal, Path pathToPrivateKeyFile, String... commands) {
        return open(host, port, user, password, terminal, ChannelType.EXEC, pathToPrivateKeyFile, commands);
    }

    public String open(String host, int port, String user, String password, Terminal terminal, ChannelType type, String... commands) {
        return open(host, port, user, password, terminal, type, null, commands);
    }

    public String open(String host, int port, String user, String password, Terminal terminal, ChannelType type, Path pathToPrivateKeyFile, String... commands) {
        return open(host, port, user, password, terminal, type, pathToPrivateKeyFile, null, commands);
    }

    public String open(String host, int port, String user, String password, Terminal terminal, ChannelType type, Path pathToPrivateKeyFile, TargetHost tunnelHost, String... commands) {
        String sessionId = host + ":" + port + ":" + user;
        JSchSession session = null;
        String shellId = UUID.randomUUID().toString();
        JSchShell shell = null;
        try {
            shell = borrowObject(shellId);
            if (shell.spawn == null) {
                session = sessionPool.borrowObject(sessionId);
                if (session.session == null || !session.session.isConnected()) {
                    String privateKeyFile = pathToPrivateKeyFile != null ? pathToPrivateKeyFile.toString() : null;
                    session.open(host, port, user, password, privateKeyFile, tunnelHost);
                }
                shell.open(session, terminal, type, commands);
            }
        } finally {
            if (session != null) {
                sessionPool.returnObject(sessionId, session);
            }
            if (shell != null) {
                shellPool.returnObject(shellId, shell);
            }
        }
        return shellId;
    }

    public String openTelnet(String host, int port) {
        String shellId = UUID.randomUUID().toString();
        JSchShell shell = null;
        try {
            shell = borrowObject(shellId);
            if (shell.spawn == null) {
                shell.openTelnet(host, port);
            }
        } finally {
            if (shell != null) {
                shellPool.returnObject(shellId, shell);
            }
        }
        return shellId;
    }

    private JSchShell borrowObject(String id) {
        JSchShell shell;
        shell = shellPool.borrowObject(id);
        return shell;
    }

    private void returnObject(String id, JSchShell shell) {
        if (shell != null)
            shellPool.returnObject(id, shell);
    }

    @VisibleForTesting
    public int getOpenSessionsCount() {
        return sessionPool.getNumIdle() + sessionPool.getNumActive();
    }

    @Override
    public void disconnect(String id) {
        JSchShell shell = shellPool.borrowObject(id);
        if (shell != null) {
            shell.disconect();
        }
        shellPool.clear(id);
    }

    @Override
    public void close() {
        shellPool.clear();
        sessionPool.clear();
    }

    @Override
    public void write(String id, String string) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            shell.write(string);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String expect(String id, String subString) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expect(subString);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String expect(String id, String subString, long timeOutSeconds) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expect(subString, timeOutSeconds);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String expect(String id, Pattern pattern) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expect(pattern);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String expect(String id, Pattern pattern, long timeOutSeconds) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expect(pattern, timeOutSeconds);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public boolean expectErr(String id, String subString) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expectErr(subString);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public boolean expectErr(String id, String subString, long timeOutSeconds) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expectErr(subString, timeOutSeconds);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public void expectClose(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            shell.expectClose();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public boolean expectClose(String id, long timeOutSeconds) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.expectClose(timeOutSeconds);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public boolean isClosed(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.isClosed();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public int getExitValue(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.getExitValue();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public int getExitValue(String id, long timeOutSeconds) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.getExitValue(timeOutSeconds);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public void setEnv(String id, String name, String value) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            shell.setEnv(name, value);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String getEnv(String id, String name) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.getEnv(name);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public Map<String, String> getEnv(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.getEnv();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public void unsetEnv(String id, String name) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            shell.unsetEnv(name);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public void resetEnv(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            shell.resetEnv();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String read(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.read();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String read(String id, long timeOutSecond) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.read(timeOutSecond);
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String readErr(String id) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.readErr();
        } finally {
            returnObject(id, shell);
        }
    }

    @Override
    public String readErr(String id, long timeOutSecond) {
        JSchShell shell = null;
        try {
            shell = borrowObject(id);
            return shell.readErr(timeOutSecond);
        } finally {
            returnObject(id, shell);
        }
    }
}
