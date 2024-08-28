package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
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
public class SingletonScopeOperatorTest {

    @org.junit.Test
    public void shouldCreateSingleInstance() throws Exception {
        MiniTestNG.runTest(OperatorSingletonScopeTest.class);
    }

    @SuppressWarnings("Duplicates")
    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class OperatorSingletonScopeTest {

        @Inject
        Provider<SingletonStatefulOperator> singletonScopedProvider;

        @Inject
        SingletonStatefulOperator singletonScopedOperator;

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

            assertThat(SingletonStatefulOperator.counter.get(), is(1));

            assertThat("There should be only one instance of operator", providerOperatorIdentityHashes.size(), is(1));
            assertThat("There should be only one instance of operator", injectedOperatorIdentityHashes.size(), is(1));
        }

        @org.testng.annotations.Test(dependsOnMethods = {"shouldTestScope1"})
        public void shouldTestScope2() {
            testStep1();
            TestScenario build = scenario("sdf").addFlow(flow("flow")
                    .addTestStep(annotatedMethod(this, "write"))
                    .addTestStep(annotatedMethod(this, "write"))
                    .withVusers(2)).build();
            runner().build().start(build);

            assertThat(SingletonStatefulOperator.counter.get(), is(1));

            assertThat("There should be only one instance of operator", providerOperatorIdentityHashes.size(), is(1));
            assertThat("There should be only one instance of operator", injectedOperatorIdentityHashes.size(), is(1));
        }

        @TestStep(id = "write")
        public void testStep1() {
            providerOperatorIdentityHashes.add(System.identityHashCode(singletonScopedProvider.get()));
            injectedOperatorIdentityHashes.add(System.identityHashCode(singletonScopedOperator));
        }

        @Operator
        @Singleton
        public static class SingletonStatefulOperator {

            public static final AtomicInteger counter = new AtomicInteger();

            public SingletonStatefulOperator() {
                counter.incrementAndGet();
            }
        }
    }
}
