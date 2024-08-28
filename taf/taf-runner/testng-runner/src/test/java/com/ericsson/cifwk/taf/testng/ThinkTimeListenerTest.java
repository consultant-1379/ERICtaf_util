package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.testapi.TestGroup;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ThinkTimeListenerTest {

    private ThinkTimeListener listener;
    private long sleepTime;

    @Before
    public void setUp() throws Exception {
        sleepTime = -1;
        listener = new ThinkTimeListener() {
            @Override
            void sleep(int thinkTime) {
                sleepTime = thinkTime;
            }
        };
    }

    @Test
    public void shouldSleepForAWhile() throws Exception {
        TestGroup context = mockSuite("2");
        listener.processTestGroup(context);
        assertEquals(2, sleepTime);
    }

    @Test
    public void shouldNotSleep() throws Exception {
        TestGroup context = mockSuite(null);
        listener.processTestGroup(context);
        assertEquals(-1, sleepTime);
    }

    @Test
    public void shouldNotSleepEver() throws Exception {
        TestGroup context = mockSuite("");
        listener.processTestGroup(context);
        assertEquals(-1, sleepTime);
    }

    private TestGroup mockSuite(String thinkTime) {
        TestGroup suite = mock(TestGroup.class);
        when(suite.getDefinitionParameter(eq(ThinkTimeListener.PARAMETER_THINK_TIME)))
                .thenReturn(thinkTime);
        return suite;
    }

}
