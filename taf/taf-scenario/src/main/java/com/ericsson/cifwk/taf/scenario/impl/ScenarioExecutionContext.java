/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioMessageListener;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationUtils.isDataSourceAllowEmptySetGlobally;

@API(Internal)
public class ScenarioExecutionContext {
    private final TestScenario scenario;
    private final ThreadPoolBasedScenarioRunnerCore core;
    private final ScenarioListener listener;
    private final ScenarioDataSourceContext dataSourceContext;

    private final VUserManager vUserManager;
    private final ScenarioGraph graph;
    private ScenarioEventBus eventBus;

    private Set<Throwable> thrownExceptionList = Sets.newConcurrentHashSet();

    private volatile boolean broken = false;

    private static final Logger logger = LoggerFactory.getLogger(ScenarioExecutionContext.class);

    protected ScenarioExecutionContext(TestScenario scenario, ThreadPoolBasedScenarioRunnerCore core, CompositeScenarioListener listener, VUserManager vUserManager, ScenarioGraph graph) {
        this.scenario = scenario;
        this.core = core;
        this.listener = listener;
        this.vUserManager = vUserManager;
        this.graph = graph;
        this.dataSourceContext = new ScenarioDataSourceContext();
    }

    public static ScenarioExecutionContext getInstance(TestScenario scenario, ThreadPoolBasedScenarioRunnerCore core, CompositeScenarioListener listener) {
        VUserManager vUserManager = new VUserManager();
        ScenarioGraph graph = ScenarioGraphImpl.create(scenario.getFlow(), vUserManager, scenario.getDefaultVusers());
        logger.debug("Scenario {} graph: {}", scenario.getName(), graph);

        ScenarioExecutionContext context = new ScenarioExecutionContext(scenario, core, listener, vUserManager, graph);
        ScenarioEventBus eventBus = initEventBus(scenario.getId(), graph, context);

        context.setEventBus(eventBus);
        return context;
    }

    private static ScenarioEventBus initEventBus(Long scenarioId, ScenarioGraph graph, ScenarioExecutionContext scenarioExecutionContext) {
        ScenarioEventBus eventBus = ScenarioEventBus.getInstance(scenarioId);

        eventBus.registerAsync(new ScenarioMessagingService(graph));
        if (!isDataSourceAllowEmptySetGlobally())
            eventBus.registerAsync(new EmptyDatasourceListener(scenarioExecutionContext));

        for (ScenarioMessageListener listener : ServiceLoader.load(ScenarioMessageListener.class)) {
            //todo read annotation here
            eventBus.registerSync(listener);
        }
        ScenarioExecutionGraphListenerBuilder.registerIfApplicable(eventBus);
        return eventBus;
    }

    public void release() {
        eventBus.shutdown();
        vUserManager.disposeContext();
    }

    public VUserManager getVUserManager() {
        return vUserManager;
    }

    public ScenarioGraph getGraph() {
        return graph;
    }

    public ScenarioEventBus getEventBus() {
        return eventBus;
    }

    public ScenarioListener getListener() {
        return listener;
    }

    public ThreadPoolBasedScenarioRunnerCore getCore() {
        return core;
    }

    public Long getScenarioFlowId() {
        return scenario.getFlow().getId();
    }

    public ScenarioType getScenarioType() {
        return scenario.getType();
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(Throwable throwable) {
        thrownExceptionList.add(throwable);
        this.broken = true;
    }

    public ScenarioDataSourceContext getDataSourceContext() {
        return dataSourceContext;
    }

    private void setEventBus(ScenarioEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Collection<Throwable> getThrownExceptionList() {
        return thrownExceptionList;
    }
}
