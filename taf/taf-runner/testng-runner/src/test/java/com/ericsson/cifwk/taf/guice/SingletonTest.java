package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.TestNgMock;
import com.ericsson.cifwk.taf.annotations.Eager;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import com.google.inject.Inject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Provider;
import javax.inject.Singleton;

import static com.ericsson.cifwk.taf.TestNgMock.runTestNg;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class SingletonTest {

    @org.junit.Test
    public void shouldCheckSingletonBehavior() {
        TestNgMock.FailureListener failureListener = new TestNgMock.FailureListener();

        runTestNg(SingletonTestNgTest.class, new TestNGAnnotationTransformer(), failureListener);

        assertFalse(failureListener.isFailed());
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class BaseTest {
    }

    @Test(enabled = false, groups = {"mock"})
    public static class SingletonTestNgTest extends BaseTest {

        @Inject
        Provider<EagerSingleton> eager;

        @Inject
        Provider<LazySingleton> lazy;

        @Test
        public void simpleCase() {
            assertFalse(LazySingleton.ready);
            assertTrue(EagerSingleton.ready);

            EagerSingleton eagerSingleton = eager.get();
            EagerSingleton eagerSingleton2 = eager.get();
            assertTrue(eagerSingleton == eagerSingleton2);

            LazySingleton lazySingleton = lazy.get();
            LazySingleton lazySingleton2 = lazy.get();
            assertTrue(lazySingleton == lazySingleton2);

            assertTrue(LazySingleton.ready);
            assertTrue(EagerSingleton.ready);
        }

    }

    @Operator
    @Singleton
    @Eager
    public static class EagerSingleton {
        public static boolean ready = false;
        public EagerSingleton() {
            ready = true;
        }
    }

    @Operator
    @Singleton
    public static class LazySingleton {
        public static boolean ready = false;
        public LazySingleton() {
            ready = true;
        }
    }

}
