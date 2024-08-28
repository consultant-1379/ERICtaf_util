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
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationUtils;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioException;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioListenerException;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioStartedMessage;
import com.google.common.base.Throwables;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public final class TafScenarioRunner implements TestScenarioRunner {

    private final CompositeScenarioListener listener = new CompositeScenarioListener();
    private final ScenarioExceptionHandler defaultHandler;
    private final ThreadPoolBasedScenarioRunnerCore core;

    private static final Logger logger = LoggerFactory.getLogger(TafScenarioRunner.class);

    static {
        enableDebugIfApplicable();
    }

    public TafScenarioRunner(ScenarioExceptionHandler defaultHandler,
                             @Nullable @Deprecated ScenarioExceptionHandler coreExceptionHandler,
                             List<ScenarioListenerSortableWrapper> listeners) {
        this.defaultHandler = defaultHandler;
        this.core = new ThreadPoolBasedScenarioRunnerCore(coreExceptionHandler);
        this.listener.add(listeners);
    }

    @Override
    public ScenarioListener getListener() {
        return listener;
    }

    @Override
    public void start(TestScenario scenario) {
        ScenarioExecutionContext context = ScenarioExecutionContext.getInstance(scenario, core, listener);

        boolean success = false;
        try {
            context.getEventBus().post(new ScenarioStartedMessage(scenario, context.getGraph()));
            listener.onScenarioStarted(scenario);
            runSequential(scenario, context);
            success = true;
        } catch (ScenarioException e) {
            Throwables.propagate(e.getCause());
        } finally {
            context.getEventBus().post(new ScenarioFinishedMessage(scenario, success));
            context.release();
            onScenarioFinished(scenario);
        }

        if (context.isBroken()) {
            String errorMessage = "Scenario contains one or more broken/failed test cases \n ";
            String individualErrorMessages = "";
            for (Throwable throwable : context.getThrownExceptionList()) {
                logger.error("Throwable ", throwable);
                individualErrorMessages = individualErrorMessages + " \n" +
                        "Error message : " + throwable.getMessage();
            }
            // exception is required to fail build
            throw new RuntimeException(errorMessage + individualErrorMessages);
        }
    }

    private void onScenarioFinished(TestScenario scenario) {
        try {
            listener.onScenarioFinished(scenario);
        } catch (ScenarioListenerException e) {
            Throwables.propagate(e.getCause());
        }
    }

    private void runSequential(TestScenario scenario, ScenarioExecutionContext context) {
        ScenarioGraph graph = context.getGraph();

        TestStepFlow flow = scenario.getFlow();

        List<Integer> vUsers = graph.getVUsers(flow, ScenarioGraph.NO_PARENT_VUSER);
        flow.initialize(context);
        List<TestStepRunner> tasks = TestStepRunner.newTestStepRunnerList(scenario.getName(), "0",
                flow,
                new LinkedHashMap<String, DataRecord>(),
                vUsers,
                context,
                scenario,
                flow.getRunOptions().getExceptionHandlerOr(ScenarioExceptionHandler.PROPAGATE));

        core.runTasks(tasks, defaultHandler);
    }

    private static void enableDebugIfApplicable() {
        if (ScenarioConfigurationUtils.shouldDebugScenario()) {
            try {
                LogManager.getLogger("com.ericsson.cifwk.taf.scenario").setLevel(Level.DEBUG);
            } catch (java.lang.NoClassDefFoundError e) {
                logger.error("Unable to set log level for com.ericsson.cifwk.taf.scenario. Probably you are not using" +
                        "log4j.");
            }
        }
    }
}
