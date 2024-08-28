package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.List;
import java.util.UUID;

import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.handlers.netsim.domain.AbstractCommandBatchExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class AbstractMonitoringCommandBatchExecutor extends AbstractCommandBatchExecutor implements NetSimCommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(AbstractMonitoringCommandBatchExecutor.class);

    /**
     * Property to set maximum timeout for executing the netsim command; Default is 300 seconds
     */
    public static final String NETSIM_COMMAND_TIMEOUT_PROPERTY = "taf.netsim.command.timeout";

    private int netsimCommandTimeoutSeconds = TafConfigurationProvider.provide().getProperty(NETSIM_COMMAND_TIMEOUT_PROPERTY, 300, Integer.class);

    @Override
    public final NetSimResult exec(NetSimCommand... commands) {
        return exec(Lists.newArrayList(commands));
    }

    @Override
    public final NetSimResult exec(List<NetSimCommand> commands) {
        checkIfClosed();

        String fullCommand = preparedCommandSet(commands);
        String executionId = UUID.randomUUID().toString();

        log.info(String.format("Running '%s' command(s) (execution ID = %s)", fullCommand, executionId));

        String output = executeCommandSetAndGetOutput(fullCommand);
        log.info(String.format("The result of the command set %s is '%s'", executionId, output));

        NetSimResult result = processTextResult(output);

        log.info(String.format("The netsim result is '%s'", result.toString()));

        verifyResult(commands, executionId, result);

        return result;
    }

    void verifyResult(List<NetSimCommand> commands, String executionId, NetSimResult result) {
        if (result.getOutput().length != commands.size()) {
            log.warn(String.format("Command set (executionId=%s) output was supposed to return %d results, but returned %d. Raw output: '%s'",
                    executionId, commands.size(), result.getOutput().length, result.getRawOutput()));
        }
    }

    protected abstract String preparedCommandSet(List<NetSimCommand> commands);

    protected abstract String executeCommandSetAndGetOutput(String fullCommand);

    protected abstract NetSimResult processTextResult(String output);

    protected abstract void checkIfClosed();

    protected int getNetsimCommandTimeoutSeconds() {
        return netsimCommandTimeoutSeconds;
    }

    protected void setNetsimCommandTimeoutSeconds(int netsimCommandTimeoutSeconds) {
        this.netsimCommandTimeoutSeconds = netsimCommandTimeoutSeconds;
    }
}
