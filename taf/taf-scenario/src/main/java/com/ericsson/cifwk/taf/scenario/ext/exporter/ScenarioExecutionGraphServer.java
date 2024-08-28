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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;

import fi.iki.elonen.NanoHTTPD;

/**
 * <pre>
 * Class Name: ScenarioExecutionGraphServer.
 * Description: This class is used for the realization of the server for the dynamic visualization of the scenario..
 * </pre>
 */
@API(Internal)
public class ScenarioExecutionGraphServer extends NanoHTTPD {
    public static final String PATH = "/api/scenario";
    private static final Logger logger = LoggerFactory.getLogger(ScenarioExecutionGraphServer.class);

    private List<ScenarioExecutionGraphExporter> exporters;

    public ScenarioExecutionGraphServer(final int port, final List<ScenarioExecutionGraphExporter> exporters) {
        super(port);
        this.exporters = exporters;

        for (final ScenarioExecutionGraphExporter exporter : exporters) {
            logger.info("Scenario debug is available on http://localhost:" + port + PATH + exporter.getExtension());
        }
    }

    @Override
    public Response serve(final IHTTPSession session) {
        for (final ScenarioExecutionGraphExporter exporter : exporters) {
            if ((PATH + exporter.getExtension()).equals(session.getUri())) {
                final StringWriter stringWriter = new StringWriter();
                exporter.export(stringWriter);

                return new Response(Response.Status.OK, "text/xml", stringWriter.toString());
            }
        }

        return new Response("");
    }

    protected static ScenarioExecutionGraphServer startIfPossible(final Integer tafScenarioDebugPort,
            final List<ScenarioExecutionGraphExporter> allExporters) {
        final ScenarioExecutionGraphServer server = new ScenarioExecutionGraphServer(tafScenarioDebugPort, allExporters);

        try {
            server.start();
        } catch (final IOException e) {
            logger.error("Exception: ", e);
            return null;
        }

        return server;
    }
}

