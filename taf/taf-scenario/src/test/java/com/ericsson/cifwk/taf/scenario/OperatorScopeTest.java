package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.ServiceRegistry;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

public class OperatorScopeTest {
    private static final String TEST_STEP_1 = "1";
    private static final String TEST_STEP_2 = "2";

    @org.junit.Test
    public void testTestNgContextTest() throws Exception {
        MiniTestNG.runTest(OperatorScopeProviderTest.class);
    }

    public static class OperatorScopeProviderTest extends AbstractOperatorScopeTest {

        @Inject
        Provider<StatefulOperator> provider;

        @Override
        public StatefulOperator getOperator() {
            return provider.get();
        }

    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static abstract class AbstractOperatorScopeTest {
        public abstract StatefulOperator getOperator();

        @org.testng.annotations.Test
        public void shouldTestScope() {

            // uncomment to break tests
//        getOperator();

            TestScenario build = scenario()
                    .withDefaultVusers(30)
                    .addFlow(flow("hello1").addTestStep(annotatedMethod(this, TEST_STEP_1)))
                    .addFlow(flow("hello2").addTestStep(annotatedMethod(this, TEST_STEP_2)))
                    .build();

            runner().build().start(build);
        }

        @TestStep(id = TEST_STEP_1)
        public void first() {
            StatefulOperator operator1 = getOperator();
            if (ServiceRegistry.getTestContextProvider().get().getVUser() > 1) {
                assertThat(operator1.getState(), nullValue());
            }

            randomSleep();
            assertThat(operator1.getState(), nullValue());
            operator1.setState("" + operator1.hashCode());
            operator1.setInternalState("" + operator1.hashCode());
            randomSleep();
        }

        @TestStep(id = TEST_STEP_2)
        public void second() {
            randomSleep();
            StatefulOperator operator = getOperator();
            assertThat(operator.getState(), equalTo("" + operator.hashCode()));
            assertThat(operator.getInternalState(), equalTo("" + operator.hashCode()));
            randomSleep();
        }

        private void randomSleep() {
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private String getVUser() {
            return "" + ServiceRegistry.getTestContextProvider().get().getVUser();
        }

        @Operator
        @VUserScoped
        public static class StatefulOperator {
            String state;

            @Inject
            InternalStatefulOperator internalOperator;

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }

            public String getInternalState() {
                return internalOperator.getState();
            }

            public void setInternalState(String state) {
                internalOperator.setState(state);
            }
        }

        @Operator
        @VUserScoped
        public static class InternalStatefulOperator {
            String state;

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }
        }
    }

}
