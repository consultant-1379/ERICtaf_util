package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.tools.TargetHost;
/**
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public interface CLITool {

    int DEFAULT_TIMEOUT_SEC = 5;

    /**
     * Open SSH shell with given <code>host</code>,<code>port</code>, <code>username</code>,
     * <code>password</code> and <code>terminal</code>
     *
     * @return session identifier, which is used for binding to a particular SSH session in CLITool
     */
    String openShell(String host, int port, String user, String password, Terminal terminal);

    /**
     * Open SSH Shell with given <code>host</code>,<code>port</code>, <code>username</code>,
     * <code>keyFile</code> and <code>terminal</code>
     *
     * @param host the host to connect to
     * @param port the port to connect at
     * @param user the user to connect with
     * @param keyFile the keyFile to use for authentication
     * @param terminal the {@link Terminal} type to use
     *
     * @return session identifier, which is used for binding to a particular SSH session in CLITool
     */
    String openShell(String host, int port, String user, Path keyFile, Terminal terminal);

    /**
     * Open Telent shell with given <code>host</code>,<code>port</code>
     *
     * @return session identifier, which is used for binding to a particular Telnet session in CLITool
     */
    String openTelnet(String host, int port);

    /**
     * Open SSH shell with given <code>host</code>,<code>port</code>, <code>username</code>,
     * <code>password</code>, <code>terminal</code> and execute <code>commands</code> on it.
     *
     * @return session identifier, which is used for binding to a particular SSH session in CLITool
     */
    String executeCommand(String host, int port, String user, String password, Terminal terminal, TargetHost tunnelHost, String... commands);

    /**
     * Open SSH shell with given <code>host</code>,<code>port</code>, <code>keyFile</code>,
     * <code>password</code>, <code>terminal</code> and execute <code>commands</code> on it.
     *
     * @return session identifier, which is used for binding to a particular SSH session in CLITool
     */
    String executeCommand(String host, int port, String user, Path keyFile, Terminal terminal, TargetHost tunnelHost, String... commands);

    /**
     * Close and invalidate SSH shell, which is associated with the given parameter
     *
     * @param id session identifier
     */
    void disconnect(String id);

    /**
     * Closes all sessions
     */
    void close();

    /**
     * Writes a string to the standard input of the shell process.
     *
     * @param id     session identifier
     * @param string The string to send.
     */
    void write(String id, String string);

    /**
     * Wait the default timeout for a sub-string to appear on standard out.
     *
     * @param id        session identifier
     * @param subString The case-sensitive substring to match against.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, String, long)
     */
    String expect(String id, String subString) throws TimeoutException;

    /**
     * Wait for a pattern to appear on standard out.
     *
     * @param id             session identifier
     * @param subString      The pattern to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, String)
     */
    String expect(String id, String subString, long timeOutSeconds) throws TimeoutException;

    /**
     * Wait the default timeout for a sub-string to appear on standard out.
     *
     * @param id      session identifier
     * @param pattern The case-sensitive substring to match against.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, String, long)
     */
    String expect(String id, Pattern pattern) throws TimeoutException;

    /**
     * Wait for a pattern to appear on standard out.
     *
     * @param id             session identifier
     * @param pattern        The pattern to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, String)
     */
    String expect(String id, Pattern pattern, long timeOutSeconds) throws TimeoutException;

    /**
     * Wait the default timeout for a sub-string to appear on standard error.
     *
     * @param id        session identifier
     * @param subString The case-sensitive substring to match against.
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, String)
     */
    boolean expectErr(String id, String subString) throws TimeoutException;

    /**
     * Wait for a sub-string to appear on standard error.
     *
     * @param id             session identifier
     * @param subString      The case-sensitive substring to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, String, long)
     */
    boolean expectErr(String id, String subString, long timeOutSeconds) throws TimeoutException;

    /**
     * Wait the default timeout for the spawned process to finish.
     *
     * @param id session identifier
     * @throws TimeoutException if the spawn didn't finish inside of the timeout.
     * @see #expectClose(String, long)
     */
    void expectClose(String id) throws TimeoutException;

    /**
     * Wait for the spawned process to finish.
     *
     * @param id             session identifier
     * @param timeOutSeconds The number of seconds to wait before giving up, or -1 to wait forever.
     * @throws TimeoutException if the spawn didn't finish inside of the timeout.
     * @see #expectClose(String)
     */
    boolean expectClose(String id, long timeOutSeconds) throws TimeoutException;


    /**
     * This method can be use use to check the target process status before invoking {@link #write(String, String)}
     *
     * @param id session identifier
     * @return true if the process has already exited.
     */
    boolean isClosed(String id);

    /**
     * Retrieve the exit code of a finished process if the process has already exited or
     * wait default time out (5 seconds) until process finished.
     *
     * @param id session identifier
     * @return the exit code of the process if the process has  already exited.
     * @throws TimeoutException if the spawn didn't finish inside of the timeout.
     */
    int getExitValue(String id) throws TimeoutException;

    /**
     * Retrieve the exit code of a finished process if the process has already exited or
     * wait passed time out until process finished.
     *
     * @param id             session identifier
     * @param timeOutSeconds The number of seconds to wait before giving up, or -1 to wait forever.
     * @return the exit code of the process if the process has  already exited.
     * @throws TimeoutException if the spawn didn't finish inside of the timeout.
     */
    int getExitValue(String id, long timeOutSeconds) throws TimeoutException;

    /**
     * Set an environment variable on a session.
     *
     * @param id    session identifier
     * @param name  environment variable name
     * @param value environment variable value
     */
    void setEnv(String id, String name, String value);

    /**
     * Get an environment variable on a session.
     *
     * @param id   session identifier
     * @param name environment variable name
     * @return value environment variable value, or null if environment variable not exist
     */
    String getEnv(String id, String name);

    /**
     * Get an all environment variables on a session.
     *
     * @param id session identifier
     * @return map of name-value
     */
    Map<String, String> getEnv(String id);

    /**
     * Unset the  environment variable on a session.
     *
     * @param id   session identifier
     * @param name environment variable name
     */
    void unsetEnv(String id, String name);

    /**
     * Reset all environment variables in the session state that was at the session is opened
     *
     * @param id session identifier
     */
    void resetEnv(String id);

    /**
     * Read standard out
     *
     * @param id session identifier
     */
    String read(String id);

    /**
     * Read standard out
     *
     * @param id             session identifier
     * @param timeOutSeconds The number of seconds to wait before giving up, or -1 to wait forever.
     */
    String read(String id, long timeOutSeconds);

    /**
     * Read standard Err
     *
     * @param id session identifier
     */
    String readErr(String id);

    /**
     * Read standard Err
     *
     * @param id             session identifier
     * @param timeOutSeconds The number of seconds to wait before giving up, or -1 to wait forever.
     */
    String readErr(String id, long timeOutSeconds);
}
