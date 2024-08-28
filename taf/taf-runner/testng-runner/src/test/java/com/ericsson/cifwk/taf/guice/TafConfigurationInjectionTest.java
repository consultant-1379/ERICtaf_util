package com.ericsson.cifwk.taf.guice;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TestNgMock;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import com.google.inject.Inject;
import org.testng.annotations.Test;

import static com.ericsson.cifwk.taf.TestNgMock.runTestNg;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;

public class TafConfigurationInjectionTest {

    @org.junit.Test
    public void shouldInjectTafConfiguration() {
        TestNgMock.FailureListener failureListener = new TestNgMock.FailureListener();

        runTestNg(TafConfigurationAwareTest.class, new TestNGAnnotationTransformer(), failureListener);

        assertFalse(failureListener.isFailed());
    }

    @Test(enabled = false, groups = {"mock"})
    public static class TafConfigurationAwareTest extends TafTestBase {

        private static final String MY_BUZZ_PROPERTY = "my.buzz";

        @Inject
        private TafConfiguration configuration;

        @Test
        public void testInjection() {
            assertThat(configuration.getString(MY_BUZZ_PROPERTY), equalTo("bar buzz"));
        }

    }

}
