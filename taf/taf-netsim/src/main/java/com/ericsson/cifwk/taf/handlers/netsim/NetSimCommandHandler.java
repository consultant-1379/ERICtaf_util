package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.execution.TestExecutionEvent;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetSimSimulationFactory;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkMap;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.ericsson.cifwk.taf.handlers.netsim.domain.SimulationGroup;
import com.ericsson.cifwk.taf.handlers.netsim.implementation.NetsimShowAllSimNesOutputProcessor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * NetSim handler which manages multiple NetSim instances using sets of predefined commands.
 */
public class NetSimCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(NetSimCommandHandler.class);
    private static ThreadLocal<NetSimContextRegistry> contextRegistryContainer;

    private final NetSimContext[] contexts;

    static {
        contextRegistryContainer = new ThreadLocal<NetSimContextRegistry>() {
            @Override
            protected NetSimContextRegistry initialValue() {
                return new NetSimContextRegistry();
            }
        };
        log.debug("NetSim context registry initialized");
    }

    /**
     * Returns an instance of NetSimCommandHandler that works with the provided set of {@link Host}'s.
     *
     * @param hosts
     *         hosts to run the NetSim commands on
     * @return an instance of NetSimCommandHandler that works with the provided set of {@link Host}'s.
     * All methods that NetSimCommandHandler instance provides will return the results for all the hosts.
     */
    public static NetSimCommandHandler getInstance(List<Host> hosts) {
        Preconditions.checkArgument(hosts.size() > 0, "Provide one or more Hosts");

        NetSimContext[] contexts = new NetSimContext[hosts.size()];
        int i = 0;
        for (Host host : hosts) {
            NetSimContext context = getContext(host);
            contexts[i++] = context;
        }

        return new NetSimCommandHandler(contexts);
    }

    /**
     * Returns an instance of NetSimCommandHandler that works with the provided set of {@link Host}'s.
     *
     * @param hosts
     *         hosts to run the NetSim commands on
     * @return an instance of NetSimCommandHandler that works with the provided set of {@link Host}'s,
     * or if no {@link Host}'s provided, an instance that works with all discovered {@link Host}'s.
     * <p>
     * All methods that NetSimCommandHandler instance provides will return the results for all the hosts.
     */
    public static NetSimCommandHandler getInstance(Host... hosts) {
        List<Host> listHosts;
        if (hosts.length > 0) {
            listHosts = Arrays.asList(hosts);
        } else {
            listHosts = DataHandler.getAllHostsByType(HostType.NETSIM);
        }
        return getInstance(listHosts);
    }

    /**
     * Returns an instance of {@link NetSimContext} that works with the particular host.
     *
     * @param host
     *         a host that the context will be working with
     * @return an instance of {@link NetSimContext} that works with the particular host.
     */
    public static NetSimContext getContext(Host host) {
        Preconditions.checkArgument(host != null, "Host cannot be null");
        NetSimContextRegistry contextRegistry = contextRegistryContainer.get();
        return contextRegistry.getContext(host);
    }

    /**
     * Returns an instance of {@link NetSimSession} that works with the particular host.
     *
     * @param host
     *         a host that the session will be working with
     * @return an instance of {@link NetSimSession} that works with the particular host.
     */
    public static NetSimSession getSession(Host host) {
        NetSimContext context = getContext(host);
        return context.openSession();
    }

    private NetSimCommandHandler(NetSimContext[] context) {
        this.contexts = context;
    }

    /**
     * Executes the provided set of commands, and returns a mapping of the context (opened for appropriate host) to the appropriate result.
     *
     * @param commands
     *         commands to execute. Please note that the element sequence in the array defines the execution sequence.
     * @return results of execution, mapped to appropriate host context. Note that {@link NetSimContext#getHostName()} returns the host name,
     * equal to the value returned by {@link Host#toString()}
     */
    public Map<NetSimContext, NetSimResult> exec(NetSimCommand... commands) {
        return exec(Lists.newArrayList(commands));
    }

    /**
     * Executes the provided set of commands, and returns a mapping of the context (opened for appropriate host) to the appropriate result.
     *
     * @param commands
     *         commands to execute. Please note that the element sequence in the list defines the execution sequence.
     * @return results of execution, mapped to appropriate host context. Note that {@link NetSimContext#getHostName()} returns the host name,
     * equal to the value returned by {@link Host#toString()}
     */
    public Map<NetSimContext, NetSimResult> exec(List<NetSimCommand> commands) {
        Map<NetSimContext, NetSimResult> result = Maps.newHashMap();
        for (NetSimContext context : contexts) {
            result.put(context, context.exec(commands));
        }

        return result;
    }

    /**
     * Returns a group of all simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.
     *
     * @return group of all simulations on all hosts set for this <code>NetSimCommandHandler</code> instance
     */
    public SimulationGroup getAllSimulations() {
        return getSimulationsWithFilter();
    }

    /**
     * Returns a group of all network elements in all simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.
     *
     * @return group of all network elements in all simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.
     * Please note that NEs with the same name from different simulations and/or hosts will be treated as different entities
     * (<code>equals()</code> will return <code>false</code>)
     */
    public NeGroup getAllNEs() {
        return new NeGroup(getAllSimulations().getAllNEs());
    }

    /**
     * Returns a group of all the started network elements in all simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.<br />
     * Please note that NEs with the same name from different simulations and/or hosts will be treated as different entities
     * (<code>equals()</code> will return <code>false</code>)
     *
     * @return group of all started network elements in all simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.
     */
    public NeGroup getAllStartedNEs() {
        final Map<NetSimContext, NetSimResult> startedCmdResults = exec(NetSimCommands.showAllsimnes());
        final NetsimShowAllSimNesOutputProcessor processor = new NetsimShowAllSimNesOutputProcessor(startedCmdResults);

        return processor.getAllStartedNEs();
    }

    /**
     * Returns a group of simulations, identified by the provided names, on all hosts set for this <code>NetSimCommandHandler</code> instance.
     *
     * @param simNames
     *         simulation names
     * @return group of simulations, identified by the provided names. If Host1 contains simulations A and B, and Host2 contains
     * simulations B and D, getSimulations("A","B","C") will return a group containing A,B(Host1),B(Host2) simulations.
     */
    public SimulationGroup getSimulations(String... simNames) {
        Preconditions.checkArgument(simNames.length > 0, "Provide one or more simulation names");
        return getSimulationsWithFilter(simNames);
    }

    /**
     * Returns a group of network elements that belong to the provided set of simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.
     *
     * @param simNames
     *         simulation names
     * @return group of network elements that belong to the provided set of simulations on all hosts set for this <code>NetSimCommandHandler</code> instance.
     */
    public NeGroup getSimulationNEs(String... simNames) {
        Preconditions.checkArgument(simNames.length > 0, "Provide one or more simulation names");

        SimulationGroup sims = getSimulationsWithFilter(simNames);

        return new NeGroup(sims.getAllNEs());
    }

    /**
     * Checks if a network element with provided name is started on any host set for this <code>NetSimCommandHandler</code> instance.
     *
     * @param neName
     *         network element name
     * @return returns <code>true</code> if a network element with provided name is started, <code>false</code> otherwise.
     */
    public boolean isStarted(String neName) {
        for (NetSimContext context : contexts) {
            NetworkMap networkMap = context.getNetworkMap();
            NetworkElement ne = networkMap.findNetworkElement(neName);
            if (ne != null) {
                return ne.isStarted();
            }
        }
        return false;
    }

    protected SimulationGroup getSimulationsWithFilter(String... simNamesToInclude) {
        SimulationGroup result = new SimulationGroup();
        for (NetSimContext context : contexts) {
            NetworkMap networkMap = context.getNetworkMap();
            List<Simulation> simulations = networkMap.getSimulations(simNamesToInclude);
            result.addAll(simulations);
        }
        return result;
    }

    List<Simulation> createSimulations(NetSimContext context, List<String> allSimNames, List<String> simNamesToInclude) {
        List<Simulation> result = Lists.newArrayList();
        boolean needToFilter = !simNamesToInclude.isEmpty();
        Collection<String> intersection = Sets.intersection(Sets.newHashSet(allSimNames), Sets.newHashSet(simNamesToInclude));

        for (String simName : allSimNames) {
            if (StringUtils.endsWith(simName, ".zip") || (needToFilter && !intersection.contains(simName))) {
                continue;
            }
            Simulation sim = NetSimSimulationFactory.getInstance(context, simName);
            result.add(sim);
        }

        return result;
    }

    /**
     * Closes all open NetSim contexts
     */
    public static void closeAllContexts() {
        NetSimContextRegistry contextRegistry = contextRegistryContainer.get();
        contextRegistry.close();
    }

    static int getOpenContextsAmount() {
        NetSimContextRegistry contextRegistry = contextRegistryContainer.get();
        return contextRegistry.getOpenContextsAmount();
    }

    // For unit test purposes
    static void setContextRegistry(NetSimContextRegistry newContextRegistry) {
        contextRegistryContainer.set(newContextRegistry);
    }

    private static boolean isCloseOnTest;
    private static boolean isCloseOnSuite;
    private static boolean isCloseOnExecution;

    /**
     * Set the default context close policy
     */
    public static void setDefaultContextClosePolicy() {
        isCloseOnTest = false;
        isCloseOnSuite = true;
        isCloseOnExecution = false;
    }

    /**
     * Close Netsim Conexts when appropriate Event Type is set
     *
     * @param eventType
     */
    public static void setContextClosePolicy(TestExecutionEvent eventType) {
        switch (eventType) {
            case ON_SUITE_FINISH:
                isCloseOnTest = false;
                isCloseOnExecution = false;
                isCloseOnSuite = true;
                break;
            case ON_TEST_FINISH:
                isCloseOnTest = true;
                isCloseOnExecution = false;
                isCloseOnSuite = false;
                break;
            case ON_EXECUTION_FINISH:
                isCloseOnTest = false;
                isCloseOnExecution = true;
                isCloseOnSuite = false;
                break;
            default:
        }
    }

    public static boolean isCloseOnTest() {
        return isCloseOnTest;
    }

    public static boolean isCloseOnSuite() {
        return isCloseOnSuite;
    }

    public static boolean isCloseOnExecution() {
        return isCloseOnExecution;
    }
}
