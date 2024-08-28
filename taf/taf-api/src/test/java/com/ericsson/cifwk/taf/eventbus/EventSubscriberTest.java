package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupFinishedEvent;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 07/12/2016
 */
public class EventSubscriberTest {

    @Test
    public void sameMethodsOnDifferentClassesShouldDiffer() throws Exception {
        EventSubscriber subscriber1 = eventSubscriber(new Subscriber1(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber2 = eventSubscriber(new Subscriber2(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber3 = eventSubscriber(new Subscriber3(), "onTestCaseEvent", TestCaseEvent.class);

        assertNotEquals(subscriber1, subscriber2);
        assertNotEquals(subscriber2, subscriber3);
        assertNotEquals(subscriber1, subscriber3);

        assertNotEquals(0, subscriber1.compareTo(subscriber2));
        assertNotEquals(0, subscriber2.compareTo(subscriber3));
        assertNotEquals(0, subscriber1.compareTo(subscriber3));
    }

    @Test
    public void fewSameSubscribersInOneClassShouldDiffer() throws Exception {
        EventSubscriber subscriber1 = eventSubscriber(new Subscriber1(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber2 = eventSubscriber(new Subscriber1(), "onTestCaseEvent2", TestCaseEvent.class);
        assertNotEquals(subscriber1, subscriber2);
    }

    @Test
    public void comparePriorities() throws Exception {
        EventSubscriber eventSubscriberWithHigherPriority = eventSubscriber(new Subscriber2(), "onTestGroupEvent", TestGroupEvent.class);
        EventSubscriber eventSubscriberWithLowerPriority = eventSubscriber(new Subscriber3(), "onTestGroupEvent", TestGroupEvent.class);

        assertEquals(-1, eventSubscriberWithHigherPriority.comparePriorityWith(eventSubscriberWithLowerPriority));
        assertEquals(1, eventSubscriberWithLowerPriority.comparePriorityWith(eventSubscriberWithHigherPriority));
    }

    @Test
    public void noPrioritiesShouldBeEqual() throws Exception {
        EventSubscriber eventSubscriberWithNoPriority1 =
                eventSubscriber(new Subscriber1(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber eventSubscriberWithNoPriority2 =
                eventSubscriber(new Subscriber2(), "onTestCaseEvent", TestCaseEvent.class);

        assertEquals(0, eventSubscriberWithNoPriority1.comparePriorityWith(eventSubscriberWithNoPriority2));
        assertEquals(0, eventSubscriberWithNoPriority2.comparePriorityWith(eventSubscriberWithNoPriority1));
    }

    @Test
    public void compareTargets() throws Exception {
        EventSubscriber subscriber1 = eventSubscriber(new Subscriber1(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber2 = eventSubscriber(new Subscriber1(), "onTestCaseEvent2", TestCaseEvent.class);
        EventSubscriber subscriber3 = eventSubscriber(new Subscriber2(), "onTestCaseEvent", TestCaseEvent.class);

        assertEquals(0, subscriber1.compareTargetWith(subscriber1));
        assertEquals(0, subscriber1.compareTargetWith(subscriber2));
        assertEquals(0, subscriber2.compareTargetWith(subscriber1));
        assertEquals(-1, subscriber1.compareTargetWith(subscriber3));
        assertEquals(1, subscriber3.compareTargetWith(subscriber1));
    }

    @Test
    public void compareTargetMethods() throws Exception {
        EventSubscriber subscriber1 = eventSubscriber(new Subscriber1(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber1Duplicate = eventSubscriber(new Subscriber1(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber2 = eventSubscriber(new Subscriber2(), "onTestCaseEvent", TestCaseEvent.class);
        EventSubscriber subscriber3 = eventSubscriber(new Subscriber1(), "onTestGroupEvent", TestGroupFinishedEvent.class);
        EventSubscriber subscriber4 = eventSubscriber(new Subscriber2(), "onTestGroupEvent", TestGroupEvent.class);

        assertEquals(0, subscriber1.compareMethodWith(subscriber1Duplicate));
        assertEquals(0, subscriber1Duplicate.compareMethodWith(subscriber1));
        assertEquals(-1, subscriber1.compareMethodWith(subscriber2));
        assertEquals(1, subscriber2.compareMethodWith(subscriber1));
        assertEquals(-1, subscriber3.compareMethodWith(subscriber4));
        assertEquals(1, subscriber4.compareMethodWith(subscriber3));
    }


    private EventSubscriber eventSubscriber(Object subscriber, String methodName, Class<?> eventClass) throws Exception {
        Method method = getMethod(subscriber.getClass(), methodName, eventClass);
        Priority priorityAnnotation = method.getAnnotation(Priority.class);
        return (priorityAnnotation == null)
                ? new EventSubscriber(subscriber, method)
                : new EventSubscriber(subscriber, method, priorityAnnotation.value());
    }

    private Method getMethod(Class<?> clazz, String methodName, Class<?> eventClass) throws Exception {
        return clazz.getMethod(methodName, eventClass);
    }

    private class Subscriber1 {

        @Subscribe
        public void onTestCaseEvent(TestCaseEvent event) {
        }

        @Subscribe
        public void onTestCaseEvent2(TestCaseEvent event) {
        }

        // Method badly named on purpose
        @Subscribe
        public void onTestGroupEvent(TestGroupFinishedEvent event) {
        }

    }

    private class Subscriber2 {

        @Subscribe
        public void onTestCaseEvent(TestCaseEvent event) {
        }

        @Subscribe
        @Priority(10)
        public void onTestGroupEvent(TestGroupEvent event) {
        }

    }

    private class Subscriber3 {

        @Subscribe
        @Priority(100)
        public void onTestCaseEvent(TestCaseEvent event) {
        }

        @Subscribe
        @Priority(20)
        public void onTestGroupEvent(TestGroupEvent event) {
        }

    }

}