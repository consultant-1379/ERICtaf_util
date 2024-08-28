package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.ParallelInvocation;
import com.ericsson.cifwk.taf.scenario.impl.SyncInvocation;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Created by ekuijah on 03/08/2016.
 */
public class AlwaysRunTest {

    @Test
    public void testAlwaysRunInTestStepFlowBuilderForAddTestStep() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);

        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1).alwaysRun().addTestStep(step2);

        TestStepFlow testStepFlow = flow1.build();
        assertTrue(testStepFlow.getTestSteps().get(0).isAlwaysRun());
        assertFalse(testStepFlow.getTestSteps().get(1).isAlwaysRun());
    }

    @Test
    public void testAlwaysRunInTestStepFlowBuilderForSplit() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable4 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);
        TestStepDefinition step4 = runnable(runnable4);

        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        TestStepFlowBuilder flow3 = flow("flow3")
                                .addTestStep(step3)
                                .split(flow1, flow2).alwaysRun()
                                .addTestStep(step4);
        TestStepFlowBuilder flow4 = flow("flow4")
                                .addTestStep(step3)
                                .split(flow1, flow2)
                                .addTestStep(step4);

        TestStepFlow testStepFlow = flow3.build();
        assertFalse(testStepFlow.getTestSteps().get(0).isAlwaysRun());
        assertTrue(testStepFlow.getTestSteps().get(1).isAlwaysRun());
        assertTrue(testStepFlow.getTestSteps().get(2).isAlwaysRun());
        assertFalse(testStepFlow.getTestSteps().get(3).isAlwaysRun());
        assertTrue(testStepFlow.getTestSteps().get(1) instanceof ParallelInvocation);
        assertTrue(testStepFlow.getTestSteps().get(2) instanceof SyncInvocation);

        testStepFlow = flow4.build();
        assertFalse(testStepFlow.getTestSteps().get(0).isAlwaysRun());
        assertFalse(testStepFlow.getTestSteps().get(1).isAlwaysRun());
        assertTrue(testStepFlow.getTestSteps().get(2).isAlwaysRun());
        assertFalse(testStepFlow.getTestSteps().get(3).isAlwaysRun());
        assertTrue(testStepFlow.getTestSteps().get(1) instanceof ParallelInvocation);
        assertTrue(testStepFlow.getTestSteps().get(2) instanceof SyncInvocation);
    }

    @Test
    public void testAlwaysRunInTestStepFlowBuilderForSubFlow() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);

        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        TestStepFlowBuilder flow3 = flow("flow3")
                                .addTestStep(step3)
                                .addSubFlow(flow1)
                                .addSubFlow(flow2).alwaysRun();

        TestStepFlow testStepFlow = flow3.build();
        assertFalse(testStepFlow.getTestSteps().get(0).isAlwaysRun());
        assertFalse(testStepFlow.getTestSteps().get(1).isAlwaysRun());
        assertTrue(testStepFlow.getTestSteps().get(2).isAlwaysRun());
    }

    @Test
    public void testAlwaysRunInTestScenarioBuilderForAddFlow() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);

        TestScenario scenario = scenario("test")
                            .addFlow(flow1)
                            .addFlow(flow2).alwaysRun()
                            .build();

        assertFalse(scenario.getFlow().getTestSteps().get(0).isAlwaysRun());
        assertTrue(scenario.getFlow().getTestSteps().get(2).isAlwaysRun());
    }

    @Test
    public void testAlwaysRunInTestScenarioBuilderForSplit() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable4 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);
        TestStepDefinition step4 = runnable(runnable4);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        TestStepFlowBuilder flow3 = flow("flow3").addTestStep(step3);
        TestStepFlowBuilder flow4 = flow("flow4").addTestStep(step4);

        TestScenario scenario = scenario("test")
                            .addFlow(flow1)
                            .split(flow2, flow3).alwaysRun()
                            .addFlow(flow4)
                            .build();

        assertFalse(scenario.getFlow().getTestSteps().get(0).isAlwaysRun());
        assertTrue(scenario.getFlow().getTestSteps().get(2).isAlwaysRun());
        assertFalse(scenario.getFlow().getTestSteps().get(4).isAlwaysRun());
        assertTrue(scenario.getFlow().getTestSteps().get(2) instanceof ParallelInvocation);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAlwaysRunSequenceCallForTestStepFlowBuilder1() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        flow("flow3")
            .addTestStep(step3)
            .addSubFlow(flow1)
            .addSubFlow(flow2).alwaysRun().alwaysRun();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAlwaysRunSequenceCallForTestStepFlowBuilder2() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        flow("flow3")
            .addTestStep(step3)
            .split(flow1, flow2).withVusers(2).alwaysRun();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAlwaysRunSequenceCallForTestStepFlowBuilder3() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        TafDataSourceDefinitionBuilder dataSource = mock(TafDataSourceDefinitionBuilder.class, RETURNS_DEEP_STUBS);
        flow("flow3").addTestStep(step3).addSubFlow(flow1).addSubFlow(flow2).withDataSources(dataSource)
                    .alwaysRun();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAlwaysRunSequenceCallForTestScenarioBuilder1() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        scenario("test")
            .addFlow(flow1)
            .addFlow(flow2).alwaysRun().alwaysRun()
            .build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAlwaysRunSequenceCallForTestScenarioBuilder2() {
        Runnable runnable1 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable2 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        Runnable runnable3 = mock(Runnable.class, RETURNS_DEEP_STUBS);
        TestStepDefinition step1 = runnable(runnable1);
        TestStepDefinition step2 = runnable(runnable2);
        TestStepDefinition step3 = runnable(runnable3);
        TestStepFlowBuilder flow1 = flow("flow1").addTestStep(step1);
        TestStepFlowBuilder flow2 = flow("flow2").addTestStep(step2);
        TestStepFlowBuilder flow3 = flow("flow3").addTestStep(step3);
        scenario("test")
                .addFlow(flow1)
                .split(flow2, flow3)
                .withDefaultVusers(2)
                .alwaysRun()
                .build();
    }
}

