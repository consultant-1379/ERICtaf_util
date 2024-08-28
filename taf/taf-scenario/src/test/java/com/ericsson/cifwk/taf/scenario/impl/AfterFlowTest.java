/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;

import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;

import static org.junit.Assert.assertEquals;

public class AfterFlowTest {

    private static int afterFlow1 = 0;
    private static int afterFlow2 = 0;
    private static Logger LOGGER = LoggerFactory.getLogger(AfterFlowTest.class);

    @Test
    public void afterFlowToWorkSuccessfullyWithTestStepsPassing() {

        TestStepFlow flow1 = flow("After Flow Test 1")
                .addTestStep(runnable(testStepRunnerAfterFlow1("Test Step 1 executed")))
                .addTestStep(runnable(testStepRunnerAfterFlow1("Test Step 2 executed")))
                .afterFlow(testStepRunnerAfterFlow1("AfterFlow Part 1 is executed"))
                .build();

        TestScenario scenario = scenario().addFlow(flow1).build();
        TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    private Runnable testStepRunnerAfterFlow1(final String content) {
        return new Runnable() {
            @Override
            public void run() {
                afterFlow1 = 1;
                LOGGER.info(content);
            }
        };
    }

    @Test(expected = AssertionError.class)
    public void afterFlowToWorkSuccessfullyWithTestStepFailing() {

        TestStepFlow flow2 = flow("After Flow Test 2")
                .addTestStep(runnable(testStepRunnerAfterFlow2("Test Step 1 executed")))
                .addTestStep(runnable(testStepFailure("null")))
                .afterFlow(testStepRunnerAfterFlow2("AfterFlow Part 2 is executed")).build();

        TestScenario scenario = scenario().addFlow(flow2).build();
        TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    private Runnable testStepFailure(final String content) {
        return new Runnable() {
            @Override
            public void run() {
                assertEquals(true, content.contains("e"));
            }
        };
    }

    private Runnable testStepRunnerAfterFlow2(final String content) {
        return new Runnable() {
            @Override
            public void run() {
                afterFlow2 = 2;
                LOGGER.info(content);
            }
        };
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("In tear down method");
        assertEquals(afterFlow1, 1);
        assertEquals(afterFlow2, 2);
    }
}