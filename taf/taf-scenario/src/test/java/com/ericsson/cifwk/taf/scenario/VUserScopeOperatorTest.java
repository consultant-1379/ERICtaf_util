package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 30/11/2015
 */
public class VUserScopeOperatorTest {

    @org.junit.Test
    public void shouldHaveOwnInstanceForVUser() throws Exception {
        MiniTestNG.runTest(OperatorVUserScopeTest.class);
    }

    @SuppressWarnings("Duplicates")
    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class OperatorVUserScopeTest {

        @Inject
        Provider<VUserScopedStatefulOperator> vUserScopedProvider;

        @Inject
        VUserScopedStatefulOperator vUserScopedOperator;

        Set<Object> providerOperatorIdentityHashes = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
        Set<Object> injectedOperatorIdentityHashes = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

        @org.testng.annotations.Test
        public void shouldTestScope1() {
            testStep1(); //first instance of operator
            TestScenario build = scenario("sdf")
                    .addFlow(flow("flow")
                            .addTestStep(annotatedMethod(this, "write"))
                            .addTestStep(annotatedMethod(this, "write"))
                            .withVusers(2)).build();
            runner().build().start(build);

            assertThat(VUserScopedStatefulOperator.counter.get(), is(3));

            assertThat("There should be 3 separate instances of operators from Registry - [root, vUser 1, vUser 2]", providerOperatorIdentityHashes.size(), is(3));
            assertThat("There should be only one instance of Injected operator", injectedOperatorIdentityHashes.size(), is(1));
        }

        @org.testng.annotations.Test(dependsOnMethods = {"shouldTestScope1"})
        public void shouldTestScope2() {
            testStep1();
            TestScenario build = scenario("sdf").addFlow(flow("flow")
                    .addTestStep(annotatedMethod(this, "write"))
                    .addTestStep(annotatedMethod(this, "write"))
                    .withVusers(2)).build();
            runner().build().start(build);

            assertThat(VUserScopedStatefulOperator.counter.get(), is(5));

            assertThat("There should be 5 separate instances of operators from Registry - [root, vUser 1, vUser 2, vUser1 (scenario 2), vUser 2 (Scenario 2)]",
                    providerOperatorIdentityHashes.size(), is(5));
            assertThat("There should be only one instance of Injected operator", injectedOperatorIdentityHashes.size(), is(1));
        }

        @TestStep(id = "write")
        public void testStep1() {
            providerOperatorIdentityHashes.add(System.identityHashCode(vUserScopedProvider.get()));
            injectedOperatorIdentityHashes.add(System.identityHashCode(vUserScopedOperator));
        }

        @Operator
        @VUserScoped
        public static class VUserScopedStatefulOperator {

            public static final AtomicInteger counter = new AtomicInteger();

            public VUserScopedStatefulOperator() {
                counter.incrementAndGet();
            }
        }
    }
}
