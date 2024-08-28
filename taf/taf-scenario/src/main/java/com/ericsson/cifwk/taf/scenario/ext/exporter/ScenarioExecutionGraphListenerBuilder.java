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
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.ext.exporter.messages.ScenarioGraphGeneratedMessage;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioEventBus;
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationProvider;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioFinishedMessage;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioConfiguration;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class ScenarioExecutionGraphListenerBuilder {
    public static final String TAF_SCENARIO_DEBUG_PORT = "taf.scenario.debug.port";
    public static final String TAF_SCENARIO_DEBUG_ENABLED = "taf.scenario.debug.enabled";
    public static final String TAF_SCENARIO_SHOW_SYNC_POINTS = "taf.scenario.debug.show.sync.points";

    public static final String OUTPUT_DIR = "target/taf-scenario";
    private static final List<ScenarioExecutionGraphExporter> allExporters = Arrays.asList(new GraphMlExporter(), new SvgExporter());
    private static final ScenarioExecutionGraphExporter onFailureExporter = new SvgExporter();
    private static final Logger logger = LoggerFactory
            .getLogger(ScenarioExecutionGraphListenerBuilder.class);

    public static void registerIfApplicable(ScenarioEventBus eventBus) {
        ScenarioConfiguration configuration = ScenarioConfigurationProvider.provide();

        Integer tafScenarioDebugPort = configuration.getProperty(TAF_SCENARIO_DEBUG_PORT, (Integer) null);
        Boolean tafScenarioDebugEnabled = configuration.getProperty(TAF_SCENARIO_DEBUG_ENABLED, (Boolean) null);
        Boolean tafScenarioShowSyncPoints = configuration.getProperty(TAF_SCENARIO_SHOW_SYNC_POINTS, Boolean.FALSE);

        if (debugExplicitlyDisabled(tafScenarioDebugEnabled)) {
            return;
        }

        ScenarioExecutionGraphListener scenarioExecutionGraphListener = new ScenarioExecutionGraphListener(tafScenarioShowSyncPoints);

        ScenarioExecutionGraphServer server = null;
        List<ScenarioExecutionGraphExporter> exporters = Lists.newArrayList();

        if (tafScenarioDebugEnabled != null) {
            for (ScenarioExecutionGraphExporter exporter : allExporters) {
                exporter.init(scenarioExecutionGraphListener.getDynamicGraph());
                exporters.add(exporter);
            }

            if (tafScenarioDebugPort != null) {
                server = ScenarioExecutionGraphServer.startIfPossible(tafScenarioDebugPort, allExporters);
            }
        } else {
            onFailureExporter.init(scenarioExecutionGraphListener.getDynamicGraph());
        }

        eventBus.registerAsync(scenarioExecutionGraphListener);
        eventBus.registerAsync(new ScenarioExecutionGraphFinalizer(scenarioExecutionGraphListener, exporters, onFailureExporter, server, eventBus));
    }

    private static boolean debugExplicitlyDisabled(Boolean tafScenarioDebugEnabled) {
        return Boolean.FALSE.equals(tafScenarioDebugEnabled);
    }

    public static class ScenarioExecutionGraphFinalizer {
        protected ScenarioExecutionGraphListener scenarioExecutionGraphListener;
        protected List<ScenarioExecutionGraphExporter> exporters;
        protected ScenarioExecutionGraphExporter onFailureExporter;
        protected ScenarioExecutionGraphServer server;
        protected ScenarioEventBus eventBus;

        public ScenarioExecutionGraphFinalizer(ScenarioExecutionGraphListener scenarioExecutionGraphListener, List<ScenarioExecutionGraphExporter> exporters, ScenarioExecutionGraphExporter onFailureExporter, @Nullable ScenarioExecutionGraphServer server, ScenarioEventBus eventBus) {
            this.scenarioExecutionGraphListener = scenarioExecutionGraphListener;
            this.exporters = exporters;
            this.onFailureExporter = onFailureExporter;
            this.server = server;
            this.eventBus = eventBus;
        }

        @Subscribe
        public synchronized void onScenarioFinished(ScenarioFinishedMessage message) {
            scenarioExecutionGraphListener.onScenarioFinished(message);

            if (server != null) {
                server.stop();
            }

            if (exporters.isEmpty() && !message.isSuccessfull()) {
                exporters.add(onFailureExporter);
            }

            for (ScenarioExecutionGraphExporter exporter : exporters) {
                saveToFile(exporter, message.getScenario());
            }
        }

        public void saveToFile(ScenarioExecutionGraphExporter exporter, TestScenario scenario) {
            String fileName = getGraphFileName(scenario, exporter.getExtension());

            File target = new File(OUTPUT_DIR);
            Path path = Paths.get(OUTPUT_DIR, fileName);

            target.mkdirs();

            try {
                logger.debug("Trying to write scenario to file " + path.toAbsolutePath().toString());
                ByteArrayOutputStream graph = new ByteArrayOutputStream();
                exporter.export(new OutputStreamWriter(graph));
                byte[] graphBytes = graph.toByteArray();
                eventBus.post(new ScenarioGraphGeneratedMessage(fileName, graphBytes));
                Files.write(path, graphBytes);
                logger.info("Scenario graph was written to file " + path.toAbsolutePath().toString());
            } catch (Throwable e) {
                logger.error("Unable to write scenario graph to file", e);
            }
        }

        protected static String getGraphFileName(TestScenario scenario, String ext) {
            String safeScenarioName = scenario.getName().replaceAll("\\W+", "");
            return safeScenarioName + "_" + scenario.getId() + ext;
        }
    }
}
