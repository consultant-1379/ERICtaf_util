package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DataDrivenCsvBasedTest {

    private static boolean failed;
    private static Map<String, AtomicInteger> testIterations = new HashMap<>();

    @org.junit.Before
    public void setUp() {
        failed = false;
    }

    @org.junit.Test
    public void shouldRunWithData() {
        runTestNg();
        assertTrue(!failed);
        assertEquals(testIterations.get("dataSourceInjection").intValue(), 3);
        assertEquals(testIterations.get("doubleDataSourceInjection").intValue(), 3);
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
        @DataDriven(name = "vusercsvcopiedtest")
        public void dataSourceInjection(@Input("x") int x,
                                        @Input("y") int y,
                                        @Output("z") int z,
                                        @Input("vusercsvcopiedtest") DataRecord dataRecord) {
            assertEquals(x + y, z);
            check(dataRecord, x, y, z);
        }

        @Test
        @DataDriven(name = "vusercsvcopiedtest")
        public void doubleDataSourceInjection(@Input("x") int x, @Input("y") int y, @Output("z") int z,
                                              @Input("vusercsvcopiedtest") DataRecord first,
                                              @Input("vusercsvcopiedtest") DataRecord second) {
            check(first, x, y, z);
            check(second, x, y, z);
        }

        private void check(DataRecord data, int x, int y, int z) {
            assertNotNull(data);
            assertThat((String)data.getFieldValue("x"), is(Integer.toString(x)));
            assertThat((String)data.getFieldValue("y"), is(Integer.toString(y)));
            assertThat((String)data.getFieldValue("z"), is(Integer.toString(z)));
        }
    }

    public static class SimpleDataSource {
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

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            failed = true;
        }
    }

    public static class IterationListener extends AbstractTestListener {
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

}
