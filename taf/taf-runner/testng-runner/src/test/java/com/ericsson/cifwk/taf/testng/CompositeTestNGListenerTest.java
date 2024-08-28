package com.ericsson.cifwk.taf.testng;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import org.testng.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class CompositeTestNGListenerTest {

    @Before
    public void setUp() {
        CompositeTestNGListener.cleanUpTestNgListeners();
    }

    @Test
    public void shouldAddListenersAccordingToOrder() throws Exception {
        IExecutionListener a = mock(IExecutionListener.class);
        IExecutionListener b = mock(IExecutionListener.class);
        IExecutionListener c = mock(IExecutionListener.class);

        CompositeTestNGListener.addListener(c, 99);
        CompositeTestNGListener.addListener(b, 50);
        CompositeTestNGListener.addListener(a, 1);

        Map<Integer,IExecutionListener> listeners = CompositeTestNGListener.executionListeners;
        assertThat(listeners.size(), equalTo(3));
        Iterator<IExecutionListener> iterator = listeners.values().iterator();
        assertThat(iterator.next(), is(a));
        assertThat(iterator.next(), is(b));
        assertThat(iterator.next(), is(c));
    }

    @Test
    public void shouldAddAllTypes() {
        IInvokedMethodListener a = mock(IInvokedMethodListener.class);
        IExecutionListener b = mock(IExecutionListener.class);
        ISuiteListener c = mock(ISuiteListener.class);
        ITestListener d = mock(ITestListener.class);
        IConfigurationListener e = mock(IConfigurationListener.class);
        IMethodInterceptor f = mock(IMethodInterceptor.class);

        CompositeTestNGListener.addListener(a, 1);
        CompositeTestNGListener.addListener(b, 1);
        CompositeTestNGListener.addListener(c, 1);
        CompositeTestNGListener.addListener(d, 1);
        CompositeTestNGListener.addListener(e, 1);
        CompositeTestNGListener.addListener(f, 1);

        assertThat(CompositeTestNGListener.executionListeners.size(), equalTo(1));
        assertThat(CompositeTestNGListener.suiteListeners.size(), equalTo(1));
        assertThat(CompositeTestNGListener.methodListeners.size(), equalTo(1));
        assertThat(CompositeTestNGListener.testListeners.size(), equalTo(1));
        assertThat(CompositeTestNGListener.configurationListeners.size(), equalTo(1));
        assertThat(CompositeTestNGListener.methodInterceptors.size(), equalTo(1));
    }

    @Test
    public void configurationListener() {
        IConfigurationListener configurationListener = mock(IConfigurationListener.class);
        CompositeTestNGListener.addListener(configurationListener, 1);

        CompositeTestNGListener compositeListener = new CompositeTestNGListener();

        compositeListener.onConfigurationFailure(null);
        verify(configurationListener).onConfigurationFailure(null);

        compositeListener.onConfigurationSkip(null);
        verify(configurationListener).onConfigurationSkip(null);

        compositeListener.onConfigurationSuccess(null);
        verify(configurationListener).onConfigurationSuccess(null);
    }

    @Test
    public void shouldSelectNewPriorityIfValueIsTaken(){
        IInvokedMethodListener a = mock(IInvokedMethodListener.class);
        IInvokedMethodListener b = mock(IInvokedMethodListener.class);
        IInvokedMethodListener c = mock(IInvokedMethodListener.class);
        
        CompositeTestNGListener.addListener(a, 5);
        CompositeTestNGListener.addListener(b, 5);
        CompositeTestNGListener.addListener(c, 5);
        
        assertEquals(CompositeTestNGListener.methodListeners.get(5), a);
        assertEquals(CompositeTestNGListener.methodListeners.get(6), b);
        assertEquals(CompositeTestNGListener.methodListeners.get(7), c);
    }
}
