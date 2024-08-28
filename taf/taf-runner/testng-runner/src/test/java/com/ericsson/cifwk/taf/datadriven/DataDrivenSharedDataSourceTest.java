package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
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
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import sun.net.www.protocol.test.TestConnection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static sun.net.www.protocol.test.CSV.csv;

public class DataDrivenSharedDataSourceTest {

    private static boolean failed;
    // vuser => test name => itration count
    private static Map<Integer, Map<String, AtomicInteger>> testIterations = new ConcurrentHashMap<>();

    @org.junit.Before
    public void setUp() {
        failed = false;

        TafConfiguration configuration = TafConfigurationProvider.provide();
        TafConfiguration runtimeConfiguration = configuration.getRuntimeConfiguration();
        runtimeConfiguration.setProperty("dataprovider.DataDrivenSharedDataSourceTest.type", "csv");
        runtimeConfiguration.setProperty("dataprovider.DataDrivenSharedDataSourceTest.uri", "test://DataDrivenSharedDataSourceTest");
        runtimeConfiguration.setProperty("dataprovider.DataDrivenSharedDataSourceTest.usage", "shared");
        TestConnection.setValue("DataDrivenSharedDataSourceTest",
                                csv("id")
                                        .add(1)
                                        .add(2)
                                        .toString()
        );
    }

    @org.junit.After
    public void tearDown() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        TafConfiguration runtimeConfiguration = configuration.getRuntimeConfiguration();
        runtimeConfiguration.clear();
    }


    @org.junit.Test
    public void shouldUseCustomDataSource() {
        runTestNg(TestNgTest.class);
        assertTrue(!failed);

        for (Map.Entry<Integer, Map<String, AtomicInteger>> entry : testIterations.entrySet()) {
            System.out.printf("for vuser:%s invoced:%s %n", entry.getKey(), entry.getValue().entrySet().size());
            for (Map.Entry<String, AtomicInteger> e : entry.getValue().entrySet()) {
                System.out.printf("    method:%s invoced:%s %n", e.getKey(), e.getValue());
            }
        }

        Map<String, AtomicInteger> v1 = testIterations.get(1);
        assertThat(v1.get("t1").get(), is(1));
        assertThat(v1.get("t2").get(), is(1));

        Map<String, AtomicInteger> v2 = testIterations.get(2);
        assertThat(v2.get("t1").get(), is(1));
        assertThat(v2.get("t2").get(), is(1));

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
        XmlTest test = new XmlTest(suite);
        test.setName("data-driven");
        List<XmlClass> classes = Lists.newArrayList();
        XmlClass clazz = new XmlClass(testClass);
        clazz.setIncludedMethods(Arrays.asList(
                new XmlInclude("t1", 1),
                new XmlInclude("t2", 2)
        ));
        classes.add(clazz);
        test.setClasses(classes);
        suites.add(suite);
        return suites;
    }

    static private AtomicInteger l1 = new AtomicInteger(0);
    static private AtomicInteger l2 = new AtomicInteger(0);

    public static class TestNgTest {

        @Test
        @DataDriven(name = "DataDrivenSharedDataSourceTest")
        public void t1(@Input("id") int id) throws InterruptedException {
            System.out.printf("> t1(%s) vuser:%s %n", id, TafTestContext.getContext().getVUser());
            waitFor(l1);
        }

        @Test
        @DataDriven(name = "DataDrivenSharedDataSourceTest")
        public void t2(@Input("id") int id) throws InterruptedException {
            System.out.printf("> t2(%s) vuser:%s %n", id, TafTestContext.getContext().getVUser());
            waitFor(l2);
        }

        void waitFor(AtomicInteger notify) throws InterruptedException {
            synchronized (notify) {
                notify.notifyAll();
                notify.wait();
                notify.notifyAll();
                notify.incrementAndGet();
            }
        }

    }

    public class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            System.err.println(result);
            result.getThrowable().printStackTrace();
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
            int vUser = TafTestContext.getContext().getVUser();
            Map<String, AtomicInteger> threadInvocationMap = testIterations.get(vUser);
            if (threadInvocationMap == null) {
                threadInvocationMap = Maps.newConcurrentMap();
                testIterations.put(vUser, threadInvocationMap);
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
