package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.assertions.SaveAsserts;
import org.junit.Before;
import org.junit.Test;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         23/12/2015
 */
public class SaveAssertsListenerTest {

    private SaveAssertsListener saveAssertsListener;
    private IInvokedMethod testMethod;
    private ITestResult testResult;

    @Before
    public void setUp() throws Exception {
        saveAssertsListener = new SaveAssertsListener();

        testMethod = mock(IInvokedMethod.class);
        when(testMethod.isTestMethod()).thenReturn(true);

        testResult = new TestResult();
        testResult.setStatus(ITestResult.SUCCESS);
    }

    @Test
    public void shouldFailTestResultWhenAssertErrorExists() throws Exception {
        SaveAsserts.saveAssertNotNull(null);

        saveAssertsListener.afterInvocation(testMethod, testResult);

        assertThat(testResult.getStatus(), is(ITestResult.FAILURE));
        assertThat(testResult.getThrowable(), instanceOf(AssertionError.class));
    }

    @Test
    public void shouldPassTestResultWhenNoErrors() throws Exception {
        SaveAsserts.saveAssertNotNull(new Object());

        saveAssertsListener.afterInvocation(testMethod, testResult);

        assertThat(testResult.getStatus(), is(ITestResult.SUCCESS));
        assertThat(testResult.getThrowable(), nullValue());
    }

    @Test
    public void shouldSkipConfigurationMethod() throws Exception {
        when(testMethod.isTestMethod()).thenReturn(false);

        saveAssertsListener.afterInvocation(testMethod, testResult);

        assertThat(testResult.getStatus(), is(ITestResult.SUCCESS));
        assertThat(testResult.getThrowable(), nullValue());
    }
}