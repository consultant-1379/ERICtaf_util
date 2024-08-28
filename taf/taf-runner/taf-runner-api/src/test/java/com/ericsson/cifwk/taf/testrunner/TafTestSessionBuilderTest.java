package com.ericsson.cifwk.taf.testrunner;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 14/12/2016
 */
public class TafTestSessionBuilderTest {

    private TafTestSessionBuilder unit = new TafTestSessionMockBuilder();

    @Test
    public void shouldBuildTestSessionWithListeners() throws Exception {
        unit.withTestClasses(new Class[] { TestClass1.class });
        unit.withTestGroupDefinitions(Collections.singletonList("mySuite.xml"));
        List<String> testTags = Arrays.asList("group1", "group2");
        unit.withTestTags(testTags);
        EventListener1 listener1 = new EventListener1();
        unit.withTestEventListener(listener1);
        EventListener2 listener2 = new EventListener2();
        unit.withTestEventListener(listener2);

        TafTestSession testSession = unit.build();

        ArgumentCaptor<TafTestSessionOptions> optionsCaptor = ArgumentCaptor.forClass(TafTestSessionOptions.class);
        verify(testSession).init(optionsCaptor.capture());
        TafTestSessionOptions options = optionsCaptor.getValue();

        assertThat(options.getTestTags(), hasItems("group1", "group2"));
        assertThat(options.getTestClasses(), hasItem(TestClass1.class));
        assertThat(options.getTestGroupDefinitions(), hasItem("mySuite.xml"));
        List<Object> testEventListeners = options.getTestEventListeners();
        assertThat(testEventListeners, hasItem(listener1));
        assertThat(testEventListeners, hasItem(listener2));
    }

    private class TafTestSessionMockBuilder extends TafTestSessionBuilder {

        private TafTestSession sessionMock;

        @Override
        protected TafTestSession getNewInstance() {
            sessionMock = mock(TafTestSession.class);
            return sessionMock;
        }

    }

    private static class TestClass1 {
    }

    private static class EventListener1 {
    }

    private static class EventListener2 {
    }

}