package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElementImpl.NE_NAME;
import static com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkMapSimulation.SIM_NAME;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.in;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.exceptions.InvalidNetworkMapException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.resultset.ResultSet;

public class NetworkMap {

    private static final String NETWORKMAP_SCHEMA_JSON = "/networkmap/schema.json";
    private static final String NETWORK_MAP_ROOT = "networkMap";

    private final IndexedCollection<Simulation> simulations;
    private final IndexedCollection<NetworkElement> networkElements;

    public NetworkMap(NetSimContext netSimContext, InputStream inputJsonData) {
        Map<String, NetworkMapSimulation> simulationsMap = new HashMap<>();
        Map<NetworkMapSimulation, List<NetworkElement>> networkElementsMap = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode networkMapNode = mapper.readTree(inputJsonData);
            if (validateJsonAgainstSchema(networkMapNode)) {

                JsonNode networkMap = networkMapNode.get(NETWORK_MAP_ROOT);

                for (JsonNode unmappedNE : networkMap) {

                    String simulationName = unmappedNE.get("Simulation").asText();
                    NetworkMapSimulation simulation = getOrCreateSimulation(simulationsMap, netSimContext, simulationName);
                    NetworkElementImpl networkElement = mapNE(netSimContext, unmappedNE, simulation);

                    List<NetworkElement> networkElements = getAssociatedNE(networkElementsMap, simulation);
                    networkElements.add(networkElement);
                }
            }

            assignNEsToSimulations(networkElementsMap);
        } catch (Exception e) {
            throw new InvalidNetworkMapException(e.getMessage(), e);
        }

        simulations = new ConcurrentIndexedCollection<>();
        simulations.add(getOrCreateSimulation(simulationsMap, netSimContext, "default"));
        simulations.addAll(simulationsMap.values());
        simulations.addIndex(HashIndex.onAttribute(SIM_NAME));

        networkElements = new ConcurrentIndexedCollection<>();
        Collection<List<NetworkElement>> values = networkElementsMap.values();
        for (List<NetworkElement> networkElementList : values) {
            networkElements.addAll(networkElementList);
        }
        networkElements.addIndex(HashIndex.onAttribute(NE_NAME));
    }

    public List<Simulation> getSimulations(String... simNamesToInclude) {
        if (simNamesToInclude == null || simNamesToInclude.length == 0) {
            return Lists.newArrayList(simulations);
        }
        ResultSet<Simulation> resultSet = simulations.retrieve(in(SIM_NAME, simNamesToInclude));
        return Lists.newArrayList(resultSet);
    }

    public NetworkElement findNetworkElement(String neName) {
        ResultSet<NetworkElement> retrieve = networkElements.retrieve(equal(NE_NAME, neName));
        if (retrieve.isNotEmpty()) {
            return retrieve.uniqueResult();
        }
        return null;
    }

    public List<NetworkElement> getNetworkElements() {
        return Collections.unmodifiableList(new ArrayList<>(networkElements));
    }

    /**
     * This will set NEs to be available from Simulation object.
     *
     * @param networkElementsMap
     */
    private void assignNEsToSimulations(Map<NetworkMapSimulation, List<NetworkElement>> networkElementsMap) {
        for (Map.Entry<NetworkMapSimulation, List<NetworkElement>> entry : networkElementsMap.entrySet()) {
            entry.getKey().setNetworkElements(entry.getValue());
        }
    }

    private NetworkElementImpl mapNE(NetSimContext netSimContext, JsonNode networkElementNode, Simulation simulation) {
        NetworkElementImpl networkElement = new NetworkElementImpl(netSimContext, simulation);
        networkElement.setName(networkElementNode.get("name").asText());
        networkElement.setIp(networkElementNode.get("ip").asText());
        networkElement.setType(networkElementNode.get("type").asText());
        networkElement.setTechType(networkElementNode.get("techType").asText());
        networkElement.setNodeType(networkElementNode.get("nodeType").asText());
        networkElement.setMim(networkElementNode.get("mim").asText());
        return networkElement;
    }

    private NetworkMapSimulation getOrCreateSimulation(Map<String, NetworkMapSimulation> simulationsMap,
                                                       NetSimContext netSimContext,
                                                       String simulationName) {
        NetworkMapSimulation simulation = simulationsMap.get(simulationName);
        if (simulation == null) {
            simulation = new NetworkMapSimulation(netSimContext, simulationName);
            simulationsMap.put(simulationName, simulation);
        }
        return simulation;
    }

    private List<NetworkElement> getAssociatedNE(Map<NetworkMapSimulation, List<NetworkElement>> networkElementsMap,
                                                 NetworkMapSimulation simulation) {
        List<NetworkElement> networkElements = networkElementsMap.get(simulation);
        if (networkElements == null) {
            networkElements = new ArrayList<>();
            networkElementsMap.put(simulation, networkElements);
        }
        return networkElements;
    }

    private boolean validateJsonAgainstSchema(JsonNode networkMapNode) throws IOException, ProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonNode schemaNode = JsonLoader.fromResource(NETWORKMAP_SCHEMA_JSON);
        JsonSchema schema = factory.getJsonSchema(schemaNode);
        ProcessingReport processingReport = schema.validate(networkMapNode);
        if (processingReport == null || !processingReport.isSuccess()) {
            throw new InvalidNetworkMapException("NetworkMap JSON file is not valid. " +
                    (processingReport != null ? processingReport.iterator().next() : ""));
        }
        return true;
    }
}
