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
package com.ericsson.cifwk.taf.tools.cli;

import static java.lang.String.format;


import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.googlecode.ipv6.IPv6Address;

/**
 * A wrapper class which utilizes the underlying CLI Tool functionality User
 * orientated providing more efficient and easier use of CLI tool
 * * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class CLICommandHelper extends CLICommandHelperConstants implements Closeable {

    private static final Pattern EXIT_CODE_MATCH_PATTERN = Pattern.compile("(?s).*" + CLIOperator.JSCH_EXIT_CODE_MARKER + "=\\d+?");
    private static final String EXIT_CODE_EXCEPTION_MESSAGE_TEMPLATE = "Unable to retrieve command exit code. Possibly command was not completed in %s seconds";

    @VisibleForTesting
    protected Shell shell;

    /**
     * Default timeout value to wait for exit code pattern to appear on shell.
     * Applies to {@link #execute(String)} command only.
     */
    public static long DEFAULT_COMMAND_TIMEOUT_VALUE = 7200;

    private CLI cli;
    private String stdOut;
    private int exitCode;
    private long executionTime;
    private String[] splittedStdOut;
    private long cmdStartTime;
    private long cmdEndTime;
    private StringBuilder builder;
    private ByteArrayOutputStream stream;
    private static final String EXIT_COMMAND = "exit";

    private final static Logger LOGGER = LoggerFactory.getLogger(CLICommandHelper.class);

    /**
     * Default constructor, requires instantiation of a CLI instance using
     * {@link #createCliInstance(Host)} or
     * {@link #createCliInstance(Host, User)}
     */
    public CLICommandHelper() {

    }

    /**
     * Constructor which takes a Host and creates a CLI instance on that host
     *
     * @param host
     */
    public CLICommandHelper(Host host) {
        createCliInstance(host);
    }

    /**
     * Constructor which takes a Host and keyFile and creates a CLI instance on that host
     * @param host
     * @param keyFile
     */
    public CLICommandHelper(final Host host, final Path keyFile) {
        createCliInstance(host, keyFile);
    }

    /**
     * Constructor which takes a Host and User as argument Create a CLI instance
     * on specified host with specified username
     *
     * @param host
     * @param username
     */
    public CLICommandHelper(Host host, User username) {
        createCliInstance(host, username);
    }

    /**
     * Constructor which takes a Host, User & key file and creates a CLI instance on specified host with specified username
     * @param host
     * @param username
     * @param keyFile
     */
    public CLICommandHelper(final Host host, final User username, final Path keyFile) {
        createCliInstance(host, username, keyFile);
    }

    /**
     * Creates CLI instance on Host with specified shell type selected<br/>
     * <br/>
     * WARN: shell is opened in the background and set to shell type specified.
     * User does not need to specifically call openShell() when using this
     * constructor.
     *
     * @param host      Host object on which shell will be created
     * @param shellType Shell type on which shell will be created
     */
    public CLICommandHelper(Host host, ShellType shellType) {
        createCliInstance(host, shellType);
    }

    /**
     * Creates CLI instance on Host with specified user and specific shell type<br/>
     * <br/>
     * WARN: shell is opened in the background and set to shell type specified.
     * User does not need to specifically call openShell() when using this
     * constructor.
     *
     * @param host      Host object on which shell will be created
     * @param username  Selected user on the shell e.g. root, litp_admin, etc...
     * @param shellType Shell type on which shell will be created
     */
    public CLICommandHelper(Host host, User username, ShellType shellType) {
        createCliInstance(host, username, shellType);
    }

    /**
     * @see CLI#openShell()
     */
    public Shell openShell() {
        LOGGER.debug("Creating shell instance");
        disconnect();
        return getShell();
    }

    /**
     * @param term
     * @return
     * @see CLI#openShell()
     */
    public Shell openShell(Terminal term) {
        LOGGER.debug("Creating shell instance");
        disconnect();
        return getShell(term);
    }

    /**
     * Method that creates CLI instance on a particular host with default
     * username
     *
     * @param host
     */
    public void createCliInstance(Host host) {
        LOGGER.debug("Initializing the CLI Object");
        cli = new CLI(host, computeUserFromHost(host));
    }

    /**
     * Create a CLI instance on a particular host with default user and key file authentication.
     * @param host
     * @param keyFile
     */
    public void createCliInstance(final Host host, final Path keyFile) {
        cli = new CLI(host, computeUserFromHost(host), keyFile);
    }

    /**
     * Method that creates CLI instance on a particular host with specified
     * username
     *
     * @param host
     * @param username
     */
    public void createCliInstance(Host host, User username) {
        LOGGER.debug("Initializing the CLI Object");
        cli = new CLI(host, username);
    }

    /**
     * Create a CLI instance on a particular host the a specified user and key file authentication.
     * @param host
     * @param username
     * @param keyFile
     */
    public void createCliInstance(final Host host, final User username, final Path keyFile) {
        cli = new CLI(host, username, keyFile);
    }

    /**
     * Allows execution of an array of command Strings [1....n]. Create CLI
     * instance with particular host on specified shell type
     *
     * @param host
     * @param shellType Type of shell to use e.g. bash, sh, tcsh
     */
    public void createCliInstance(Host host, ShellType shellType) {
        LOGGER.debug("Initializing the CLI Object");
        cli = new CLI(host, computeUserFromHost(host));
        setShell(shellType);
    }

    /**
     * Create CLI instance with particular host & user on a specified shell type
     *
     * @param host
     * @param username
     * @param shellType Type of shell to use e.g. bash, sh, tcsh
     */
    public void createCliInstance(Host host, User username, ShellType shellType) {
        LOGGER.debug("Initializing the CLI Object");
        cli = new CLI(host, username);
        setShell(shellType);
    }

    /**
     * Allows execution of an array of command Strings [1....n]. User can simply
     * execute one command by passing in a single String. User can execute
     * multiple commands by passing a String[] or by passing in comma separated
     * Strings ("pwd", "ls", "whoami").
     *
     * @param commands Commands to be executed
     * @return stdOut Returns standard out of command(s) executed
     */
    public String simpleExec(String... commands) {
        LOGGER.debug("Command(s) to execute are {}", Arrays.toString(commands));
        cmdStartTime = System.nanoTime();
        shell = cli.executeCommand(commands);
        stdOut = getStreamOutput();
        cmdEndTime = System.nanoTime();
        calculateExecutionTime(cmdStartTime, cmdEndTime);
        exitCode = getShellExitValue();
        disconnect();
        return stdOut;
    }

    /**
     * <pre>
     * Allows execution of an array of command Strings [1....n] in a particular terminal type
     * </pre>
     * <p/>
     * User can simply execute one command by passing in a single String.
     * <p/>
     * <pre></pre>
     *
     * User can execute multiple commands by passing a String[] or by passing in
     * comma seperated Strings ("pwd", "ls", "whoami")
     *
     * <pre></pre>
     *
     * @param terminal Terminal type to use
     * @param commands Commands to be executed
     * @return stdOut Returns standard out of command(s) executed
     */
    public String simpleExec(Terminal terminal, String... commands) {
        LOGGER.debug("Command(s) to execute are {}", Arrays.toString(commands));
        cmdStartTime = System.nanoTime();
        shell = cli.executeCommand(terminal, commands);
        stdOut = getStreamOutput();
        cmdEndTime = System.nanoTime();
        calculateExecutionTime(cmdStartTime, cmdEndTime);
        exitCode = getShellExitValue();
        disconnect();
        return stdOut;
    }

    /**
     * Method to execute a command on a shell
     * <p/>
     * <pre>
     * Note: Method should not be used to execute an interactive script (i.e. a script requiring
     * user input) See
     * {@link #runInteractiveScript(String)} for executing interactive script
     *
     * <pre>
     *
     * @param command
     */
    public String execute(String command) {
      return execute(command, DEFAULT_COMMAND_TIMEOUT_VALUE);
    }

    /**
     * Method to execute a command on a shell
     * <p/>
     * <pre>
     * Note: Method should not be used to execute an interactive script (i.e. a script requiring
     * user input) See
     * {@link #runInteractiveScript(String)} for executing interactive script
     *
     * <pre>
     *
     * @param command
     * @param timeout
     */
    public String execute(String command, long timeout) {
        long cmdTimeOut = timeout <= DEFAULT_COMMAND_TIMEOUT_VALUE ? timeout : DEFAULT_COMMAND_TIMEOUT_VALUE;
        LOGGER.debug("Command to be executed is: {}", command);
        builder = new StringBuilder();
        builder.append(command).append("; ").append(CLIOperator.ECHO_EXIT_CODE_CMD);
        cmdStartTime = System.nanoTime();
        getShell().read(0);
        shell.writeln(builder.toString());
        stdOut = expectOutput(cmdTimeOut);
        cmdEndTime = System.nanoTime();
        calculateExecutionTime(cmdStartTime, cmdEndTime);
        return filterStandardOut();
    }

    @VisibleForTesting
    String expectOutput(long timeout) {
        try {
            return expect(EXIT_CODE_MATCH_PATTERN, timeout);
        } catch (TimeoutException ex) {
            if (ex.getMessage().contains(EXIT_CODE_MATCH_PATTERN.pattern())) {
                String message = format(EXIT_CODE_EXCEPTION_MESSAGE_TEMPLATE, timeout);
                LOGGER.error(message);
                ex = new TimeoutException(message, ex);
            }

            throw ex;
        }
    }

    /**
     * @see Shell#disconnect()
     */
    public void disconnect() {
        LOGGER.debug("Closing the shell");
        if (shell != null) {
            shell.disconnect();
            shell = null;
        }
        cli.close();
    }

    /**
     * @see Shell#expectClose()
     */
    public void expectShellClosure() {
        LOGGER.debug("Expecting shell closure within default timeout of 5 seconds");
        if (shell != null) {
            shell.expectClose();
        }
    }

    /**
     * @param timeoutInSeconds
     * @see Shell#expectClose(long timeOutSeconds)
     */
    public void expectShellClosure(long timeoutInSeconds) {
        LOGGER.debug("Expecting shell closure within {} seconds", timeoutInSeconds);
        if (shell != null) {
            shell.expectClose(timeoutInSeconds);
        }
    }

    /**
     * @see Shell#isClosed()
     */
    public boolean isClosed() {
        if (shell != null) {
            return shell.isClosed();
        }
        LOGGER.warn("Shell has not been created or has been disconnected.");
        return true;
    }

    /**
     * Method which will write string input to the shell
     *
     * @param input
     */
    public void interactWithShell(String input) {
        LOGGER.debug("String input written to shell is: {}", input);
        getShell().writeln(input);
    }

    /**
     * Executes a script which requires some form of user input. To execute a
     * non-interactive script please use {@link #execute(String)} method to do
     * so
     *
     * @param script
     */
    public void runInteractiveScript(String script) {
        LOGGER.debug("Interactive script to execute is: {}", script);
        getShell().writeln(script);
    }

    /**
     * @param expectedOutcome
     * @return Read of the standard output
     * @see Shell#expect(String)
     */
    public String expect(String expectedOutcome) {
        LOGGER.debug("Expecting {} within default timeout of 5 seconds", expectedOutcome);
        stdOut = shell.expect(expectedOutcome);
        return stdOut;

    }

    /**
     * @param expectedOutcome
     * @param timeoutInSeconds
     * @return Read of the standard out else timeout exception thrown
     * @see Shell#expect(String, long)
     */
    public String expect(String expectedOutcome, long timeoutInSeconds) {
        LOGGER.debug("Expecting {} within {} seconds", expectedOutcome, timeoutInSeconds);
        stdOut = shell.expect(expectedOutcome, timeoutInSeconds);
        return stdOut;
    }

    /**
     * @param pattern
     * @return Read of the standard out
     * @see Shell#expect(java.util.regex.Pattern)
     */
    public String expect(Pattern pattern) {
        LOGGER.debug("Expecting {} within default timeout of 5 seconds", pattern);
        stdOut = shell.expect(pattern);
        return stdOut;
    }

    /**
     * @param pattern
     * @param timeoutInSeconds
     * @return Read of the standard out else timeout exception thrown
     * @see Shell#expect(Pattern, long)
     */
    public String expect(Pattern pattern, long timeoutInSeconds) {
        LOGGER.debug("Expecting {} within {} seconds", pattern, timeoutInSeconds);
        stdOut = shell.expect(pattern, timeoutInSeconds);
        return stdOut;
    }

    /**
     * @param err
     * @return True if match is found
     * @see Shell#expectErr(String)
     */
    public boolean expectErr(String err) {
        LOGGER.debug("Expecting {} as standard error within default timeout of 5 seconds", err);
        return shell.expectErr(err);
    }

    /**
     * @param err
     * @param timeoutInSeconds
     * @return True if match found after the default timeout
     * @see Shell#expectErr(String, long)
     */
    public boolean expectErr(String err, long timeoutInSeconds) {
        LOGGER.debug("Expecting {} as standard error within {} seconds", err, timeoutInSeconds);
        return shell.expectErr(err, timeoutInSeconds);
    }

    /**
     * @param text
     * @see Shell#write(String)
     */
    public void write(String text) {
        LOGGER.debug("Writing {} to standard in", text);
        getShell().write(text);
    }

    /**
     * @see Shell#getExitValue()
     */
    public int getShellExitValue() {
        LOGGER.debug("Retrieving exit value of Shell within default timeout of 5 seconds");
        if (shell != null) {
            return shell.getExitValue();
        }
        LOGGER.warn("You have called shell.getShellExitValue() on a closed shell");
        return -1;
    }

    /**
     * @param timeoutInSeconds
     * @return Exit Value of shell
     * @see Shell#getExitValue(long)
     */
    public int getShellExitValue(long timeoutInSeconds) {
        LOGGER.debug("Retrieving exit value of Shell within {} seconds", timeoutInSeconds);
        if (shell != null) {
            return shell.getExitValue(timeoutInSeconds);
        }
        LOGGER.warn("You have called shell.getShellExitValue(long timeoutInSeconds) on a closed shell");
        return -1;

    }

    private void ensureShellCreated() {
        if (shell == null) {
            openShell();
        }
    }

    /**
     * @param name
     * @param value
     * @see Shell#setEnv(String, String)
     */
    public void setEnv(String name, String value) {
        LOGGER.debug("Setting environment variable {} with a value of {}", name, value);
        ensureShellCreated();
        shell.setEnv(name, value);
    }

    /**
     * @param name
     * @return
     * @see Shell#getEnv(String)
     */
    public String getEnv(String name) {
        LOGGER.debug("Retrieving environment variable: {}", name);
        ensureShellCreated();
        return shell.getEnv(name);
    }

    /**
     * @return
     * @see Shell#getEnv()
     */
    public Map<String, String> getEnv() {
        LOGGER.debug("Returning all environment variables for the current session");
        ensureShellCreated();
        return shell.getEnv();
    }

    /**
     * @param name
     * @see Shell#unsetEnv(name)
     */
    public void unsetEnv(String name) {
        LOGGER.debug("Unsetting {} environment variable", name);
        ensureShellCreated();
        shell.unsetEnv(name);
    }

    /**
     * @see Shell#resetEnv()
     */
    public void resetEnv() {
        LOGGER.debug("Resetting all environment variables");
        if (shell != null)
            shell.resetEnv();
    }

    /**
     * Method that gets exit value of previously executed command
     *
     * @return Exit Value of last command executed
     */
    public int getCommandExitValue() {
        if (shell != null) {
            computeExitCode();
            return exitCode;
        }
        return exitCode;
    }

    /**
     * Gets standard error from executed command
     *
     * @return stdErr String representation of standard error
     */
    public String getCommandStdErr() {
        LOGGER.debug("Getting standard error within default timeout of 5 seconds");
        return getShell().readErr();
    }

    /**
     * Gets standard error from executed command within specified timeout
     *
     * @return stdErr String representation of standard error
     */
    public String getCommandStdErr(long timeoutInSeconds) {
        LOGGER.debug("Getting standard error within {} seconds", timeoutInSeconds);
        return getShell().readErr(timeoutInSeconds);
    }

    /**
     * Retrieves execution time of previously executed command in milliseconds
     *
     * @return Execution time of command last executed
     */
    public long getCommandExecutionTime() {
        return executionTime;
    }

    /**
     * Returns the standard output
     *
     * @return Unfiltered standard output
     */
    public String getStdOut() {
        LOGGER.debug("Getting standard output");
        return filterStandardOut();
    }

    /**
     * Returns the standard output after specified timeout
     *
     * @return Unfiltered standard output
     */
    public String getStdOut(long timeoutInSeconds) {
        LOGGER.debug("Getting standard output, within {} seconds", timeoutInSeconds);
        return filterStandardOut();
    }

    @VisibleForTesting
    protected String[] splitStdOut(String stdOut) {
        return splitStdOutPattern.matcher(stdOut).replaceAll("").split("(\\n)+");
    }

    private String filterStandardOut() {
        if (stdOut != null) {
            splittedStdOut = splitStdOut(stdOut);
            builder = new StringBuilder();
            for (String s : splittedStdOut) {
                if (!s.contains(CLIOperator.JSCH_EXIT_CODE_MARKER)) {
                    builder.append(s).append("\n");
                } else {
                    LOGGER.trace("Filtering {} from standard output", s);
                }
            }
            return builder.toString();

        }
        return null;
    }

    /**
     * @see CLI#openNullTerminalShell()
     */
    protected Shell openNullTerminalShell() {
        LOGGER.debug("Creating shell instance with null terminal");
        shell = cli.openNullTerminalShell();
        return shell;
    }

    private void calculateExecutionTime(long start, long end) {
        LOGGER.debug("Execution start time is {}", start);
        LOGGER.debug("Execution end time is {}", end);
        if (start != 0 || end != 0) {
            long result = (cmdEndTime - cmdStartTime) / 1_000_000;
            if (result < 1 && result > 0) {
                executionTime = (cmdEndTime - cmdStartTime) / 1000;
                LOGGER.debug("Execution time of the command is 0.{} milliseconds", executionTime);
            } else {
                executionTime = result;
                LOGGER.debug("Execution time of the command is {} milliseconds", executionTime);
            }
        } else {
            LOGGER.error("Execution time cannot be retrieved");
        }
    }

    private void computeExitCode() {
        if (stdOut != null) {
            splittedStdOut = splitStdOut(stdOut);
            List<String> filteredOutput = Lists.newArrayList(splittedStdOut);
            for (String s : filteredOutput) {
                if (s.matches(CLIOperator.JSCH_EXIT_CODE_MARKER + "=\\d*.*")) {
                    String[] codeString = s.split("=");
                    exitCode = Integer.parseInt(codeString[1]);
                    return;
                }
            }
            getShell().writeln(CLIOperator.ECHO_EXIT_CODE_CMD);
            stdOut += getShell().read();
            splittedStdOut = splitStdOut(stdOut);
            filteredOutput = Lists.newArrayList(splittedStdOut);
            for (String s : filteredOutput) {
                if (s.matches(CLIOperator.JSCH_EXIT_CODE_MARKER + "=\\d*.*")) {
                    String[] codeString = s.split("=");
                    exitCode = Integer.parseInt(codeString[1]);
                    return;
                }
            }
        } else {
            LOGGER.error("Exit code cannot be retrieved\n"
                    + "Standard output is null");
            throw new IllegalStateException("Cannot retrieve command exit code\n"
                    + "Standard out is null");
        }
    }

    /**
     * Writes "exit" command to the most recent shell process
     *
     * @return Exit code of previous shell
     */
    public int closeAndValidateShell() {
        getShell().writeln(EXIT_COMMAND);
        return getShellExitValue();
    }

    /**
     * Writes "exit" command to the most recent shell process
     *
     * @param timeoutInSeconds Time in seconds after which to check the shell is closed
     * @return Exit code of shell closure
     */
    public int closeAndValidateShell(long timeoutInSeconds) {
        getShell().writeln(EXIT_COMMAND);
        return getShellExitValue(timeoutInSeconds);
    }

    /**
     * Returns the current shell instance, if shell is null, a new shell
     * instance is created
     *
     * @return shell instance
     */
    public Shell getShell() {
        if (shell == null) {
            LOGGER.debug("Creating new shell");
            shell = cli.openShell();
        }
        return shell;
    }

    private Shell getShell(Terminal term) {
        if (shell == null) {
            LOGGER.debug("Creating new shell on {} terminal", term);
            shell = cli.openShell(term);
        }
        return shell;
    }

    private String getStreamOutput() {
        stream = new ByteArrayOutputStream();
        byte[] a = getShell().read().getBytes();
        try {
            stream.write(a);
            while (!getShell().isClosed()) {
                a = getShell().read().getBytes();
                stream.write(a);
            }
            return stream.toString();
        } catch (IOException e) {
            LOGGER.error("Error writing ouputStream during simpleExec command ", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.error("Error closing outputStream during simpleExec command", e);
            }
        }
        return null;
    }

    /**
     * Sets the shell type to be used. <br>
     * The Shell to use must be present in list of installed shells
     *
     * @param shellType Shell type to use e.g. "bash", "tcsh", "sh" etc...
     */
    protected void setShell(ShellType shellType) {
        checkShellTypeIsInstalled(shellType);
        shell.writeln(shellType.getShellType());
    }

    private void checkShellTypeIsInstalled(ShellType shellType) {
        String osType = execute("echo $OSTYPE");
        String shell_type = shellType.getShellType();
        if (osType.contains("linux")) {
            shell.writeln("cat /etc/shells");
        } else if (osType.contains("solaris")) {
            shell.writeln("which " + shell_type);
        }
        try {
            shell.expect("/bin/" + shell_type);
        } catch (TimeoutException e) {
            LOGGER.error("Could not find {} in list of installed shell types on {}", shell_type, osType);
        }
    }

    protected User computeUserFromHost(Host host) {
        final String operUser = host.getUser(UserType.OPER);
        final String firstAdminUser = host.getUser(UserType.ADMIN);
        if(operUser != null){
            LOGGER.info("Operator user found for host. Operator user will be used");
            return host.getUser(operUser);
        } else if (firstAdminUser != null){
            LOGGER.info("No operator user found. Administrator user will be used");
            return host.getUser(firstAdminUser);
        } else{
            LOGGER.info("Operator or administrator user not found. First available user will be used");
            return host.getUser(host.getUser());
        }
    }

    public HopBuilder newHopBuilder() {
        return new HopBuilder();
    }

    @Override
    public void close() throws IOException {
        disconnect();
    }

    public class HopBuilder extends CLICommandHelperConstants {

        /**
         * Allows user to ssh to a Host with specified user information <br/>
         * Login/Hop will be achieved in the following way <br/>
         * <p/>
         * <pre>
         * ssh user.getUsername()@host.getIp()
         * </pre>
         * <p/>
         * With user.getPassword() providing the password credentials to login
         * <p/>
         * Note: If the host object does not contain an IP address, Hopbuilder will attempt to retrieve the
         * IP address from /etc/hosts on the current hop server
         *
         * @param host Host information in which connection will be made
         * @param user User object which will contain username and password login
         *             credentials
         * @return
         */
        public HopBuilder hop(Host host, User user) {
            hopDefaultHostKeyChecking(host, user);
            return this;
        }

        /**
         * Allows user to ssh to a Host with specified user information <br/>
         * Login/Hop will be achieved in the following way <br/>
         * <p/>
         * <pre>
         * ssh -o StrictHostKeyChecking=yes|no user.getUsername()@host.getIp()
         * </pre>
         * <p/>
         * With user.getPassword() providing the password credentials to login
         * <p/>
         * Note: If the host object does not contain an IP address, Hopbuilder will attempt to retrieve the
         * IP address from /etc/hosts on the current hop server
         *
         * @param host                         Host information in which connection will be made
         * @param user                         User object which will contain username and password login
         *                                     credentials
         * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
         *                                     Set to true to turn StrictHostChecking on
         * @return
         */
        public HopBuilder hop(Host host, User user, boolean enforceStrictHostKeyChecking) {
            hopWithHostKeyChecking(host, user, enforceStrictHostKeyChecking);
            return this;
        }

        /**
         * Perform an ssh to a particular host with default host user
         * credentials
         *
         * @param host Host information in which connection will be made
         * @return
         */
        public HopBuilder hop(Host host) {
            final User user = computeUserFromHost(host);
            return hop(host, user);
        }

        /**
         * Perform an ssh to a particular host with default host user
         * credentials
         *
         * @param host                         Host information in which connection will be made
         * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
         *                                     Set to true to turn StrictHostChecking on
         * @return
         */
        public HopBuilder hop(Host host, boolean enforceStrictHostKeyChecking) {
            final User user = computeUserFromHost(host);
            return hop(host, user, enforceStrictHostKeyChecking);
        }

        /**
         * Perform an ssh to a host with full path to ssh private key specified
         * @param host Host information in which connnection will be made using default user
         * @param pathToSshPrivateKeyFile Absolute path to location of private key file on server
         * @return
         */
        public HopBuilder hopWithKeyFile(Host host, String pathToSshPrivateKeyFile){
            verifyPrivateKeyExists(pathToSshPrivateKeyFile);
            final User user = computeUserFromHost(host);
            hopWithSshKeyDefaultKeyChecking(host, user, pathToSshPrivateKeyFile);
            return this;
        }

        /**
         * Perform an ssh to a host with full path to ssh private key specified
         * @param host Host information in which connection will be made
         * @param user User object which will contain username login credentials
         * @param pathToSshPrivateKeyFile Absolute path to location of private key file on server
         * @return
         */
        public HopBuilder hopWithKeyFile(Host host, User user, String pathToSshPrivateKeyFile){
            verifyPrivateKeyExists(pathToSshPrivateKeyFile);
            hopWithSshKeyDefaultKeyChecking(host, user, pathToSshPrivateKeyFile);
            return this;
        }

        /**
         * Perform an ssh to a host with full path to ssh private key specified
         * @param host Host information in which connection will be made
         * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
         *                                     Set to true to turn StrictHostChecking on
         * @param pathToSshPrivateKeyFile Absolute path to location of private key file on server
         * @return
         */
        public HopBuilder hopWithKeyFile(Host host, boolean enforceStrictHostKeyChecking, String pathToSshPrivateKeyFile){
            verifyPrivateKeyExists(pathToSshPrivateKeyFile);
            final User user = computeUserFromHost(host);
            hopWithHostKeyChecking(host, user, enforceStrictHostKeyChecking, pathToSshPrivateKeyFile);
            return this;
        }


        /**
         * Perform an ssh to a host with full path to ssh private key specified
         * @param host Host information in which connection will be made using default user
         * @param user User object which will contain username login credentials
         * @param enforceStrictHostKeyChecking Set to false to turn StrictHostChecking off
         *                                     Set to true to turn StrictHostChecking on
         * @param pathToSshPrivateKeyFile Absolute path to location of private key file on server
         * @return
         */
        public HopBuilder hopWithKeyFile(Host host, User user, boolean enforceStrictHostKeyChecking, String pathToSshPrivateKeyFile){
            verifyPrivateKeyExists(pathToSshPrivateKeyFile);
            hopWithHostKeyChecking(host, user, enforceStrictHostKeyChecking, pathToSshPrivateKeyFile);
            return this;
        }

        private void verifyPrivateKeyExists(String pathToSshPrivateKeyFile) {
            Preconditions.checkArgument(StringUtils.isNotBlank(pathToSshPrivateKeyFile), INVALID_PATH_TO_FILE_MSG + pathToSshPrivateKeyFile);
            execute("test -f " + pathToSshPrivateKeyFile);
            Preconditions.checkArgument(getCommandExitValue() == 0, format(FILE_NOT_FOUND_MSG, pathToSshPrivateKeyFile));
        }

        private Map mapHostnameToIp = new HashMap<>();

        private void hopDefaultHostKeyChecking(Host host, User user) {
            String targetHostIP = preCheck(host, user);
            String hopCommand = format("ssh %s@%s -p %s", user.getUsername(), host.getIp(), getSshPort(host));
            retryMechanism(host, user, targetHostIP, hopCommand);
        }

        private void hopWithHostKeyChecking(Host host, User user, boolean enforceStrictHostKeyChecking) {
            String targetHostIP = preCheck(host, user);
            String hopCommand = constructSshCommandWithHostKeyChecking(host, user, enforceStrictHostKeyChecking);
            retryMechanism(host, user, targetHostIP, hopCommand);
        }

        private void hopWithHostKeyChecking(Host host, User user, boolean enforceStrictHostKeyChecking, String pathToSshPrivateKeyFile) {
            preCheck(host, user);
            if(enforceStrictHostKeyChecking){
                final String hopCommand = format("ssh -i %s %s@%s %s", pathToSshPrivateKeyFile, user.getUsername(), host.getIp(), STRICT_HOST_KEY_CHECKING_YES);
                performHop(host, user.getUsername(), user.getPassword(), hopCommand);
            } else{
                final String hopCommand = format("ssh -i %s %s@%s %s", pathToSshPrivateKeyFile, user.getUsername(), host.getIp(), STRICT_HOST_KEY_CHECKING_NO);
                performHop(host, user.getUsername(), user.getPassword(), hopCommand);
            }
        }

        private void hopWithSshKeyDefaultKeyChecking(Host host, User user, String pathToSshPrivateKeyFile) {
            final String hopCommand = format("ssh -i %s %s@%s", pathToSshPrivateKeyFile, user.getUsername(), host.getIp());
            performHop(host, user.getUsername(), user.getPassword(), hopCommand);
        }

        private String preCheck(Host host, User user) {
            String hostIP = host.getIp();
            if (StringUtils.isBlank(hostIP)) {
                LOGGER.warn("IP address of supplied host was null or empty, attempting to retrieve IP from /etc/hosts on current hop server");
                hostIP = resolveIpFromCache(host);
                host.setIp(hostIP);
            }

            LOGGER.info("Attempting to hop to {} using ip {} with user {}", host, hostIP, user);
            return pingHostAndGetIP(host);
        }

        @VisibleForTesting
        protected String resolveIpFromCache(Host host) {
            String hostname = host.getHostname();
            if (!mapHostnameToIp.containsKey(hostname)) {
                String result = resolveIpOnHopServer(host, hostname);
                mapHostnameToIp.put(hostname, result);
            }
            return (String) mapHostnameToIp.get(hostname);
        }

        private String resolveIpOnHopServer(Host host, String hostname) {
            getShell().read(0);
            String getHostIpCommand = format(GETENT_HOSTS, hostname);
            shell.writeln(getHostIpCommand);
            String response = shell.expect(GENERIC_PROMPT_PATTERN);
            String ipAddress = extractFirstIpAddressFromResponse(response);
            if (StringUtils.isBlank(ipAddress)) {
                throw throwHostSSHFailRuntimeException(host, FAILED_TO_GET_IP_FROM_ETC_HOST_FILE_ERR_MSG);
            }
            return ipAddress;
        }

        @VisibleForTesting
        protected String extractFirstIpAddressFromResponse(String response) {
            List<String> responseAsListString = Arrays.asList(response.split(System.lineSeparator()));
            for (String each : responseAsListString) {
                String token = each.trim();
                if (InetAddresses.isInetAddress(token)) {
                    return token;
                }
            }
            return "";
        }

        @VisibleForTesting
        protected void retryMechanism(Host host, User user, String targetHostIP, String hopCommand) {
            performHop(host, user.getUsername(), user.getPassword(), hopCommand);

            if (!verifyCorrectHost(targetHostIP)) {
                performHop(host, user.getUsername(), user.getPassword(), hopCommand);
            }
            if (!verifyCorrectHost(targetHostIP)) {
                throw new RuntimeException(format(FAILED_TO_HOP_TO, host));
            }
        }

        @VisibleForTesting
        protected String constructSshCommandWithHostKeyChecking(Host host, User user, boolean enforceStrictHostKeyChecking) {
            String hostIpAddress = host.getIp();
            String userUsername = user.getUsername();
            String command;
            String port = getSshPort(host);
            if (enforceStrictHostKeyChecking) {
                command = format("ssh %s %s@%s -p %s", STRICT_HOST_KEY_CHECKING_YES, userUsername, hostIpAddress, port);
            } else {
                command = format("ssh %s %s@%s -p %s", STRICT_HOST_KEY_CHECKING_NO, userUsername, hostIpAddress, port);
            }
            return command;
        }

        @VisibleForTesting
        protected String getSshPort(Host host) {
            String port;
            if (host.getPort(Ports.SSH) == null) {
                port = "22";
            } else
                port = host.getPort(Ports.SSH).toString();
            return port;
        }

        @VisibleForTesting
        protected void performHop(Host host, String userUsername, String password, String command) {
            getShell().read(0);
            shell.writeln(command);

            String response = shell.expect(INITIAL_LOGIN_PATTERN);
            if (response.toLowerCase().contains(CONNECTION_REFUSED_ERR_MSG.toLowerCase())) {
                throw throwHostSSHFailRuntimeException(host, CONNECTION_REFUSED_ERR_MSG);
            }
            if(response.toLowerCase().contains(NO_ROUTE_TO_HOST_ERR_MSG.toLowerCase())){
                throw throwHostSSHFailRuntimeException(host, NO_ROUTE_TO_HOST_ERR_MSG);
            }

            Matcher m1 = PASSWORD_PATTERN.matcher(response);
            if (response.contains("Are you sure")) {
                shell.read(0);
                shell.writeln("yes");
                String passwdPrompt = shell.expect(Pattern.compile(USER_PASSWORD_PROMPT + format(USER_NAME_PATTERN, userUsername)));
                Matcher m2 = PASSWORD_PATTERN.matcher(passwdPrompt);
                if (m2.find()) {
                    sendPassword(userUsername, password);
                }
            } else if (m1.find()) {
                sendPassword(userUsername, password);
            }
        }

        /**
         * Switch to a certain user on the current host
         *
         * @param user User information
         * @return
         */
        public HopBuilder hop(User user) {
            int count = USER_HOP_REPEAT_COUNT;
            boolean foundCorrectUser = false;
            do {
                performUserHop(user);
                count--;
                foundCorrectUser = verifyCorrectUser(user);
            } while (!foundCorrectUser && count > 0);

            if (!foundCorrectUser) {
                throw new RuntimeException(FAILED_TO_HOP_TO_USER + user.getUsername());
            }
            return this;
        }

        @VisibleForTesting
        protected void performUserHop(User user) {
            String userUsername = user.getUsername();
            getShell().read(0);
            shell.writeln("su - " + userUsername + ";");
            String suResponse = shell.expect(Pattern.compile(USER_PASSWORD_PROMPT + format(USER_NAME_PATTERN, userUsername)));
            Matcher m1 = PASSWORD_PATTERN.matcher(suResponse);
            if (m1.find()) {
                sendPassword(user.getUsername(), user.getPassword());
            }
            if (suResponse.toLowerCase().contains(DOES_NOT_EXIST)) {
                throw new RuntimeException(format("User '%s' does not exist on the system", user.getUsername()));
            }
        }

        private String sendPassword(String userName, String userPassword) {
            getShell().read(0);
            shell.writeln(userPassword);
            String response = shell.expect(SEND_PASS_PATTERN);
            if (response.toLowerCase().contains(INCORRECT_PASSWORD.toLowerCase())) {
                enterCtrlC();
                throw new RuntimeException(format("Incorrect password (%s) used for user %s", userPassword, userName));
            }
            Matcher m1 = PASSWORD_PATTERN.matcher(response);
            if (m1.find()) {
                enterCtrlC();
                throw new RuntimeException(format("%s %s/%s", PASSWORD_NOT_ACCEPTED_ERR_MSG, userName, userPassword));
            }
            if (response.contains(PERMISSION_DENIED_ERR_MSG)) {
                enterCtrlC();
                throw new RuntimeException(format("Failed to switch user due to error %s", PERMISSION_DENIED_ERR_MSG));
            }
            return response;
        }

        private void enterCtrlC() {
            char ctrlC = 0x3;
            String controlC = Character.toString(ctrlC);
            shell.writeln(controlC);
            shell.expect(GENERIC_PROMPT_PATTERN);
        }

        @VisibleForTesting
        protected String pingHostAndGetIP(Host host) {
            final String ipAddress = getIpAddressOfHostname(host.getIp());
            getShell().read(0);
            LOGGER.trace("Host ip to ping is {}", ipAddress);

            if (InetAddressUtils.isIPv6Address(ipAddress)) {
                final String pingIPv6Command = "ping6 -c 1 " + ipAddress;
                return executePing(host, pingIPv6Command, ipAddress);
            } else {
                final String pingIPv4Command = "ping -c 1 " + ipAddress;
                return executePing(host, pingIPv4Command, ipAddress);
            }
        }

        @VisibleForTesting
        protected String getIpAddressOfHostname(String hostname) {
            try {
                String hostAddress = InetAddress.getByName(hostname).getHostAddress();
                LOGGER.trace("IP Address of {} host is {} ", hostname, hostAddress);
                if (InetAddressUtils.isIPv6Address(hostAddress)) {
                    return IPv6Address.fromString(hostAddress).toString();
                }
                return hostAddress;
            } catch (UnknownHostException e) {
            }
            return hostname;
        }

        private String executePing(Host host, String pingCommand, String ipAddress) {
            execute(pingCommand);
            if (getCommandExitValue() == 0) {
                return ipAddress;
            }
            throw throwHostSSHFailRuntimeException(host, DESTINATION_HOST_UNREACHABLE_ERR_MSG);
        }

        @VisibleForTesting
        protected RuntimeException throwHostSSHFailRuntimeException(Host host, String errorMessage) {
            return new RuntimeException(format("Failed to ssh to %s due to error: %s", host, errorMessage));
        }

        @VisibleForTesting
        protected boolean verifyCorrectHost(String hostIpAddress) {
            getShell().read(0);
            shell.writeln(format("ip addr list | grep \"inet\" | grep %s", hostIpAddress));
            try {
                shell.expect(Pattern.compile(format("\\s*inet[6]*\\s*%s", hostIpAddress)));
            } catch (TimeoutException e) {
                return false;
            }
            return true;
        }

        @VisibleForTesting
        protected boolean verifyCorrectUser(User user) {
            getShell().read(0);
            shell.writeln("whoami");
            try {
                shell.expect(user.getUsername());
            } catch (TimeoutException e) {
                LOGGER.debug("User was not as expected");
                return false;
            }
            return true;
        }

        /**
         * Returns the CliCommandHelper instance<br/>
         * User can continue to execute commands on the shell after all
         * "hopping" has been completed.
         *
         * @return CLICommandHelper instance
         */
        public CLICommandHelper build() {
            return CLICommandHelper.this;
        }
    }
}
