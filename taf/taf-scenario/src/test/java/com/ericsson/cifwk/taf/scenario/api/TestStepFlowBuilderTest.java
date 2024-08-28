package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioTest;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.google.common.truth.Truth.assertWithMessage;

public class TestStepFlowBuilderTest extends ScenarioTest {

    private static final String STEP_1 = "step1";
    private static final String RECORD_START_TIME = "recordStartTime";
    private static final String RECORD_END_TIME = "recordEndTime";
    private static final String VERIFY = "verify";

    private static long WAIT_TIME = 2000;
    private static ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static ThreadLocal<Long> endTime = new ThreadLocal<>();

    @After
    public void destroy() {
        startTime.remove();
        endTime.remove();
    }

    @Test
    public void splitSubFlowsWithinFlowTest() {
        scenario("Split subflow test")
                .addFlow(splitSubFlow())
                .build();
    }

    private TestStepFlow splitSubFlow() {
        return flow("Parent Flow")
                .split(flow1(), flow2())
                .build();
    }

    private TestStepFlow flow1() {
        return flow("split flow 2")
                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                .build();
    }

    private TestStepFlow flow2() {
        return flow("split flow 1")
                .addTestStep(annotatedMethod(this, PUSH_VUSER))
                .addTestStep(annotatedMethod(this, SET_ATTRIBUTE))
                .addTestStep(annotatedMethod(this, CHECK_ATTRIBUTE))
                .build();
    }

    @Test
    public void shouldTakeLongerThanWaitTimeSpecified() {
        TestScenario scenario = scenario("Test Wait Flow Method")
                .addFlow(flowWait("Test Flow With Wait"))
                .build();
        runner.start(scenario);
    }

    @Test
    public void shouldTakeLongerThanWaitTimeSpecifiedWithSubFlow() {
        TestScenario scenario = scenario("Test Wait SubFlow Method")
                .addFlow(subFlowWait())
                .build();
        runner.start(scenario);
    }

    @Test
    public void shouldTakeLongerThanWaitTimeSpecifiedWithVUsers() {
        TestScenario scenario = scenario("Test Wait SubFlow Method")
                .addFlow(subFlowWait())
                .withDefaultVusers(4)
                .build();
        runner.start(scenario);
    }

    @Test
    public void waitWithVUsersShouldNotImpactEachOther() {
        int vUsers = 3;
        TestScenario scenario = scenario()
                .addFlow(flowWaitWithStackPush())
                .withDefaultVusers(vUsers)
                .build();
        startTime.set(System.currentTimeMillis());
        runner.start(scenario);
        assertWithMessage("Test ran for too long").that(System.currentTimeMillis() - startTime.get()).isLessThan(WAIT_TIME * vUsers);
        assertWithMessage("Test run time was shorter than the wait time").that(System.currentTimeMillis() - startTime.get()).isGreaterThan(WAIT_TIME);
        stackContains("before1", "step1", "step1", "step1", "step2", "step2", "step2", "after1");
    }

    @Test
    public void waitWithSplit() {
        TestScenario scenario = scenario()
                .addFlow(flow("Parent Flow with Wait and Stack Push")
                        .split(flowWaitWithStackPush(), flowWaitWithStackPush(), flowWaitWithStackPush())
                ).build();
        startTime.set(System.currentTimeMillis());
        runner.start(scenario);
        assertWithMessage("Test ran for too long").that(System.currentTimeMillis() - startTime.get()).isLessThan(WAIT_TIME * 3);
        assertWithMessage("Test run time was shorter than the wait time").that(System.currentTimeMillis() - startTime.get()).isGreaterThan(WAIT_TIME);
    }

    private TestStepFlow subFlowWait() {
        return flow("Test Flow With Sub Flow and Wait")
                .addSubFlow(flowWait("Sub Flow With Wait"))
                .build();
    }

    private TestStepFlow flowWait(String flowName) {
        return flow(flowName)
                .addTestStep(annotatedMethod(this, RECORD_START_TIME))
                .pause(WAIT_TIME, TimeUnit.MILLISECONDS)
                .addTestStep(annotatedMethod(this, RECORD_END_TIME))
                .addTestStep(annotatedMethod(this, VERIFY))
                .build();
    }

    private TestStepFlowBuilder flowWaitWithStackPush() {
        return flow("Test Flow with Wait and Stack Push")
                .beforeFlow(pushToStack("before1"))
                .addTestStep(runnable(pushToStack("step1")))
                .pause(WAIT_TIME, TimeUnit.MILLISECONDS)
                .addTestStep(runnable(pushToStack("step2")))
                .afterFlow(pushToStack("after1"));
    }

    @TestStep(id = STEP_1)
    public void testStep1() {
        //do nothing
    }

    @TestStep(id = RECORD_START_TIME)
    public void recordStartTime() {
        startTime.set(System.currentTimeMillis());
    }

    @TestStep(id = RECORD_END_TIME)
    public void recordEndTime() {
        endTime.set(System.currentTimeMillis());
    }

    @TestStep(id = VERIFY)
    public void verify() {
        assertWithMessage("Test run time was shorter than the wait time").that(endTime.get() - startTime.get()).isAtLeast(WAIT_TIME - 1);
    }
}
