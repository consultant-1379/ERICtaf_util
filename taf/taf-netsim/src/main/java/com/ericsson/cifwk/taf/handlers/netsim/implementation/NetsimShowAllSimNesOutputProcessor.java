package com.ericsson.cifwk.taf.handlers.netsim.implementation;

import static com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants.NetworkElements.SHOW_ALL_SIM_NES_ADDRESS_HEADING;
import static com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants.NetworkElements.SHOW_ALL_SIM_NES_NAME_HEADING;
import static com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants.NetworkElements.SHOW_ALL_SIM_NES_SERVER_HEADING;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetSimSimulationFactory;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElementImpl;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;

/**
 * This class is responsible for parsing the output of a Netsim <code>.show allsimnes</code> command.
 *
 * @author edunsea
 */
public class NetsimShowAllSimNesOutputProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetsimShowAllSimNesOutputProcessor.class);

    private Map<NetSimContext, NetSimResult> commandResults;

    /**
     * Construct a new instance of this processor passing the result of an execution of the {@link NetSimCommands#showAllsimnes()} command.
     *
     * @param showAllSimNesCommandResult
     *        The output from a Netsim <code>.show allsimnes</code> command.
     */
    public NetsimShowAllSimNesOutputProcessor(final Map<NetSimContext, NetSimResult> showAllSimNesCommandResult) {
        this.commandResults = showAllSimNesCommandResult;
    }

    /**
     * Retrieve an {@link NeGroup} containing all started network elements in Netsim.
     * 
     * @return An NeGroup with all started network elements from Netsim.
     */
    public NeGroup getAllStartedNEs() {
        final NeGroup allNes = new NeGroup();

        for (final Entry<NetSimContext, NetSimResult> resultEntry : commandResults.entrySet()) {

            for (final CommandOutput cOutput : resultEntry.getValue().getOutput()) {
                final NeGroup contextNes = extractNesFromCommandOutput(cOutput, resultEntry.getKey());
                allNes.addAll(contextNes);
            }
        }
        return allNes;
    }

    private NeGroup extractNesFromCommandOutput(final CommandOutput cmdOutput, final NetSimContext context) {
        final NeGroup neGroup = new NeGroup();
        final Map<String, List<Map<String, String>>> cmdOutputSections = cmdOutput.asSections();

        for (final Entry<String, List<Map<String, String>>> simEntry : cmdOutputSections.entrySet()) {
            final String simName = simEntry.getKey();
            final Simulation sim = NetSimSimulationFactory.getInstance(context, simName);

            for (final Map<String, String> neData : simEntry.getValue()) {
                final NetworkElement ne = createNe(context, sim, neData, cmdOutput);
                if (ne != null) {
                    neGroup.add(ne);
                }
            }
        }
        return neGroup;
    }

    private NetworkElement createNe(final NetSimContext context, final Simulation sim, final Map<String, String> neData, final CommandOutput cmdOutput) {
        final String neServer = neData.get(SHOW_ALL_SIM_NES_SERVER_HEADING);
        if (neServer == null) {
            LOGGER.error("Error occured, could not retrieve NE \"{}\" details for simulation {} on Netsim Host {}", SHOW_ALL_SIM_NES_SERVER_HEADING,
                    sim.getName(), context.getHostName());
            LOGGER.error("Printing current Netsim shell console details\n{}", cmdOutput.getRawOutput());
            return null;
        }
        if (isNeStarted(neServer)) {

            final String neName = neData.get(SHOW_ALL_SIM_NES_NAME_HEADING);
            final String neAddress = neData.get(SHOW_ALL_SIM_NES_ADDRESS_HEADING).split("\\s", 0)[0];
            final String[] typeParts = neServer.substring(0, neServer.indexOf('@')).split("_");
            final String techType = typeParts[2];
            final String nodeType = typeParts[3];
            final String mim = typeParts[4];
            final String longType = String.format("%s %s %s", techType, nodeType, mim);

            final NetworkElementImpl ne = new NetworkElementImpl(context, sim);
            ne.setName(neName);
            ne.setIp(neAddress);
            ne.setTechType(techType);
            ne.setNodeType(nodeType);
            ne.setMim(mim);
            ne.setType(longType);
            return ne;
        }
        return null;
    }

    private boolean isNeStarted(final String neServer) {
        return !neServer.equals("not started");
    }
}
