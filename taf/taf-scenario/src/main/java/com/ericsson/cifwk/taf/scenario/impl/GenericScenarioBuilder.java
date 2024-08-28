package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.CompositeExceptionHandlerBuilder;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioRunnerBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ResetDataSourcesInvocation;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.HashSet;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Builder for scenario object.
 */
@API(Internal)
public abstract class GenericScenarioBuilder<T extends GenericScenarioBuilder>  {

    private final String name;
    protected ScenarioType type;
    protected final TestStepFlowBuilder scenarioFlow;
    private DataSourceDefinition[] dataSources = new DataSourceDefinition[] {};
    private HashSet<Long> flowIds = Sets.newHashSet();
    private ScenarioExceptionHandler handler;

    private int defaultVusers = 1;
    private boolean vusersSet = false;
    private TestStepDefinition lastTestStepAdded = null;

    @Deprecated
    private CompositeExceptionHandlerBuilder exceptionHandlerBuilder;

    public GenericScenarioBuilder(String name) {
        this.name = name;
        this.scenarioFlow = new TestStepFlowBuilder(name + " Scenario Flow");
    }

    /**
     * Set Exception Handler for all Flows in Scenario
     * Will be called on exception in Test Steps. If ExceptionHandler does not propagate Exception, scenario flow will continue
     * In case you need multiple exception handlers use {@link com.ericsson.cifwk.taf.scenario.TestScenarios#compositeExceptionHandler()}
     * <pre>
     * {@code
     *.withExceptionHandler(
     *                       compositeExceptionHandler()
     *                               .addExceptionHandler(handler1)
     *                               .addExceptionHandler(handler2)
     *                               .build())
     * }
     * </pre>
     * @see TestStepFlowBuilder#withExceptionHandler(com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler)
     * @see TestScenarioRunnerBuilder#withDefaultExceptionHandler(ScenarioExceptionHandler)
     * @param exceptionHandler
     * @return builder
     */
    public T withExceptionHandler(ScenarioExceptionHandler exceptionHandler) {
        Preconditions.checkNotNull(exceptionHandler, "ExceptionHandler cannot be null");
        Preconditions.checkState(this.handler == null, "You can not set exception handler twice. " +
                "In case you need multiple exception handlers " +
                "use {@link com.ericsson.cifwk.taf.scenario.TestScenarios#compositeExceptionHandler()");
        this.handler = exceptionHandler;

        return (T) this;
    }

    /**
     * Sets default concurrency level for flows in scenario if not specified in {@link TestStepFlowBuilder#withVusers(int)} 
     *
     * @param vUsers number of virtual users to use
     * @return builder
     */
    public T withDefaultVusers(int vUsers) {
        lastTestStepAdded = null;
        checkArgument(vUsers > 0,
                "vUser value should be greater than zero");
        Preconditions.checkState(!vusersSet, "vUsers can't be set twice");

        this.defaultVusers = vUsers;
        vusersSet = true;
        return (T) this;
    }

    public T addFlow(TestStepFlowBuilder builder) {
        return addFlow(builder.build());
    }

    /**
     * Adds test step flow to the current scenario.
     *
     * @param flow
     *            flow to add
     * @return builder
     */
    public T addFlow(TestStepFlow flow) {
        addFlow(flow, false);
        return (T) this;
    }

    /**
     * Last added flow will be run even if previous flow threw an un-handled exception.
     *
     * @return builder
     */
    public T alwaysRun() {
        if(lastTestStepAdded !=null) {
            lastTestStepAdded.alwaysRun();
            lastTestStepAdded = null;
        }else {
            throw new UnsupportedOperationException("Use alwaysRun() only if previous method call have added any Test Step");
        }
        return (T) this;
    }

    private void addFlow(TestStepFlow flow, boolean required) {
        Preconditions.checkNotNull(flow, "Test flow can't be empty");
        checkFlowIdUniqueness(flow);

        TestStepDefinition stepDefinition = new TestStepDefinition(new ChildFlowInvocationImpl(flow));
        if (required) {
            stepDefinition.alwaysRun();
        }
        scenarioFlow.addTestStep(stepDefinition);
        scenarioFlow.addTestStep(resetAllDataSources());
        lastTestStepAdded = stepDefinition;
    }

    private TestStepDefinition resetAllDataSources() {
        return new TestStepDefinition(new ResetDataSourcesInvocation());
    }

    /**
     * Execute flows passed in param in parallel
     * @param flows to execute in parallel
     * @return builder
     */
    public T split(TestStepFlow... flows) {
        checkFlowIdUniqueness(flows);

        TestStepDefinition stepDefinition = new TestStepDefinition(new SplitInvocationImpl(flows));
        scenarioFlow.addTestStep(stepDefinition);
        scenarioFlow.addTestStep(resetAllDataSources());
        lastTestStepAdded = stepDefinition;
        return (T) this;
    }

    /**
     * Build flow builders passed in param and execute flows in parallel
     * @param flowBuilders to execute in parallel
     * @return builder
     */
    public T split(TestStepFlowBuilder... flowBuilders) {
        TestStepFlow[] testStepFlows = new TestStepFlow[flowBuilders.length];
        for (int i = 0; i < flowBuilders.length; i++) {
            testStepFlows[i] = flowBuilders[i].build();
        }
        split(testStepFlows);

        return (T) this;
    }

    /**
     * Returns a constructed instance of scenario.
     *
     * @return scenario
     */
    public TestScenario build() {
        lastTestStepAdded = null;
        setExceptionHandler();
        return new TestScenarioImpl(name, type, scenarioFlow.build(), dataSources, defaultVusers);
    }

    private void setExceptionHandler() {
        Preconditions.checkState(!(handler != null && exceptionHandlerBuilder != null));
        if (exceptionHandlerBuilder != null) {
            handler = exceptionHandlerBuilder.build();
        }

        if (handler != null) {
            scenarioFlow.withExceptionHandler(handler);
        }
    }

    private void checkFlowIdUniqueness(TestStepFlow... flows) {
        for (TestStepFlow flow : flows) {
            checkArgument(flowIds.add(flow.getId()),
                    "Flows can not be reused within one scenario. Please create new Flow. You may extract Flow " +
                            "creation to method if you need identical Flows.");
        }
    }
}
