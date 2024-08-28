package com.ericsson.de.scenariorx.testware;

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.google.common.base.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxFlow;
import com.ericsson.de.scenariorx.api.RxFlowBuilderInterfaces;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.junit.After;
import org.junit.Test;

/**
 * Ported from com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilderTest
 */
@SuppressWarnings({"DanglingJavadoc", "deprecation"})
public class PortedTestStepFlowBuilderTest {

    private TestSteps testSteps = new TestSteps();

    @After
    public void destroy() {
        testSteps.clear();
    }

    @Test
    public void shouldTakeLonger_thanWaitTimeSpecified() {
        RxScenario scenario = scenario("Test Wait Flow Method")
                .addFlow(flowWait("Test Flow With Wait"))
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void shouldTakeLonger_thanWaitTimeSpecified_withSubFlow() {
        RxScenario scenario = scenario("Test Wait SubFlow Method")
                .addFlow(subFlowWait())
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void shouldTakeLonger_thanWaitTimeSpecified_withVUsers() {
        RxScenario scenario = scenario("Test Wait SubFlow Method")
                .addFlow(subFlowWait().withVUsers(4))
                .build();

        RxApi.run(scenario);
    }

    private RxFlowBuilderInterfaces.Steps<RxFlow> subFlowWait() {
        return flow("Test Flow With Sub Flow and Wait")
                .addSubFlow(flowWait("Sub Flow With Wait"));
    }

    private RxFlow flowWait(String flowName) {
        return flow(flowName)
                /** {@link TestSteps#recordStartTime()} */
                .addTestStep(TafRxScenarios.annotatedMethod(testSteps, TestSteps.RECORD_START_TIME))
                /** {@link TestSteps#waitSomeTime()} */
                .addTestStep(TafRxScenarios.annotatedMethod(testSteps, TestSteps.WAIT_SOME_TIME))
                /** {@link TestSteps#recordEndTime()} */
                .addTestStep(TafRxScenarios.annotatedMethod(testSteps, TestSteps.RECORD_END_TIME))
                /** {@link TestSteps#verify()} */
                .addTestStep(TafRxScenarios.annotatedMethod(testSteps, TestSteps.VERIFY_PASSED_TIME))
                .build();
    }

    public static class TestSteps {

        private static final String RECORD_START_TIME = "recordStartTime";
        private static final String RECORD_END_TIME = "recordEndTime";
        private static final String WAIT_SOME_TIME = "waitSomeTime";
        private static final String VERIFY_PASSED_TIME = "verifyPassedTime";

        private static final long WAIT_TIME = 3000;

        private static ThreadLocal<Long> startTime = new ThreadLocal<>();
        private static ThreadLocal<Long> endTime = new ThreadLocal<>();

        void clear() {
            startTime.remove();
            endTime.remove();
        }

        @TestStep(id = RECORD_START_TIME)
        public void recordStartTime() {
            startTime.set(System.currentTimeMillis());
        }

        @TestStep(id = RECORD_END_TIME)
        public void recordEndTime() {
            endTime.set(System.currentTimeMillis());
        }

        @TestStep(id = WAIT_SOME_TIME)
        public void waitSomeTime() {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                propagate(e);
            }
        }

        @TestStep(id = VERIFY_PASSED_TIME)
        public void verifyPassedTime() {
            assertThat(endTime.get() - startTime.get())
                    .isGreaterThanOrEqualTo(WAIT_TIME - WAIT_TIME / 10)
                    .withFailMessage("Test run time was shorter than the wait time");
        }
    }
}
