package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.datasource.ResetDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.impl.GenericScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.impl.ParallelInvocation;
import com.ericsson.cifwk.taf.scenario.impl.SplitInvocationImpl;
import com.ericsson.cifwk.taf.scenario.impl.SubFlowInvocationImpl;
import com.ericsson.cifwk.taf.scenario.impl.SyncInvocation;
import com.ericsson.cifwk.taf.scenario.impl.SyncSingleInvocation;
import com.ericsson.cifwk.taf.scenario.impl.TestStepFlowImpl;
import com.ericsson.cifwk.taf.scenario.impl.WaitInvocation;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.RunnableSyncSingleInvocation;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Builder for test step flow object.
 */
@API(Stable)
public final class TestStepFlowBuilder {

    private final String name;
    private final List<TestStepInvocation> testSteps = newArrayList();
    private final List<SyncSingleInvocation> beforeSteps = newArrayList();
    private final List<SyncSingleInvocation> afterSteps = newArrayList();
    private DataSourceDefinitionBuilder[] dataSourceBuilders = new DataSourceDefinitionBuilder[]{};
    private int vusers = -1;
    private boolean vusersSet = false;
    private boolean dataSourcesSet = false;
    private boolean hasTestCases = false;
    private RampUp.StrategyProvider rampUpStrategyProvider = RampUp.allAtOnce();
    private Long startDelay = 0L;
    private ScenarioExceptionHandler exceptionHandler;
    private TestStepDefinition lastTestStepAdded = null;
    private List<Predicate<TestStepFlow.State>> repeatWhile = newArrayList();

    public TestStepFlowBuilder(String name) {
        this.name = name;
    }

    /**
     * Adds a test step as the last one in this flow.
     *
     * @param testStep test step to add
     * @return builder
     */
    public TestStepFlowBuilder addTestStep(TestStepDefinition testStep) {
        Preconditions.checkNotNull(testStep, "Test step can't be null.");
        Preconditions.checkState(!hasTestCases, "Test step can't be mixed with test case in flow");

        for (TestStepInvocation invocation : testStep.getInvocations()) {
            checkIdUniqueness(invocation, testSteps);
            testSteps.add(invocation);
        }
        lastTestStepAdded = testStep;
        return this;
    }

    /**
     * Adds a flow as a subflow to this flow.
     *
     * @param subFlow flow to add as subFlow
     * @return builder
     */
    public TestStepFlowBuilder addSubFlow(TestStepFlow subFlow) {
        Preconditions.checkNotNull(subFlow, "Sub Flow can't be null.");
        addTestStep(subFlowToStep(subFlow));
        return this;
    }

    private static TestStepDefinition subFlowToStep(TestStepFlow subFlow) {
        ParallelInvocation invocation = new SubFlowInvocationImpl(subFlow);
        return syncInvocation(invocation);
    }

    private static TestStepDefinition syncInvocation(ParallelInvocation invocation) {
        return new TestStepDefinition(
                    invocation,
                    new SyncInvocation("sync-after-" + invocation.getName())
            );
    }

    /**
     * Last added flow will be run even if previous flow threw an un-handled exception.
     *
     * @return TestStepFlowBuilder
     */
    public TestStepFlowBuilder alwaysRun() {
        if (lastTestStepAdded != null) {
            lastTestStepAdded.alwaysRun();
            lastTestStepAdded = null;
        } else {
            throw new UnsupportedOperationException("Use alwaysRun() only if previous method call have added any Test Step");
        }
        return this;
    }

    /**
     * @see #addSubFlow(com.ericsson.cifwk.taf.scenario.TestStepFlow)
     */
    public TestStepFlowBuilder addSubFlow(TestStepFlowBuilder subFlowBuilder) {
        Preconditions.checkNotNull(subFlowBuilder, "Sub Flow can't be null.");
        addTestStep(subFlowToStep(subFlowBuilder.build()));
        return this;
    }



    /**
     * Run given flows in parallel
     *
     * @param parallelFlows flow to run in parallel
     * @return builder
     */
    public TestStepFlowBuilder split(TestStepFlow... parallelFlows) {
        Preconditions.checkNotNull(parallelFlows, "Sub Flow can't be null.");
        addTestStep(doParallel(parallelFlows));

        return this;
    }

    private static TestStepDefinition doParallel(TestStepFlow... flows) {
        Preconditions.checkArgument(flows.length > 0);
        ParallelInvocation splitInvocation = new SplitInvocationImpl(flows);

        return syncInvocation(splitInvocation);
    }

    private TestStepDefinition doParallel(TestStepFlowBuilder... flowBuilders) {
        TestStepFlow[] testStepFlows = new TestStepFlow[flowBuilders.length];
        for (int i = 0; i < flowBuilders.length; i++) {
            testStepFlows[i] = flowBuilders[i].build();
        }
        return doParallel(testStepFlows);
    }

    /**
     * @see #split(com.ericsson.cifwk.taf.scenario.TestStepFlow...)
     */
    public TestStepFlowBuilder split(TestStepFlowBuilder... parallelFlowBuilders) {
        Preconditions.checkNotNull(parallelFlowBuilders, "Sub Flow can't be null.");
        addTestStep(doParallel(parallelFlowBuilders));

        return this;
    }

    /**
     * Add Data Sources to Flow. Flow will repeated with each Data Record
     *
     * @param dataSources Data Sources
     * @return builder
     */
    public TestStepFlowBuilder withDataSources(DataSourceDefinitionBuilder... dataSources) {
        lastTestStepAdded = null;
        checkArgument(dataSources != null && dataSources.length > 0, "No data sources supplied");
        Preconditions.checkState(!dataSourcesSet, "Data sources can't be set twice");

        this.dataSourceBuilders = dataSources;
        this.dataSourcesSet = true;
        return this;
    }

    /**
     * Sets concurrency level for current Flow. Flow will be executed in parallel for each vUser
     *
     * @param vUsers number of vUsers
     * @return builder
     */
    public TestStepFlowBuilder withVusers(int vUsers) {
        lastTestStepAdded = null;
        checkArgument(vUsers > 0, "Vuser value should be greater than zero");
        Preconditions.checkState(!vusersSet, "Vusers can't be set twice");

        this.vusers = vUsers;
        this.vusersSet = true;
        return this;
    }

    /**
     * Set exception handler for Flow
     * Will be called on exception in Test Steps. If ExceptionHandler does not propagate Exception, scenario flow will continue
     * In case you need multiple exception handlers use {@link com.ericsson.cifwk.taf.scenario.TestScenarios#compositeExceptionHandler()}
     * <pre>
     * {@code
     * .withExceptionHandler(
     *                       compositeExceptionHandler()
     *                               .addExceptionHandler(handler1)
     *                               .addExceptionHandler(handler2)
     *                               .build())
     * }
     * </pre>
     *
     * @param exceptionHandler
     * @return builder
     * @see GenericScenarioBuilder#withExceptionHandler(com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler)
     * @see TestScenarioRunnerBuilder#withDefaultExceptionHandler(ScenarioExceptionHandler)
     */
    public TestStepFlowBuilder withExceptionHandler(ScenarioExceptionHandler exceptionHandler) {
        Preconditions.checkNotNull(exceptionHandler);
        Preconditions.checkState(this.exceptionHandler == null, "You can not set exception handler twice. " +
                "In case you need multiple exception handlers " +
                "use {@link com.ericsson.cifwk.taf.scenario.TestScenarios#compositeExceptionHandler()");
        this.exceptionHandler = exceptionHandler;

        return this;
    }

    /**
     * Constructs instance of test step flow object.
     *
     * @return flow
     */
    public TestStepFlow build() {
        Map<String, DataSourceDefinition> dataSources = Maps.newHashMap();
        List<DataRecordTransformer> dataRecordTransformers = newCopyOnWriteArrayList();
        for (final DataSourceDefinitionBuilder dataSourceBuilder : dataSourceBuilders) {
            DataSourceDefinition definition = dataSourceBuilder.build();
            dataSources.put(definition.getName(), definition);
            checkIfTestStepExists(dataSourceBuilder.getDataRecordTransformers());
            dataRecordTransformers.addAll(dataSourceBuilder.getDataRecordTransformers());
        }

        FlowRunOptions flowRunOptions = FlowRunOptions.newFlowRunOptions(
                vusers,
                rampUpStrategyProvider,
                startDelay,
                getRepeatWhile(),
                exceptionHandler);

        return new TestStepFlowImpl(name,
                testSteps,
                beforeSteps,
                afterSteps,
                dataSources,
                dataRecordTransformers,
                flowRunOptions);
    }

    private void checkIfTestStepExists(List<DataRecordTransformer> dataRecordTransformers) {
        for (DataRecordTransformer dataRecordTransformer : dataRecordTransformers) {
            final String testStepName = dataRecordTransformer.getTestStepName();

            if (testStepName == null || testStepExists(testStepName)) {
                continue;
            }

            throw new IllegalStateException("Test step '" + testStepName + "' does not exist in flow '" + name + "'");
        }
    }

    private boolean testStepExists(String testStepName) {
        for (TestStepInvocation testStep : testSteps) {
            if (testStepName.equals(testStep.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Run given runnables before executing flow. Will be run once not depending on vUser count
     *
     * @param beforeSteps runnables to run
     * @return builder
     */
    public TestStepFlowBuilder beforeFlow(Runnable... beforeSteps) {
        lastTestStepAdded = null;
        Preconditions.checkNotNull(beforeSteps, "After step can't be null.");
        for (Runnable beforeStep : beforeSteps) {
            if (beforeStep instanceof TestScenarios.Pause) {
                startDelay += TestScenarios.Pause.class.cast(beforeStep).getMillis();
            } else if (beforeStep instanceof ResetDataSource) {
                ResetDataSource resetDataSource = ResetDataSource.class.cast(beforeStep);
                this.beforeSteps.add(TestScenarios.resetDataSource(resetDataSource.getName()));
            } else {
                this.beforeSteps.add(new RunnableSyncSingleInvocation(beforeStep, "before step"));
            }
        }

        return this;
    }


    /**
     * @see #beforeFlow(Runnable... beforeSteps)
     */
    public TestStepFlowBuilder beforeFlow(SyncSingleInvocation... beforeSteps) {
        lastTestStepAdded = null;
        Preconditions.checkNotNull(beforeSteps, "After step can't be null.");
        for (SyncSingleInvocation beforeStep : beforeSteps) {
            this.beforeSteps.add(beforeStep);
        }

        return this;
    }

    /**
     * Run given runnables after executing flow. Will be run once not depending on vUser count
     *
     * @param afterSteps runnables to run
     * @return builder
     */
    public TestStepFlowBuilder afterFlow(Runnable... afterSteps) {
        lastTestStepAdded = null;
        Preconditions.checkNotNull(afterSteps, "After step can't be null.");
        for (Runnable afterStep : afterSteps) {
            if (afterStep instanceof ResetDataSource) {
                ResetDataSource resetDataSource = ResetDataSource.class.cast(afterStep);
                this.afterSteps.add(TestScenarios.resetDataSource(resetDataSource.getName()));
            } else {
                this.afterSteps.add(new RunnableSyncSingleInvocation(afterStep, "after step"));
            }
        }

        return this;
    }

    /**
     * @see #afterFlow(Runnable... afterSteps)
     */
    public TestStepFlowBuilder afterFlow(SyncSingleInvocation... afterSteps) {
        lastTestStepAdded = null;
        Preconditions.checkNotNull(afterSteps, "After step can't be null.");
        Collections.addAll(this.afterSteps, afterSteps);

        return this;
    }

    /**
     * Wait until all vUsers reach this point, then continue
     *
     * @param name of the sync point
     * @return builder
     */
    public TestStepFlowBuilder syncPoint(String name) {
        lastTestStepAdded = null;
        testSteps.add(new SyncInvocation(name));

        return this;
    }

    /**
     * Pauses execution between Test Steps/Flow iterations
     *
     * @param pause to wait
     * @param unit  time unit
     * @return builder
     */
    public TestStepFlowBuilder pause(long pause, TimeUnit unit) {
        lastTestStepAdded = null;
        checkArgument(pause > 0, "Pause should be greater than 0");
        long pauseMs = MILLISECONDS.convert(pause, unit);
        if (testSteps.isEmpty()) {
            startDelay = startDelay + pauseMs;
            testSteps.add(new WaitInvocation("Wait Step", pauseMs, true));
        } else {
            testSteps.add(new WaitInvocation("Wait Step", pauseMs, false));
        }
        return this;
    }

    /**
     * @param iterationsPerVuser Number of times to loop the flow for each vUser.
     * @see #repeatWhile(Predicate)
     * @see #withDuration(long, TimeUnit)
     */
    public TestStepFlowBuilder withIterationsPerVuser(final long iterationsPerVuser) {
        lastTestStepAdded = null;
        checkArgument(iterationsPerVuser > 0, "Iterations per vUser should be greater than 0");
        repeatWhile.add(getIterationsPredicate(iterationsPerVuser));
        return this;
    }

    /**
     * @param duration If defined, after this duration no new iteration wil be started. All existing scenario executions will be finished.
     * @see #repeatWhile(Predicate)
     * @see #withIterationsPerVuser(long)
     */
    public TestStepFlowBuilder withDuration(final long duration, final TimeUnit unit) {
        lastTestStepAdded = null;
        checkArgument(duration > 0, "Duration should be greater than 0");
        this.repeatWhile.add(getDurationPredicate(MILLISECONDS.convert(duration, unit)));

        return this;
    }

    /**
     * Repeat flow while {@param condition} predicate returns true.
     * If multiple {@link #repeatWhile(Predicate)} methods are called, or combined with {@link #withDuration(long, TimeUnit)}
     * {@link #withIterationsPerVuser(long)} flow repeats until any of condition returns `false`
     * If predicate will return `false` on first application Flow will not be executed at all
     *
     * @see #withDuration(long, TimeUnit)
     * @see #withIterationsPerVuser(long)
     * @return builder
     */
    public TestStepFlowBuilder repeatWhile(Predicate<TestStepFlow.State> condition) {
        repeatWhile.add(condition);
        return this;
    }

    /**
     * Ramp Up is needed to gradually increase number of simultaneously running vUsers from zero to number specified in
     * {@link #withVusers(int)}. For example {@link RampUp#during(long, TimeUnit)}
     *
     * @param rampUpStrategy Provider for strategy how to {@link RampUp} users.
     * @return builder
     */
    public TestStepFlowBuilder withRampUp(RampUp.StrategyProvider rampUpStrategy) {
        lastTestStepAdded = null;
        this.rampUpStrategyProvider = rampUpStrategy;
        return this;
    }

    private void checkIdUniqueness(TestStepInvocation testStep, List<TestStepInvocation> testSteps) {
        Long testStepId = testStep.getId();
        for (TestStepInvocation existingTestStep : testSteps) {
            checkArgument(!existingTestStep.getId().equals(testStepId),
                    "Test Steps can not be reused within one scenario. Please create new Test Step. You may extract" +
                            " Test Step creation to method if you need identical Test Steps.");
        }
    }

    /**
     * If not defined otherwise, repeat 1 time
     */
    private Predicate<TestStepFlow.State> getRepeatWhile() {
        if (repeatWhile.isEmpty()) {
            repeatWhile.add(getIterationsPredicate(1));
        }

        return Predicates.and(repeatWhile);
    }

    private Predicate<TestStepFlow.State> getIterationsPredicate(final long iterationsPerVuser) {
        return new Predicate<TestStepFlow.State>() {
            @Override
            public boolean apply(TestStepFlow.State state) {
                return state.getIteration() <= iterationsPerVuser;
            }
        };
    }

    private Predicate<TestStepFlow.State> getDurationPredicate(final long duration) {
        return new Predicate<TestStepFlow.State>() {
            @Override
            public boolean apply(TestStepFlow.State input) {
                return (System.currentTimeMillis() - input.getStartTime()) < duration;
            }
        };
    }
}
