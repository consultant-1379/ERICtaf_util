package com.ericsson.cifwk.taf.handlers.netsim.domain;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimSession;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Group of network elements, a facility to run the set of the same commands
 * against a group of particular network elements.
 */
public class NeGroup extends AbstractCommandBatchExecutor implements Collection<NetworkElement> {

    private final List<NetworkElement> neList;

    public NeGroup(NetworkElement... nes) {
        this.neList = Lists.newArrayList(nes);
    }

    public NeGroup(List<NetworkElement> nes) {
        this.neList = nes;
    }

    /**
     * Finds a NE by name in this group.
     * 
     * @param neName
     * @return returns a NE found by name, or <code>null</code> if it wasn’t
     *         found.
     */
    public NetworkElement get(String neName) {
        for (NetworkElement ne : neList) {
            if (StringUtils.equals(neName, ne.getName())) {
                return ne;
            }
        }

        return null;
    }

    /**
     * Executes the provided commands for all the group members, and returns a
     * Map where the result is mapped to appropriate element.
     * 
     * @param commands
     *            commands to execute. Please note that the element sequence in
     *            the array defines the execution sequence.
     * @return a Map where the execution result is mapped to appropriate
     *         element.
     */
    public Map<NetworkElement, NetSimResult> exec(NetSimCommand... commands) {
        Map<NetworkElement, NetSimResult> resultMap = Maps.newHashMap();
        sortNEsByHostAndSimulationName(neList);

        Simulation lastSimulation = null;
        NetSimSession session = null;
        try {
            for (final NetworkElement ne : neList) {

                String rawOutput = "";
                CommandOutput[] structuredOutput = new CommandOutput[0];

                List<NetSimCommand> additionalCommands = Lists.newArrayList();
                Simulation simulation = ne.getSimulation();
                NetSimContext context = simulation.getContext();

                if (!simulation.equals(lastSimulation)) {
                    if (session != null) {
                        session.close();
                    }
                    session = context.openSession();

                    NetSimCommand[] openCommand = {NetSimCommands.open(simulation.getName())};
                    NetSimResult openCommandResult = runWithAdditionalCommands(openCommand, additionalCommands, session);
                    rawOutput += openCommandResult.getRawOutput();
                    structuredOutput = openCommandResult.getOutput();
                }

                NetSimCommand[] selectCommand = {NetSimCommands.selectnocallback(ne.getName())};
                NetSimResult selectCommandResult = runWithAdditionalCommands(selectCommand, additionalCommands, session);
                rawOutput += selectCommandResult.getRawOutput();
                structuredOutput = (CommandOutput[]) ArrayUtils.addAll(structuredOutput, selectCommandResult.getOutput());

                NetSimResult execResult = runWithAdditionalCommands(commands, additionalCommands, session);
                rawOutput += execResult.getRawOutput();
                structuredOutput = (CommandOutput[]) ArrayUtils.addAll(structuredOutput, execResult.getOutput());

                resultMap.put(ne, new NetSimResult(rawOutput, structuredOutput));
                lastSimulation = ne.getSimulation();
            }
        }
        finally{
            if (session != null) {
                session.close();
            }
        }
        return resultMap;
    }

    private static void sortNEsByHostAndSimulationName(List<NetworkElement> neList) {
        Collections.sort(neList, new NetworkElementComparator());
    }

    /**
     * Returns the group members, mapped to their names
     * 
     * @return group members, mapped to their names
     */
    public Map<String, NetworkElement> getNEMap() {
        Map<String, NetworkElement> results = Maps.newHashMap();
        for (NetworkElement ne : neList) {
            results.put(ne.getName(), ne);
        }
        return results;
    }

    /**
     * Returns the list of group members
     * 
     * @return list of group members
     */
    public List<NetworkElement> getNetworkElements() {
        return Lists.newArrayList(neList);
    }

    /**
     * Adds a new instance of {@link NetworkElement} to the group.
     * 
     * @param ne
     *            network element. Please note that <code>NeGroup</code> is
     *            backed by the {@link java.util.List}, so technically you can
     *            add the same NE many times.
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(NetworkElement ne) {
        return neList.add(ne);
    }

    @Override
    public boolean isEmpty() {
        return neList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return neList.contains(o);
    }

    @Override
    public Iterator<NetworkElement> iterator() {
        return neList.iterator();
    }

    public int size() {
        return neList.size();
    }

    @Override
    public Object[] toArray() {
        return neList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return neList.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return neList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return neList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends NetworkElement> c) {
        return neList.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return neList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return neList.retainAll(c);
    }

    @Override
    public void clear() {
        neList.clear();
    }

}
