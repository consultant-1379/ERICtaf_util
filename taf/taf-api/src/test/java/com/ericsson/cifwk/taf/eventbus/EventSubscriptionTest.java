package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestGroup;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;
import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestCaseStartedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupStartedEvent;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 07/12/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class EventSubscriptionTest {

    @Spy
    private TestEventBusImpl eventBus;

    private Map<Class<?>, EventNotifications> notificationsReceived = Maps.newHashMap();

    @Test
    public void shouldSupportMultipleSubscribers() {
        eventBus.register(new Subscriber1());
        eventBus.register(new Subscriber2());
        eventBus.register(new Subscriber3());
        EmptySubscriber emptySubscriber = new EmptySubscriber();
        eventBus.register(emptySubscriber);

        eventBus.post(new TestCaseStartedEvent(mock(TestMethodExecutionResult.class), mock(TestExecutionContext.class)));

        verifyNotifications(Subscriber1.class, TestCaseStartedEvent.class, 1);
        verifyNotifications(Subscriber2.class, TestCaseStartedEvent.class, 1);
        verifyNotifications(Subscriber3.class, TestCaseStartedEvent.class, 1);

        verifyNotifications(EmptySubscriber.class, TestCaseStartedEvent.class, 0);
        verify(eventBus).warnAboutEmptySubscriberClass(emptySubscriber);
    }

    @Test
    public void shouldIgnoreDuplicateRegistration() {
        eventBus.register(new Subscriber1());
        Subscriber1 duplicate = new Subscriber1();
        eventBus.register(duplicate);

        eventBus.post(new TestCaseStartedEvent(mock(TestMethodExecutionResult.class), mock(TestExecutionContext.class)));

        verifyNotifications(Subscriber1.class, TestCaseStartedEvent.class, 1);
        verify(eventBus).warnAboutDuplicateRegistration(duplicate);
    }

    @Test
    public void shouldBeAbleToSubscribeToEventParentClass() {
        eventBus.register(new Subscriber1());
        eventBus.register(new Subscriber2());
        eventBus.register(new Subscriber3());

        eventBus.post(new TestGroupStartedEvent(mock(TestGroup.class)));
        eventBus.post(new TestGroupFinishedEvent(mock(TestGroup.class)));

        verifyNotifications(Subscriber1.class, TestGroupStartedEvent.class, 1);
        verifyNotifications(Subscriber1.class, TestGroupFinishedEvent.class, 1);
        verifyNotifications(Subscriber2.class, TestGroupStartedEvent.class, 1);
        verifyNotifications(Subscriber3.class, TestGroupFinishedEvent.class, 1);
    }

    private void verifyNotifications(Class<?> subscriberClass, Class<? extends TestEvent> eventClass, int expectedCount) {
        int actualCount = 0;

        EventNotifications eventNotifications = notificationsReceived.get(subscriberClass);
        if (eventNotifications != null) {
            actualCount = eventNotifications.getCount(eventClass);
        }
        assertEquals(expectedCount, actualCount);
    }

    private class Subscriber1 extends CountingListener {

        @Subscribe
        public void onTestCaseEvent(TestCaseEvent event) {
            registerNotification(event);
        }

        @Subscribe
        public void onTestGroupEvent(TestGroupEvent event) {
            registerNotification(event);
        }

    }

    private class Subscriber2 extends CountingListener {

        @Subscribe
        public void onTestCaseEvent(TestCaseEvent event) {
            registerNotification(event);
        }

        @Subscribe
        public void onTestGroupStartedEvent(TestGroupStartedEvent event) {
            registerNotification(event);
        }

    }

    private class Subscriber3 extends CountingListener {

        @Subscribe
        @Priority(100)
        public void onTestCaseEvent(TestCaseEvent event) {
            registerNotification(event);
        }

        @Subscribe
        public void onTestGroupFinishedEvent(TestGroupFinishedEvent event) {
            registerNotification(event);
        }

    }

    private class EmptySubscriber extends CountingListener {

        public void onTestCaseEvent(TestCaseEvent event) {
            registerNotification(event);
        }
    }

    private abstract class CountingListener {

        protected void registerNotification(TestEvent testEvent) {
            EventNotifications eventNotifications = notificationsReceived.get(getClass());
            if (eventNotifications == null) {
                eventNotifications = new EventNotifications();
                notificationsReceived.put(getClass(), eventNotifications);
            }
            eventNotifications.registerNotification(testEvent);
        }
    }

    private class EventNotifications {

        private final Map<Class<? extends TestEvent>, AtomicInteger> notifications = Maps.newHashMap();

        public void registerNotification(TestEvent testEvent) {
            Class<? extends TestEvent> eventClass = testEvent.getClass();
            AtomicInteger count = notifications.get(eventClass);
            if (count == null) {
                count = new AtomicInteger(0);
                notifications.put(eventClass, count);
            }
            count.incrementAndGet();
        }

        public int getCount(Class<? extends TestEvent> eventClass) {
            AtomicInteger count = notifications.get(eventClass);
            return (count == null) ? 0 : count.get();
        }
    }

}
