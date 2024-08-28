package com.ericsson.cifwk.taf.tools.http.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

import com.ericsson.cifwk.taf.tools.http.HttpToolListener;

@RunWith(MockitoJUnitRunner.class)
public class HttpToolListenersTest {

    @Mock
    private HttpToolListener listener;

    @Before
    public void setUp() {
        HttpToolListeners.addListener(listener);
    }

    @Test
    public void addListener() {
        assertEquals(1, HttpToolListeners.getListeners().size());
        assertEquals(listener, HttpToolListeners.getListeners().iterator().next());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getListenersNotModifiable() {
        List<HttpToolListener> listeners = HttpToolListeners.getListeners();
        listeners.add(listener);
    }

    @Test
    public void removeAllListeners() {
        assertEquals(1, HttpToolListeners.getListeners().size());
        HttpToolListeners.removeAllListeners();
        assertEquals(0, HttpToolListeners.getListeners().size());
    }

}
