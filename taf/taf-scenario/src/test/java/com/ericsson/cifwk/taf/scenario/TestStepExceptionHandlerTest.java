package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.compositeExceptionHandler;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestStepExceptionHandlerTest {

    private static AtomicInteger counter = new AtomicInteger(10);
    private static AtomicInteger exceptionCounter = new AtomicInteger(10);

    private TestScenario buildScenario(ScenarioExceptionHandler handler) {
        CustomTestStep testStep = new CustomTestStep();

        return scenario()
                .withDefaultVusers(2)
                .addFlow(flow("1")
                        .addTestStep(runnable(testStep))
                        .addTestStep(runnable(testStep)).build())
                .addFlow(flow("2")
                        .addTestStep(runnable(testStep))
                        .addTestStep(runnable(testStep)).build())
                .addFlow(flow("3")
                        .addTestStep(runnable(testStep))
                        .addTestStep(runnable(testStep)).build())
                .withExceptionHandler(handler)
                .build();
    }

    private TestScenario buildScenario() {
        CustomTestStep testStep = new CustomTestStep();

        return scenario()
                .withDefaultVusers(1)
                .addFlow(flow("3")
                        .addTestStep(runnable(testStep)).build())
                .withExceptionHandler(compositeExceptionHandler()
                        .addExceptionHandler(new MyTestStepExceptionHandler())
                        .setFinalExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                        .build()
                )
                .build();
    }

    public static class CustomTestStep implements Runnable {

        @Override
        public void run() {
            counter.getAndAdd(10);
            fail("Failing");
        }
    }

    @Test
    public void test_shouldStopAfterFirstStepButRunAllFlows() {
        counter.set(0);
        try {
            runner().withExceptionHandler(ExceptionHandler.IGNORE).build()
                    .start(buildScenario(ScenarioExceptionHandler.PROPAGATE));
        } catch (AssertionError ignored) {
        }
        assertThat(counter.get(), equalTo(60));
    }

    @Test
    public void test_shouldStopAfterFirstStepFromFirstFlow() {
        counter.set(0);
        try {
            runner().build()
                    .start(buildScenario(ScenarioExceptionHandler.PROPAGATE));
        } catch (AssertionError ignored) {
        }
        assertThat(counter.get(), equalTo(20));
    }

    @Test
    public void test_shouldRunAllSteps() {
        counter.set(0);

        runner().withDefaultExceptionHandler(ScenarioExceptionHandler.IGNORE).build()
                .start(buildScenario(ScenarioExceptionHandler.IGNORE));
        assertThat(counter.get(), equalTo(120));
    }

    @Test(expected = Throwable.class)
    public void test_shouldOnlyPropagateFinalExceptionHandler() {
        runner().build().start(buildScenario());
    }

    @AfterClass
    public static void verifyLoggedExceptionCount() {
        assertThat(exceptionCounter.get(), equalTo(30));
    }

    private class MyTestStepExceptionHandler implements ScenarioExceptionHandler {
        @Override
        public Outcome onException(Throwable e) {
            exceptionCounter.getAndAdd(10);
            return Outcome.CONTINUE_FLOW;
        }
    }
}
