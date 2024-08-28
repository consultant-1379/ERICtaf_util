package com.ericsson.cifwk.taf.handlers.netsim.domain;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimSession;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Group of simulations, a facility to run the set of the same commands against a group of particular NetSim simulations.
 */
public class SimulationGroup extends AbstractCommandBatchExecutor implements Collection<Simulation> {

    private final List<Simulation> simulations;

    public SimulationGroup(Simulation... simulations) {
        this.simulations = Lists.newArrayList(simulations);
    }

    public SimulationGroup(List<Simulation> simulations) {
        this.simulations = simulations;
    }

    /**
     * Finds a simulation by name in this group.
     *
     * @param simName
     *         simulation name
     * @return simulation found by name, or <code>null</code> if it wasn’t found.
     */
    public Simulation get(String simName) {
        for (Simulation sim : simulations) {
            if (StringUtils.equals(simName, sim.getName())) {
                return sim;
            }
        }
        return null;
    }

    /**
     * Returns the list of all network elements that belong to simulations in this group
     * @return	list of all network elements that belong to simulations in this group
     */
    public List<NetworkElement> getAllNEs() {
        List<NetworkElement> allNEs = Lists.newArrayList();
        for (Map.Entry<NetSimContext, SimulationGroup> entry: getSimulationsByContext().entrySet()) {
            NetSimContext context = entry.getKey();
            SimulationGroup simulationGroup = entry.getValue();
            NetSimSession session = context.openSession();
            allNEs.addAll(simulationGroup.getAllNEs(session));
            session.close();
        }
        return allNEs;
    }


    /**
     * Returns the list of all network elements that belong to simulations in this group
     * but only checks for network elements on a particular context or session
     *
     * @return list of all network elements that belong to simulations in this group on the given context/session
     */
    private List<NetworkElement> getAllNEs(NetSimCommandExecutor executor) {
        List<NetworkElement> allNEs = Lists.newArrayList();
        for (Simulation simulation : simulations) {
            allNEs.addAll(simulation.getAllNEs(executor));
        }
        return allNEs;
    }

    /**
     * Executes the provided commands for all the group members, and returns a Map where the result is mapped to appropriate element.
     *
     * @param commands
     *         commands to execute. Please note that the element sequence in the array defines the execution sequence.
     * @return a Map where the result is mapped to appropriate element.
     */
    public Map<Simulation, NetSimResult> exec(NetSimCommand... commands) {
        return exec(Lists.newArrayList(commands));
    }

    /**
     * Executes the provided commands for all the group members, and returns a Map where the result is mapped to appropriate element.
     *
     * @param commands
     *         commands to execute. Please note that the element sequence in the list defines the execution sequence.
     * @return a Map where the result is mapped to appropriate element.
     */
    public Map<Simulation, NetSimResult> exec(List<NetSimCommand> commands) {
        Map<Simulation, NetSimResult> results = Maps.newHashMap();
        for (final Simulation simulation : simulations) {
            NetSimCommand simulationOpenCommand = NetSimCommands.open(simulation.getName());

            NetSimResult result = runWithAdditionalCommands(commands, Lists.newArrayList(simulationOpenCommand), new Function<List<NetSimCommand>, NetSimResult>() {
                @Override
                public NetSimResult apply(List<NetSimCommand> commands) {
                    return simulation.exec(commands);
                }
            });
            results.put(simulation, result);
        }

        return results;
    }

    /**
     * Returns the group members, mapped to their names
     *
     * @return group members, mapped to their names
     */
    public Map<String, Simulation> getSimulationsMap() {
        Map<String, Simulation> results = Maps.newHashMap();
        for (Simulation sim : simulations) {
            results.put(sim.getName(), sim);
        }
        return results;
    }

    /**
     * Returns the list of group members
     *
     * @return list of group members
     */
    public List<Simulation> getSimulations() {
        return Lists.newArrayList(simulations);
    }

    /**
     * Returns simulation groups, each mapped to the context of the simulations in the groups
     *
     * @return simulation groups, each mapped to the context of the simulations in the groups
     */
    public Map<NetSimContext, SimulationGroup> getSimulationsByContext() {
        Map<NetSimContext, SimulationGroup> simulationsByContext = Maps.newHashMap();
        for (Simulation simulation : simulations) {
            NetSimContext simulationContext = simulation.getContext();
            SimulationGroup simulationGroup = new SimulationGroup();
            if (simulationsByContext.containsKey(simulationContext)) {
                simulationGroup = simulationsByContext.get(simulationContext);
            }
            simulationGroup.add(simulation);
            simulationsByContext.put(simulationContext, simulationGroup);
        }
        return simulationsByContext;
    }

    @Override
    public int size() {
        return simulations.size();
    }

    /**
     * Adds a new instance of {@link NetworkElement} to the group.
     *
     * @param simulation
     *         new simulation in the group. Please note that <code>SimulationGroup</code> is backed by the {@link java.util.List},
     *         so technically you can add the same simulation many times.
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(Simulation simulation) {
        return simulations.add(simulation);
    }

    @Override
    public boolean isEmpty() {
        return simulations.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return simulations.contains(o);
    }

    @Override
    public Iterator<Simulation> iterator() {
        return simulations.iterator();
    }

    @Override
    public Object[] toArray() {
        return simulations.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return simulations.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return simulations.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return simulations.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Simulation> c) {
        return simulations.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return simulations.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return simulations.retainAll(c);
    }

    @Override
    public void clear() {
        simulations.clear();
    }
}
