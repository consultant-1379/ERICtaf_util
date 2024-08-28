package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.google.common.base.Throwables;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.fail;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/05/2016
 */
public class MultipleDataSourceAdaptersTest {

    private final Map<String, AtomicInteger> testIterations = new HashMap<>();

    @org.junit.Test
    public void shouldUseAllAdaptersToFindDataSources() {
        runTestNg(DataDrivenTest.class);
        assertEquals(3, testIterations.get("useTafDataSource").intValue());
        assertEquals(1, testIterations.get("useTdmDataSource").intValue());
    }

    @org.junit.Test
    public void shouldFailOnMissingDataSource() {
        FailureListener failureListener = runTestNg(NoSuchDataSourceTest.class);
        assertThat(failureListener.isFailed(), is(true));
        assertThat(failureListener.failureCause.getMessage(), containsString("Failed to find an applicable data source"));
        assertThat(failureListener.failureCause.getMessage(), containsString("com.ericsson.cifwk.taf.datasource.AnotherDataSourceAdapter"));
        assertThat(failureListener.failureCause.getMessage(), containsString("com.ericsson.cifwk.taf.scenario.TafDataSourceAdapter"));
    }

    private FailureListener runTestNg(Class<?> testClass) {
        FailureListener failureListener = new FailureListener();
        TestNG testNG = new TestNG(false);
        testNG.addListener(failureListener);
        testNG.addListener(new IterationListener());
        testNG.setTestClasses(new Class[] {testClass});
        testNG.run();
        return failureListener;
    }

    public static class FailureListener extends AbstractTestListener {

        private boolean failed;
        public Throwable failureCause;

        @Override
        public void onTestFailure(ITestResult result) {
            this.failed = true;
            failureCause = result.getThrowable();
            Throwables.propagate(result.getThrowable());
        }

        public boolean isFailed() {
            return failed;
        }
    }

    public class IterationListener extends AbstractTestListener {
        @Override
        public void onTestSuccess(ITestResult result) {
            String testName = result.getName();
            AtomicInteger iterationCount = testIterations.get(testName);
            if (iterationCount == null) {
                iterationCount = new AtomicInteger(1);
                testIterations.put(testName, iterationCount);
            } else {
                iterationCount.incrementAndGet();
            }
        }
    }

    public static class DataDrivenTest {

        @Test
        @DataDriven(name = "simpleDS")
        public void useTafDataSource(@Input("x") Integer x, @Input("y") Integer y, @Input("z") Integer z) {
             assertThat(x + y, equalTo(z));
        }

        @Test
        @DataDriven(name = "externalAdapterDS")
        public void useTdmDataSource(@Input("netsimHostIP") String hostIp, @Input("netsimHostName") String hostName) {
             assertThat(hostIp, equalTo("1.0.0.1"));
             assertThat(hostName, equalTo("netsim1"));
        }

    }

    public static class NoSuchDataSourceTest {

        @Test
        @DataDriven(name = "noSuchDs")
        public void failOnUnknownDataSource(@Input("param1") String param1) {
            fail("This shouldn't have been called");
        }
    }

    public static class SumDataSource {
        @DataSource
        public Iterable<Map<String, Object>> list() {
            ArrayList<Map<String, Object>> list = new ArrayList<>();
            list.add(item(1, 1, 2));
            list.add(item(2, 1, 3));
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
}
