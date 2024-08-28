package com.ericsson.cifwk.taf.scenario.impl;

/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import org.junit.Test;

import java.util.Stack;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.compositeExceptionHandler;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler.Outcome.CONTINUE_FLOW;
import static com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler.Outcome.PROPAGATE_EXCEPTION;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.fail;


public class ExceptionHandlerTest extends ScenarioTest {

    @Test
    public void shouldPropagateNextLevel() throws Exception {
        ExceptionAccumulator flowHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator subFlowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addSubFlow(
                                        flow("subFlow")
                                                .addTestStep(runnable(pushToStack("b")))
                                                .addTestStep(throwException())
                                                .addTestStep(runnable(pushToStack("c")))
                                                .withExceptionHandler(subFlowHandlerPropagate)
                                )
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerIgnore)
                )
                .build();

        runner().build().start(scenario);

        assertThat(stack).containsExactly("a", "b", "d").inOrder();
        assertThat(flowHandlerIgnore.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(subFlowHandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
    }

    @Test
    public void shouldPropagateThroughLevels() throws Exception {
        ExceptionAccumulator subFlowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator flowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION) {
            @Override
            public Outcome onException(Throwable e) {
                return super.onException(e);
            }
        };
        ExceptionAccumulator scenarioHandler = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(runnable(pushToStack("b")))
                                .addSubFlow(flow("subFlow")
                                        .addTestStep(throwException())
                                        .addTestStep(runnable(pushToStack("c")))
                                        .withExceptionHandler(subFlowHandlerPropagate)
                                )
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerPropagate)
                )
                .withExceptionHandler(scenarioHandler)
                .build();

        runner().build().start(scenario);

        assertThat(stack).containsExactly("a", "b").inOrder();
        assertThat(flowHandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(subFlowHandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(scenarioHandler.getExceptions()).containsExactly(VerySpecialException.class);
    }

    @Test
    public void shouldHandleByParentIfNoChildDefined() throws Exception {
        ExceptionAccumulator flowHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator scenarioHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator runnerHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addSubFlow(
                                        flow("subFlow")
                                                .addTestStep(runnable(pushToStack("b")))
                                                .addTestStep(throwException())
                                                .addTestStep(runnable(pushToStack("c")))
                                )
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerIgnore)
                )
                .withExceptionHandler(scenarioHandlerPropagate)
                .build();

        runner().withDefaultExceptionHandler(runnerHandlerIgnore).build().start(scenario);

        assertThat(stack).containsExactly("a", "b", "c", "d").inOrder();
        assertThat(flowHandlerIgnore.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(scenarioHandlerPropagate.getExceptions()).isEmpty();
        assertThat(runnerHandlerIgnore.getExceptions()).isEmpty();
    }

    @Test
    public void shouldHandleExceptionsInSplitAndVUsers() throws Exception {
        ExceptionAccumulator subflow1HandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator subflow2HandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator flowHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator scenarioHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator runnerHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .split(
                                        flow("subFlow1")
                                                .addTestStep(throwExceptionForVUser(5))
                                                .addTestStep(runnable(pushToStack("b")))
                                                .withExceptionHandler(subflow1HandlerPropagate),
                                        flow("subFlow2")
                                                .addTestStep(runnable(pushToStack("c")))
                                                .withExceptionHandler(subflow2HandlerIgnore)
                                )
                                .withVusers(2)
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerIgnore)
                )
                .withExceptionHandler(scenarioHandlerPropagate)
                .build();

        runner().withDefaultExceptionHandler(runnerHandlerIgnore).build().start(scenario);

        assertThat(stack).containsExactly("a", "c", "d", "a", "b", "c", "d");
        assertThat(subflow1HandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(subflow2HandlerIgnore.getExceptions()).isEmpty();
        assertThat(flowHandlerIgnore.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(scenarioHandlerPropagate.getExceptions()).isEmpty();
        assertThat(runnerHandlerIgnore.getExceptions()).isEmpty();
    }

    @Test
    public void shouldPropagateThroughAllLevels() throws Exception {
        ExceptionAccumulator subFlowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator flowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator scenarioHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator runnerHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(runnable(pushToStack("b")))
                                .addSubFlow(flow("subFlow")
                                        .addTestStep(throwException())
                                        .addTestStep(runnable(pushToStack("c")))
                                        .withExceptionHandler(subFlowHandlerPropagate)
                                )
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerPropagate)
                )
                .withExceptionHandler(scenarioHandlerPropagate)
                .build();

        runner().withDefaultExceptionHandler(runnerHandlerIgnore).build().start(scenario);

        assertThat(stack).containsExactly("a", "b").inOrder();
        assertThat(flowHandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(subFlowHandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(scenarioHandlerPropagate.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(runnerHandlerIgnore.getExceptions()).containsExactly(VerySpecialException.class);
    }

    @Test(expected = VerySpecialException.class)
    public void shouldPropagateThroughScenario() throws Exception {
        ExceptionAccumulator subFlowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator flowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator scenarioHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator runnerHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(runnable(pushToStack("b")))
                                .addSubFlow(flow("subFlow")
                                        .addTestStep(throwException())
                                        .addTestStep(runnable(pushToStack("c")))
                                        .withExceptionHandler(subFlowHandlerPropagate)
                                )
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerPropagate)
                )
                .withExceptionHandler(scenarioHandlerPropagate)
                .build();

        runner().withDefaultExceptionHandler(runnerHandlerPropagate).build().start(scenario);
    }

    @Test
    public void shouldHandleExceptionsInBeforeAfterFlow_flowHandler() throws Exception {
        ExceptionAccumulator flowHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .beforeFlow(throwDifferentException())
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(throwException())
                                .addTestStep(runnable(pushToStack("b")))
                                .withExceptionHandler(flowHandlerIgnore)
                                .afterFlow(throwDifferentException(),
                                        pushToStack("c"))
                )
                .build();

        runner().build().start(scenario);

        assertThat(stack).containsExactly("a", "b", "c").inOrder();

        assertThat(flowHandlerIgnore.getExceptions()).containsExactly(VerySpecialException.class, DifferentException.class, DifferentException.class);
    }

    @Test
    public void shouldHandleExceptionsInBeforeAfterFlow_scenarioHandler() throws Exception {
        ExceptionAccumulator flowHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator scenarioHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator runnerHandlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .beforeFlow(throwDifferentException())
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(throwException())
                                .addTestStep(runnable(pushToStack("b")))
                                .afterFlow(throwDifferentException(),
                                        pushToStack("c"))
                                .withExceptionHandler(flowHandlerPropagate)
                )
                .withExceptionHandler(scenarioHandlerIgnore)
                .build();

        runner().withDefaultExceptionHandler(runnerHandlerPropagate).build().start(scenario);

        assertThat(stack).containsExactly("c").inOrder();

        assertThat(runnerHandlerPropagate.getExceptions()).isEmpty();
        //only last exception handled
        assertThat(scenarioHandlerIgnore.getExceptions()).containsExactly(DifferentException.class);
        //if exception thrown in before method, flow is not started
        assertThat(flowHandlerPropagate.getExceptions()).containsExactly(DifferentException.class, DifferentException.class);
    }

    @Test
    public void shouldAllowMultipleExceptionHandlers() throws Exception {
        ExceptionAccumulator handler1 = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator handler2 = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(throwException())
                                .addTestStep(runnable(pushToStack("b")))
                )
                .withExceptionHandler(
                        compositeExceptionHandler()
                                .addExceptionHandler(handler1)
                                .setFinalExceptionHandler(handler2)
                                .build())
                .build();

        runner.start(scenario);

        assertThat(stack).containsExactly("a", "b").inOrder();
        assertThat(handler1.getExceptions()).containsExactly(VerySpecialException.class);
        assertThat(handler1.getExceptions()).containsExactly(VerySpecialException.class);
    }

    @Test
    public void shouldNotHandleExceptionThrownByHandler() throws Exception {
        ScenarioExceptionHandler subFlowHandlerThrowsException = new ScenarioExceptionHandler() {
            @Override
            public Outcome onException(Throwable e) {
                throw new DifferentException();
            }
        };
        ExceptionAccumulator flowHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator scenarioHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);
        ExceptionAccumulator runnerHandlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);

        TestScenario scenario = scenario("scenario")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .addTestStep(runnable(pushToStack("b")))
                                .addSubFlow(flow("subFlow")
                                        .addTestStep(throwException())
                                        .addTestStep(runnable(pushToStack("c")))
                                        .withExceptionHandler(subFlowHandlerThrowsException)
                                )
                                .addTestStep(runnable(pushToStack("d")))
                                .withExceptionHandler(flowHandlerIgnore)
                )
                .withExceptionHandler(scenarioHandlerIgnore)
                .build();

        try {
            runner().withDefaultExceptionHandler(runnerHandlerIgnore).build().start(scenario);
            fail("Exception expected");
        } catch (DifferentException e) {
            //expected
        }

        assertThat(stack).containsExactly("a", "b").inOrder();
        assertThat(flowHandlerIgnore.getExceptions()).isEmpty();
        assertThat(scenarioHandlerIgnore.getExceptions()).isEmpty();
        assertThat(runnerHandlerIgnore.getExceptions()).isEmpty();
    }



    protected static class ExceptionAccumulator implements ScenarioExceptionHandler {
        private final Stack<Class<? extends Throwable>> exceptions = new Stack<>();
        private final ScenarioExceptionHandler.Outcome outcome;

        public ExceptionAccumulator(Outcome outcome) {
            this.outcome = outcome;
        }

        @Override
        public ScenarioExceptionHandler.Outcome onException(Throwable e) {
            exceptions.add(e.getClass());
            return outcome;
        }

        public Stack<Class<? extends Throwable>> getExceptions() {
            return exceptions;
        }
    }

    protected static class DifferentException extends RuntimeException {
    }

    public Runnable throwDifferentException() {
        return new Runnable() {
            @Override
            public void run() {
                throw new DifferentException();
            }
        };
    }
}
