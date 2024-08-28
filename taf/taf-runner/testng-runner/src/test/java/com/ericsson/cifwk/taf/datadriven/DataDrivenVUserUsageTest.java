package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.ericsson.cifwk.taf.testng.TafTestRunnerFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class DataDrivenVUserUsageTest {

    private static boolean failed;
    private static Multimap<Object, Integer[]> argListsPerVUser;
    private static Logger log = LoggerFactory
            .getLogger(DataDrivenVUserUsageTest.class);

    @org.junit.Before
    public void setUp() {
        failed = false;
        argListsPerVUser = ArrayListMultimap.create();
    }

    private static synchronized void addArgList(Object vuser, Integer[] argList) {
        log.info("Updating ArrayListMultimap: vuser:" + vuser + " arglist: " + Arrays.toString(argList));
        argListsPerVUser.put(vuser, argList);
    }

    @org.junit.Test
    public void shouldRunWithClassUsageShared() {
        runTestNg(ClassUsageTestShared.class);

        assertTrue(!failed);

        Map<Object, Collection<Integer[]>> argListMap = argListsPerVUser
                .asMap();
        log.info("Threads worked:" + argListMap.keySet().size());
        /*
         * Based on Race Condition,Single Thread can consume all data before
         * other Threads Catch up.
         */
        assertTrue(argListMap.keySet().size() >= 1);
        SimpleDataSource dataSource = new SimpleDataSource();
        assertEquals(
                ((ArrayList<Map<String, Object>>) dataSource.list()).size(),
                argListsPerVUser.size());
    }

    public static class ClassUsageTestShared {

        private static Logger log = LoggerFactory
                .getLogger(ClassUsageTestShared.class);

        @Test
        @DataDriven(name = "vuserclasssharedtest")
        public void simpleCase(@Input("x") int x, @Input("y") int y,
                @Output("z") int z) {
            assertEquals(x + y, z);
            log.info(Thread.currentThread().getName() + "==x:" + x + " y:" + y
                    + " z:" + z);
            addArgList(Thread.currentThread().getName(), new Integer[] { x, y,
                z });
        }
    }

    @org.junit.Test
    public void shouldRunWithClassUsageCopied() {
        runTestNg(ClassUsageTestCopied.class);

        assertTrue(!failed);

        Map<Object, Collection<Integer[]>> argListMap = argListsPerVUser
                .asMap();

        log.info("Threads worked:" + argListMap.keySet().size());
        assertTrue(argListMap.keySet().size() == 3);// Number of Threads
        assertEquals(9, argListsPerVUser.size());// 3(No of Rows)*3(No of
        // vUsers)
    }

    public static class ClassUsageTestCopied {

        private static Logger log = LoggerFactory
                .getLogger(ClassUsageTestCopied.class);

        @Test
        @DataDriven(name = "vuserclasscopiedtest")
        public void simpleCase(@Input("x") int x, @Input("y") int y,
                @Output("z") int z) {
            assertEquals(x + y, z);
            log.info(Thread.currentThread().getName() + "==x:" + x + " y:" + y
                    + " z:" + z);
            addArgList(Thread.currentThread().getName(), new Integer[] { x, y,
                z });
        }
    }

    @org.junit.Test
    public void shouldRunWithCSVUsageShared() {
        runTestNg(CSVUsageTestShared.class);

        Map<Object, Collection<Integer[]>> argListMap = argListsPerVUser
                .asMap();
        log.info("Threads worked:" + argListMap.keySet().size());
        /*
         * Based on Race Condition,Single Thread can consume all data before
         * other Threads Catch up.
         */
        assertTrue(argListMap.keySet().size() >= 1);
        assertEquals(3, argListsPerVUser.size());// 3 is no of Rows in
        // calculator.csv
    }

    public static class CSVUsageTestShared {

        private static Logger log = LoggerFactory.getLogger(CSVUsageTestShared.class);

        @Test
        @DataDriven(name = "vusercsvsharedtest")
        public void simpleCase(@Input("x") int x, @Input("y") int y,
                @Output("z") int z) {
            assertEquals(x + y, z);
            log.info(Thread.currentThread().getName() + "==x:" + x + " y:" + y
                    + " z:" + z);
            addArgList(Thread.currentThread().getName(), new Integer[] { x, y,
                z });
        }
    }

    @org.junit.Test
    public void shouldRunWithCSVUsageCopied() {
        runTestNg(CSVUsageTestCopied.class);

        Map<Object, Collection<Integer[]>> argListMap = argListsPerVUser
                .asMap();
        log.info("Threads worked:" + argListMap.keySet().size());
        assertTrue(argListMap.keySet().size() == 3);// Number of Threads
        assertEquals(9, argListsPerVUser.size());// 3(No of Rows)*3(No of
        // vUsers)
    }

    public static class CSVUsageTestCopied {

        private static Logger log = LoggerFactory.getLogger(CSVUsageTestCopied.class);

        @Test
        @DataDriven(name = "vusercsvcopiedtest")
        public void simpleCase(@Input("x") int x, @Input("y") int y,
                @Output("z") int z) {
            assertEquals(x + y, z);
            log.info(Thread.currentThread().getName() + "==x:" + x + " y:" + y
                    + " z:" + z);
            addArgList(Thread.currentThread().getName(), new Integer[] { x, y,
                z });
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

        List<XmlSuite> suites = prepareSuite(testClass);
        testNG.setXmlSuites(suites);

        testNG.run();
    }

    private List<XmlSuite> prepareSuite(Class<?> testClass) {
        List<XmlSuite> suites = Lists.newArrayList();
        XmlSuite suite = new XmlSuite();
        Map<String, String> parameters = Maps.newHashMap();
        parameters.put("taf.vusers", "3");
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
        public synchronized Iterable<Map<String, Object>> list() {
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
            result.getThrowable().printStackTrace();
        }
    }

}
