/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;

import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

public class SharedFlows {

    private static final String FLOW1_EXCEPTION_ON_EVEN_CALL = "FLOW1_EXCEPTION_ON_EVEN_CALL";
    private static final String FLOW2_EXCEPTION_ON_EVEN_CALL = "FLOW2_EXCEPTION_ON_EVEN_CALL";

    private boolean thrown1 = false;
    private boolean thrown2 = false;

    TestStepFlowBuilder sharedFlow1() {
        return flow("shared1")
                .pause(1, TimeUnit.SECONDS)
                .addTestStep(annotatedMethod(this, FLOW1_EXCEPTION_ON_EVEN_CALL))
                .withIterationsPerVuser(10);
    }

    TestStepFlowBuilder sharedFlow2() {
        return flow("shared2")
                .pause(1, TimeUnit.SECONDS)
                .addTestStep(annotatedMethod(this, FLOW2_EXCEPTION_ON_EVEN_CALL))
                .withIterationsPerVuser(5);
    }

    @TestStep(id = FLOW1_EXCEPTION_ON_EVEN_CALL)
    public void testStep1() {
        thrown1 = !thrown1;
        if (thrown1) {
            throw new IllegalArgumentException();
        }
    }

    @TestStep(id = FLOW2_EXCEPTION_ON_EVEN_CALL)
    public void testStep2() {
        thrown2 = !thrown2;
        if (thrown2) {
            throw new IllegalArgumentException();
        }
    }
}
