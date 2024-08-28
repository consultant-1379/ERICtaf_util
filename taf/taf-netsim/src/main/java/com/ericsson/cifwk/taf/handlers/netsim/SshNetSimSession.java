package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.handlers.netsim.commands.EchoCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.google.common.collect.Lists;

/**
 * SSH implementation of {@link NetSimSession}
 */
class SshNetSimSession extends AbstractMonitoringCommandBatchExecutor implements NetSimSession {

    private static final String ECHO_DELIMITER = "END_OF_COMMAND_BATCH";

    private static final Pattern BATCH_FINISH_PATTERN = Pattern.compile(NetSimCommandEmitter.getCmdExecutionStartPattern(EchoCommand.class, ECHO_DELIMITER));

    private final Shell shell;

    private boolean closed = false;

    private static final Logger log = LoggerFactory.getLogger(SshNetSimSession.class);

    public SshNetSimSession(Shell shell) {
        this.shell = shell;
    }

    @Override
    public void close() {
        checkIfClosed();
        if (shell != null && !shell.isClosed()) {
            shell.disconnect();
        }
        closed = true;
    }

    @Override
    protected String preparedCommandSet(List<NetSimCommand> commands) {
        ArrayList<NetSimCommand> allCommands = Lists.newArrayList(commands);
        allCommands.add(NetSimCommands.echo(ECHO_DELIMITER));
        return NetSimCommandEmitter.buildShellCommand(allCommands);
    }

    @Override
    protected String executeCommandSetAndGetOutput(String fullCommand) {
        return executeCommandSetAndGetOutput(getNetsimCommandTimeoutSeconds(), fullCommand);
    }

    /**
     * @return Result of whether command execution finished, else timeout exception is thrown.
     * @Description Method which executes command(s) allowing user to provide timeout value
     */
    public String executeCommandSetAndGetOutput(int timeout, String fullCommand) {
        shell.writeln(fullCommand);
        try {
            return shell.expect(BATCH_FINISH_PATTERN, timeout);
        } catch (Exception e) {
            throw new NetSimException(String.format("Timeout while waiting for the commands to finish execution,timeout set is %d Seconds", timeout));
        }
    }

    @Override
    protected NetSimResult processTextResult(String output) {
        System.out.println("From processTextResult getResultWithoutEcho type.......");
        return getResultWithoutEcho(output);
    }

    @Override
    protected void checkIfClosed() {
        if (closed) {
            throw new NetSimException("Session is closed");
        }
    }

    private NetSimResult getResultWithoutEcho(String output) {
        CommandOutput[] commandResults = NetSimResult.parseRawOutput(output);
        // First result coming through J2Sch shell channel is an echo of the command - need to remove it
        // The last result is response to "echo" command which is used as demarcation point - need to remove, too
        return new NetSimResult(output, Arrays.copyOfRange(commandResults, 1, commandResults.length - 1));
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public NetSimResult exec(int timeout, NetSimCommand... commands) {
        return exec(timeout, Lists.newArrayList(commands));
    }

    @Override
    public NetSimResult exec(int timeout, List<NetSimCommand> commands) {
        checkIfClosed();

        String fullCommand = preparedCommandSet(commands);
        String executionId = UUID.randomUUID().toString();
        if (log.isTraceEnabled()) {
            log.trace(String.format("Running '%s' command(s) (execution ID = %s)", fullCommand, executionId));
        }
        String output = executeCommandSetAndGetOutput(timeout, fullCommand);
        if (log.isTraceEnabled()) {
            log.trace(String.format("The result of the command set %s is '%s'", executionId, output));
        }

        NetSimResult result = processTextResult(output);
        verifyResult(commands, executionId, result);

        return result;
    }
}
