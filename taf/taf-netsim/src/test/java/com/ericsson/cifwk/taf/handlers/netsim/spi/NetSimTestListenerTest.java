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
package com.ericsson.cifwk.taf.handlers.netsim.spi;

import com.ericsson.cifwk.taf.execution.TestExecutionEvent;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseFailedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseSucceededEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestSessionFinishedEvent;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.class)
public class NetSimTestListenerTest {

    private boolean closeContextsCalled = false;

    @Mock
    NetSimCommandHandler netSimCommandHandler;

    @Mock
    private TestGroup suite;

    @Mock
    private TestMethodExecutionResult successfulResult;

    @Mock
    private TestMethodExecutionResult failedResult;

    @Mock
    private TestExecutionContext executionContext;

    @Spy
    private NetSimTestListener listener = new NetSimTestListener();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                closeContextsCalled = true;
                return null;
            }
        }).when(listener).closeAllContexts();
        when(successfulResult.getExecutionState()).thenReturn(TestEvent.ExecutionState.SUCCEEDED);
        when(failedResult.getExecutionState()).thenReturn(TestEvent.ExecutionState.FAILED);
    }

    @Test
    public void closeContextsOnSuiteFinishByDefault() {
        NetSimCommandHandler.setDefaultContextClosePolicy();
        assertFalse(closeContextsCalled);
        listener.onTestSuiteFinishEvent(new TestGroupFinishedEvent(suite));
        assertTrue(closeContextsCalled);
    }

    @Test
    public void closeWindowOnExecutionFinishOnly() {
        NetSimCommandHandler.setContextClosePolicy(TestExecutionEvent.ON_EXECUTION_FINISH);
        assertFalse(closeContextsCalled);
        listener.onTestSuiteFinishEvent(new TestGroupFinishedEvent(suite));
        assertFalse(closeContextsCalled);
        listener.onTestCaseEvent(new TestCaseSucceededEvent(successfulResult, executionContext));
        assertFalse(closeContextsCalled);
        listener.onTestSessionFinishEvent(new TestSessionFinishedEvent());
        assertTrue(closeContextsCalled);
    }

    @Test
    public void closeWindowOnSuiteFinishOnly() {
        NetSimCommandHandler.setContextClosePolicy(TestExecutionEvent.ON_SUITE_FINISH);
        assertFalse(closeContextsCalled);
        listener.onTestCaseEvent(new TestCaseSucceededEvent(successfulResult, executionContext));
        assertFalse(closeContextsCalled);
        listener.onTestSessionFinishEvent(new TestSessionFinishedEvent());
        assertFalse(closeContextsCalled);
        listener.onTestSuiteFinishEvent(new TestGroupFinishedEvent(suite));
        assertTrue(closeContextsCalled);
    }

    @Test
    public void closeWindowOnTestFinishOnly() {
        NetSimCommandHandler.setContextClosePolicy(TestExecutionEvent.ON_TEST_FINISH);
        assertFalse(closeContextsCalled);
        listener.onTestSessionFinishEvent(new TestSessionFinishedEvent());
        assertFalse(closeContextsCalled);
        listener.onTestSuiteFinishEvent(new TestGroupFinishedEvent(suite));
        assertFalse(closeContextsCalled);
        listener.onTestCaseEvent(new TestCaseFailedEvent(failedResult, executionContext));
        assertTrue(closeContextsCalled);
    }

}
