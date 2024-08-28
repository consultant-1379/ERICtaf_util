package com.ericsson.cifwk.taf.handlers.netsim.domain;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimException;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.OpenCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SelectnocallbackCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.ShowSimnesCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.StartCommand;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

class SimulationImpl extends AbstractCommandBatchExecutor implements Simulation {
    public static final String NE_NAME = "NE Name";
    private final NetSimContext context;
    private final String simName;

    NetSimResultMapper mapper;

    SimulationImpl(NetSimContext context, String simName) {
        this.context = context;
        this.simName = simName;
        mapper = new NetSimResultMapper(context, this);
    }

    @Override
    public NetworkElement getNetworkElement(String neName) {
        List<NetworkElement> allNEs = getAllNEs();
        for (NetworkElement ne : allNEs) {
            if (StringUtils.equals(ne.getName(), neName)) {
                return ne;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return simName;
    }

    @Override
    public List<NetworkElement> getAllNEs() {
        return getAllNEs(context);
    }

    @Override
    public List<NetworkElement> getAllNEs(NetSimCommandExecutor executor) {
        ShowSimnesCommand showSimnes = NetSimCommands.showSimnes();
        NetSimResult simNEs = exec(executor, showSimnes);

        CommandOutput[] output = simNEs.getOutput();

        if (output.length > 0) {
            return mapper.parse(output[0]);
        }
        throw new NetSimException(String.format("Error parsing raw Output, No valid Output received from netsim. Output from Netsim:\n %s",simNEs
                .getRawOutput()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(simName, context);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimulationImpl other = (SimulationImpl) obj;

        return Objects.equal(this.simName, other.simName)
                && Objects.equal(this.context, other.context);
    }

    @Override
    public String toString() {
        return "NetsimSimulationImpl [context=" + context + ", simName=" + simName + "]";
    }

    @Override
    public NetSimResult exec(NetSimCommand... netSimCommands) {
        return exec(context, netSimCommands);
    }

    private NetSimResult exec(NetSimCommandExecutor executor, NetSimCommand... netSimCommands) {
        return runWithAdditionalCommands(netSimCommands, getAdditionalCommands(), executor);
    }

    @Override
    public NetSimResult exec(List<NetSimCommand> netSimCommands) {
        return exec(context, netSimCommands);
    }

    private NetSimResult exec(NetSimCommandExecutor executor, List<NetSimCommand> netSimCommands) {
        return runWithAdditionalCommands(netSimCommands, getAdditionalCommands(), executor);
    }

    private List<NetSimCommand> getAdditionalCommands() {
        List<NetSimCommand> additionalCommands = Lists.newArrayList();
        additionalCommands.add(NetSimCommands.open(this.getName()));
        return additionalCommands;
    }

    @Override
    public NetSimContext getContext() {
        return context;
    }

    @Override
    public List<NetworkElement> getStartedNEs() {
        NetSimResult execResult = exec(NetSimCommands.showSimnes(),
                NetSimCommands.showStarted());

        return mapper.mapToNetworkElements(execResult);
    }

    @Override
    public boolean stopAllStartedNEs() {
        NetSimCommand stopCommand =  NetSimCommands.stop().setParallel(true);
        return executeCommand(stopCommand, getStartedNEs());
    }

    @Override
    public boolean startAllNEs() {
        StartCommand startCommand = NetSimCommands.start().setParallel(true);
        return executeCommand(startCommand, getAllNEs());
    }

    private boolean executeCommand(NetSimCommand command, List<NetworkElement> networkElements) {
        String[] startedNeNames = getNamesOfNetworkElements(networkElements);
        OpenCommand openCommand = NetSimCommands.open(simName);
        SelectnocallbackCommand selectnocallbackCommand = NetSimCommands.selectnocallback(Joiner.on(" ").join(startedNeNames));
        NetSimResult result = exec(openCommand, selectnocallbackCommand, command);
        return result.getRawOutput().contains("OK");
    }

    private String[] getNamesOfNetworkElements(final List<NetworkElement> networkElements) {
        return FluentIterable.from(networkElements).transform(new Function<NetworkElement, String>() {
            @Override
            public String apply(final NetworkElement networkElement) {
                return networkElement.getName();
            }
        }).toArray(String.class);
    }
}
