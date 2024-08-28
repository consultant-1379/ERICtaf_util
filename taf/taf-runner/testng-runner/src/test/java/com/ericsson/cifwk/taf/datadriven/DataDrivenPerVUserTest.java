package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.ericsson.cifwk.taf.testng.TafTestRunnerFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DataDrivenPerVUserTest {

    private static boolean failed;
    private static Multimap<Object, Integer[]> argListsPerVUser;
    private static Map<String, Map<String, AtomicInteger>> testIterations = new ConcurrentHashMap<>(); // thread id => test name => iteration count

    @org.junit.Before
    public void setUp() {
        failed = false;
        argListsPerVUser = ArrayListMultimap.create();
    }

    private static synchronized void addArgList(Object vuser, Integer[] argList) {
        argListsPerVUser.put(vuser, argList);
    }

    private static void checkDataSource(TestDataSource dataSource) {
        assertNotNull(dataSource);
        assertThat((Iterable<Object>) dataSource, iterableWithSize(3));
    }

    @org.junit.Test
    public void shouldRunWithDataAndVUsers() {
        runTestNg(DataAndVUsersTest.class);

        assertTrue(!failed);
        assertEquals(6, argListsPerVUser.size());

        Map<Object, Collection<Integer[]>> argListMap = argListsPerVUser
                .asMap();
        assertEquals(2, argListMap.keySet().size());
        for (Collection<Integer[]> argLists : argListMap.values()) {
            assertDataSourceOrder(argLists);
        }
    }

    public static class DataAndVUsersTest {
        @Test
        @DataDriven(name = "per_vuser")
        public void simpleCase(@Input("x") int x, @Input("y") int y,
                @Output("z") int z) {
            assertEquals(x + y, z);
            addArgList(Thread.currentThread().getId(),
                    new Integer[] { x, y, z });
        }
    }

    public static class DataAndVUsersAndContextsTest {
        @Test
        @DataDriven(name = "per_vuser")
        public void simpleCase(@Input("x") int x, @Input("y") int y, @Output("z") int z, TestDataSource dataSource) {
            assertEquals(x + y, z);
            addArgList(Thread.currentThread().getId(), new Integer[]{x, y, z});
            checkDataSource(dataSource);
        }
    }

    static class RelaxedTestNG extends TestNG {
        @Override
        protected void setTestRunnerFactory(ITestRunnerFactory factory) {
            super.setTestRunnerFactory(factory);
        }
    }

    private void runTestNg(Class<?> testClass) {
        RelaxedTestNG testNG = new RelaxedTestNG();
        testNG.setTestRunnerFactory(new TafTestRunnerFactory());

        testNG.addListener(new DataDrivenAnnotationTransformer());
        testNG.addListener(new TestNGAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.addListener(new IterationListener());

        List<XmlSuite> suites = prepareSuite(testClass);
        testNG.setXmlSuites(suites);

        testNG.run();
    }

    private List<XmlSuite> prepareSuite(Class<?> testClass) {
        List<XmlSuite> suites = Lists.newArrayList();
        XmlSuite suite = new XmlSuite();
        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("taf.vusers", "2");
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

    private static void assertDataSourceOrder(Collection<Integer[]> argLists) {
        SimpleDataSource dataSource = new SimpleDataSource();
        Iterator<Map<String, Object>> argsInOrder = Iterators.cycle(dataSource
                .list());
        for (Integer[] argList : argLists) {
            Map<String, Object> argsMap = argsInOrder.next();
            Object[] expectedArgsList = { argsMap.get("x"), argsMap.get("y"),
                    argsMap.get("z") };
            assertArrayEquals(expectedArgsList, argList);
        }
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestStart(ITestResult result) {
            super.onTestStart(result);
        }

        @Override
        public void onTestFailure(ITestResult result) {
            failed = true;
            result.getThrowable().printStackTrace();
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
