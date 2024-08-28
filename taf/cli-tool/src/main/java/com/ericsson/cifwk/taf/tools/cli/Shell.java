package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.util.Map;
import java.util.regex.Pattern;

import com.ericsson.cifwk.meta.API;

/**
 * This interface is used to describe a interaction with the shell process to read and write to it.
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public interface Shell {

    /**
     * Close and invalidate SSH shell, which is associated with the given {@link Shell}
     */
    void disconnect();

    /**
     * Writes a string to the standard input of the shell process.
     *
     * @param string The string to send.
     */
    void write(String string);

    /**
     * Writes a string to the standard input of the shell process.
     * and terminate it with \n before writes if it isn't terminated with \n
     *
     * @param string The string to send
     */
    void writeln(String string);

    /**
     * Wait the default timeout for a sub-string to appear on standard out.
     *
     * @param subString The case-sensitive substring to match against.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, long)
     */
    String expect(String subString) throws TimeoutException;

    /**
     * Wait for a sub-string to appear on standard out.
     *
     * @param subString      The case-insensitive substring to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String)
     */
    String expect(String subString, long timeOutSeconds) throws TimeoutException;

    /**
     * Wait the default timeout for a pattern to appear on standard out.
     *
     * @param pattern The pattern to match against.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String, long)
     */
    String expect(Pattern pattern) throws TimeoutException;

    /**
     * Wait for a pattern to appear on standard out.
     *
     * @param pattern        The pattern to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @return read standard out
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expect(String)
     */
    String expect(Pattern pattern, long timeOutSeconds) throws TimeoutException;

    /**
     * Wait the default timeout for a sub-string to appear on standard error.
     *
     * @param subString The case-sensitive substring to match against.
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expectErr(String, long)
     */
    boolean expectErr(String subString) throws TimeoutException;

    /**
     * Wait for a sub-string to appear on standard error.
     *
     * @param subString      The case-sensitive substring to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @throws TimeoutException on timeout waiting for pattern
     * @see #expectErr(String)
     */
    boolean expectErr(String subString, long timeOutSeconds) throws TimeoutException;

    /**
     * Wait the default timeout for the spawned process to finish.
     *
     * @throws TimeoutException if the spawn didn't finish inside of the timeout.
     * @see #expectClose(long)
     */
    void expectClose() throws TimeoutException;

    /**
     * Wait for the spawned process to finish.
     *
     * @param timeOutSeconds The number of seconds to wait before giving up, or -1 to wait forever.
     * @throws TimeoutException if the spawn didn't finish inside of the timeout.
     * @see #expectClose()
     */
    boolean expectClose(long timeOutSeconds) throws TimeoutException;

    /**
     * This method can be use use to check the target process status before invoking {@link #write(String)}
     *
     * @return true is session is closed
     */
    boolean isClosed();

    /**
     * Retrieve the exit code of a finished process if the process has already exited
     * or Wait default time out (5 seconds) until process finished.
     *
     * @return exit code
     * @throws TimeoutException if the process didn't finish inside of the timeout.
     */
    int getExitValue() throws TimeoutException;

    /**
     * Retrieve the exit code of a finished process if the process has already exited
     * or wait passed time out until process finished.
     *
     * @param timeOutSeconds The number of seconds to wait before giving up, or -1 to wait forever.
     * @return exit code
     * @throws TimeoutException if the process didn't finish inside of the timeout.
     */
    int getExitValue(long timeOutSeconds) throws TimeoutException;

    /**
     * Set an environment variable on a session.
     *
     * @param name  environment variable name
     * @param value environment variable value
     */
    void setEnv(String name, String value);

    /**
     * Get an environment variable on a session.
     *
     * @param name environment variable name
     * @return value environment variable value, or null if environment variable not exist
     */
    String getEnv(String name);

    /**
     * Get an all environment variables on a session.
     *
     * @return map of name-value
     */
    Map<String, String> getEnv();

    /**
     * Unset the  environment variable on a session.
     *
     * @param name environment variable name
     */
    void unsetEnv(String name);

    /**
     * Reset all environment variables in the session state that was at the session is opened
     */
    void resetEnv();

    /**
     * Read standard out
     */
    String read();

    /**
     * Read standard out
     *
     * @param timeOutSeconds The timeout in seconds before the match fails.
     */
    String read(long timeOutSeconds);

    /**
     * Read standard err
     */
    String readErr();

    /**
     * Read standard err
     *
     * @param timeOutSeconds The timeout in seconds before the match fails.
     */
    String readErr(long timeOutSeconds);

}
