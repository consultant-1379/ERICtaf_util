package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.datasource.DataSourceControl;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class ConcurrentCyclicDataSourceTest {
    private static Logger log = LoggerFactory.getLogger(ConcurrentCyclicDataSourceTest.class);
    private static boolean failed;
    static final AtomicInteger count1 = new AtomicInteger();
    static final AtomicInteger count2 = new AtomicInteger();
    public static final int COUNT1_RUNS = 8;
    public static final int COUNT2_RUNS = 32;

    @org.junit.Before
    public void setUp() {
        failed = false;
        count1.set(0);
        count2.set(0);
    }

    @org.junit.Test(timeout = 7000)
    public void stopExecutionShouldBeIsolated() {
        runTestNg();
        assertThat(count1.intValue(), equalTo(COUNT1_RUNS));
        assertThat(count2.intValue(), equalTo(COUNT2_RUNS));
        assertTrue(!failed);
    }

    private void runTestNg() {
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(new Class[]{DataDrivenTest.class});
        testNG.addListener(new DataDrivenAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.setThreadCount(2);
        testNG.setParallel("true");
        testNG.run();
    }

    public static class DataDrivenTest {
        @Test
        @DataDriven(name = "calculator_cyclic")
        public void loop1(@Input("x") Integer x, @Input("y") Integer y, @Input("z") Integer z) throws NoSuchFieldException, IllegalAccessException {
            int counter = count1.incrementAndGet();
            if (counter == COUNT1_RUNS) {
                DataSourceControl.stopExecution("calculator_cyclic");
            }
        }

        @Test
        @DataDriven(name = "calculator_cyclic")
        public void loop2(@Input("x") Integer x, @Input("y") Integer y, @Input("z") Integer z) throws NoSuchFieldException, IllegalAccessException {
            int counter = count2.incrementAndGet();
            if (counter == COUNT2_RUNS) {
                DataSourceControl.stopExecution("calculator_cyclic");
            }
        }
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            failed = true;
            Throwable throwable = result.getThrowable();
            if (throwable != null) {
                log.error("Thrown\n", throwable);
            }
        }
    }
}
