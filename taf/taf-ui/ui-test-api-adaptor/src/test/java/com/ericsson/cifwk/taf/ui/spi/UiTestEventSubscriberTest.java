/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.ui.spi;

import com.ericsson.cifwk.taf.execution.TestExecutionEvent;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseFailedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseSucceededEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionFinishedEvent;
import com.ericsson.cifwk.taf.ui.UI;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UiTestEventSubscriberTest {

    private boolean closeWindowCalled = false;

    @Mock
    private UI ui;

    @Mock
    private TestGroup suite;

    @Mock
    private TestExecutionContext executionContext;

    @Spy
    private UiTestEventSubscriber listener;

    @Before
    public void setUp() {
        listener = new UiTestEventSubscriber();
        MockitoAnnotations.initMocks(this);
        Mockito.doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                closeWindowCalled = true;
                return null;
            }
        }).when(listener).closeAllUiWindows();
    }

    @Test
    public void closeWindowOnTestFinishByDefault() {
        assertFalse(closeWindowCalled);
        listener.onTestCaseEvent(testCaseSucceeded());
        assertTrue(closeWindowCalled);
    }

    @Test
    public void closeWindowOnExecutionFinishOnly() {
        UI.closeWindow(TestExecutionEvent.ON_EXECUTION_FINISH);
        assertFalse(closeWindowCalled);
        listener.onTestSuiteFinishEvent(suiteFinishedEvent());
        assertFalse(closeWindowCalled);
        listener.onTestCaseEvent(testCaseSucceeded());
        assertFalse(closeWindowCalled);
        listener.onTestSessionEvent(sessionFinishedEvent());
        assertTrue(closeWindowCalled);
    }

    @Test
    public void closeWindowOnSuiteFinishOnly() {
        UI.closeWindow(TestExecutionEvent.ON_SUITE_FINISH);
        assertFalse(closeWindowCalled);
        listener.onTestCaseEvent(testCaseSucceeded());
        assertFalse(closeWindowCalled);
        listener.onTestSessionEvent(sessionFinishedEvent());
        assertFalse(closeWindowCalled);
        listener.onTestSuiteFinishEvent(suiteFinishedEvent());
        assertTrue(closeWindowCalled);
    }

    @Test
    public void closeWindowOnTestFinishOnly() {
        UI.closeWindow(TestExecutionEvent.ON_TEST_FINISH);
        assertFalse(closeWindowCalled);
        listener.onTestSessionEvent(sessionFinishedEvent());
        assertFalse(closeWindowCalled);
        listener.onTestSuiteFinishEvent(suiteFinishedEvent());
        assertFalse(closeWindowCalled);
        listener.onTestCaseEvent(testCaseFailedEvent());
        assertTrue(closeWindowCalled);
    }

    private TestCaseEvent testCaseFailedEvent() {
        TestMethodExecutionResult executionResult = getTestMethodExecutionResult(TestEvent.ExecutionState.FAILED);
        return new TestCaseFailedEvent(executionResult, executionContext);
    }

    private TestCaseEvent testCaseSucceeded() {
        TestMethodExecutionResult executionResult = getTestMethodExecutionResult(TestEvent.ExecutionState.SUCCEEDED);
        return new TestCaseSucceededEvent(executionResult, executionContext);
    }

    private TestMethodExecutionResult getTestMethodExecutionResult(TestEvent.ExecutionState state) {
        TestMethodExecutionResult executionResult = mock(TestMethodExecutionResult.class);
        when(executionResult.getExecutionState()).thenReturn(state);
        return executionResult;
    }

    private TestSessionEvent sessionFinishedEvent() {
        return new TestSessionFinishedEvent();
    }

    private TestGroupFinishedEvent suiteFinishedEvent() {
        return new TestGroupFinishedEvent(suite);
    }

}
