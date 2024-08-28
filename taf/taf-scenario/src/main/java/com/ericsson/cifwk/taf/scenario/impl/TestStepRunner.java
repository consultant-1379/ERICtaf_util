package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.api.TestStepResult.failure;
import static com.ericsson.cifwk.taf.scenario.api.TestStepResult.success;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.ExtendedScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.RampUp;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.TestFlowResult;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioDataSourceValidationException;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioListenerException;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowExecutionCancelledMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.TestStepFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.TestStepStartedMessage;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(Internal)
public final class TestStepRunner implements Callable<Object> {
    private static AtomicLong idGenerator = new AtomicLong();

    private static final Logger logger = LoggerFactory.getLogger(TestStepRunner.class);

    private final ScenarioExecutionContext scenarioContext;
    private final TestScenario scenario;
    private final TestStepFlow flow;
    private final int vuserId;
    private final ExceptionManager handler;
    private final long startDelay;

    private final ScenarioEventBus eventBus;
    private final Map<String, DataRecord> parentDataSourcesRecords;
    private final String name;
    private final String executionId;

    private TestStepRunner(String name, String executionId, ScenarioExecutionContext scenarioContext, TestScenario scenario,
                           TestStepFlow flow, LinkedHashMap<String, DataRecord> parentDataSourcesRecords, int vuserId,
                           ScenarioExceptionHandler handler, long startDelay) {
        this.name = name;
        this.executionId = executionId;
        this.scenarioContext = scenarioContext;
        this.scenario = scenario;
        this.flow = flow;
        this.parentDataSourcesRecords = parentDataSourcesRecords;
        this.vuserId = vuserId;
        this.handler = new ExceptionManager(handler, false);
        this.startDelay = startDelay;
        this.eventBus = scenarioContext.getEventBus();
    }

    public List<TestStepRunner> cloneChainWithFlow(TestStepFlow otherFlow,
                                                   LinkedHashMap<String, DataRecord> parentDataSourcesRecords,
                                                   Collection<Integer> vUsers) {

        ScenarioExceptionHandler otherHandler = otherFlow.getRunOptions().getExceptionHandlerOr(handler.getScenarioExceptionHandler());

        return newTestStepRunnerList(name + otherFlow.getName(), executionId,
                otherFlow,
                parentDataSourcesRecords,
                vUsers,
                scenarioContext,
                scenario,
                otherHandler);
    }

    public static List<TestStepRunner> newTestStepRunnerList(String name, String executionId, TestStepFlow otherFlow,
                                                             LinkedHashMap<String, DataRecord> parentDataSourcesRecords,
                                                             Collection<Integer> vUsers,
                                                             ScenarioExecutionContext scenarioContext,
                                                             TestScenario scenario,
                                                             ScenarioExceptionHandler exceptionHandler) {
        long startDelay = otherFlow.getRunOptions().getStartDelay();
        RampUp.Strategy rampUpStrategy = otherFlow.getRunOptions().getRampUpStrategy();

        List<TestStepRunner> tasks = newArrayList();
        for (Integer vUser : vUsers) {
            startDelay += rampUpStrategy.nextVUserDelayDelta();
            TestStepRunner testStepRunner = new TestStepRunner(name + ".", executionId + "." + idGenerator.incrementAndGet(), scenarioContext, scenario, otherFlow,
                    parentDataSourcesRecords, vUser, exceptionHandler, startDelay);
            tasks.add(testStepRunner);
        }

        return tasks;
    }

    @Override
    public Object call() throws Exception {
        try {
            logger.debug("Starting flow " + flow.getName() + " with execution id " + executionId);

            runBeforeAfterSteps(flow.getBeforeSteps());

            TestContext vUserContext = applyVUserContext();

            DataSourceDefinition[] flowDataSources = flow.getDataSources();

            State state = new State(vUserContext);

            int executionCount = 0;
            while (state.shouldRepeat()) {
                DataSourceDefinition[] allDefinitions = ObjectArrays.concat(scenario.getDataSources(), flow.getDataSources(), DataSourceDefinition.class);

                DataSourceSupplier dataSourceSupplier = scenarioContext.getDataSourceContext().provideDataSources(allDefinitions, parentDataSourcesRecords);
                executionCount += performTestSteps(dataSourceSupplier, vUserContext);
            }

            eventBus.post(new FlowFinishedAllIterationsMessage(flow, vuserId, flowDataSources, executionId, executionCount));
        } catch (Throwable unhandled) {
            logger.debug("Unhandled exception during execution of Flow {}:", flow, unhandled);
            eventBus.post(FlowFinishedMessage.newInstance(scenarioContext, flow, vuserId, executionId, unhandled));

            Throwables.propagate(unhandled);
        } finally {
            runBeforeAfterSteps(flow.getAfterSteps());
        }
        return "";
    }

    private void runBeforeAfterSteps(List<SyncSingleInvocation> steps) {
        Map<Integer, TestContext> contextMap = scenarioContext.getVUserManager().getContextMap();
        TestContext context = contextMap.get(vuserId);

        List<Throwable> unhandledExceptions = newArrayList();

        for (SyncSingleInvocation syncSingleInvocation : steps) {
            try {
                syncSingleInvocation.run(this, scenarioContext, Maps.<String, DataRecord>newLinkedHashMap(), context, flow.getDataRecordTransformers());
            } catch (Throwable e) {
                handleAlwaysRunException(syncSingleInvocation, e, unhandledExceptions);
            }
        }

        if (!unhandledExceptions.isEmpty()) {
            Throwables.propagate(unhandledExceptions.get(unhandledExceptions.size() - 1));
        }
    }

    private TestContext applyVUserContext() {
        Map<Integer, TestContext> contextMap = scenarioContext.getVUserManager().getContextMap();
        TestContext context = contextMap.get(vuserId);
        ServiceRegistry.getTestContextProvider().initialize(context);
        Thread.currentThread().setName(name + "vUser-" + vuserId);
        return context;
    }

    private int performTestSteps(DataSourceSupplier dataSourceSupplier, TestContext context) throws Exception {
        int executionCount = 0;

        Optional<LinkedHashMap<String, DataRecord>> dataRecords = dataSourceSupplier.get();

        if (!dataRecords.isPresent()) {
            return executionCount;
        } else if (dataRecords.get().isEmpty()) {
            runSequenceOfSteps(new LinkedHashMap<String, DataRecord>(), context);
            executionCount++;
        } else {
            while (dataRecords.isPresent()) {
                runSequenceOfSteps(dataRecords.get(), context);
                executionCount++;
                dataRecords = dataSourceSupplier.get();
            }
        }

        return executionCount;
    }

    private void runSequenceOfSteps(LinkedHashMap<String, DataRecord> dataSourcesRecords, TestContext context) throws Exception {
        ScenarioListener listener = scenarioContext.getListener();
        TestFlowResult result = null;
        try {
            eventBus.post(FlowStartedMessage.newInstance(scenarioContext, flow, vuserId, dataSourcesRecords));
            if (!isScenarioFlow()) {
                listener.onFlowStarted(flow);
            }

            runAllTestSteps(dataSourcesRecords, listener, context);
            result = TestFlowResult.success(flow.getName(), vuserId);
            eventBus.post(FlowFinishedMessage.newInstance(scenarioContext, flow, vuserId, executionId));
        } catch (ScenarioDataSourceValidationException e) {
            result = TestFlowResult.failure(flow.getName(), vuserId, e);
            throw e;
        } catch (Throwable e) {
            result = TestFlowResult.failure(flow.getName(), vuserId, e);
            if (isDataDrivenScenario() && isScenarioFlow()) {
                scenarioContext.setBroken(e);
                eventBus.post(FlowFinishedMessage.newInstance(scenarioContext, flow, vuserId, executionId, Throwables.getRootCause(e)));
            } else {
                throw e;
            }
        } finally {
            if (!isScenarioFlow()) {
                if (listener instanceof ExtendedScenarioListener) {
                    ExtendedScenarioListener.class.cast(listener).onFlowFinished(flow, result);
                } else {
                    listener.onFlowFinished(flow);
                }
            }
        }
    }

    private boolean isScenarioFlow() {
        return flow.getId().equals(scenarioContext.getScenarioFlowId());
    }

    private boolean isDataDrivenScenario() {
        return ScenarioType.DATA_DRIVEN.equals(scenarioContext.getScenarioType());
    }


    private void runAllTestSteps(LinkedHashMap<String, DataRecord> dataSourcesRecords, ScenarioListener listener,
                                 TestContext context) throws Exception {
        LinkedHashMap<String, DataRecord> joinedDataSourcesRecords = new LinkedHashMap<>();
        joinedDataSourcesRecords.putAll(parentDataSourcesRecords);
        joinedDataSourcesRecords.putAll(dataSourcesRecords);

        Collection<TestStepInvocation> invocations = prepareTestStepChain(flow);
        Iterator<TestStepInvocation> invocationsIterator = invocations.iterator();

        try {
            while (invocationsIterator.hasNext()) {
                runTestStepAndHandleExceptions(invocationsIterator.next(), context, joinedDataSourcesRecords);
            }
        } finally {
            List<Throwable> unhandledExceptions = newArrayList();
            //continue iteration and execute alwaysRun steps if any
            while (invocationsIterator.hasNext()) {
                runAlwaysRunTestStepsAndCancelSubflows(invocationsIterator.next(), context, joinedDataSourcesRecords, unhandledExceptions);
            }
            if (!unhandledExceptions.isEmpty()) {
                Throwables.propagate(unhandledExceptions.get(unhandledExceptions.size() - 1));
            }
        }
    }

    private void runTestStepAndHandleExceptions(TestStepInvocation invocation, TestContext context, LinkedHashMap<String, DataRecord> joinedDataSourcesRecords) {
        try {
            runTestStep(context, joinedDataSourcesRecords, invocation);
        } catch (ScenarioListenerException testStepError) {
            logger.error("Error in listener while executing listener of Test Step {}:", invocation, Throwables.getRootCause(testStepError));
            onTestStepException(flow, invocation, context, vuserId, testStepError);
            Throwables.propagate(testStepError);
        } catch (ScenarioDataSourceValidationException validationError) {
            logger.error("Error validating test step {} parameters:", invocation, Throwables.getRootCause(validationError));
            onTestStepException(flow, invocation, context, vuserId, validationError);
            Throwables.propagate(validationError);
        } catch (Throwable testStepError) {
            logger.debug("Error during execution of Test Step {}:", invocation, Throwables.getRootCause(testStepError));
            onTestStepException(flow, invocation, context, vuserId, testStepError);
            handler.handle(testStepError);
        }
    }

    private void runAlwaysRunTestStepsAndCancelSubflows(TestStepInvocation invocation, TestContext context, LinkedHashMap<String, DataRecord> joinedDataSourcesRecords, List<Throwable> unhandledExceptions) {
        if (invocation.isAlwaysRun()) {
            try {
                logger.debug("After failure {}: executing step {}", flow.getId(), invocation.getName());
                runTestStep(context, joinedDataSourcesRecords, invocation);
                logger.debug("After failure {}: finished executing step {}", flow.getId(), invocation.getName());
            } catch (Throwable testStepError) {
                onTestStepException(flow, invocation, context, vuserId, testStepError);
                handleAlwaysRunException(invocation, testStepError, unhandledExceptions);
            }
        }

        if (invocation instanceof ParallelInvocation) {
            logger.debug("After failure {}: cancelling parallel invocation {}", flow.getId(), invocation.getName());
            eventBus.post(new FlowExecutionCancelledMessage((ParallelInvocation) invocation, vuserId));
            logger.debug("After failure {}: continuing", flow.getId());
        }
    }

    private void runTestStep(TestContext context, LinkedHashMap<String, DataRecord> joinedDataSourcesRecords, TestStepInvocation invocation) throws Exception {
        eventBus.post(new TestStepStartedMessage(flow, invocation, joinedDataSourcesRecords, vuserId));
        Optional<Object> result = invocation.run(this, scenarioContext, joinedDataSourcesRecords, context, newCopyOnWriteArrayList(flow.getDataRecordTransformers()));
        TestStepResult testStepResult = success(invocation.getName(), result);
        context.setAttribute(invocation.getName(), testStepResult);
        eventBus.post(TestStepFinishedMessage.newInstance(flow, invocation, vuserId));
    }

    private void onTestStepException(TestStepFlow flow, TestStepInvocation invocation, TestContext context, int vuserId, Throwable testStepError) {
        TestStepResult testStepResult = failure(invocation.getName(), testStepError);
        context.setAttribute(invocation.getName(), testStepResult);
        context.setAttribute(TestStepResult.LAST_FAILED_TEST_STEP, testStepResult);
        eventBus.post(TestStepFinishedMessage.newInstance(flow, invocation, vuserId, testStepError));
    }

    private void handleAlwaysRunException(TestStepInvocation invocation, Throwable testStepError, List<Throwable> unhandledExceptions) {
        try {
            handler.handle(testStepError);
            logger.info("Error during execution of always Run Test Step {}:", invocation, Throwables.getRootCause(testStepError));
        } catch (Throwable e) {
            logger.error("Error during execution of always Run Test Step {}:", invocation, Throwables.getRootCause(testStepError));
            unhandledExceptions.add(e);
        }
    }

    private Collection<TestStepInvocation> prepareTestStepChain(TestStepFlow flow) {
        Collection<TestStepInvocation> invocations = newArrayList();
        List<TestStepInvocation> testSteps = flow.getTestSteps();
        for (TestStepInvocation testStep : testSteps) {
            invocations.add(testStep);
        }

        return invocations;
    }

    protected long getStartDelay() {
        return startDelay;
    }

    public int getVuserId() {
        return vuserId;
    }

    protected String getExecutionId() {
        return executionId;
    }

    protected ScenarioExceptionHandler getExceptionHandler() {
        return handler.getScenarioExceptionHandler();
    }

    private class State implements TestStepFlow.State {
        private int iteration = 0;
        private long startTime = System.currentTimeMillis();
        private TestContext vUserContext;

        State(TestContext vUserContext) {
            this.vUserContext = vUserContext;
        }

        @Override
        public Long getStartTime() {
            return startTime;
        }

        @Override
        public int getIteration() {
            return iteration;
        }

        @Override
        public TestContext getContext() {
            return vUserContext;
        }

        @Override
        public int getVUser() {
            return vUserContext.getVUser();
        }

        private boolean shouldRepeat() {
            iteration++;
            final Predicate<TestStepFlow.State> repeatWhilePredicate = flow.getRunOptions().getRepeatWhile();
            synchronized (repeatWhilePredicate) {
                return repeatWhilePredicate.apply(this);
            }
        }
    }
}
