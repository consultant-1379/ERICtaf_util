package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

/**
 * Harness for NetSim command execution results, and a utility to get these
 * results in different projections.
 */
public class NetSimResult {
    private final CommandOutput[] structuredOutput;
    private final String rawOutput;

    /**
     * Creates an instance of <code>NetSimResult</code>, using raw output from
     * NetSim command(-s) execution.
     * 
     * @param rawOutput
     *            raw output from NetSim command(-s) execution.
     */
    public NetSimResult(String rawOutput) {
        this.rawOutput = rawOutput;
        this.structuredOutput = parseRawOutput(rawOutput);
    }

    // We need this constructor to be able to remove the output parts in
    // structured output that belong to additional commands
    // that were not requested but added by Netsim engine (for example, to open
    // simulation and select the node).
    // Raw output will still contain the execution results for those commands.
    public NetSimResult(String rawOutput, CommandOutput[] structuredOutput) {
        this.rawOutput = rawOutput;
        this.structuredOutput = structuredOutput;
    }

    /**
     * Returns raw output from NetSim command execution
     * 
     * @return raw output from NetSim command execution. Please note that NetSim
     *         classes that run the commands may add some additional commands to
     *         distinguish the results ("<code>.echo</code>") or to pre-select
     *         the appropriate entity. In this case the raw output will contain
     *         the traces of these commands.
     */
    public String getRawOutput() {
        return rawOutput;
    }

    /**
     * Returns the array of command outputs.
     * 
     * @return array of command outputs. The element amount is equal to the
     *         amount of commands that were executed.
     */
    public CommandOutput[] getOutput() {
        return structuredOutput;
    }

    /**
     * Parses the NetSim output.
     * 
     * @param rawOutput
     *            command execution output to parse.
     * @return array of command outputs. The element amount is equal to the
     *         amount of commands that were executed.
     */
    public static final CommandOutput[] parseRawOutput(String rawOutput) {
        Iterable<String> byLine = Splitter.on(NetSimConstants.NEW_LINE).split(rawOutput);
        List<CommandOutput> outputs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String line : byLine) {
            if (line.startsWith(NetSimConstants.COMMAND_EXEC_START) && builder.length() > 0) {
                outputs.add(new CommandOutput(builder.toString()));
                builder.setLength(0);
            }
            builder.append(line).append(NetSimConstants.NEW_LINE);
        }
        if (builder.length() > 0) {
            outputs.add(new CommandOutput(builder.toString()));
        }
        return outputs.toArray(new CommandOutput[outputs.size()]);
    }

    @Override
    public String toString() {
        return rawOutput;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rawOutput);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NetSimResult other = (NetSimResult) obj;

        return Objects.equal(this.rawOutput, other.rawOutput);
    }
}
