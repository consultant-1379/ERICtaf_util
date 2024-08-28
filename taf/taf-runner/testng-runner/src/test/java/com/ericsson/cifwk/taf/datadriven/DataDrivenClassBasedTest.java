package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DataDrivenClassBasedTest {

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
        @DataDriven(name = "calculator_from_class")
        public void simpleCase(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertEquals(x + y, z);
        }

        @Test
        @DataDriven(name = "calculator_from_class")
        public void typeConversionOfSameParam(@Input("x") String x1, @Input("x") Integer x3) {
            assertEquals(x1, x3.toString());
        }

        @Test
        @DataDriven(name = "calculator_from_class")
        public void missingProperties(@Input("x") Integer x, @Input("q") String missing) {
            assertNotNull(x);
            assertNull(missing);
        }

        @Test
        @DataDriven(name = "calculator_from_class")
        public void dataSourceInjection(@Input("x") final int x,
                                        @Input("y") final int y,
                                        @Output("z") final int z,
                                        @Input("calculator_from_class") DataRecord dataRecord) {
            assertEquals(x + y, z);
            check(dataRecord,x,y,z);
        }

        @Test
        @DataDriven(name = "calculator_from_class")
        public void doubleDataSourceInjection(@Input("x") int x, @Input("y") int y, @Output("z") int z,
                                              @Input("calculator_from_class") DataRecord firstDataRecord,
                                              @Input("calculator_from_class") DataRecord secondDataRecord) {
            check(firstDataRecord,x, y, z);
            check(secondDataRecord,x, y, z);
        }

        private void check(DataRecord dataRecord, final int x, final int y, final int z) {
            assertThat(dataRecord, new BaseMatcher<DataRecord>() {
                @Override
                public boolean matches(Object item) {
                    DataRecord dataRecord = (DataRecord) item;
                    assertNotNull(item);
                    assertThat((int) dataRecord.getFieldValue("x"), is(x));
                    assertThat((int) dataRecord.getFieldValue("y"), is(y));
                    assertThat((int) dataRecord.getFieldValue("z"), is(z));
                    return true;
                }

                @Override
                public void describeTo(Description description) {
                }
            });
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
