package com.ericsson.cifwk.taf.performance.metric;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseStartedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestContextTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestMethodExecutionResult testResult;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestExecutionContext testContext;

    // Mocking event class fails silently - looks like Mockito issue (upgrade to 1.10.19 didn't help)
    // @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    @InjectMocks
    private TestCaseStartedEvent event;

    @Before
    public void setUp() throws NoSuchMethodException, SecurityException {
        when(testContext.getTestGroup().getId()).thenReturn("suite1");
        when(testResult.getTestMethod().getName()).thenReturn("test1");
        when(testResult.getTestMethod().getJavaMethod())
                .thenReturn(this.getClass().getMethod("defaultTest", (Class<?>[]) null));
        TestContext.set(event);
    }

    @Test
    public void current() {
        assertEquals("suite1", TestContext.getCurrentSuiteName());
        assertEquals("test1", TestContext.getCurrentTestName());
        assertTrue(TestContext.getTestStartedMillis() > 0);
    }

    @Test
    public void shouldReadTestId() throws NoSuchMethodException, SecurityException {
        when(testResult.getTestMethod().getJavaMethod())
                .thenReturn(this.getClass().getMethod("testMethod", (Class<?>[]) null));
        TestContext.set(event);
        assertEquals("testId testTitle", TestContext.getCurrentTestName());
    }

    @Test
    public void reset() {
        TestContext.reset();
        assertEquals(null, TestContext.getCurrentSuiteName());
        assertEquals(null, TestContext.getCurrentTestName());
        assertEquals(null, TestContext.getTestStartedMillis());
    }

    @TestId(id = "testId", title = "testTitle")
    public void testMethod() {

    }

    public void defaultTest() {

    }

}
