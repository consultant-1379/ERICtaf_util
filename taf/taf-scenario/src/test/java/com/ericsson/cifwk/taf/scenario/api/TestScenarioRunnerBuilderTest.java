package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.compositeExceptionHandler;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by ethomev on 7/7/15.
 */
public class TestScenarioRunnerBuilderTest {

    private static AtomicInteger count = new AtomicInteger(10);

    @Test(expected = NumberFormatException.class)
    public void shouldThrowAnExceptionWhenNoFinalHandlerSpecifiedTest(){
        TestScenarioRunner runner = runner()
                .withDefaultExceptionHandler(compositeExceptionHandler()
                        .addExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                        .build()
                )
                .build();
        runner.start(buildScenario());
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowAnExceptionWhenFinalHandlerSpecifiedTest(){
        TestScenarioRunner runner = runner()
                .withDefaultExceptionHandler(compositeExceptionHandler()
                        .setFinalExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                        .build()
                )
                .build();
        runner.start(buildScenario());
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowAnExceptionByDefaultTest(){
        TestScenarioRunner runner = runner().build();
        runner.start(buildScenario());
    }

    @Test
    public void shouldLogAllExceptionHandlersTest(){
        try {
            TestScenarioRunner runner = runner()
                    .withDefaultExceptionHandler(compositeExceptionHandler()
                            .addExceptionHandler(new MyExceptionHandler())
                            .setFinalExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                            .build()
                    )
                    .build();
            runner.start(buildScenario());
            fail();
        } catch (NumberFormatException e) {
            //expected
        }

        assertEquals(20, count.get());
    }

    private TestScenario buildScenario(){
        return scenario()
                .addFlow(flow("Flow1")
                        .addTestStep(annotatedMethod(this, "step1"))
                        .build())
                .build();
    }

    @TestStep(id="step1")
    public void step1(){
        throw new NumberFormatException();
    }

    private class MyExceptionHandler implements ScenarioExceptionHandler {
        @Override
        public Outcome onException(Throwable e) {
            count.getAndAdd(10);
            return Outcome.CONTINUE_FLOW;
        }
    }
}
