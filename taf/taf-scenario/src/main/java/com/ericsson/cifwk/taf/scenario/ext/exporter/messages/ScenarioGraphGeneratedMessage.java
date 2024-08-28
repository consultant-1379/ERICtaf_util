package com.ericsson.cifwk.taf.scenario.ext.exporter.messages;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioMessage;

@API(Internal)
public class ScenarioGraphGeneratedMessage implements ScenarioMessage {
    final String name;
    final byte[] graph;

    public ScenarioGraphGeneratedMessage(String name, byte[] graph) {
        this.name = name;
        this.graph = graph;
    }

    public String getName() {
        return name;
    }

    public byte[] getGraph() {
        return graph;
    }
}
