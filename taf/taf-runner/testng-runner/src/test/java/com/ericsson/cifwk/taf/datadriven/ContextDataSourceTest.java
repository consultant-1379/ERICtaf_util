package com.ericsson.cifwk.taf.datadriven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.google.common.collect.Maps;

public class ContextDataSourceTest {
    private static Logger log = LoggerFactory.getLogger(ContextDataSourceTest.class);
    private static boolean failed;
    private static volatile Map<String, Map<String, AtomicInteger>> testIterations = new ConcurrentHashMap<>(); // thread id => test name => iteration count

    @org.junit.Before
    public void setUp() {
        failed = false;
    }

    @org.junit.Test
    public void shouldUseCustomDataSource() {
        runTestNg();
        assertTrue(!failed);
        assertEquals(4, getTestIterations("customDataSourcePopulation"));
        assertEquals(2, getTestIterations("customDataSourceUsage"));
    }

    private int getTestIterations(String testName) {
        int result = 0;
        for (Map<String, AtomicInteger> iterationMap : testIterations.values()) {
            AtomicInteger inThreadIterations = iterationMap.get(testName);
            if (inThreadIterations != null) {
                result += inThreadIterations.intValue();
            }
        }
        return result;
    }

    private void runTestNg() {
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(new Class[]{DataDrivenTest.class});
        testNG.addListener(new DataDrivenAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.addListener(new IterationListener());
        testNG.run();
    }

    public static class DataDrivenTest {
        @Test
        @DataDriven(name = "contextdstest")
        public void customDataSourcePopulation(@Input("x") int x, @Input("y") int y, @Input("z") int z) {
            assertEquals(x + y, z);
            if (z < 0) {
                TestContext context = TafTestContext.getContext();
                // if addition returns negative, put result into custom DS
                context.dataSource("negativeResults").addRecord()
                .setField("result", z)
                .setField("doubled", z * z);
            }
        }

        @Test(dependsOnMethods = { "customDataSourcePopulation" })
        @DataDriven(name = "negativeResults")
        public void customDataSourceUsage(@Input("result") int addResults, @Input("doubled") int multiplyResults) {
            assertTrue(addResults < 0);
            assertEquals(addResults * addResults, multiplyResults);
        }
    }

    public static class SimpleDataSource {
        @DataSource
        public Iterable<Map<String, Object>> list() {
            ArrayList<Map<String, Object>> list = new ArrayList<>();
            list.add(item(1, -5, -4));
            list.add(item(5, 1, 6));
            list.add(item(-2, 1, -1));
            list.add(item(-1, 1, 0));
            return list;
        }

        private HashMap<String, Object> item(int x, int y, int z) {
            HashMap<String, Object> items = new HashMap<>();
            items.put("x", x);
            items.put("y", y);
            items.put("z", z);
            return items;
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

    public static class IterationListener implements IInvokedMethodListener {
        @Override
        public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        }

        @Override
        public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
            String testName = testResult.getName();
            String threadName = Thread.currentThread().getName();
            Map<String, AtomicInteger> threadInvocationMap = testIterations.get(threadName);
            if (threadInvocationMap == null) {
                threadInvocationMap = Maps.newConcurrentMap();
                testIterations.put(threadName, threadInvocationMap);
            }
            AtomicInteger iterationCount = threadInvocationMap.get(testName);
            if (iterationCount == null) {
                iterationCount = new AtomicInteger(1);
                threadInvocationMap.put(testName, iterationCount);
            } else {
                iterationCount.incrementAndGet();
            }
        }
    }

}
