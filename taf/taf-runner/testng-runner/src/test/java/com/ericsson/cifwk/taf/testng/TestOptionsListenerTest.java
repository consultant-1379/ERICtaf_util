package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.TestNgMock;
import com.ericsson.cifwk.taf.annotations.TestOptions;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;

import static com.google.common.truth.Truth.assertThat;
import static org.testng.Assert.fail;


public class TestOptionsListenerTest {
    private static final int TIMEOUT = 1000;
    private static final String TIMEOUT_PROPERTY = "timeout";

    @org.junit.Test
    public void runTestTimeoutTestNg() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        configuration.setProperty(TIMEOUT_PROPERTY, TIMEOUT);
        TestNgMock.FailureListener failureListener = new TestNgMock.FailureListener();

        try {
            TestNgMock.runTestNg(TestTimeoutTestNg.class,
                    new TestNGAnnotationTransformer(),
                    failureListener);

            assertThat(failureListener.isFailed()).isTrue();
            assertThat(failureListener.failureCause).isInstanceOf(org.testng.internal.thread.ThreadTimeoutException.class);
        } finally {
            configuration.clearProperty(TIMEOUT_PROPERTY);
        }
    }

    public static class TestTimeoutTestNg {
        @org.testng.annotations.Test
        @TestOptions(timeout = "${configuration['timeout']}")
        public void testTimeout() throws Exception {
            Thread.sleep(TIMEOUT * 3); // NOSONAR
            fail("Test was not interrupted by timeout! (It should be)");
        }
    }
}