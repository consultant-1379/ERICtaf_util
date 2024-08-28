package com.ericsson.de.scenariorx.testware;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

import static com.ericsson.de.scenariorx.api.RxExceptionHandler.Outcome.CONTINUE_FLOW;
import static com.ericsson.de.scenariorx.api.RxExceptionHandler.Outcome.PROPAGATE_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.junit.Test;

/**
 * Ported from com.ericsson.cifwk.taf.scenario.impl.ExceptionHandlerTest
 */
@SuppressWarnings("unchecked")
public class PortedExceptionHandlerTest extends ExceptionHandlerTest {

    @Test
    public void shouldAllowMultipleExceptionHandlers() throws Exception {
        ExceptionAccumulator handlerPropagate = new ExceptionAccumulator(PROPAGATE_EXCEPTION);
        ExceptionAccumulator handlerIgnore = new ExceptionAccumulator(CONTINUE_FLOW);

        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(RxApi.flow("flow")
                        .addTestStep(pushToStack("a"))
                        .addTestStep(new ThrowExceptionNow("exception"))
                        .addTestStep(pushToStack("b"))
                        .withExceptionHandler(
                                TafRxScenarios.compositeExceptionHandler()
                                        .addExceptionHandler(handlerPropagate)
                                        .setFinalExceptionHandler(handlerIgnore)
                                        .build()
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly("a", "b");
        assertThat(handlerPropagate.getExceptions()).containsExactly(VeryExpectedException.class);
        assertThat(handlerIgnore.getExceptions()).containsExactly(VeryExpectedException.class);
    }
}
