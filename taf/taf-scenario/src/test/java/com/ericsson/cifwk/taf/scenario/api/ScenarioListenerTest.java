package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import org.junit.Before;
import org.junit.Test;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ScenarioListenerTest {

    TestScenarioRunner runner;

    AtomicInteger checksum;
    AtomicInteger total;
    Stack<String> stack;

    @Before
    public void setUp() throws Exception {
        this.runner = runner().withListener(new CustomListener()).build();
        this.checksum = new AtomicInteger();
        this.total = new AtomicInteger();
        this.stack = new Stack<>();
    }

    @Test
    public void shouldListenSequential() {
        TestScenario scenario = buildScenario();

        runner.start(scenario);

        assertThat(checksum.get(), equalTo(0));
        assertThat(total.get(), equalTo(1072));
    }

    @Test
    public void shouldListenMultipleListeners() {
        TestScenario scenario = buildScenario();
        runner = runner()
                .withListener(new CustomListener())
                .withListener(new CustomListener())
                .build();

        runner.start(scenario);

        assertThat(checksum.get(), equalTo(0));
        assertThat(total.get(), equalTo(1072 * 2));
    }

    @Test
    public void shouldListenMultipleListenersWithPriority() {
        TestScenario scenario = scenario()
                .addFlow(flow("1")
                        .addTestStep(runnable(new CustomTestStep()))).build();
        runner = runner()
                .withListener(new StackListener("two"), 2)
                .withListener(new StackListener("three"), 3)
                .withListener(new StackListener("one"), 1)
                .build();

        runner.start(scenario);

        assertThat(stack, contains(
                "three onScenarioStarted", "two onScenarioStarted", "one onScenarioStarted",
                "three onFlowStarted", "two onFlowStarted", "one onFlowStarted",
                "three onTestStepStarted", "two onTestStepStarted", "one onTestStepStarted",
                "three onTestStepFinished", "two onTestStepFinished", "one onTestStepFinished",
                "three onFlowFinished", "two onFlowFinished", "one onFlowFinished",
                "three onScenarioFinished", "two onScenarioFinished", "one onScenarioFinished"
        ));
    }

    private TestScenario buildScenario() {
        CustomTestStep testStep = new CustomTestStep();

        return scenario()
                .withDefaultVusers(2)
                .addFlow(
                        flow("1")
                                .addTestStep(runnable(testStep))
                                .addTestStep(runnable(testStep))
                                .build())
                .addFlow(
                        flow("2")
                                .addTestStep(runnable(testStep))
                                .addTestStep(runnable(testStep))
                                .build()
                )
                .addFlow(
                        flow("3")
                                .addTestStep(runnable(testStep))
                                .addTestStep(runnable(testStep))
                                .build())
                .build();
    }

    public static class CustomTestStep implements Runnable {

        @Override
        public void run() {
        }

    }

    public class CustomListener implements ScenarioListener {

        @Override
        public void onScenarioStarted(TestScenario scenario) {
           checksum.addAndGet(1000);
           total.addAndGet(1000);
        }

        @Override
        public void onScenarioFinished(TestScenario scenario) {
            checksum.addAndGet(-1000);
        }

        @Override
        public void onFlowStarted(TestStepFlow flow) {
            checksum.addAndGet(10);
            total.addAndGet(10);
        }

        @Override
        public void onFlowFinished(TestStepFlow flow) {
            checksum.addAndGet(-10);
        }

        @Override
        public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
            checksum.addAndGet(1);
            total.addAndGet(1);
        }

        @Override
        public void onTestStepFinished(TestStepInvocation invocation) {
            checksum.addAndGet(-1);
        }
    }

    public class StackListener implements ScenarioListener {
        final String name;

        public StackListener(String name) {
            this.name = name;
        }

        @Override
        public void onScenarioStarted(TestScenario scenario) {
            stack.add(name + " onScenarioStarted");
        }

        @Override
        public void onScenarioFinished(TestScenario scenario) {
            stack.add(name + " onScenarioFinished");
        }

        @Override
        public void onFlowStarted(TestStepFlow flow) {
            stack.add(name + " onFlowStarted");
        }

        @Override
        public void onFlowFinished(TestStepFlow flow) {
            stack.add(name + " onFlowFinished");
        }

        @Override
        public void onTestStepStarted(TestStepInvocation invocation, Object[] args) {
            stack.add(name + " onTestStepStarted");
        }

        @Override
        public void onTestStepFinished(TestStepInvocation invocation) {
            stack.add(name + " onTestStepFinished");
        }
    }

}
