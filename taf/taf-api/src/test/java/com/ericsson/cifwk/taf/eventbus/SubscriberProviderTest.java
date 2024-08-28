package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.taf.testapi.events.TestCaseEvent;
import com.ericsson.cifwk.taf.testapi.events.TestGroupEvent;
import com.google.common.collect.Multimap;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 16/09/2016
 */
public class SubscriberProviderTest {

    @Test
    public void getAnnotatedMethods_happyPath() throws Exception {
        Set<Method> annotatedMethods = SubscriberProvider.getAnnotatedMethods(GoodEventHandler.class);
        assertThat(annotatedMethods, hasSize(2));
        assertThat(annotatedMethods, hasItem(GoodEventHandler.class.getDeclaredMethod("testCaseEvent", new Class[]{TestCaseEvent.class})));
        assertThat(annotatedMethods, hasItem(GoodEventHandler.class.getDeclaredMethod("testGroupEvent", new Class[]{TestGroupEvent.class})));
    }

    @Test
    public void getAnnotatedMethods_wrongSignature() throws Exception {
        verifyBadSignature(BadEventHandler1.class);
        verifyBadSignature(BadEventHandler2.class);
    }

    @Test
    public void shouldGetAllSubscribers() {
        Multimap<String, EventSubscriber> allSubscribers = SubscriberProvider.getAllSubscribers(new GoodEventHandler());
        assertEquals(2, allSubscribers.size());
        Set<String> keySet = allSubscribers.asMap().keySet();
        assertThat(keySet, hasItem(TestCaseEvent.class.getName()));
        assertThat(keySet, hasItem(TestGroupEvent.class.getName()));
    }

    private void verifyBadSignature(Class clazz) {
        try {
            SubscriberProvider.getAnnotatedMethods(clazz);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("TAF Test event subscriber has to accept only one non-null parameter"));
        }
    }

    private static class GoodEventHandler {

        @Subscribe
        void testCaseEvent(TestCaseEvent event) {
        }

        @Priority(2)
        @Subscribe
        void testGroupEvent(TestGroupEvent event) {
        }

        void notAnnotated() {
        }

    }

    private static class BadEventHandler1 {

        @Subscribe
        void testGroupEvent(TestGroupEvent event) {
        }

        @Subscribe
        void annotatedButWithoutEventParameter(Object object) {
        }
    }

    private static class BadEventHandler2 {

        @Subscribe
        void testGroupEvent(TestGroupEvent event, Object wrong) {
        }

    }
}