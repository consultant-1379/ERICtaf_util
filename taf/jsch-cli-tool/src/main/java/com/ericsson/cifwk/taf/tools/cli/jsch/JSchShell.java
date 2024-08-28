package com.ericsson.cifwk.taf.tools.cli.jsch;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Pattern;

import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import expectj.ExpectJException;

/**
 * This class wrap {@link expectj.Spawn} in order to be able to keep it in pool
 * and also is a proxy for access to the {@link expectj.Spawn} methods
 */
public class JSchShell {

    static final int RETRY = 5;

    static final int MAX_TIMEOUT_SEC = 60;

    String id;

    Spawn spawn;

    JSchSession session;

    enum ChannelType {
        SHELL("shell"),
        EXEC("exec");
        final String type;

        ChannelType(String type) {
            this.type = type;
        }
    }

    public JSchShell(String id) {
        this.id = id;
    }

    /**
     * Open SSH shell with given <code>session id</code>,<code>session</code>, <code>terminal</code>, <code>ChannelType</code> and and
     * execute <code>commands</code> on it.
     */
    void open(JSchSession session, Terminal terminal, ChannelType type, String... commands) {
        this.session = session;
        try {
            switch (type) {
                case SHELL:
                    this.spawn = openShell(session, terminal);
                    break;
                case EXEC:
                    this.spawn = executeCommand(session, terminal, commands);
                    break;
                default:
                    String msg = MessageFormat.format("Unknown SSH type:{3} ssh://{2}@{0}:{1}", session.host, session.port, session.user, type);
                    throw new JSchCLIToolException(msg);
            }
        } catch (JSchException e) {
            String msg = MessageFormat.format("Can''t open ssh://{2}@{0}:{1}, throws {3}({4})", session.host, session.port, session.user, e.getClass()
                    .getName(), e.getMessage());
            throw new JSchCLIToolException(msg, e);
        }
    }

    void openTelnet(String host, int port) {
        this.spawn = Spawn.telnetSpawn(host, port, JSchCLITool.DEFAULT_TIMEOUT_SEC);
    }

    Spawn openShell(JSchSession session, Terminal terminal) throws JSchException {
        ChannelShell channel = createChannelShell(session.session, terminal);
        return Spawn.shell(channel, JSchCLITool.DEFAULT_TIMEOUT_SEC);
    }

    Spawn executeCommand(JSchSession session, Terminal terminal, String[] commands) throws JSchException {
        ChannelExec channel = createChannelExec(session.session, terminal, commands);
        return Spawn.exec(channel, JSchCLITool.DEFAULT_TIMEOUT_SEC);
    }

    static ChannelShell createChannelShell(Session session, Terminal terminal) throws JSchException {
        ChannelShell channel = (ChannelShell) session.openChannel(ChannelType.SHELL.type);
        if (terminal != null) {
            channel.setPty(true);
            channel.setPtyType(terminal.getType(), terminal.getColumns(), terminal.getRows(), terminal.getWidth(), terminal.getHeight());
        } else {
            channel.setPty(false);
        }
        return channel;
    }

    static ChannelExec createChannelExec(Session session, Terminal terminal, String... commands) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel(ChannelType.EXEC.type);
        if (terminal != null) {
            channel.setPty(true);
            channel.setPtyType(terminal.getType(), terminal.getColumns(), terminal.getRows(), terminal.getWidth(), terminal.getHeight());
        }
        channel.setCommand(createCommand(commands));
        return channel;
    }

    static String createCommand(String[] commands) {
        StringBuilder command = new StringBuilder();
        for (String cmd : commands) {
            if (command.length() != 0)
                command.append(';');
            command.append(cmd);
        }
        return command.toString();
    }

    public JSchSession getSession() {
        return session;

    }

    /**
     * Stop SSH shell
     */
    public void disconect() {
        if (spawn != null)
            spawn.stop();
    }

    /**
     * Writes a string to the standard input of the shell process.
     */
    public void write(String string) {
        try {
            spawn.send(string);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Wait the default timeout for a sub-string to appear on standard out.
     *
     * @param subString
     *        The case-sensitive substring to match against.
     * @return read standard out
     * @throws TimeoutException
     *         on timeout waiting for pattern
     */
    public String expect(String subString) throws TimeoutException {
        try {
            return spawn.expect(subString);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Wait for a sub-string to appear on standard out.
     *
     * @param subString
     *        The case-sensitive substring to match against.
     * @param timeOutSeconds
     *        The timeout in seconds before the match fails.
     * @return read standard out
     * @throws TimeoutException
     *         on timeout waiting for pattern
     */
    public String expect(String subString, long timeOutSeconds) throws TimeoutException {
        try {
            return spawn.expect(subString, timeOutSeconds);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Wait the default timeout for a pattern to appear on standard out.
     *
     * @param pattern
     *        The pattern to match against.
     * @return read standard out
     * @throws TimeoutException
     *         on timeout waiting for pattern
     */
    public String expect(Pattern pattern) throws TimeoutException {
        try {
            return spawn.expect(pattern);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Wait for a pattern to appear on standard out.
     *
     * @param pattern
     *        The pattern to match against.
     * @param timeOutSeconds
     *        The timeout in seconds before the match fails.
     * @return read standard out
     * @throws TimeoutException
     *         on timeout waiting for pattern
     */
    public String expect(Pattern pattern, long timeOutSeconds) throws TimeoutException {
        try {
            return spawn.expect(pattern, timeOutSeconds);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Wait the default timeout for a sub-string to appear on standard error.
     *
     * @param subString
     *        The case-sensitive substring to match against.
     * @throws TimeoutException
     *         on timeout waiting for pattern
     */
    public boolean expectErr(String subString) throws TimeoutException {
        try {
            spawn.expectErr(subString);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
        return true;
    }

    /**
     * Wait for a subString to appear on standard error.
     *
     * @param subString
     *        The case-sensitive substring to match against.
     * @param timeOutSeconds
     *        The timeout in seconds before the match fails.
     * @throws TimeoutException
     *         on timeout waiting for pattern
     */
    public boolean expectErr(String subString, long timeOutSeconds) throws TimeoutException {
        try {
            spawn.expectErr(subString, timeOutSeconds);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
        return true;
    }

    /**
     * Wait the default timeout for the spawned process to finish.
     *
     * @throws TimeoutException
     *         if the spawn didn't finish inside of the timeout.
     */
    public void expectClose() throws TimeoutException {
        try {
            spawn.expectClose();
        } catch (ExpectJException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Wait for the spawned process to finish.
     *
     * @param timeOutSeconds
     *        The number of seconds to wait before giving up, or -1 to wait
     *        forever.
     * @throws TimeoutException
     *         if the spawn didn't finish inside of the timeout.
     */
    public boolean expectClose(long timeOutSeconds) throws TimeoutException {
        try {
            spawn.expectClose(timeOutSeconds);
        } catch (ExpectJException e) {
            throw new JSchCLIToolException(e);
        }
        return true;
    }

    /**
     * This method can be use use to check the target process status
     *
     * @return true if the process has already exited.
     */
    public boolean isClosed() {
        return spawn.isClosed();
    }

    /**
     * Retrieve the exit code of a finished process
     * or wait passed time out until process finished
     *
     * @return the exit code of the process if the process has already exited.
     * @throws TimeoutException
     *         if the spawn didn't finish inside of the timeout.
     */
    public int getExitValue() throws TimeoutException {
        try {
            return spawn.getExitValue();
        } catch (ExpectJException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Retrieve the exit code of a finished process.
     * or wait passed time out until process finished
     *
     * @param timeOutSeconds
     *        The number of seconds to wait before giving up, or -1 to wait forever.
     * @return the exit code of the process if the process has already exited.
     * @throws TimeoutException
     *         if the spawn didn't finish inside of the timeout.
     */
    public int getExitValue(long timeOutSeconds) throws TimeoutException {
        try {
            return spawn.getExitValue(timeOutSeconds);
        } catch (ExpectJException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Set an environment variable on a session.
     *
     * @param name
     *        environment variable name
     * @param value
     *        environment variable value
     */
    public void setEnv(String name, String value) {
        try {
            spawn.setEnv(name, value);
        } catch (IOException | TimeoutException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Get an environment variable on a session.
     *
     * @param name
     *        environment variable name
     * @return value environment variable value, or null if environment variable
     *         not exist
     */
    public String getEnv(String name) {
        try {
            return spawn.getEnv(name);
        } catch (IOException | TimeoutException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Get an all environment variables on a session.
     *
     * @return map of name-value
     */
    public Map<String, String> getEnv() {
        try {
            return spawn.getEnv();
        } catch (IOException | TimeoutException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Unset the environment variable on a session.
     *
     * @param name
     *        environment variable name
     */
    public void unsetEnv(String name) {
        try {
            spawn.unsetEnv(name);
        } catch (IOException | TimeoutException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Reset all environment variables in the session state that was at the
     * session is opened
     */
    public void resetEnv() {
        spawn.resetEnv();
    }

    /**
     * Read standard out
     */
    public String read() {
        try {
            return spawn.read();
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Read standard out
     *
     * @param timeOutSeconds
     *        The number of seconds to wait before giving up, or -1 to wait
     *        forever.
     */
    public String read(long timeOutSeconds) {
        try {
            return spawn.read(timeOutSeconds);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Read standard Err
     */
    public String readErr() {
        try {
            return spawn.readErr();
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    /**
     * Read standard Err
     *
     * @param timeOutSeconds
     *        The number of seconds to wait before giving up, or -1 to wait
     *        forever.
     */
    public String readErr(long timeOutSeconds) {
        try {
            return spawn.readErr(timeOutSeconds);
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

}
