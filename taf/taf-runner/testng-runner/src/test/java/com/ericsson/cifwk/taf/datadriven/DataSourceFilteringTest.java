package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.ericsson.cifwk.taf.testng.TafTestRunnerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataSourceFilteringTest {
    private static boolean failed;
    private static volatile Map<String, Map<String, AtomicInteger>> testIterations = new ConcurrentHashMap<>(); // thread id => test name => iteration count

    @org.junit.Before
    public void setUp() {
        failed = false;
        testIterations.clear();
    }

    @org.junit.Test
    public void shouldFilterClassDataSources() {
        TestNG testNG = initializeTestNgFor(ClassDataDrivenTest.class);
        testNG.run();
        assertTrue(!failed);
        assertEquals(3, getTestIterations("simpleCase"));
        assertEquals(2, getTestIterations("positiveXFilter"));
        assertEquals(1, getTestIterations("complexFilter"));
        assertEquals(2, getTestIterations("contextAttributeFilter"));
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

    @org.junit.Test
    public void shouldFilterClassDataSourcesWithVUsers() {
        TestNG testNG = initializeTestNgForRunWithVUsers(ClassDataDrivenTest.class, 3);
        testNG.run();
        assertTrue(!failed);
        assertEquals(9, getTestIterations("simpleCase"));
        assertEquals(6, getTestIterations("positiveXFilter"));
        assertEquals(3, getTestIterations("complexFilter"));
        assertEquals(3, getTestIterations("vUserFilter"));
        assertEquals(6, getTestIterations("contextAttributeFilter"));
    }

    @org.junit.Test
    public void shouldFilterCsvDataSources() {
        TestNG testNG = initializeTestNgFor(CsvDataDrivenTest.class);
        testNG.run();
        assertTrue(!failed);
        assertEquals(3, getTestIterations("simpleCase"));
        assertEquals(2, getTestIterations("xFilter"));
        assertEquals(1, getTestIterations("complexFilter"));
        assertEquals(2, getTestIterations("contextAttributeFilter"));
    }

    private TestNG initializeTestNgFor(Class... testClasses) {
        RelaxedTestNG testNG = new RelaxedTestNG();
        testNG.setTestRunnerFactory(new TafTestRunnerFactory());
        testNG.setTestClasses(testClasses);
        testNG.addListener(new DataDrivenAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.addListener(new IterationListener());
        return testNG;
    }

    private TestNG initializeTestNgForRunWithVUsers(Class testClass, int vUserAmount) {
        TestNG testNG = initializeTestNgFor();
        List<XmlSuite> suites = prepareSuite(testClass, vUserAmount);
        testNG.setXmlSuites(suites);
        return testNG;
    }

    private List<XmlSuite> prepareSuite(Class<?> testClass, int vUserAmount) {
        List<XmlSuite> suites = Lists.newArrayList();
        XmlSuite suite = new XmlSuite();
        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("taf.vusers", String.valueOf(vUserAmount));
        suite.setParameters(parameters);
        XmlTest test = new XmlTest();
        test.setName("data-driven");
        List<XmlClass> classes = Lists.newArrayList();
        classes.add(new XmlClass(testClass));
        test.setClasses(classes);
        suite.addTest(test);
        suites.add(suite);
        test.setXmlSuite(suite);
        return suites;
    }

    public static class ClassDataDrivenTest {

        @Test
        @DataDriven(name = "calculator_from_class_filterable")
        public void simpleCase(@Input("vUser") int vUser, @Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertEquals(x + y, z);
        }

        @Test
        @DataDriven(name = "calculator_from_class_filterable", filter = "x > 0")
        public void positiveXFilter(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertTrue(x > 0);
        }

        @Test
        @DataDriven(name = "calculator_from_class_filterable", filter = "(x > 0 && y != 1) || z == 0")
        public void complexFilter(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertTrue(z == 0);
            TestContext context = TafTestContext.getContext();
            context.setAttribute("contextVar1", "contextVar1Value");
        }

        @Test
        @DataDriven(name = "calculator_from_class_filterable", filter = "$VUSER==vUser")
        public void vUserFilter(@Input("vUser") int vUser, @Input("x") int x, @Input("y") int y, @Output("z") int z) {
        }

        @Test(dependsOnMethods = "complexFilter")
        @DataDriven(name = "calculator_from_class_filterable", filter = "x > 0 && contextVar1=='contextVar1Value'")
        public void contextAttributeFilter(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertTrue(x > 0);
        }
    }

    public static class CsvDataDrivenTest {

        @Test
        @DataDriven(name = "vusercsvcopiedtest")
        public void simpleCase(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertEquals(x + y, z);
        }

        @Test
        @DataDriven(name = "vusercsvcopiedtest", filter = "x > 1")
        public void xFilter(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertTrue(x > 1);
        }

        @Test
        @DataDriven(name = "vusercsvcopiedtest", filter = "(x > 1 && y != x) || z == 6")
        public void complexFilter(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertTrue(z == 6);
            TestContext context = TafTestContext.getContext();
            context.setAttribute("contextVar1", "contextVar1Value");
        }

        @Test(dependsOnMethods = "complexFilter")
        @DataDriven(name = "vusercsvcopiedtest", filter = "x > 1 && $VUSER>0 && contextVar1=='contextVar1Value'")
        public void contextAttributeFilter(@Input("x") int x, @Input("y") int y, @Output("z") int z) {
            assertTrue(x > 1);
        }
    }

    public static class SimpleDataSource {
        @DataSource
        public Iterable<Map<String, Object>> list() {
            ArrayList<Map<String, Object>> list = new ArrayList<>();
            list.add(item(1, 1, 1, 2));
            list.add(item(2, 2, 1, 3));
            list.add(item(3, -1, 1, 0));
            return list;
        }

        private HashMap<String, Object> item(int vUser, int x, int y, int z) {
            HashMap<String, Object> items = new HashMap<>();
            items.put("vUser", vUser);
            items.put("x", x);
            items.put("y", y);
            items.put("z", z);
            return items;
        }
    }

    static class RelaxedTestNG extends TestNG {
        @Override
        protected void setTestRunnerFactory(ITestRunnerFactory factory) {
            super.setTestRunnerFactory(factory);
        }
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            failed = true;
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
