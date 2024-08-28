package com.ericsson.cifwk.taf.swt.agent;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Before;
import org.junit.Test;

public class ObjectLocatorTest {

    private static final String KEY = "objectId";

    private ObjectLocator objectLocator;

    @Before
    public void setUp() {
        objectLocator = new ObjectLocator();
        objectLocator = spy(objectLocator);
        doReturn(null).when(objectLocator).findWindow("nonExistingWindowTitle");
        doReturn(mock(SWTBot.class)).when(objectLocator).findWindow("windowTitle");
        objectLocator.put(KEY, BigDecimal.ZERO);
    }

    @Test
    public void get() {
        assertNull(objectLocator.get("nonExistingObjectId"));
        assertEquals(BigDecimal.ZERO, objectLocator.get(KEY));
        assertNull(objectLocator.get("window:nonExistingWindowTitle"));
        assertNotNull(objectLocator.get("window:windowTitle"));
    }

    @Test
    public void put() {
        assertEquals(BigDecimal.ZERO, objectLocator.get(KEY));
        objectLocator.put(KEY, BigDecimal.ONE);
        assertEquals(BigDecimal.ONE, objectLocator.get(KEY));
        String objectId = objectLocator.put(BigDecimal.TEN);
        assertEquals(BigDecimal.TEN, objectLocator.get(objectId));
    }

    @Test
    public void testReset() throws Exception {
        objectLocator.reset();
        assertNull(objectLocator.get(KEY));
    }

}
