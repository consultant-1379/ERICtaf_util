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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraph.GraphNode;
import com.google.common.io.CharStreams;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

/**
 * <pre>
 * Class Name: SvgExporter.
 * Description: This class is used to implement the SVG scenario export function.
 * </pre>
 */
@API(Internal)
public class SvgExporter implements ScenarioExecutionGraphExporter {
    public static final String SVG_MIME = "image/svg+xml";
    public static final String SVG_EXT = ".svg";

    public static final int TOOLTIP_WIDTH = 340;
    public static final int TOOLTIP_HEIGHT = 541;

    private static final Logger logger = LoggerFactory.getLogger(SvgExporter.class);

    private static final String WIDTH_LABEL = "width";
    private static final String HEIGHT_LABEL = "height";
    private TooltipGenerator tooltipGenerator = new TooltipGenerator(false);
    private Transformer transformer;
    private DocumentBuilder documentBuilder;

    private ScenarioExecutionGraph dynamicGraph;

    @Override
    public void init(final ScenarioExecutionGraph dynamicGraph) {
        try {
            this.dynamicGraph = dynamicGraph;

            documentBuilder = hackInDocumentBuilderFactory().newDocumentBuilder();
            transformer = hackInTransformerFactory().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //ASCII
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            logger.error("Exception: ", e);
        }
    }

    @Override
    public String getExtension() {
        return SVG_EXT;
    }

    @Override
    public void export(final Writer writer) {
        try {
            final mxGraph graph;
            synchronized (dynamicGraph) {
                graph = new VisualScenarioExecutionGraph(dynamicGraph);
            }
            removeInvisibleCells(graph);

            final mxGraphLayout layout = new mxHierarchicalLayout(graph);
            layout.execute(graph.getDefaultParent());

            final Document svgDocument = mxCellRenderer.createSvgDocument(graph, null, 1d, null, null);
            updateDimensions(svgDocument);

            addElementFromFile(svgDocument, "svg/style.css", "style", "text/css");
            addElementFromFile(svgDocument, "svg/script.js", "script", "text/javascript");

            addNotSupportedWarning(svgDocument);

            addToolTips(graph, svgDocument);

            transformer.transform(new DOMSource(svgDocument), new StreamResult(writer));
        } catch (final Exception e) {
            logger.error("Exception: ", e);
        }
    }

    private void updateDimensions(final Document svgDocument) {
        assureMinValue(svgDocument, WIDTH_LABEL, TOOLTIP_WIDTH);
        assureMinValue(svgDocument, HEIGHT_LABEL, TOOLTIP_HEIGHT);
        final NamedNodeMap attributes = svgDocument.getDocumentElement().getAttributes();
        attributes.getNamedItem("viewBox")
                .setNodeValue("0 0 " + attributes.getNamedItem(WIDTH_LABEL).getNodeValue() + " "
                        + attributes.getNamedItem(HEIGHT_LABEL).getNodeValue());
    }

    public void assureMinValue(final Document root, final String dimension, final int minValue) {
        final Node attribute = root.getDocumentElement().getAttributes().getNamedItem(dimension);
        final String width = attribute.getNodeValue();
        if (Integer.valueOf(width) < minValue) {
            attribute.setNodeValue("" + minValue);
        }
    }

    private void addNotSupportedWarning(final Document document) {
        final Element root = document.getDocumentElement();

        final Element body = document.createElement("body");
        body.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");

        final Element foreign = document.createElement("foreignObject");
        foreign.appendChild(body);

        final Element text = document.createElement("text");
        text.setAttribute("x", "10");
        text.setAttribute("y", "30");
        text.setAttribute("fill", "red");
        text.setTextContent("Your browser does not support HTML as foreignObject. To view scenario details open this " + "file in Firefox or Chrome");

        final Element aswitch = document.createElement("switch");
        aswitch.appendChild(foreign);
        aswitch.appendChild(text);

        root.appendChild(aswitch);
    }

    private void addToolTips(final mxGraph graph, final Document document) throws ParserConfigurationException, SAXException, IOException {
        final Element root = document.getDocumentElement();

        final Object[] cells = graph.getChildCells(graph.getDefaultParent(), true, true);
        for (final Object cell : cells) {
            final mxCell mxCell = cast(cell);
            final Object value = mxCell.getValue();
            if (value instanceof GraphNode) {
                final GraphNode node = cast(value);

                final String toolTipId = "tooltip" + node.getId();

                final Node toolTip = createToolTipDOM(document, node, toolTipId);
                final Element foreign = createForeignObject(document, mxCell, toolTipId);
                foreign.appendChild(toolTip);

                root.appendChild(foreign);
            }
        }
    }

    private void removeInvisibleCells(final mxGraph graph) {
        logger.debug("Removing invisible cells");
        final Object[] cells = graph.getChildCells(graph.getDefaultParent(), true, true);
        for (final Object cell : cells) {
            final mxCell mxCell = cast(cell);
            final Object value = mxCell.getValue();
            if (value instanceof GraphNode) {
                final GraphNode node = cast(value);
                if (!node.isVisible()) {
                    graph.removeCells(new Object[] { cell });
                }
            }
        }
    }

    private Element createForeignObject(final Document document, final mxCell mxCell, final String toolTipId) {
        final Element root = document.getDocumentElement();
        final Integer maxWidth = Integer.valueOf(root.getAttributes().getNamedItem(WIDTH_LABEL).getNodeValue());
        final Integer maxHeight = Integer.valueOf(root.getAttributes().getNamedItem(HEIGHT_LABEL).getNodeValue());

        final Element foreign = document.createElement("foreignObject");
        foreign.setAttribute("id", toolTipId);
        foreign.setAttribute("class", "tooltip");
        foreign.setAttribute("x", applyBoundary(mxCell.getGeometry().getX(), maxWidth, TOOLTIP_WIDTH));
        foreign.setAttribute("y", applyBoundary(mxCell.getGeometry().getY(), maxHeight, TOOLTIP_HEIGHT));
        foreign.setAttribute(WIDTH_LABEL, "" + TOOLTIP_WIDTH);
        foreign.setAttribute(HEIGHT_LABEL, "" + TOOLTIP_HEIGHT);

        return foreign;
    }

    private Node createToolTipDOM(final Document document, final GraphNode node, final String toolTipId) throws SAXException, IOException {
        final String toolTip = tooltipGenerator.export(node);
        final Document parsedToolTip = documentBuilder.parse(new ByteArrayInputStream(toolTip.getBytes()));
        final Node importedToolTip = document.importNode(parsedToolTip.getDocumentElement(), true);

        final Element closeLink = document.createElement("a");
        closeLink.setAttribute("href", "javascript:hide('" + toolTipId + "')");
        closeLink.setAttribute("class", "close");
        closeLink.setTextContent("x"); // \u2716

        importedToolTip.insertBefore(closeLink, importedToolTip.getFirstChild());

        return importedToolTip;
    }

    private void addElementFromFile(final Document document, final String filename, final String tag, final String type) throws IOException {
        final InputStream stream = getClass().getResourceAsStream(filename);
        final String text = CharStreams.toString(new InputStreamReader(stream));
        @SuppressWarnings("CheckStyle") final CDATASection CDATA = document.createCDATASection(text);
        final Element element = document.createElement(tag);
        element.setAttribute("type", type);
        element.appendChild(CDATA);
        final Element root = document.getDocumentElement();
        root.insertBefore(element, root.getFirstChild());
    }

    private String applyBoundary(final double desired, final int boundary, final int size) {
        if (desired + size > boundary) {
            return "" + (boundary - size);
        }
        return "" + desired;
    }

    private <T> T cast(final Object object) {
        return (T) object;
    }

    private TransformerFactoryImpl hackInTransformerFactory() {
        // correct way to to this would be
        //        SAXTransformerFactory factory =
        //                (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        // but because class path contains net.sf.saxon.saxon it is loaded via SPI
        // Because saxon generates random text instead of xml, we instantiate xalan manually

        return new TransformerFactoryImpl();
    }

    private DocumentBuilderFactoryImpl hackInDocumentBuilderFactory() {
        // correct way to to this would be
        //        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // see reason in {link: com.ericsson.cifwk.taf.scenario.ext.exporter.SvgExporter.hackInTransformerFactory}

        return new DocumentBuilderFactoryImpl();
    }
}
