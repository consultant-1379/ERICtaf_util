package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static com.ericsson.cifwk.taf.handlers.netsim.domain.SimulationImpl.NE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.handlers.netsim.CommandOutput;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

class NetSimResultMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetSimResultMapper.class);
    private NetSimContext context;
    private Simulation simulation;

    public NetSimResultMapper(NetSimContext context, Simulation simulation) {
        this.context = context;
        this.simulation = simulation;
    }

    public List<NetworkElement> mapToNetworkElements(NetSimResult netSimResult) {
        CommandOutput[] output = netSimResult.getOutput();
        CommandOutput simnesOutput = output[0];
        CommandOutput startedOutput = output[1];
        List<NetworkElement> nes = parse(simnesOutput);
        return populateListOfStartedNEs(startedOutput, nes);
    }

    List<NetworkElement> populateListOfStartedNEs(final CommandOutput startedOutput,
                                                          final List<NetworkElement> nes) {
        final List<NetworkElement> startedNEList = Lists.newArrayList();

        for (NetworkElement networkElement : nes) {
            Optional<List<String>> startedNEdetails = getNeDetailsIfStarted(startedOutput, networkElement);
            try {
                if (startedNEdetails.isPresent() && isStartedNEInSim(networkElement, startedNEdetails.get())) {
                    startedNEList.add(networkElement);
                } else {
                    logFailedNEData(startedNEdetails.orNull(), null);
                }
            } catch (RuntimeException e) {
                logFailedNEData(startedNEdetails.orNull(), e);
            }

        }
        return startedNEList;
    }

    /**
     * LTE02ERDB00028 192.168.100.28 /netsim/netsimdir/LTEH11-40-V1limx-FDD-LTE02
     * asRows().get(LTE02ERDB00028) will return list of 2 elements "ip", "name".
     *
     * @param startedOutput
     *         e.g. LTE02ERDB00028 192.168.100.28 /netsim/netsimdir/LTEH11-40-V1limx-FDD-LTE02
     * @param networkElement
     *         e.g. LTE02ERDB00028
     * @return Set e.g. { "192.168.100.28", "/netsim/netsimdir/LTEH11-40-V1limx-FDD-LTE02"}
     */
    static Optional<List<String>> getNeDetailsIfStarted(CommandOutput startedOutput, NetworkElement networkElement) {
        List<String> neDetails = startedOutput.asRows().get(networkElement.getName());
        if (neDetails != null) {
            List<String> details = new ArrayList<>(neDetails);
            return Optional.of(details);
        }
        return Optional.absent();
    }

    static boolean isStartedNEInSim(final NetworkElement simulationNE, final List<String> startedNEDetails) {
        String startedNESimulation = startedNEDetails.get(1);
        String simulationName = simulationNE.getSimulationName();
        return startedNESimulation.contains(simulationName);
    }

    List<NetworkElement> parse(CommandOutput nesInExecOutput) {
        List<Map<String, String>> nesDataColumnsList = nesInExecOutput.asColumns();
        List<NetworkElement> result = new ArrayList<>();
        for (Map<String, String> nesDataColumns : nesDataColumnsList) {
            NetworkElementImpl netsimNEInstance = createNetsimNEInstance();
            try {
                if (nesDataColumns.containsKey(NE_NAME)) {
                    // Don't try to process broadcast messages from Netsim
                    String neData = nesDataColumns.get(NE_NAME);
                    if (!neData.contains("Delayed response") && !neData.contains("set_interface_version")) {
                        populateNetsimNEDetails(netsimNEInstance, nesDataColumns);
                        result.add(netsimNEInstance);
                    }
                } else {
                    logFailedNEData(nesDataColumns, null);
                }
            } catch (RuntimeException e) {
                logFailedNEData(nesDataColumns, e);
            }
        }
        return result;
    }

    void populateNetsimNEDetails(NetworkElementImpl netsimNEInstance,
                                 Map<String, String> nesDataColumns) {
        netsimNEInstance.setName(StringUtils.trim(nesDataColumns
                .get(NetSimConstants.NetworkElements.SHOW_NES_NAME_HEADING)));
        netsimNEInstance
                .setIp(StringUtils.trim(nesDataColumns
                        .get(NetSimConstants.NetworkElements.SHOW_NES_ADDRESS_HEADING).split("\\s", 0)[0]));
        String longType = StringUtils.trim(nesDataColumns
                .get(NetSimConstants.NetworkElements.SHOW_NES_TYPE_HEADING));
        netsimNEInstance.setType(longType);

        String[] typeParts = longType.split("\\s+");
        netsimNEInstance.setTechType(typeParts[0]);
        netsimNEInstance.setNodeType(typeParts[1]);
        netsimNEInstance.setMim(typeParts[2]);
    }

    private NetworkElementImpl createNetsimNEInstance() {
        return new NetworkElementImpl(context, simulation);
    }


    void logFailedNEData(Map<String, String> nesDataColumns, Exception e) {
        if (e != null) {
            LOGGER.error("Parsing netsim data caused error", e);
        }
        LOGGER.error("NE information causing error:");
        Set<Map.Entry<String, String>> entries = nesDataColumns.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            LOGGER.error(entry.getKey() + " = " + nesDataColumns.get(entry.getValue()));
        }
    }

    void logFailedNEData(List<String> neDetails, Exception e) {
        if (e != null) {
            LOGGER.error("Parsing netsim data caused error", e);
        }
        if (neDetails == null) {
            LOGGER.error("NE details are null");
        } else {
            for (String detail : neDetails) {
                LOGGER.error("NE details causing error: {}", detail);
            }
        }
    }
}
