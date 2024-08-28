package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.taf.scenario.TestScenarios;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by ethomev on 7/6/15.
 */
public class CompositeExceptionHandlerTest {

    @Test
    public void constructCompositeExceptionHandlerWithMultipleExceptionHandlersTest(){
        CompositeExceptionHandler handler = TestScenarios.compositeExceptionHandler()
                .addExceptionHandler(new MyFirstExceptionHandler())
                .addExceptionHandler(new MySecondExceptionHandler())
                .build();
        assertEquals(2, handler.handlers.size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldPropagateFinalHandlerExceptionTest(){
        CompositeExceptionHandler handler = TestScenarios.compositeExceptionHandler()
                .setFinalExceptionHandler(new MyFirstExceptionHandler())
                .addExceptionHandler(new MySecondExceptionHandler())
                .build();
        handler.onException(new Throwable());
    }

    private class MyFirstExceptionHandler implements ScenarioExceptionHandler{

        @Override
        public Outcome onException(Throwable e) {
            throw new IllegalStateException();
        }
    }

    private class MySecondExceptionHandler implements ScenarioExceptionHandler{

        @Override
        public Outcome onException(Throwable e) {
            throw new NumberFormatException();
        }
    }
}