/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.cifwk.taf.scenario.ext.exporter;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.ext.JGraphXAdapter;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.ErrorNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.FlowEndedNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.FlowNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.GraphNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.RootNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.ScenarioFinishedNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.SyncNode;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.TestStepNode;
import com.google.common.collect.Maps;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;

/**
 * <pre>
 * Class Name: VisualScenarioExecutionGraph.
 * Description: This class is used to create a graphic representation of scenario (static and dynamic). It use an external package (mxgraph) and
 *      nodes informations.
 * </pre>
 */
@API(Internal)
public class VisualScenarioExecutionGraph extends JGraphXAdapter<GraphNode, ScenarioExecutionGraph.LabeledEdge> {

    @SuppressWarnings("CheckStyle")
    final String ROOTSTARTNODE_FILLCOLOR = System.getProperty("scenario.node.rootStart.fillcolor", RgbColorCostant.COLOR_BROWN);
    @SuppressWarnings("CheckStyle")
    final String ROOTSTARTNODE_FONTCOLOR = System.getProperty("scenario.node.rootStart.fontcolor", RgbColorCostant.COLOR_WHITE);
    @SuppressWarnings("CheckStyle")
    final String ROOTSTARTNODE_SHAPE = System.getProperty("scenario.node.rootStart.shape", mxConstants.SHAPE_HEXAGON);
    @SuppressWarnings("CheckStyle")
    final int ROOTSTARTNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.rootStart.space.top", "15"));
    @SuppressWarnings("CheckStyle")
    final int ROOTSTARTNODE_SPACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.rootStart.space.bottom", "15"));
    @SuppressWarnings("CheckStyle")
    final String ROOTENDNODE_FILLCOLOR = System.getProperty("scenario.node.rootEnd.fillcolor", RgbColorCostant.COLOR_BROWN);
    @SuppressWarnings("CheckStyle")
    final String ROOTENDNODE_FONTCOLOR = System.getProperty("scenario.node.rootEnd.fontcolor", RgbColorCostant.COLOR_WHITE);
    @SuppressWarnings("CheckStyle")
    final String ROOTENDNODE_SHAPE = System.getProperty("scenario.node.rootEnd.shape", mxConstants.SHAPE_HEXAGON);
    @SuppressWarnings("CheckStyle")
    final int ROOTENDNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.rootEnd.space.top", "15"));
    @SuppressWarnings("CheckStyle")
    final int ROOTENDNODE_SPACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.rootEnd.space.bottom", "15"));
    @SuppressWarnings("CheckStyle")
    final String FLOWSTARTNODE_FILLCOLOR = System.getProperty("scenario.node.flowStart.fillcolor", RgbColorCostant.COLOR_CYAN);
    @SuppressWarnings("CheckStyle")
    final String FLOWSTARTNODE_FONTCOLOR = System.getProperty("scenario.node.flowStart.fontcolor", RgbColorCostant.COLOR_BLACK);
    @SuppressWarnings("CheckStyle")
    final String FLOWSTARTNODE_SHAPE = System.getProperty("scenario.node.flowStart.shape", mxConstants.SHAPE_CYLINDER);
    @SuppressWarnings("CheckStyle")
    final int FLOWSTARTNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.flowStart.space.top", "20"));
    @SuppressWarnings("CheckStyle")
    final int FLOWSTARTNODE_SPACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.flowStart.space.bottom", "10"));
    @SuppressWarnings("CheckStyle")
    final String FLOWENDNODE_FILLCOLOR = System.getProperty("scenario.node.flowEnd.fillcolor", RgbColorCostant.COLOR_BLUE);
    @SuppressWarnings("CheckStyle")
    final String FLOWENDNODE_FONTCOLOR = System.getProperty("scenario.node.flowEnd.fontcolor", RgbColorCostant.COLOR_BLACK);
    @SuppressWarnings("CheckStyle")
    final String FLOWENDNODE_SHAPE = System.getProperty("scenario.node.flowEnd.shape", mxConstants.SHAPE_CYLINDER);
    @SuppressWarnings("CheckStyle")
    final int FLOWENDNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.flowEnd.space.top", "20"));
    @SuppressWarnings("CheckStyle")
    final int FLOWENDNODE_SPACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.flowEnd.space.bottom", "10"));
    @SuppressWarnings("CheckStyle")
    final String SYNCNODE_FILLCOLOR = System.getProperty("scenario.node.sync.fillcolor", RgbColorCostant.COLOR_YELLOW);
    @SuppressWarnings("CheckStyle")
    final String SYNCNODE_FONTCOLOR = System.getProperty("scenario.node.sync.fontcolor", RgbColorCostant.COLOR_BLACK);
    @SuppressWarnings("CheckStyle")
    final String SYNCNODE_SHAPE = System.getProperty("scenario.node.sync.shape", mxConstants.SHAPE_RHOMBUS);
    @SuppressWarnings("CheckStyle")
    final int SYNCNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.sync.space.top", "20"));
    @SuppressWarnings("CheckStyle")
    final int SYNCNODE_SPACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.sync.space.bottom", "20"));
    @SuppressWarnings("CheckStyle")
    final String TESTSTEPNODE_FILLCOLOR = System.getProperty("scenario.node.teststep.fillcolor", RgbColorCostant.COLOR_ORANGE);
    @SuppressWarnings("CheckStyle")
    final String TESTSTEPNODE_FONTCOLOR = System.getProperty("scenario.node.teststep.fontcolor", RgbColorCostant.COLOR_BLACK);
    @SuppressWarnings("CheckStyle")
    final String TESTSTEPNODE_SHAPE = System.getProperty("scenario.node.teststep.shape", mxConstants.SHAPE_RECTANGLE);
    @SuppressWarnings("CheckStyle")
    final int TESTSTEPNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.sync.space.top", "10"));
    @SuppressWarnings("CheckStyle")
    final int TESTSTEPNODE_SPACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.sync.space.bottom", "10"));
    @SuppressWarnings("CheckStyle")
    final String ERRORNODE_FILLCOLOR = System.getProperty("scenario.node.error.fillcolor", RgbColorCostant.COLOR_RED);
    @SuppressWarnings("CheckStyle")
    final String ERRORNODE_FONTCOLOR = System.getProperty("scenario.node.error.fontcolor", RgbColorCostant.COLOR_BLACK);
    @SuppressWarnings("CheckStyle")
    final String ERRORNODE_SHAPE = System.getProperty("scenario.node.error.shape", mxConstants.SHAPE_DOUBLE_ELLIPSE);
    @SuppressWarnings("CheckStyle")
    final int ERRORNODE_SPACING_TOP = Integer.parseInt(System.getProperty("scenario.node.shape.space.top", "15"));
    @SuppressWarnings("CheckStyle")
    final int ERRORNODE_SACING_BOTTOM = Integer.parseInt(System.getProperty("scenario.node.shape.space.bottom", "15"));

    public VisualScenarioExecutionGraph(final ScenarioExecutionGraph graph) {
        super(graph);
    }

    @Override
    public String getLabel(final Object cell) {
        final mxCell mxCell = cast(cell);
        final Object value = mxCell.getValue();
        if (value instanceof GraphNode) {
            final GraphNode node = cast(value);
            return node.getName();
        }

        return super.getLabel(cell);
    }

    @Override
    protected String getLinkForCell(final Object cell) {
        final mxCell mxCell = cast(cell);
        final Object value = mxCell.getValue();
        if (value instanceof GraphNode) {
            final GraphNode node = cast(value);
            return "javascript:showTooltip('" + node.getId() + "')";
        }

        return super.getLinkForCell(cell);
    }

    @Override
    public String getToolTipForCell(final Object cell) {
        return "Click to see details...";
    }

    @Override
    public Map<String, Object> getCellStyle(final Object cell) {
        final mxCell mxCell = cast(cell);
        final Object value = mxCell.getValue();
        final HashMap<String, Object> style = Maps.newHashMap();

        // Configure specific parameter for object
        style.put(mxConstants.STYLE_STROKECOLOR, RgbColorCostant.COLOR_NAVY);
        style.put(mxConstants.SHAPE_ARROW, mxConstants.ARROW_CLASSIC);
        if (value instanceof RootNode) {
            setStyle(style, ROOTSTARTNODE_FILLCOLOR, ROOTSTARTNODE_FONTCOLOR, ROOTSTARTNODE_SPACING_TOP, ROOTSTARTNODE_SPACING_BOTTOM,
                    ROOTSTARTNODE_SHAPE, RgbColorCostant.COLOR_BLACK);
        } else if (value instanceof FlowNode) {
            setStyle(style, FLOWSTARTNODE_FILLCOLOR, FLOWSTARTNODE_FONTCOLOR, FLOWSTARTNODE_SPACING_TOP, FLOWSTARTNODE_SPACING_BOTTOM,
                    FLOWSTARTNODE_SHAPE, RgbColorCostant.COLOR_BLACK);
        } else if (value instanceof FlowEndedNode) {
            setStyle(style, FLOWENDNODE_FILLCOLOR, FLOWENDNODE_FONTCOLOR, FLOWENDNODE_SPACING_TOP, FLOWENDNODE_SPACING_BOTTOM, FLOWENDNODE_SHAPE,
                    RgbColorCostant.COLOR_BLACK);
        } else if (value instanceof SyncNode) {
            setStyle(style, SYNCNODE_FILLCOLOR, SYNCNODE_FONTCOLOR, SYNCNODE_SPACING_TOP, SYNCNODE_SPACING_BOTTOM, SYNCNODE_SHAPE,
                    RgbColorCostant.COLOR_BLACK);
        } else if (value instanceof ErrorNode) {
            setStyle(style, ERRORNODE_FILLCOLOR, ERRORNODE_FONTCOLOR, ERRORNODE_SPACING_TOP, ERRORNODE_SACING_BOTTOM, ERRORNODE_SHAPE,
                    RgbColorCostant.COLOR_BLACK);
        } else if (value instanceof TestStepNode) {
            setStyle(style, TESTSTEPNODE_FILLCOLOR, TESTSTEPNODE_FONTCOLOR, TESTSTEPNODE_SPACING_TOP, TESTSTEPNODE_SPACING_BOTTOM, TESTSTEPNODE_SHAPE,
                    RgbColorCostant.COLOR_BLACK);
        } else if (value instanceof ScenarioFinishedNode) {
            setStyle(style, ROOTENDNODE_FILLCOLOR, ROOTENDNODE_FONTCOLOR, ROOTENDNODE_SPACING_TOP, ROOTENDNODE_SPACING_BOTTOM, ROOTENDNODE_SHAPE,
                    RgbColorCostant.COLOR_BLACK);
        }

        if (value instanceof GraphNode && ((GraphNode) value).getError() != null) {
            setStyle(style, ERRORNODE_FILLCOLOR, ERRORNODE_FONTCOLOR, ERRORNODE_SPACING_TOP, ERRORNODE_SACING_BOTTOM, ERRORNODE_SHAPE,
                    RgbColorCostant.COLOR_BLACK);
        }

        return style.isEmpty() ? super.getCellStyle(cell) : style;
    }

    private void setStyle(final HashMap<String, Object> thisStyle, final String fillColor, final String fontColor, final int topSpacing,
            final int bottomSpacing, final String shape, final String strokeColor) {
        // Set the value for the spacingTop style. The value represents the spacing, in pixels, added to the top side of a label in a vertex
        //      (style applies to vertices only).
        if (topSpacing > 0) {
            thisStyle.put(mxConstants.STYLE_SPACING_TOP, topSpacing);
        }
        // Set the value for the spacing Bottom style. The value represents the spacing, in pixels, added to the bottom side of a label in a vertex
        //      (style applies to vertices only).
        if (bottomSpacing > 0) {
            thisStyle.put(mxConstants.STYLE_SPACING_BOTTOM, bottomSpacing);
        }
        // Set Color and shape of element.
        setStyle(thisStyle, fillColor, fontColor, shape, strokeColor);
    }

    private void setStyle(final HashMap<String, Object> thisStyle, final String fillColor, final String fontColor, final String shape,
            final String strokeColor) {
        thisStyle.put(mxConstants.STYLE_SPACING, 20);

        // Setting Style with selected values.
        if (fillColor != null) {
            thisStyle.put(mxConstants.STYLE_FILLCOLOR, fillColor);
        }
        if (fontColor != null) {
            thisStyle.put(mxConstants.STYLE_FONTCOLOR, fontColor);
        }

        if (shape != null) {
            thisStyle.put(mxConstants.STYLE_SHAPE, shape);
        }
        if (strokeColor != null) {
            thisStyle.put(mxConstants.STYLE_STROKECOLOR, strokeColor);
        }
    }

    private <T> T cast(final Object object) {
        return (T) object;
    }

    /**
     * <pre>
     * Name: RgbColorCostant()
     * Description: Define some RGB colors to 'set' background of graphic shapes.
     * </pre>
     */
    private final class RgbColorCostant {
        @SuppressWarnings("CheckStyle")
        final static String COLOR_RED = "#e6194b";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_GREEN = "#3cb44b";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_YELLOW = "#ffe119";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_BLUE = "#0082c8";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_ORANGE = "#f58231";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_PURPLE = "#911eb4";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_CYAN = "#46f0f0";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_MAGENTA = "#f032e6";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_LIME = "#d2f53c";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_PINK = "#fabebe";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_TEAL = "#008080";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_LAVENDER = "#e6beff";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_BROWN = "#aa6e28";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_BEIGE = "#fffac8";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_MAROON = "#800000";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_MINT = "#aaffc3";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_OLIVE = "#808000";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_CORAL = "#ffd8b1";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_NAVY = "#000080";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_GREY = "#808080";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_WHITE = "#FFFFFF";
        @SuppressWarnings("CheckStyle")
        final static String COLOR_BLACK = "#000000";

        private RgbColorCostant() {
        }
    }
}
