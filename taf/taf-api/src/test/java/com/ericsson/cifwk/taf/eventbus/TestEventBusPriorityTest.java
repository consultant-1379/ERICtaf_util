package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.TestGroupResult;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseFailedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 22/08/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEventBusPriorityTest {

    private TestEventBus eventBus;

    @Spy
    private FirstEventHandler eventHandler1;

    @Spy
    private SecondEventHandler eventHandler2;

    @Spy
    private ThirdEventHandler eventHandler3;

    @Mock
    private TestMethodExecutionResult badResult;

    @Before
    public void setUp() {
        this.eventBus = new TestEventBusImpl();
        eventBus.register(eventHandler3);
        eventBus.register(eventHandler1);
        eventBus.register(eventHandler2);
        when(badResult.getExecutionState()).thenReturn(TestEvent.ExecutionState.FAILED);
    }

    @Test
    public void shouldSendEventsConsideringThePriority() {
        eventBus.post(new TestCaseFailedEvent(badResult, null));
        eventBus.post(new TestGroupFinishedEvent(new TestGroupImpl()));

        InOrder testStartedReceiveOrder = inOrder(eventHandler1, eventHandler2, eventHandler3);
        testStartedReceiveOrder.verify(eventHandler1).testCaseEvent(any(TestCaseEvent.class));
        testStartedReceiveOrder.verify(eventHandler2).testCaseEvent(any(TestCaseEvent.class));
        testStartedReceiveOrder.verify(eventHandler3).testCaseEvent(any(TestCaseEvent.class));

        testStartedReceiveOrder.verify(eventHandler2).testGroupEvent(any(TestGroupEvent.class));
        testStartedReceiveOrder.verify(eventHandler1).testGroupEvent(any(TestGroupEvent.class));
    }

    private static class FirstEventHandler {

        @Priority(1)
        @Subscribe
        void testCaseEvent(TestCaseEvent event) {
        }

        @Priority(2)
        @Subscribe
        void testGroupEvent(TestGroupEvent event) {
        }

    }

    private static class SecondEventHandler {

        @Priority(999)
        @Subscribe
        void testCaseEvent(TestCaseEvent event) {
        }

        @Priority(1)
        @Subscribe
        void testGroupEvent(TestGroupEvent event) {
        }
    }

    private static class ThirdEventHandler {

        // Should be notified last, since no priority is defined
        @Subscribe
        void testCaseEvent(TestCaseEvent event) {
        }
    }

    private class TestGroupImpl implements TestGroup {

        @Override
        public String getId() {
            return "id";
        }

        @Override
        public List<TestGroupResult> getResults() {
            return Lists.newArrayList();
        }

        @Override
        public String getDefinitionParameter(String parameterName) {
            return null;
        }

        @Override
        public Optional<String> getDefinitionFileName() {
            return Optional.absent();
        }

        @Override
        public <T> T getAttribute(String attributeName) {
            return null;
        }

        @Override
        public <T> void setAttribute(String attributeName, T attributeValue) {

        }

        @Override
        public void removeAttribute(String attributeName) {

        }
    }

}