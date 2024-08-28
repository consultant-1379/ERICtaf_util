package com.ericsson.cifwk.taf.scenario;
/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.api.AbstractScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioTest.VerySpecialException;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static junit.framework.TestCase.fail;

public class ScenarioListenerTest {
    @Test
    public void shouldThrowListenerExceptions() {
        for (ThrowException throwException : ThrowException.values()) {
            try {
                TestStepFlow flow = flow("exceptionHandlerPropogate")
                        .addTestStep(annotatedMethod(this, "emptyTestStep"))
                        .build();

                TestScenarioRunner runner = runner()
                        .withListener(new ThrowingScenarioListener(throwException))
                        .withDefaultExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                        .build();

                TestScenario scenario = scenario()
                        .withExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                        .addFlow(flow)
                        .build();

                runner.start(scenario);

                fail("Exception not thrown in " + throwException);
            } catch (VerySpecialException e) {
                //expected
            } catch (Throwable e) {
                fail("Other exception in " + throwException + " " + e);
            }
        }
    }

    @TestStep(id = "emptyTestStep")
    public void doNothing() {
        // do nothing
    }

    enum ThrowException {
        ON_SCENARIO_STARTED, ON_SCENARIO_FINISHED, ON_FLOW_STARTED,
        ON_FLOW_FINISHED, ON_TEST_STEP_STARTED, ON_TEST_STEP_FINISHED
    }

    private static class ThrowingScenarioListener extends AbstractScenarioListener {
        private ThrowException methodToThrowException;

        public ThrowingScenarioListener(ThrowException methodToThrowException) {
            this.methodToThrowException = methodToThrowException;
        }

        @Override
        public void onScenarioStarted(TestScenario testScenario) {
            throwExceptionIfApplicable(ThrowException.ON_SCENARIO_STARTED);
        }

        @Override
        public void onScenarioFinished(TestScenario testScenario) {
            throwExceptionIfApplicable(ThrowException.ON_SCENARIO_FINISHED);
        }

        @Override
        public void onFlowStarted(TestStepFlow testStepFlow) {
            throwExceptionIfApplicable(ThrowException.ON_FLOW_STARTED);
        }

        @Override
        public void onFlowFinished(TestStepFlow testStepFlow) {
            throwExceptionIfApplicable(ThrowException.ON_FLOW_FINISHED);
        }

        @Override
        public void onTestStepStarted(TestStepInvocation testStepInvocation, Object[] objects) {
            throwExceptionIfApplicable(ThrowException.ON_TEST_STEP_STARTED);
        }

        @Override
        public void onTestStepFinished(TestStepInvocation testStepInvocation) {
            throwExceptionIfApplicable(ThrowException.ON_TEST_STEP_FINISHED);
        }

        private void throwExceptionIfApplicable(ThrowException currentMethod) {
            if (methodToThrowException == currentMethod) {
                throw new VerySpecialException();
            }
        }
    }

}
