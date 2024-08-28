package com.ericsson.cifwk.taf.scenario;/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

public class TestStepInterceptorTest {

    @Test
    public void runTestNg() throws Exception {
        MiniTestNG.runTest(TestNgTest.class);
    }

    public static class TestNgTest {
        @org.testng.annotations.Test
        public void shouldNotBeExecuted() {

            TafTestContext.getContext().addDataSource("nodes", TafDataSources.fromCsv("node.csv"));

            TestScenario scenario = scenario("intercept")
                    .split(
                            flow("flow")
                                    .addTestStep(annotatedMethod(this, "BADDIE")
                                            .withParameter("name", "value"))
                                    .addTestStep(annotatedMethod(this, "BADDIE2"))
                                    .withDataSources(dataSource("nodes"))

                    )
                    .build();

            runner().build().start(scenario);
        }

        @TestStep(id = "BADDIE")
        public void testStep1(@Input("name") String name) {
            throw new IllegalStateException("This should not be run!");
        }

        @TestStep(id = "BADDIE2")
        public Object testStep2() {
            throw new IllegalStateException("This also should not be run!");
        }
    }


}
