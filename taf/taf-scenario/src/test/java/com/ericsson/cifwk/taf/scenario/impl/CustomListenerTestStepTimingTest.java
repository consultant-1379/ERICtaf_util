package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.assertj.core.api.Assertions.assertThat;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ExtendedScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import org.junit.Test;

public class CustomListenerTestStepTimingTest {

    @Test
    public void simpleScenario() {
        TestScenario scenario = scenario("Sleepy Scenario")
                .split(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, "SLEEP_1"))
                                .addTestStep(annotatedMethod(this, "SLEEP_2"))
                )
                .build();

        runner()
                .withListener(new ExecutionTimeListener())
                .build()
                .start(scenario);
    }

    @TestStep(id = "SLEEP_1")
    public void testStep1() throws InterruptedException {
        Thread.sleep(200l);
    }

    @TestStep(id = "SLEEP_2")
    public void testStep2() throws InterruptedException {
        Thread.sleep(400l);
    }

    class ExecutionTimeListener extends ExtendedScenarioListener {

        @Override
        public void onTestStepFinished(TestStepInvocation invocation, TestStepResult result) {
            final long testStepStartTime = result.getTestStepStartTime();
            assertThat(testStepStartTime).isGreaterThan(0L);
            final long testStepEndTime = result.getTestStepEndTime();
            assertThat(result.getTestStepExecutionTime()).isEqualTo(testStepEndTime - testStepStartTime);
        }
    }
}
