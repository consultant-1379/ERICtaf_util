package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Random;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 *
 */
public class OperatorScopeDynamicTest  {

    private static final String TEST_STEP_1 = "1";
    
    private static final String TEST_STEP_2 = "2";

    @org.junit.Test
    public void testTestNgContextTest() throws Exception {
        MiniTestNG.runTest(TestNgOperatorScopeDynamicTest.class);
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class TestNgOperatorScopeDynamicTest {
        @Inject
        Provider<StatefulOperator> provider;

        @org.testng.annotations.Test
        public void shouldTestScope() {
            TestScenarioRunner runner = runner().build();
            TestScenario build = scenario()
                    .withDefaultVusers(30)
                    .addFlow(flow("hello1").addTestStep(annotatedMethod(provider, TEST_STEP_1)))
                    .addFlow(flow("hello2").addTestStep(annotatedMethod(provider, TEST_STEP_2)))
                    .build();
            runner.start(build);
        }

        @Operator
        @VUserScoped
        public static class StatefulOperator {

            private String state;

            @TestStep(id = TEST_STEP_1)
            public void first() {
                randomSleep();
                assertThat(state, nullValue());
                this.state = "" + hashCode();
                randomSleep();
            }

            @TestStep(id = TEST_STEP_2)
            public void second() {
                randomSleep();
                assertThat(state, equalTo("" + hashCode()));
                randomSleep();
            }

            private void randomSleep() {
                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
