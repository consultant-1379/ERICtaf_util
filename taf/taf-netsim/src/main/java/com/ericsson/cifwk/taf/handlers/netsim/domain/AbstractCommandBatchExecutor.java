package com.ericsson.cifwk.taf.handlers.netsim.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.OpenCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SelectnocallbackCommand;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

public abstract class AbstractCommandBatchExecutor {
    protected NetSimResult runWithAdditionalCommands(NetSimCommand[] NetSimCommands, List<NetSimCommand> additionalCommands,
                                                     Function<List<NetSimCommand>, NetSimResult> execFunction) {
        ArrayList<NetSimCommand> allCommands = commandsInList(NetSimCommands);
        return runWithAdditionalCommands(allCommands, additionalCommands, execFunction);
    }

    protected NetSimResult runWithAdditionalCommands(List<NetSimCommand> NetSimCommands, List<NetSimCommand> additionalCommands,
            Function<List<NetSimCommand>, NetSimResult> execFunction) {
        ArrayList<NetSimCommand> allCommands = Lists.newArrayList(NetSimCommands);
        allCommands.addAll(0, additionalCommands);

        NetSimResult originalResult = execFunction.apply(allCommands);

        CommandOutput[] originalOutput = originalResult.getOutput();
        System.out.println("From AbstractCommandBatchExecutor.........");

        System.out.println("From the original output....");
        for(int m=0; m< originalOutput.length ; m++)
        {
            System.out.print(originalOutput[m] +"   ");
        }
        CommandOutput[] filteredOutput = Arrays.copyOfRange(originalOutput, additionalCommands.size(), originalOutput.length);
        System.out.println("From the filtered output ......");
        for(int m=0; m< filteredOutput.length ; m++)
        {
            System.out.print(filteredOutput[m] +"   ");
        }

        return new NetSimResult(originalResult.getRawOutput(), filteredOutput);
    }

    protected NetSimResult runWithAdditionalCommands(NetSimCommand[] NetSimCommands, List<NetSimCommand> additionalCommands,
            NetSimCommandExecutor executor) {
        ArrayList<NetSimCommand> allCommands = commandsInList(NetSimCommands);
        return runWithAdditionalCommands(allCommands, additionalCommands, executor);
    }

    protected NetSimResult runWithAdditionalCommands(List<NetSimCommand> netSimCommands, List<NetSimCommand> additionalCommands,
            NetSimCommandExecutor executor) {
        ArrayList<NetSimCommand> allCommands = Lists.newArrayList(netSimCommands);
        allCommands.addAll(0, additionalCommands);

        NetSimResult originalResult = executor.exec(allCommands);

        CommandOutput[] originalOutput = originalResult.getOutput();
        System.out.println("From AbstractCommandBatchExecutor.........");

        System.out.println("From the original output....");
        for(int m=0; m< originalOutput.length ; m++)
        {
            System.out.println(originalOutput[m]);
        }

        int cutoffAmount = additionalCommands.size();
        System.out.println("The cutoffcommands size is " + cutoffAmount);

        CommandOutput[] filteredOutput = Arrays.copyOfRange(originalOutput, cutoffAmount, originalOutput.length);

        System.out.println("From the filtered output ......");
        for(int m=0; m< filteredOutput.length ; m++)
        {
            System.out.println(filteredOutput[m]);
        }

        return new NetSimResult(originalResult.getRawOutput(), filteredOutput);
    }

    protected ArrayList<NetSimCommand> commandsInList(NetSimCommand... netSimCommands) {
        return Lists.newArrayList(netSimCommands);
    }


}
