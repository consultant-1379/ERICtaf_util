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

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.ExtendedScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestFlowResult;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.filter;
import static java.util.Collections.frequency;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ScenarioFlowsTest extends ScenarioTest {
    @Rule
    public Timeout globalTimeout = new Timeout(10_000);

    ListMultimap<Integer, String> stacksByVUsers = Multimaps.synchronizedListMultimap(ArrayListMultimap.<Integer, String>create());

    @Test
    public void shouldSupportBeforeFlowAfterFlow() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                .beforeFlow(
                                        pushToStack("before1"),
                                        pushToStack("before2"))
                                .addTestStep(runnable(
                                        pushToStack("step")))
                                .afterFlow(
                                        pushToStack("after1"),
                                        pushToStack("after2"))
                )
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        assertThat(stack, contains("before1", "before2", "step", "step", "step", "after1", "after2"));
    }

    @Test
    public void shouldSupportBeforeFlowAfterFlowExceptions() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                .beforeFlow(
                                        pushToStack("before1"),
                                        throwOnRun("ex1"),
                                        pushToStack("before2"))
                                .addTestStep(runnable(
                                        pushToStack("step")))
                                .afterFlow(
                                        pushToStack("after1"),
                                        throwOnRun("ex2"),
                                        pushToStack("after2"))
                )
                .withDefaultVusers(3)
                .build();

        try {
            runner.start(scenario);
            fail("Exception expected");
        } catch (VerySpecialException e) {
            assertThat(e.getMessage(), equalTo("ex2"));
        }

        stackContains("before1", "before2", "after1", "after2");
    }

    @Test
    public void shouldStoreResultOfFlowSuccessful(){
        TestScenario scenario = scenario().addFlow(flow("flow").addTestStep(runnable(pushToStack("1")))).build();
        final TestListener listener = new TestListener(true);
        runner().withListener(listener).build().start(scenario);
        assertThat(listener.isCalled(), is(true));
    }

    @Test
    public void shouldStoreResultOfFlowFailureForExceptionThrown(){
        TestScenario scenario = scenario().addFlow(flow("flow").addTestStep(runnable(throwOnRun("exception"))).withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE)))
                                          .build();
        final TestListener listener = new TestListener(false);
        try {
            runner().withListener(listener).build().start(scenario);
        } catch (Exception e){
            // ignore
        }
        assertThat(listener.isCalled(), is(true));
    }

    static class TestListener extends ExtendedScenarioListener {

        private final boolean isSuccessful;
        private boolean called;

        public TestListener(final boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
        }

        @Override
        public void onFlowFinished(final TestStepFlow flow, TestFlowResult result) {
            assertThat(result.isSuccessful(), is(isSuccessful));
            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }

    @Test
    public void shouldRunOnlyOnceInSubflow() {
        final TestStepFlow subFlow = flow("subFlow")
                .beforeFlow(
                        sleepAndPushToStack("before1"),
                        pushToStack("before2"))
                .addTestStep(runnable(
                        pushToStack("substep")))
                .withDataSources(dataSource(CSV_DATA_SOURCE))
                .afterFlow(
                        sleepAndPushToStack("after1"),
                        pushToStack("after2"))
                .build();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(flow("main")
                                .addTestStep(runnable(pushToStack("step1")))
                                .addSubFlow(subFlow)
                                .addTestStep(runnable(pushToStack("step2")))
                                .syncPoint("wait-before-next-ds-iteration")
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                                .withVusers(4)
                )
                .build();

        runner.start(scenario);

        assertThat(stack, contains(
                //GLOBAL_DATA_SOURCE 1 data record
                "step1", "step1", "step1", "step1",
                "before1", "before2",
                "substep", "substep", "substep", "substep", //CSV_DATA_SOURCE * vUsers
                "substep", "substep", "substep", "substep",
                "substep", "substep", "substep", "substep",
                "after1", "after2",
                "step2", "step2", "step2", "step2",

                //GLOBAL_DATA_SOURCE 2 data record
                "step1", "step1", "step1", "step1",
                "before1", "before2",
                "substep", "substep", "substep", "substep", //CSV_DATA_SOURCE * vUsers
                "substep", "substep", "substep", "substep",
                "substep", "substep", "substep", "substep",
                "after1", "after2",
                "step2", "step2", "step2", "step2"
        ));
    }

    protected Runnable sleepAndPushToStack(final String push) {
        return new Runnable() {
            @Override
            public void run() {
                sleep(1000L);
                stack.push(push);
            }
        };
    }

    @Test
    public void shouldRunOnlyOnceInSubflowAndDoParallel() {
        final TestStepFlow subFlow1 = flow("subsubFlow1")
                .beforeFlow(pushToStack("subsubFlow1.before1"))
                .addTestStep(runnable(pushToStack("subsubFlow1.subsubstep")))
                .afterFlow(pushToStack("subsubFlow1.after1"))
                .build();

        final TestStepFlow subFlow2 = flow("subsubFlow2")
                .beforeFlow(pushToStack("subsubFlow2.before2"))
                .addTestStep(runnable(pushToStack("subsubFlow2.subsubstep")))
                .afterFlow(pushToStack("subsubFlow2.after2"))
                .build();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(flow("main")
                                .addSubFlow(
                                        flow("subFlow")
                                                .addTestStep(runnable(pushToStack("subFlow.subStep1")))
                                                .split(subFlow1, subFlow2)
                                                .addTestStep(runnable(pushToStack("subFlow.subStep2")))
                                )
                        .addTestStep(runnable(pushToStack("main.step2")))
                                .syncPoint("wait-before-next-ds-iteration")
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                                .withVusers(2)
                )
                .build();

        runner.start(scenario);

        printStack();

        assertThat(
                filter(stack,
                        or(startsWith("subsubFlow1"),
                                startsWith("subFlow"),
                                startsWith("main"))),
                contains(
                        //GLOBAL_DATA_SOURCE 1 data record
                        "subFlow.subStep1", "subFlow.subStep1",
                        "subsubFlow1.before1",
                        "subsubFlow1.subsubstep", "subsubFlow1.subsubstep",
                        "subsubFlow1.after1",
                        "subFlow.subStep2", "subFlow.subStep2",
                        "main.step2", "main.step2", //2 vUsers


                        //GLOBAL_DATA_SOURCE 2 data record
                        "subFlow.subStep1", "subFlow.subStep1",
                        "subsubFlow1.before1",
                        "subsubFlow1.subsubstep", "subsubFlow1.subsubstep",
                        "subsubFlow1.after1",
                        "subFlow.subStep2", "subFlow.subStep2",
                        "main.step2", "main.step2" //2 vUsers
                ));

        assertThat(
                filter(stack,
                        or(startsWith("subsubFlow2"),
                                startsWith("subFlow"),
                                startsWith("main"))),
                contains(
                        //GLOBAL_DATA_SOURCE 1 data record
                        "subFlow.subStep1", "subFlow.subStep1",
                        "subsubFlow2.before2",
                        "subsubFlow2.subsubstep", "subsubFlow2.subsubstep",
                        "subsubFlow2.after2",
                        "subFlow.subStep2", "subFlow.subStep2",
                        "main.step2", "main.step2", //2 vUsers

                        //GLOBAL_DATA_SOURCE 1 data record
                        "subFlow.subStep1", "subFlow.subStep1",
                        "subsubFlow2.before2",
                        "subsubFlow2.subsubstep", "subsubFlow2.subsubstep",
                        "subsubFlow2.after2",
                        "subFlow.subStep2", "subFlow.subStep2",
                        "main.step2", "main.step2" //2 vUsers
                ));
    }

    @Test
    public void shouldRunAfterStepsInCaseOfException() {
        final TestStepFlow subFlow1 = flow("subFlow")
                .beforeFlow(pushToStack("before1"))
                .addTestStep(throwExceptionForVUser(2))
                .addTestStep(runnable(pushToStack("substep")))
                .afterFlow(pushToStack("after1"))
                .build();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(flow("main")
                                .addTestStep(runnable(pushToStack("step1")))
                                .addSubFlow(subFlow1)
                                .addTestStep(runnable(pushToStack("step2")))
                                .withVusers(2)
                )
                .build();

        try {
            runner.start(scenario);
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();

        assertThat(stack, contains(
                "step1", "step1",
                "before1",
                "substep", //here exception was thrown
                "after1", //continue ignoring exception
                "step2" //runner for vUser1 finished, runner for vUser1 interrupted
        ));
    }

    @Test
    public void shouldAlwaysRunStepsInCaseOfException() {
        try {
            runScenarioWithAlwaysRunSteps(ScenarioExceptionHandler.PROPAGATE, ExceptionHandler.PROPAGATE, 1);
            fail("Should throw VerySpecialException");
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "alwaysRun1",
                "alwaysRun2"
        ));
    }

    @Test
    public void shouldAlwaysRunFlowInCaseOfExceptionInPreviousFlow() {
        TestStepFlowBuilder flowWithException =
                flow("flow1")
                        .addTestStep(runnable(pushToStack("stepBeforeException")))
                        .addTestStep(throwExceptionForVUser(1));

        TestStepFlowBuilder shouldRunFlow =
                flow("shouldRunFlow1")
                        .addTestStep(runnable(pushToStack("alwaysFlow1Step1")))
                        .addTestStep(throwExceptionForVUser(1))
                        .addTestStep(runnable(pushToStack("alwaysFlow1Step2"))).alwaysRun()
                        .withDataSources(dataSource(CSV_DATA_SOURCE));

        TestScenario scenario = scenario()
                .addFlow(flowWithException.withDataSources(dataSource(CSV_DATA_SOURCE)))
                .addFlow(shouldRunFlow).alwaysRun()
                .addFlow(shouldRunFlow).alwaysRun()
                .build();

        try {
            runner.start(scenario);
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "alwaysFlow1Step1",
                "alwaysFlow1Step2",
                "alwaysFlow1Step1",
                "alwaysFlow1Step2"
        ));
    }

    @Test
    public void shouldAlwaysRunFlowAndSubflowInCaseOfExceptionInPreviousFlow() {
        TestStepFlowBuilder flowWithException =
                flow("flow1")
                        .addTestStep(runnable(pushToStack("stepBeforeException")))
                        .addTestStep(throwExceptionForVUser(1));

        TestStepFlowBuilder flowWithSubFlow =
                flow("flow2")
                        .addSubFlow(flow("subFlow")
                                .addTestStep(runnable(pushToStack("subFlow"))))
                        .addTestStep(throwExceptionForVUser(1))
                        .addSubFlow(flow("alwaysRunSubFlow")
                                .addTestStep(runnable(pushToStack("alwaysRunSubFlowStep")))).alwaysRun()
                        .withDataSources(dataSource(CSV_DATA_SOURCE));

        TestStepFlowBuilder tearDownFlow =
                flow("shouldRunFlow1")
                        .addTestStep(runnable(pushToStack("alwaysFlow1Step1")))
                        .addTestStep(throwExceptionForVUser(1))
                        .addTestStep(runnable(pushToStack("alwaysFlow1Step2"))).alwaysRun()
                        .withDataSources(dataSource(CSV_DATA_SOURCE));


        TestScenario scenario = scenario()
                .addFlow(flowWithException.withDataSources(dataSource(CSV_DATA_SOURCE)))
                .addFlow(flowWithSubFlow).alwaysRun()
                .addFlow(tearDownFlow).alwaysRun()
                .build();

        try {
            runner.start(scenario);
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "subFlow",
                "alwaysRunSubFlowStep",
                "alwaysFlow1Step1",
                "alwaysFlow1Step2"
        ));
    }

    @Test
    public void shouldAlwaysRunParallelFlowInCaseOfExceptionInPreviousFlowInTestScenario() {
        TestStepFlowBuilder flowWithException =
                flow("flow1")
                        .addTestStep(runnable(pushToStack("stepBeforeException")))
                        .addTestStep(throwExceptionForVUser(1));
        TestStepFlowBuilder flow2 =
                flow("flow2")
                        .addTestStep(runnable(pushToStack("flow2")));
        TestStepFlowBuilder flow3 =
                flow("flow3")
                        .addTestStep(runnable(pushToStack("flow3")));
        TestStepFlowBuilder flow4 =
                flow("flow4")
                        .addTestStep(runnable(pushToStack("flow4")))
                        .withDataSources(dataSource(CSV_DATA_SOURCE));

        TestScenario scenario = scenario()
                .addFlow(flowWithException.withDataSources(dataSource(CSV_DATA_SOURCE)))
                .split(flow2,flow3).alwaysRun()
                .addFlow(flow4)
                .build();
        try {
            runner.start(scenario);
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();
        assertThat(stack, hasSize(3));
        assertThat(stack.get(0), equalTo("stepBeforeException"));
        assertThat(stack, containsInAnyOrder(
                "stepBeforeException",
                "flow2","flow3"
                ));
    }

    @Test
    public void shouldAlwaysRunParallelFlowInCaseOfExceptionInPreviousStepInTestStepFlowBuilder() {
        TestStepFlowBuilder flow11 =
                flow("flow11")
                        .addTestStep(runnable(pushToStack("flow11")));
        TestStepFlowBuilder flow12 =
                flow("flow12")
                        .addTestStep(runnable(pushToStack("flow12")));
        TestStepFlowBuilder flow1 =
                flow("flow1")
                        .addTestStep(runnable(pushToStack("stepBeforeException")))
                        .addTestStep(throwExceptionForVUser(1))
                        .split(flow11, flow12).alwaysRun()
                        .addTestStep(runnable(pushToStack("End of flow1")));
        TestScenario scenario = scenario()
                .addFlow(flow1)
                .build();
        try {
            runner.start(scenario);
        } catch (VerySpecialException e) {
            //expected
        }
        printStack();
        assertThat(stack, containsInAnyOrder(
                "stepBeforeException",
                "flow11","flow12"
                ));
    }

    @Test
    public void shouldAlwaysRunStepsInCaseOfExceptionWithVUsers() {
        try {
            runScenarioWithAlwaysRunSteps(ScenarioExceptionHandler.PROPAGATE, ExceptionHandler.IGNORE, 2);
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();
        assertThat(frequency(stack, "stepBeforeException"), equalTo(2));
        assertThat(frequency(stack, "stepAfterException"), equalTo(1));
        assertThat(frequency(stack, "alwaysRun1"), equalTo(2));
        assertThat(frequency(stack, "alwaysRun2"), equalTo(2));
        assertThat(frequency(stack, "flow2testStep"), equalTo(2));
    }

    @Test
    public void shouldAlwaysRunStepsWithDataSources() {
        TestScenario scenario = scenario()
                .addFlow(getAlwaysRunFlow()
                        .withDataSources(dataSource(CSV_DATA_SOURCE))).build();
        try {
            runner.start(scenario);
        } catch (VerySpecialException e) {
            //expected
        }

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "alwaysRun1",
                "alwaysRun2"
        ));
    }

    @Test
    public void shouldStoreLastFailedTestStep() {
        TestScenario scenario = scenario()
                .addFlow(flow("flow")
                                .addTestStep(runnable(pushToStack("stepBeforeException")))
                                .addTestStep(throwException("exception1"))
                                .addTestStep(checkLastTestStepException("exception1"))
                                .addTestStep(checkLastTestStepException("exception1"))
                                .addTestStep(throwException("exception2"))
                                .addTestStep(checkLastTestStepException("exception2"))
                                .addTestStep(runnable(pushToStack("normalTestStep")))
                                .addTestStep(checkLastTestStepException("exception2"))
                )
                .withExceptionHandler(ScenarioExceptionHandler.IGNORE)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(4));
        assertThat(stack, contains("stepBeforeException", "normalTestStep"));
    }

    @Test
    public void shouldStoreLastFailedTestStep_alwaysRun() {
        TestScenario scenario = scenario()
                .addFlow(flow("flow")
                                .addTestStep(runnable(pushToStack("stepBeforeException")))
                                .addTestStep(throwException("testStep"))
                                .addTestStep(checkLastTestStepException("testStep").alwaysRun())
                                .addTestStep(checkLastTestStepException("testStep").alwaysRun())
                                .addTestStep(throwException("alwaysRun1").alwaysRun())
                                .addTestStep(checkLastTestStepException("alwaysRun1").alwaysRun())
                                .addTestStep(runnable(pushToStack("alwaysRun2")).alwaysRun())
                                .addTestStep(checkLastTestStepException("alwaysRun1").alwaysRun())
                                .addTestStep(throwException("lastFailingStep").alwaysRun())
                                .addTestStep(checkLastTestStepException("lastFailingStep").alwaysRun())

                ).build();
        try {
            runner.start(scenario);
        } catch (VerySpecialException expected) {
            assertThat(expected.getMessage(), equalTo("lastFailingStep"));
        }

        assertThat(count.get(), equalTo(5));
        assertThat(stack, contains("stepBeforeException", "alwaysRun2"));
    }

    private TestStepDefinition throwException(final String message) {
        return runnable(throwOnRun(message));
    }

    private Runnable throwOnRun(final String message) {
        return new Runnable() {
            @Override
            public void run() {
                throw new VerySpecialException(message);
            }
        };
    }

    private TestStepDefinition checkLastTestStepException(final String message) {
        return runnable(new Runnable() {
            @Override
            public void run() {
                count.incrementAndGet();

                TestStepResult lastFailedTestStep = ServiceRegistry.getTestContextProvider().get().getAttribute(TestStepResult.LAST_FAILED_TEST_STEP);

                assertThat(lastFailedTestStep.getName(), equalTo(RunnableInvocation.NAME));
                assertFalse(lastFailedTestStep.isSuccessful());
                assertThat(lastFailedTestStep.getException(), instanceOf(VerySpecialException.class));
                assertThat(lastFailedTestStep.getException().getMessage(), equalTo(message));
            }
        });
    }

    @Test
    public void shouldIgnoreAlwaysRunStepsInSubflow() {
        TestScenario scenario = scenario()
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("stepBeforeException")))
                        .addTestStep(throwExceptionForVUser(1))
                        .addSubFlow(flow("subflow")
                                .addTestStep(runnable(pushToStack("subFlowAlwaysRun")).alwaysRun()))
                        .addTestStep(runnable(pushToStack("alwaysRun")).alwaysRun())
                        .withDataSources(dataSource(CSV_DATA_SOURCE))).build();

        runner().withDefaultExceptionHandler(ScenarioExceptionHandler.IGNORE).build().start(scenario);

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "alwaysRun"
        ));
    }


    @Test
    public void shouldRunWithAlwaysRun() {
        runScenarioWithAlwaysRunSteps(ScenarioExceptionHandler.IGNORE, ExceptionHandler.PROPAGATE, 1);

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "stepAfterException",
                "alwaysRun1",
                "alwaysRun2",
                "flow2testStep"
        ));
    }

    @Test
    public void shouldAlwaysRunNotAffectFlowExecution() {
        runScenarioWithAlwaysRunSteps(ScenarioExceptionHandler.PROPAGATE, ExceptionHandler.IGNORE, 1);

        printStack();
        assertThat(stack, contains(
                "stepBeforeException",
                "alwaysRun1",
                "alwaysRun2",
                "flow2testStep"
        ));
    }


    private void runScenarioWithAlwaysRunSteps(ScenarioExceptionHandler testStepExceptionHandler,
                                               ExceptionHandler runnerExceptionHandler,
                                               int vUsers) {
        TestScenario scenario = scenario("Scenario 1")
                .addFlow(
                        getAlwaysRunFlow()
                )
                .addFlow(
                        flow("flow2")
                                .addTestStep(runnable(pushToStack("flow2testStep"))))
                .withDefaultVusers(vUsers)
                .withExceptionHandler(testStepExceptionHandler)
                .build();

        runner().withExceptionHandler(runnerExceptionHandler).build().start(scenario);
    }

    private TestStepFlowBuilder getAlwaysRunFlow() {
        return flow("flow1")
                .addTestStep(runnable(pushToStack("stepBeforeException")))
                .addTestStep(throwExceptionForVUser(1))
                .addTestStep(runnable(pushToStack("stepAfterException")))
                .addTestStep(runnable(pushToStack("alwaysRun1")).alwaysRun())
                .addTestStep(runnable(pushToStack("alwaysRun2")).alwaysRun());
    }

    @Test
    public void shouldSupportBeforeFlowAfterFlowParallel() {
        TestScenario scenario = scenario()
                .split(
                        flow("flow1")
                                .beforeFlow(pushToStack("flow1.before"))
                                .addTestStep(runnable(pushToStack("flow1.step")))
                                .afterFlow(pushToStack("flow1.after"))
                                .withVusers(3),
                        flow("flow2")
                                .beforeFlow(pushToStack("flow2.before"))
                                .addTestStep(runnable(pushToStack("flow2.step")))
                                .afterFlow(pushToStack("flow2.after"))
                                .withVusers(3)
                )
                .build();

        runner.start(scenario);

        printStack();

        assertThat(filter(stack, startsWith("flow1.")),
                contains("flow1.before",
                        "flow1.step", "flow1.step", "flow1.step",
                        "flow1.after"));
        assertThat(filter(stack, startsWith("flow2.")),
                contains("flow2.before",
                        "flow2.step", "flow2.step", "flow2.step",
                        "flow2.after"));
    }

    @Test
    public void shouldSupportBeforeFlowInDataSource() {
        TestScenario scenario = scenario().addFlow(
                flow("flow1")
                        .beforeFlow(pushToStack("flow1.before"))
                        .addTestStep(runnable(pushToStack("flow1.step")))
                        .afterFlow(pushToStack("flow1.after"))
                        .withVusers(3)
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                .build();

        runner.start(scenario);

        assertThat(stack,
                contains("flow1.before",
                        "flow1.step", "flow1.step", "flow1.step",
                        "flow1.step", "flow1.step", "flow1.step",
                        "flow1.after"
                ));
    }

    @Test
    public void shouldSupportBeforeFlowInparentDataSource() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("mainFlow").split(
                                flow("flow1")
                                        .beforeFlow(pushToStack("flow1.before"))
                                        .addTestStep(runnable(pushToStack("flow1.step")))
                                        .afterFlow(pushToStack("flow1.after"))
                                        .withVusers(3))
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                .build();

        runner.start(scenario);

        assertThat(stack,
                contains("flow1.before",
                        "flow1.step", "flow1.step", "flow1.step",
                        "flow1.after",
                        "flow1.before",
                        "flow1.step", "flow1.step", "flow1.step",
                        "flow1.after"
                ));
    }

    @Test
    public void shouldSupportBeforeFlowInTrickyDataSource() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("mainFlow")
                                .split(
                                        flow("flow1")
                                                .beforeFlow(pushToStack("flow1.before"))
                                                .addTestStep(runnable(pushToStack("flow1.step")))
                                                .afterFlow(pushToStack("flow1.after"))
                                )
                                .withDataSources(dataSource(CSV_DATA_SOURCE).withFilter("$VUSER == 1"))
                                .withVusers(3))
                .build();

        runner.start(scenario);

        assertThat(stack,
                contains("flow1.before",
                        "flow1.step",
                        "flow1.after",
                        "flow1.before",
                        "flow1.step",
                        "flow1.after",
                        "flow1.before",
                        "flow1.step",
                        "flow1.after"
                ));
    }

    private Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.startsWith(prefix);
            }
        };
    }

    @Test
    public void shouldSupportBeforeFlowAfterFlowInSubFlow() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                .beforeFlow(pushToStack("before1"))
                                .addTestStep(runnable(pushToStack("step")))
                                .addSubFlow(flow("subFlow")
                                        .beforeFlow(pushToStack("subBefore1"))
                                        .addTestStep(runnable(pushToStack("subStep")))
                                        .afterFlow(pushToStack("subAfter1")))
                                .afterFlow(pushToStack("after1"))
                )
                .build();

        runner.start(scenario);

        assertThat(stack, contains("before1", "step", "subBefore1", "subStep", "subAfter1", "after1"));
    }

    @Test
    public void shouldPredictablyAssignVUsersInComplexScenarios() {
        int vUsers = 2;
        TestScenario scenario = scenario("test attributes")
                .addFlow(
                        flow("mainFlow")
                                .addTestStep(pushVUserToStack("testStep1"))
                                .addSubFlow(flow("subflow1")
                                        .addTestStep(pushVUserToStack("subflow1-testStep1"))
                                        .addSubFlow(
                                                flow("subsubflow1")
                                                        .addTestStep(pushVUserToStack("subsubflow1-testStep1"))
                                        )
                                )
                                .addTestStep(pushVUserToStack("testStep2"))
                                .split(
                                        flow("splitflow1")
                                                .addTestStep(pushVUserToStack("splitflow1-testStep1"))
                                                .addSubFlow(flow("splitflow1subflow1")
                                                        .addTestStep(pushVUserToStack("splitflow1subflow1-testStep1"))
                                                )
                                                .withVusers(2),
                                        flow("splitflow2")
                                                .addTestStep(pushVUserToStack("splitflow2-testStep1"))
                                )
                )
                .withDefaultVusers(vUsers)
                .build();

        runner.start(scenario);

        assertThat(stack, containsInAnyOrder(
                "vUser1 testStep1",
                "vUser1 subflow1-testStep1",
                "vUser1 subsubflow1-testStep1",
                "vUser1 testStep2",
                "vUser3 splitflow1-testStep1",
                "vUser3 splitflow1subflow1-testStep1",
                "vUser4 splitflow1-testStep1",
                "vUser4 splitflow1subflow1-testStep1",
                "vUser5 splitflow2-testStep1",

                "vUser2 testStep1",
                "vUser2 subflow1-testStep1",
                "vUser2 subsubflow1-testStep1",
                "vUser2 testStep2",
                "vUser6 splitflow1-testStep1",
                "vUser6 splitflow1subflow1-testStep1",
                "vUser7 splitflow1-testStep1",
                "vUser7 splitflow1subflow1-testStep1",
                "vUser8 splitflow2-testStep1"
        ));

    }

    private TestStepDefinition pushVUserToStack(final String testStep) {
        return runnable(new Runnable() {
            @Override
            public void run() {
                TestContext context = ServiceRegistry.getTestContextProvider().get();
                stack.push("vUser" + context.getVUser() + " " + testStep);
            }
        });
    }
}
