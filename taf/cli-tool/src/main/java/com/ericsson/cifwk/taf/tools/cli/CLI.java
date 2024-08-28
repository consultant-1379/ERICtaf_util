package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.nio.file.Path;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.TargetHost;

/**
 * The main class of CLI Tool which provides a access
 * to the shell and enables the commands execution
 * <p/>
 * Example:
 * <pre>
 * CLI cli = new CLI(host);
 * Shell shell = cli.openShell(Terminal.VT100);
 * shell.writeln("echo 'Hello World!'");
 * assertTrue(shell.expect("Hello World!"));
 * shell.writeln("exit");
 * assertTrue(shell.expectClose());
 * assertTrue(shell.isClosed());
 * </pre>
 * or by execution the command:
 * <pre>
 * CLI cli = new CLI(host);
 * Shell shell = cli.executeCommand(Terminal.VT100,"echo \"Hello World!\"");
 * assertTrue(shell.expect("Hello World!"));
 * assertTrue(shell.expectClose());
 * assertEquals(shell.getExitValue(), 0);
 * assertTrue(shell.isClosed());
 *
 * </pre>
 *  * * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class CLI {

    public static final int DEFAULT_SSH_PORT = 22;

    CLITool cliTool;

    String host;
    int port;
    String user;
    String password;
    private Path keyFile;
    private AuthenticationMethod authenticationMethod = AuthenticationMethod.PASSWORD;

    private final TargetHost tunnelHost;

    /**
     * Instantiates the <code>CLI</code> object with given
     * <code>host</code>
     * Initializes itself suitable CLITool.
     *
     * @param host host
     * @see CLI#CLI(String, int, String, String)
     */
    public CLI(Host host) {
        this(host.getIp(), getPort(host), host.getUser(), host.getPass());
    }

    /**
     * Instantiates the <code>CLI</code> object with given <code>host</code> and <code>keyFile</code> for public key authentication
     *
     * @param host
     * @param keyFile
     */
    public CLI(final Host host, final Path keyFile) {
        this(host.getIp(), getPort(host), host.getUser(), keyFile);
    }

    /**
     * Instantiates the <code>CLI</code> object with given
     * <code>host</code> and <code>user</code>
     * Initializes itself suitable CLITool.
     *
     * @param host host
     * @param user user
     * @see CLI#CLI(String, int, String, String)
     */
    public CLI(Host host, User user) {
        this(host.getIp(), getPort(host), user.getUsername(), user.getPassword());
    }

    /**
     * Instantiates the <code>CLI</code> object with given <code>host</code>, <code>user</code> and <code>keyFile</code> for public key
     * authentication
     *
     * @param host
     * @param user
     * @param keyFile
     */
    public CLI(final Host host, final User user, final Path keyFile) {
        this(host.getIp(), getPort(host), user.getUsername(), keyFile);
    }

    /**
     * Instantiates the <code>CLI</code> object with given <code>host</code>,<code>port</code>,
     * <code>username</code> and <code>password</code>.
     * Initializes itself suitable CLITool.
     *
     * @param host     hostname
     * @param port     port number
     * @param user     user name
     * @param password user password
     * @see CLI#init()
     */
    public CLI(String host, int port, String user, String password) {
        this(host, port, user, password, null, null);
    }

    /**
     * Instantiates the <code>CLI</code> object with given <code>host</code>, <code>port</code>, <code>username</code> and <code>keyFile</code> for
     * public key
     * authentication
     *
     * @param host
     * @param port
     * @param user
     * @param keyFile
     */
    public CLI(final String host, final int port, final String user, final Path keyFile){
        this(host, port, user, null, null, keyFile);
        this.authenticationMethod = AuthenticationMethod.PUBLIC_KEY;
    }

    @API(API.Quality.Internal)
    public CLI(String host, int port, String user, String password, TargetHost tunnelHost, Path keyFile) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.tunnelHost = tunnelHost;
        this.keyFile = keyFile;
        init();
    }

    /**
     * Get the port number from the Host, and convert it into integer
     *
     * @param host to get the port number for the SSH
     * @return SSH port number
     * @throws CLIToolException if string presentation of port isn't correct number format
     */
    static int getPort(Host host) {
        String port = host.getPort().get(Ports.SSH);
        if (port == null || port.trim().isEmpty()) {
            return DEFAULT_SSH_PORT;
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException nfe) {
            throw new CLIToolException("Wrong number format for SSH port [" + port + "] in host " + host.getHostname(), nfe);
        }
    }

    /**
     * Initializes itself suitable CLITool.
     * Uses the <code>system property "taf.cli.clitool"</code> as provider name to find a suitable
     * {@link CLIToolProvider} by calling his method {@link CLIToolProvider#getName()}.
     * Default provider name is <code>"jsch"</code>.
     * Use the {@link CLIToolProvider} to create a suitable {@CLITool}
     */
    protected void init() {
        cliTool = CLIToolProvider.provide();
    }

    /**
     * Create {@link Shell} object and  create SSH shell in {@link CLITool}
     *
     * @return new shell object
     */
    public Shell openShell() {
        return openShell(Terminal.VT100);
    }

    /**
     * Create {@link Shell} object and  create SSH shell in {@link CLITool}
     *
     * @param terminal shell terminal or <doe>null</doe> if it does not matter
     * @return new shell object
     */
    public Shell openShell(Terminal terminal) {
        final String shellId;
        if(authenticationMethod.equals(AuthenticationMethod.PASSWORD)) {
            shellId = cliTool.openShell(host, port, user, password, terminal);
        } else if(authenticationMethod.equals(AuthenticationMethod.PUBLIC_KEY)) {
            shellId = cliTool.openShell(host, port, user, keyFile, terminal);
        } else {
            throw new UnsupportedOperationException("Can't determine authentication method");
        }
        return createShell(shellId);
    }

    /**
     * Create {@link Shell} object and  create telnet shell in {@link CLITool}
     *
     * @param telnetPort to be used for telnet connection
     * @return new shell object
     */
    public Shell openTelnetShell(int telnetPort) {
        String shellId = cliTool.openTelnet(host, telnetPort);
        return createShell(shellId);
    }

    /**
     * Create {@link Shell} and execute the command on it<br />
     * Command will be like a single command, or a list of commands that can be executed one after the other
     *
     * @param commands executed commands
     * @return new shell object, representing the shell result of the executed command
     */
    public Shell executeCommand(String... commands) {
        return executeCommand(Terminal.VT100, commands);
    }

    /**
     * Create {@link Shell} and execute the command on it<br />
     * Command will be like a single command, or a list of commands that can be executed one after the other
     * <p/>
     * Example:
     * <pre>
     * CLI cli = new CLI(host);
     * Shell shell = cli.executeCommand(Terminal.VT100,"echo \"Hello World!\"");
     * assertTrue(shell.expect("Hello World!"));
     * assertTrue(shell.expectClose());
     * assertEquals(shell.getExitValue(), 0);
     * assertTrue(shell.isClosed());
     * </pre>
     * or
     * <pre>
     * CLI cli = new CLI(host);
     * Shell shell = cli.executeCommand(Terminal.VT100,"read -p \"Enter text:\" text","echo \"Text:$text\"" );
     * assertTrue(shell.expect("Enter text:"));
     * shell.writeln("TEST_MESSAGE");
     * assertTrue(shell.expect("Text:TEST_MESSAGE"));
     * assertTrue(shell.expectClose());
     * assertEquals(shell.getExitValue(), 0);
     * assertTrue(shell.isClosed());
     * </pre>
     *
     * @param commands executed commands
     * @param terminal shell terminal or <doe>null</doe> if it does not matter
     * @return new shell object, representing the shell result of the executed command
     */
    public Shell executeCommand(Terminal terminal, String... commands) {
        final String shellId;
        if(authenticationMethod.equals(AuthenticationMethod.PASSWORD)) {
            shellId = cliTool.executeCommand(host, port, user, password, terminal, tunnelHost, commands);
        } else if(authenticationMethod.equals(AuthenticationMethod.PUBLIC_KEY)) {
            shellId = cliTool.executeCommand(host, port, user, keyFile, terminal, tunnelHost, commands);
        } else {
            throw new UnsupportedOperationException("Can't determine authentication method");
        }
        return createShell(shellId);
    }

    /**
     * Create {@link Shell} object and binding it to a existing SSH session in CLITool
     *
     * @param id session identifier
     * @return new shell object
     */
    public Shell createShell(String id) {
        return new CLIShell(id, cliTool);
    }

    /**
     * Open a shell with null terminal type
     *
     * @return
     */
    protected Shell openNullTerminalShell() {
        return openShell(Terminal.NULL);
    }

    /**
     * Closes all sessions
     */
    public void close() {
        cliTool.close();
    }
}

