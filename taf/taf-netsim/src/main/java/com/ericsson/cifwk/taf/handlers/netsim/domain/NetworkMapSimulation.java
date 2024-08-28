package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElementImpl.NE_NAME;
import static com.googlecode.cqengine.query.QueryFactory.equal;

import java.util.Collections;
import java.util.List;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.OpenCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SelectnocallbackCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.StartCommand;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

class NetworkMapSimulation extends AbstractCommandBatchExecutor implements Simulation {

    static final Attribute<Simulation, String> SIM_NAME = new SimpleAttribute<Simulation, String>() {
        @Override
        public String getValue(Simulation simulation, QueryOptions queryOptions) {
            return simulation.getName();
        }
    };

    private final NetSimContext context;
    private final String simName;
    private List<NetworkElement> networkElements = Collections.EMPTY_LIST;
    private ConcurrentIndexedCollection<NetworkElement> indexedNetworkElements;

    NetworkMapSimulation(NetSimContext context, String simName) {
        this.context = context;
        this.simName = simName;
    }

    void setNetworkElements(List<NetworkElement> elements) {
        indexedNetworkElements = new ConcurrentIndexedCollection<>();
        indexedNetworkElements.addAll(elements);
        indexedNetworkElements.addIndex(HashIndex.onAttribute(NetworkElementImpl.NE_NAME));
        networkElements = Collections.unmodifiableList(elements);
    }

    @Override
    public NetworkElement getNetworkElement(String neName) {
        ResultSet<NetworkElement> retrieve = indexedNetworkElements.retrieve(equal(NE_NAME, neName));
        if (retrieve.isNotEmpty()) {
            return retrieve.uniqueResult();
        }
        return null;
    }

    @Override
    public String getName() {
        return simName;
    }

    @Override
    public List<NetworkElement> getAllNEs() {
        return networkElements;
    }

    @Override
    public List<NetworkElement> getAllNEs(NetSimCommandExecutor executor) {
        return networkElements;
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
        final NetworkMapSimulation other = (NetworkMapSimulation) obj;

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

    @Override
    public List<NetworkElement> getStartedNEs() {
        NetSimResult execResult = exec(NetSimCommands.showSimnes(),
                NetSimCommands.showStarted());
        return new NetSimResultMapper(context, this).mapToNetworkElements(execResult);
    }
}
