package com.ericsson.cifwk.taf.scenario.impl;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import com.ericsson.cifwk.taf.scenario.MiniTestNG;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import org.junit.Test;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Stack;

import static com.ericsson.cifwk.taf.scenario.CustomMatchers.allElementsAreDifferent;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ScenarioSplitOperatorTest {
    private static final String PUSH_OPERATOR_1 = "pushOperator1";
    private static final String PUSH_OPERATOR_2 = "pushOperator2";

    @Test
    public void testTestNgContextTest() throws Exception {
        MiniTestNG.runTest(TestNgDataSourceTest.class);
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class TestNgDataSourceTest {
        final Stack<String> operatorStack = new Stack<>();
        final Stack<String> testStep1Stack = new Stack<>();
        final Stack<String> testStep2Stack = new Stack<>();

        @Inject
        private Provider<TheOperator> provider;

        @org.testng.annotations.Test
        public void shouldNotMixOperatorsInSplit() {
            TestScenario scenario = scenario("split-merge")
                    .split(
                            flow("parallel1")
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_1))
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_2)),
                            flow("parallel2")
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_1))
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_2)),
                            flow("parallel3")
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_1))
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_2))
                    )
                    .withDefaultVusers(30)
                    .build();

            runner().build().start(scenario);

            assertThat(operatorStack, allElementsAreDifferent());
            assertThat(testStep1Stack.size(), equalTo(testStep2Stack.size()));
            for (String vUserAndOperatorHashCode : testStep1Stack) {
                assertTrue(testStep2Stack.contains(vUserAndOperatorHashCode));
            }
        }

        @org.testng.annotations.Test
        public void shouldNotMixOperatorsInSplitWithSubflow() {
            TestScenario scenario = scenario("split-merge")
                    .split(
                            flow("parallel1")
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_1))
                                    .addTestStep(annotatedMethod(this, PUSH_OPERATOR_2)),
                            flow("parallel2")
                                    .addSubFlow(flow("subflow-inside-split1")
                                            .addTestStep(annotatedMethod(this, PUSH_OPERATOR_1))
                                            .addTestStep(annotatedMethod(this, PUSH_OPERATOR_2))),
                            flow("parallel3")
                                    .addSubFlow(flow("subflow-inside-split1")
                                            .addTestStep(annotatedMethod(this, PUSH_OPERATOR_1))
                                            .addTestStep(annotatedMethod(this, PUSH_OPERATOR_2)))
                    )
                    .withDefaultVusers(30)
                    .build();

            runner().build().start(scenario);

            assertThat(operatorStack, allElementsAreDifferent());
            assertThat(testStep1Stack.size(), equalTo(testStep2Stack.size()));
            for (String vUserAndOperatorHashCode : testStep1Stack) {
                assertTrue(testStep2Stack.contains(vUserAndOperatorHashCode));
            }
        }

        @TestStep(id = PUSH_OPERATOR_1)
        public void pushOperator1() {
            TestContext context = ServiceRegistry.getTestContextProvider().get();
            TheOperator operator = provider.get();
            String hashCode = Integer.toHexString(System.identityHashCode(operator));
            operatorStack.push(hashCode);
            testStep1Stack.push("vuser" + context.getVUser() + " " + hashCode);
        }

        @TestStep(id = PUSH_OPERATOR_2)
        public void pushOperator2() {
            TestContext context = ServiceRegistry.getTestContextProvider().get();
            TheOperator operator = provider.get();
            testStep2Stack.push("vuser" + context.getVUser() + " " + Integer.toHexString(System.identityHashCode(operator)));
        }

        @Operator
        @VUserScoped
        public static class TheOperator {

        }
    }

}
