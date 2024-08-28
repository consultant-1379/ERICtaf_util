package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.ui.core.UiWindowProviderRegistry;
import com.ericsson.cifwk.taf.ui.selenium.AbstractBrowserAwareITest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by ethomev on 8/17/15.
 */
public class BrowsersAcrossTestNGAndScenarioThreadsTest extends AbstractBrowserAwareITest {

    private static AtomicInteger singleCount = new AtomicInteger(10);
    private static AtomicInteger parallelSuitesCount = new AtomicInteger(0);
    private static AtomicInteger multipleVusersCount = new AtomicInteger(0);
    private static AtomicInteger countBeforeClose = new AtomicInteger(10);
    private static AtomicInteger countAfterClose = new AtomicInteger(10);
    private static List<UiWindowProviderRegistry> registries = Collections.synchronizedList(new ArrayList<UiWindowProviderRegistry>());

    @Test
    public void singleThreadTest() {
        runTestNGSingleThread();
        assertEquals(0, singleCount.get());
    }

    @Test
    public void parallelSuitesShouldHaveSeparateUIRegistries() {
        runTestNGParallelSuites();
        assertEquals(2, parallelSuitesCount.get());
        assertNotEquals(registries.get(0), registries.get(1));
        for (UiWindowProviderRegistry registry : registries) {
            assertEquals(0, registry.getActiveBrowserCount());
        }
    }

    @Test
    public void scenarioMultipleVusersShouldHaveSeparateUIRegistries() {
        TestScenario scenario = scenario("Multiple VUsers").addFlow(flow("Flow").addTestStep(annotatedMethod(this, "OpenBrowserA"))).withDefaultVusers(2).build();
        TestScenarioRunner runner = runner().build();
        runner.start(scenario);
        assertEquals(2, multipleVusersCount.get());
        assertNotEquals(registries.get(0), registries.get(1));
        assertEquals(12, countBeforeClose.get());
        assertEquals(0, countAfterClose.get());
    }

    @Test
    public void scenarioMultipleVusersDontInterfereWithEachOthersRegistries() {
        TestScenario scenario = scenario("Multiple VUsers").split(flow("Flow").addTestStep(annotatedMethod(this, "OpenBrowserA")), flow("Flow").addTestStep(annotatedMethod(this, "OpenBrowserC"))).build();
        TestScenarioRunner runner = runner().build();
        runner.start(scenario);
        UI.closeAllWindows();
    }

    //There is a registry per suite because the UiTestListener is loaded through TestNG's Service Loader
    @Test
    public void scenarioMultipleVusersInParallelSuitesShouldHaveRegistryPerSuite() {
        TestNG testNG = new TestNG(false);
        testNG.setSuiteThreadPoolSize(3);
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(createXmlSuite("Suite4", "Test4", UIScenarioTest.class));
        suites.add(createXmlSuite("Suite5", "Test5", UIScenarioTest.class));
        testNG.setXmlSuites(suites);
        testNG.run();
        assertNotEquals(registries.get(0), registries.get(1));
        assertEquals(14, countBeforeClose.get());
        assertEquals(0, countAfterClose.get());
    }

    @Before
    public void clearRegistryContainer() {
        UI.registryContainer.remove();
    }

    @After
    public void reset() {
        countAfterClose.getAndSet(10);
        countBeforeClose.getAndSet(10);
        registries.clear();
    }

    private void runTestNGSingleThread() {
        TestNG testNG = new TestNG(false);
        testNG.setSuiteThreadPoolSize(2);
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(createXmlSuite("Suite3", "Test3", UITest.class));
        testNG.setXmlSuites(suites);
        testNG.run();
    }

    private void runTestNGParallelSuites() {
        TestNG testNG = new TestNG(false);
        testNG.setSuiteThreadPoolSize(2);
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(createXmlSuite("Suite1", "Test1", UITest.class));
        suites.add(createXmlSuite("Suite2", "Test2", UITest.class));
        testNG.setXmlSuites(suites);
        testNG.run();
    }

    private XmlSuite createXmlSuite(String suiteName, String testName, Class clazz) {
        XmlSuite suite = new XmlSuite();
        suite.setName(suiteName);
        XmlTest test = new XmlTest(suite);
        test.setName(testName);
        List<XmlClass> classes = new ArrayList<XmlClass>();
        classes.add(new XmlClass(clazz));
        test.setXmlClasses(classes);
        return suite;
    }

    @TestStep(id = "OpenBrowserA")
    public void openBrowser() {
        Browser browser = UI.newBrowser(getBrowserType());
        multipleVusersCount.getAndAdd(UI.getActiveBrowserCount());
        registries.add(UI.registryContainer.get());
        countBeforeClose.getAndIncrement();
        UI.closeAllWindows();
        countAfterClose.getAndSet(UI.getActiveBrowserCount());
    }

    @TestStep(id = "OpenBrowserC")
    public void openBrowserC() {
        Browser browser = UI.newBrowser(getBrowserType());
        UI.pause(2000);
        assertEquals(false, browser.isClosed());
    }

    public static class UITest extends AbstractBrowserAwareITest {
        @org.testng.annotations.Test
        public void singleThreadTest() {
            openBrowserTab("first.htm");
            parallelSuitesCount.getAndAdd(UI.getActiveBrowserCount());
            registries.add(UI.registryContainer.get());
            UI.closeAllWindows();
            singleCount.getAndSet(UI.getActiveBrowserCount());
        }
    }

    public static class UIScenarioTest extends AbstractBrowserAwareITest {
        @org.testng.annotations.Test
        public void scenarioTest() {
            TestScenario scenario = scenario("Multiple VUsers")
                    .addFlow(flow("Flow")
                            .addTestStep(annotatedMethod(this, "OpenBrowserB"))
                            .afterFlow(closeWindows()))
                    .withDefaultVusers(2)
                    .build();

            TestScenarioRunner runner = runner().build();
            runner.start(scenario);
        }

        @TestStep(id = "OpenBrowserB")
        public void openBrowser() {
            Browser browser = UI.newBrowser(getBrowserType());
            if (!registries.contains(UI.registryContainer.get()))
                registries.add(UI.registryContainer.get());
            countBeforeClose.getAndIncrement();
        }

        private Runnable closeWindows() {
            return new Runnable() {
                @Override
                public void run() {
                    UI.closeAllWindows();
                    countAfterClose.getAndSet(UI.getActiveBrowserCount());
                }
            };
        }
    }
}
