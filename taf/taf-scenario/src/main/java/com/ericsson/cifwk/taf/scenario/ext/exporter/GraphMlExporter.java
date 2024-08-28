/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.ext.exporter;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.ext.exporter.jgrapht.AttributeProvider;
import com.ericsson.cifwk.taf.scenario.ext.exporter.jgrapht.SuperGraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.GraphNode;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.LabeledEdge;

@API(Internal)
public class GraphMlExporter implements ScenarioExecutionGraphExporter {
    private static final Logger logger = LoggerFactory.getLogger(GraphMlExporter.class);
    private ScenarioExecutionGraph graph;
    private SuperGraphMLExporter<GraphNode, LabeledEdge> exporter;

    final TooltipGenerator tooltipGenerator = new TooltipGenerator(true);

    @Override
    public void init(ScenarioExecutionGraph graph) {
        this.graph = graph;

        List<AttributeProvider<GraphNode>> vertexAttributeProviders = Arrays.<AttributeProvider<GraphNode>>asList(
                new AttributeProvider<GraphNode>("Vertex Label") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return graphNode.getName();
                    }
                },
                new AttributeProvider<GraphNode>("Vertex ID") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return graphNode.getId().toString();
                    }
                },
                new AttributeProvider<GraphNode>("Vertex Type") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return graphNode.getClass().getSimpleName();
                    }
                },
                new AttributeProvider<GraphNode>("Vertex Time") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return new SimpleDateFormat(TooltipGenerator.DATE_FORMAT).format(graphNode.getStartTime().getTime());
                    }
                },
                new AttributeProvider<GraphNode>("Vertex Duration") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return "" + graphNode.getDuration();
                    }
                },
                new AttributeProvider<GraphNode>("Vertex Tooltip") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return tooltipGenerator.export(graphNode);
                    }
                },
                new AttributeProvider<GraphNode>("Vertex Failed") {
                    @Override
                    public String getAttributeValue(GraphNode graphNode) {
                        return Boolean.valueOf(graphNode.getError() != null).toString();
                    }
                }
        );

        List<AttributeProvider<LabeledEdge>> edgeAttributeProviders = Arrays.<AttributeProvider<LabeledEdge>>asList(
                new AttributeProvider<LabeledEdge>("Edge Label") {
                    @Override
                    public String getAttributeValue(LabeledEdge edge) {
                        return edge.toString();
                    }
                }
        );

        exporter = new SuperGraphMLExporter<>(
                new IntegerNameProvider<GraphNode>(),
                vertexAttributeProviders,
                new IntegerEdgeNameProvider<LabeledEdge>(),
                edgeAttributeProviders
        );
    }

    @Override
    public String getExtension() {
        return ".graphml";
    }

    @Override
    public void export(Writer writer) {
        try {
            exporter.export(writer, graph);
        } catch (Throwable e) {
            logger.error("Exception during graph export", e);
        }
    }
}
