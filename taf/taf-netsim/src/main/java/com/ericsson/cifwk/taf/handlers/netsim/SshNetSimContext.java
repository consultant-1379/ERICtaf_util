package com.ericsson.cifwk.taf.handlers.netsim;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.ShowStartedCommand;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkMap;
import com.ericsson.cifwk.taf.handlers.netsim.exceptions.NetworkMapCopyException;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * SSH implementation of {@link NetSimContext}
 */
class SshNetSimContext extends AbstractMonitoringCommandBatchExecutor implements NetSimContext {

    private static final Logger log = LoggerFactory.getLogger(SshNetSimContext.class);

    private final CLI cli;
    private final String hostName;
    private final Host host;
    private final List<NetSimSession> openedSessions = Collections.synchronizedList(Lists
            .<NetSimSession>newArrayList());
    private Shell shell;
    private boolean closed = false;

    /**
     * Variable might be accessed by multiple threads,
     * volatile guarantees it is not cached by threads.
     */
    private volatile NetworkMap networkMap;

    public SshNetSimContext(Host host) {
        this(new CLI(host), host);
    }

    @VisibleForTesting
    protected SshNetSimContext(CLI cli, Host host) {
        this.cli = cli;
        this.hostName = host.getHostname();
        this.host = host;
    }

    /**
     * Synchronized lazy initialization. Guarantees variable networkMap
     *
     * @return
     */
    @Override
    public NetworkMap getNetworkMap() {
        if (networkMap == null) {
            refreshNetworkMapData();
        }
        return networkMap;
    }

    @Override
    protected String preparedCommandSet(List<NetSimCommand> commands) {
        return NetSimCommandEmitter.buildCommandLine(commands);
    }

    @Override
    protected NetSimResult processTextResult(String output) {
        System.out.println("From processTextResult Netsim type.......");
        return new NetSimResult(output);
    }

    @Override
    protected synchronized String executeCommandSetAndGetOutput(String fullCommand) {
        return executeCommandSetAndGetOutput(getNetsimCommandTimeoutSeconds(), fullCommand);
    }

    private String executeCommandSetAndGetOutput(int timeOutSeconds, String fullCommand) {
        this.shell = cli.executeCommand(Terminal.XTERM, fullCommand);
        String output = "";
        long runUntil = System.currentTimeMillis() + (timeOutSeconds > 0 ? (timeOutSeconds * 1000) : 100);
        while (!shell.isClosed()) {
            long msLeft = runUntil - System.currentTimeMillis();
            if (msLeft > 0) {
                output += shell.read();
                System.out.println("From executeCommandsetandgetOutput..... " + output);
            } else {
                String msg = String.format("Timeout while waiting for the commands [%s] to finish execution,timeout set is %s Seconds", fullCommand, timeOutSeconds);
                log.warn(msg, fullCommand, timeOutSeconds);
                throw new NetSimException(msg);
            }
        }
        shell.disconnect();
        System.out.println("Outside while executeCommandsetandgetOutput..... " + output);
        return output;
    }

    @Override
    public synchronized void close() {
        checkIfClosed();
        log.debug("Closing the current stateless NetSim shell");
        if (shell != null) {
            if (!shell.isClosed()) {
                shell.disconnect();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Closing open sessions (%s instances)", getOpenSessionsAmount()));
        }
        for (NetSimSession session : openedSessions) {
            if (!session.isClosed()) {
                session.close();
            }
        }
        openedSessions.clear();
        cli.close();
        closed = true;
    }

    @Override
    public synchronized NetSimSession openSession() {
        checkIfClosed();
        Shell shell = openShell();

        SshNetSimSession sshNetSimSession = new SshNetSimSession(shell);
        openedSessions.add(sshNetSimSession);

        return sshNetSimSession;
    }

    protected Shell openShell() {
        Shell shell = cli.openShell(Terminal.XTERM);
        shell.writeln(ShellCommands.NETSIM_SHELL_CMD);
        // Remove the NetSim shell start command from the output stack
        shell.read(1);
        return shell;
    }

    protected int getOpenSessionsAmount() {
        int result = 0;
        for (NetSimSession session : openedSessions) {
            if (!session.isClosed()) {
                result++;
            }
        }
        return result;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public Host getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "SshNetsimContext [hostName=" + hostName + "]";
    }

    @Override
    public boolean isNetSimRunning() {
        checkIfClosed();
        NetSimResult execResult = exec(NetSimCommands.showStarted());
        String executionStartPattern = NetSimCommandEmitter.getCmdExecutionStartPattern(ShowStartedCommand.class);
        String rawOutput = execResult.getRawOutput();

        return rawOutput.matches(executionStartPattern);
    }

    @Override
    protected void checkIfClosed() {
        if (closed) {
            throw new NetSimException("Context is closed");
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void refreshNetworkMapData() {
        log.info("Initializing network map for host - " + host.toString());
        try {
            File networkMapTempFile = copyNetworkMapFromHost();
            log.info("Copied network map file");

            try (InputStream neIn = new FileInputStream(networkMapTempFile)) {
                networkMap = new NetworkMap(this, neIn);
            }
            log.info("Deleting temporary Netsim Network Map file. Result = " + networkMapTempFile.delete());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Network map for host - " + host.toString() + " initialized");
    }

    private File copyNetworkMapFromHost() throws IOException {
        File tmpFile = File.createTempFile("networkMap_", ".json");

        RemoteObjectHandler remoteObjectHandler = new RemoteObjectHandler(host);
        for (int i = 1; i < 4; i++) {
            try {
                boolean copied = remoteObjectHandler.copyRemoteFileToLocal("/netsim/netsimdir/networkMap.json", tmpFile.getAbsolutePath());
                log.info("Copying Netsim Network Map file into temporary. Result = " + copied);

                if (copied) {
                    return tmpFile;
                }
                log.info(String.format("Attempt %d :Failed to copy Netsim Network Map file into temporary location. retrying",i));
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error("Failed to copy Network Map file", e);
            }
        }
        throw new NetworkMapCopyException("Failed to copy Network Map file 3 times, please check the version of the netsim");
    }
}
