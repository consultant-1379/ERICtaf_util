package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import com.google.inject.Singleton;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

/**
 *
 */
public class MoreOperatorScopeTest {

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

        @Inject
        StatefulOperator operator;

        @Inject
        Provider<VUserScopedStatefulOperator> vUserScopedProvider;

        @Inject
        VUserScopedStatefulOperator vUserScopedOperator;

        @Inject
        Provider<SingletonStatefulOperator> singletonProvider;

        @Inject
        SingletonStatefulOperator singletonStatefulOperator;

        Map<String, StringBuilder> table = new LinkedHashMap<String, StringBuilder>() {
            @Override
            public StringBuilder get(Object key) {
                if (!super.containsKey(key)) {
                    super.put(key.toString(), new StringBuilder());
                }

                return super.get(key);
            }
        };

        @org.testng.annotations.Test
        public void shouldTestScope1() {
            testStep1("T1 Body");
            TestScenario build = scenario("sdf")
                    .addFlow(flow("flow")
                            .addTestStep(annotatedMethod(this, "write").withParameter("caller", "S1 TS1"))
                            .addTestStep(annotatedMethod(this, "write").withParameter("caller", "S1 TS2"))
                            .withVusers(2)).build();
            runner().build().start(build);
        }

        @org.testng.annotations.Test(dependsOnMethods = {"shouldTestScope1"})
        public void shouldTestScope2() {
            testStep1("T2 Body");
            TestScenario build = scenario("sdf").addFlow(flow("flow")
                    .addTestStep(annotatedMethod(this, "write").withParameter("caller", "S2 TS1"))
                    .addTestStep(annotatedMethod(this, "write").withParameter("caller", "S3 TS2"))
                    .withVusers(2)).build();
            runner().build().start(build);

            for (Map.Entry<String, StringBuilder> entry : table.entrySet()) {
                System.out.println(entry.getKey() + entry.getValue());
            }
        }

        @TestStep(id = "write")
        public void testStep1(@Input("caller") String caller) {
            table.get("| Operator     |                | ").append(caller).append(" VU").append(TafTestContext.getContext().getVUser()).append("|");
            table.get("|              | provider.get() |").append(System.identityHashCode(provider.get())).append("|");
            table.get("|              | @Inject        |").append(System.identityHashCode(operator)).append("|");
            table.get("| @vUserScoped | provider.get() |").append(System.identityHashCode(vUserScopedProvider.get())).append("|");
            table.get("| @vUserScoped | @Inject        |").append(System.identityHashCode(vUserScopedOperator)).append("|");
            table.get("| @Singleton   | provider.get() |").append(System.identityHashCode(singletonProvider.get())).append("|");
            table.get("| @Singleton   | @Inject        |").append(System.identityHashCode(singletonStatefulOperator)).append("|");
        }

        @Operator
        public static class StatefulOperator {
        }

        @Operator
        @VUserScoped
        public static class VUserScopedStatefulOperator {
        }

        @Operator
        @Singleton
        public static class SingletonStatefulOperator {
        }
    }

}
