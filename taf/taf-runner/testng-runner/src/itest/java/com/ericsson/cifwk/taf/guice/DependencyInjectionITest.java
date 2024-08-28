package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.testng.MiniTestNG;
import org.junit.Test;
import org.testng.annotations.Guice;

import static org.junit.Assert.assertNotNull;

public class DependencyInjectionITest {

    @Test
    public void testGuiceInjectorIsAvailable() throws Exception {
        MiniTestNG.runTestNg(TestInjection.class);
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class TestInjection {

        @org.testng.annotations.Test
        public void test() {
            assertNotNull(GuiceBeanManager.injector);
        }

    }
}
