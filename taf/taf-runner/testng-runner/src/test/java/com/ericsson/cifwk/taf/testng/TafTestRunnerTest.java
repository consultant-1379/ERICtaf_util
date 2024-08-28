package com.ericsson.cifwk.taf.testng;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.TestRunner;
import org.testng.internal.IConfiguration;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class TafTestRunnerTest {

    private final AtomicInteger runCounter = new AtomicInteger();
    private final ISuite suite = mock(ISuite.class);
    @SuppressWarnings("unchecked")
    Invoker<TestRunner> invoker = (Invoker<TestRunner>) mock(Invoker.class);
    XmlTest xmlTest = mock(XmlTest.class);
    private final SuiteReader suiteReader = mock(SuiteReader.class);
    private final IConfiguration configuration = mock(IConfiguration.class);

    //For testing passing in the default value simulates not setting the value on a suite file
    static final long DEFAULT_REPEAT_UNTIL = Long.MAX_VALUE;
    static final int DEFAULT_REPEAT_COUNT = 1;
    static final int DEFAULT_VUSERS = 1;
    static final String DEFAULT_CRON_EXPRESSION = null;

    @Before
    public void setUp() throws Exception {
        when(invoker.invoke(anyString())).thenReturn(null);
        when(invoker.invoke("privateRun", xmlTest)).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                runCounter.incrementAndGet();
                return null;
            }
        });

        XmlSuite xmlSuite = mock(XmlSuite.class);
        when(suite.getXmlSuite()).thenReturn(xmlSuite);
        when(xmlSuite.getConfigFailurePolicy()).thenReturn(XmlSuite.FailurePolicy.SKIP);
        when(xmlTest.isJUnit()).thenReturn(false);
    }

    @Test
    public void shouldInvokeRun_Once() throws Exception {
        TafTestRunner runner = createTafTestRunner(DEFAULT_VUSERS, DEFAULT_REPEAT_COUNT, DEFAULT_REPEAT_UNTIL, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertEquals(1, runCounter.get());
    }

    @Test
    public void shouldInvokeRun_Twice() throws Exception {
        TafTestRunner runner = createTafTestRunner(DEFAULT_VUSERS, 2, DEFAULT_REPEAT_UNTIL, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertEquals(2, runCounter.get());
    }

    @Test
    public void shouldInvokeRun_ForTwoVusers() throws Exception {
        TafTestRunner runner = createTafTestRunner(2, DEFAULT_REPEAT_COUNT, DEFAULT_REPEAT_UNTIL, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertEquals(2, runCounter.get());
    }

    @Test
    public void shouldInvokeRun_OnceForEachVuser() throws Exception {
        TafTestRunner runner = createTafTestRunner(5, DEFAULT_REPEAT_COUNT, DEFAULT_REPEAT_UNTIL, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertEquals(5, runCounter.get());
    }

    @Test
    public void shouldInvokeRun_MultipleTimesForEachVuser() throws Exception {
        TafTestRunner runner = createTafTestRunner(3, 3, DEFAULT_REPEAT_UNTIL, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertEquals(9, runCounter.get());
    }

    @Test
    public void shouldInvokeRun_UntilTimeout() throws Exception {
        TafTestRunner runner = createTafTestRunner(DEFAULT_VUSERS, DEFAULT_REPEAT_COUNT, 1000L, DEFAULT_CRON_EXPRESSION);
        Long startTime = System.currentTimeMillis();
        runner.run();
        Long endTime = System.currentTimeMillis();
        assertThat("Execution finished before repeat until was reached", endTime-startTime, greaterThan(998L));
        assertThat("Run counter not called enough times", runCounter.get(), greaterThan(1));
    }

    @Test
    public void shouldInvokeRun_5Times_WithTimeout() throws Exception {
        TafTestRunner runner = createTafTestRunner(DEFAULT_VUSERS, 5, 3000L, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertThat("Run counter not called enough times", runCounter.get(), is(5));
    }

    @Test
    public void shouldInvokeRun_UntilTimeout_With_RepeatCount() throws Exception {
        TafTestRunner runner = createTafTestRunner(DEFAULT_VUSERS, 99_999, 1000L, DEFAULT_CRON_EXPRESSION);
        Long startTime = System.currentTimeMillis();
        runner.run();
        Long endTime = System.currentTimeMillis();
        assertThat("Execution finished before repeat until was reached", endTime-startTime, greaterThan(900L));
        assertThat("Run counter called too many times", runCounter.get(), lessThan(99_999));
    }

    @Test
    public void shouldInvokeRun_UntilTimeoutForEachVuser() throws Exception {
        TafTestRunner runner = createTafTestRunner(3, DEFAULT_REPEAT_COUNT, 1000L, DEFAULT_CRON_EXPRESSION);
        runner.run();
        assertThat("Run counter not called enough times", runCounter.get(), greaterThan(5));
    }

    @Test
    public void shouldInvokeScheduler_OnceASecond() throws Exception {
        try{
            TafTestRunner runner = createTafTestRunner(DEFAULT_VUSERS, DEFAULT_REPEAT_COUNT, 1500L, "0/1 * * * * ?");
            runner.run();
            fail();
        }catch(RuntimeException e){
            assertThat(e.getMessage(), equalTo("Scheduled Task no longer supported due to the removal of quartz"));
        }
    }

    private TafTestRunner createTafTestRunner(int vUsers, int repeatCount, Long repeatFor, String cronExpression) {
        if(repeatCount==1 && repeatFor!=DEFAULT_REPEAT_UNTIL) {
            repeatCount = Integer.MAX_VALUE;
        }
        final Collection<IInvokedMethodListener> invokedMethodListeners = Collections.emptyList();
        final List<IClassListener> classListeners = Collections.emptyList();
        TafTestRunner runner = spy(new TafTestRunner(configuration, suite, xmlTest, false, invokedMethodListeners, classListeners));
        runner.setSuiteReader(suiteReader);
        runner.setRunnerInvoker(invoker);
        runner.setVuserFactory(new TafTestRunnerFactory() {
            @Override
            TafVUserTestRunner newVUserTestRunner(ISuite iSuite, XmlTest xmlTest, Collection<IInvokedMethodListener> invokedMethodListeners, List<IClassListener> classListeners) {
                TafVUserTestRunner runner =
                        spy(new TafVUserTestRunner(configuration, suite, xmlTest, false, invokedMethodListeners, classListeners));
                runner.setSuiteReader(suiteReader);
                runner.setRunnerInvoker(invoker);
                return runner;
            }

            @Override
            TafScheduledVUserTestRunner newScheduledVUserTestRunner(ISuite iSuite, XmlTest xmlTest, Collection<IInvokedMethodListener> invokedMethodListeners, List<IClassListener> classListeners, CountDownLatch latch) {
                TafScheduledVUserTestRunner runner =
                        spy(new TafScheduledVUserTestRunner(configuration, suite, xmlTest, false, invokedMethodListeners, classListeners, latch));
                runner.setSuiteReader(suiteReader);
                runner.setRunnerInvoker(invoker);
                return runner;
            }
        });

        doReturn(vUsers).when(suiteReader).getVusers();
        doReturn(repeatCount).when(suiteReader).getRepeatCount();
        doReturn(getRepeatUntil(repeatFor)).when(suiteReader).getRepeatUntil();
        doReturn(repeatCount!=Integer.MAX_VALUE).when(suiteReader).isRepeatCountSet();
        doReturn(repeatFor != DEFAULT_REPEAT_UNTIL).when(suiteReader).isRepeatUntilSet();
        doReturn(cronExpression).when(suiteReader).getCronExpression();

        return runner;
    }

    private long getRepeatUntil(long repeatFor) {
        if(repeatFor==Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }else {
            return System.currentTimeMillis() + repeatFor;
        }
    }
}
