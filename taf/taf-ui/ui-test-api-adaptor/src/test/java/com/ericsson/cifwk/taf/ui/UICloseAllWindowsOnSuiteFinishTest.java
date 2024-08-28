package com.ericsson.cifwk.taf.ui;

import com.ericsson.cifwk.taf.execution.TestExecutionEvent;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.testng.TestNG;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Test covers issue CIS-20340 where browsers were closed by first finished test (method) and other tests received session closed exceptions.
 * Issue caused InheritableThreadLocal registry,
 *
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 06/01/2016
 */

public class UICloseAllWindowsOnSuiteFinishTest {

    private static final int INIT_BROWSER_COUNT_PER_TEST = 10;
    private static int EXPECTED_ACTIVE_BROWSER_COUNT_BEFORE_CLOSE;
    private static TestNG testNG;
    private UiCloseAllWindowsFailureListener failureListener;

    @Before
    public void setUp() {
        failureListener = new UiCloseAllWindowsFailureListener();
        testNG = new TestNG(false);
        testNG.addListener(failureListener);
    }

    @org.junit.Test
    public void shouldCloseAllBrowsersOnSuiteFinish_Suite_with_ParallelMethods() throws Exception {
        runSuiteWithParallel("methods");
    }

    @org.junit.Test
    public void shouldCloseAllBrowsersOnSuiteFinish_Suite_with_ParallelTests() throws Exception {
        runSuiteWithParallel("tests");
    }

    @org.junit.Test
    public void shouldCloseAllBrowsersOnSuiteFinish_Suite_with_ParallelClasses() throws Exception {
        runSuiteWithParallel("classes");
    }

    private void runSuiteWithParallel(String parallel) {
        EXPECTED_ACTIVE_BROWSER_COUNT_BEFORE_CLOSE = (1 + 1 + 10) * 3 + 10;

        List<String> suites = asList("src/test/resources/CloseAllWindowsOnSuiteFinishTestSuite.xml");
        testNG.setTestSuites(suites);
        testNG.setThreadCount(3);
        testNG.setParallel(parallel);
        testNG.run();

        Assertions.assertThat(UI.getActiveBrowserCount()).isEqualTo(0);
        failureListener.failOnException();
    }

    @org.junit.Test
    public void shouldCloseAllBrowsersOnSuiteFinish_Class_with_ParallelMethods() throws Exception {
        runClassWithParallel("methods");
    }

    @org.junit.Test
    public void shouldCloseAllBrowsersOnSuiteFinish_Class_with_ParallelTests() throws Exception {
        runClassWithParallel("tests");
    }

    @org.junit.Test
    public void shouldCloseAllBrowsersOnSuiteFinish_Class_with_ParallelClasses() throws Exception {
        runClassWithParallel("classes");
    }

    private void runClassWithParallel(String parallel) {
        EXPECTED_ACTIVE_BROWSER_COUNT_BEFORE_CLOSE = (1 + 1 + 10) * 3;

        testNG.setTestClasses(new Class[]{InitBrowserClass.class});
        testNG.setThreadCount(3);
        testNG.setParallel(parallel);
        testNG.run();

        //verify browsers are closed after testNg run
        Assertions.assertThat(UI.getActiveBrowserCount()).isEqualTo(0);
        failureListener.failOnException();
    }

    public static class InitBrowserClass {

        @BeforeSuite
        public void setupSuite() {
            UI.closeWindow(TestExecutionEvent.ON_SUITE_FINISH);
        }

        /**
         * It's a valid case when browsers are initialized in before test.
         * This is considered preparation for test. Opened browsers need to be in scope.
         */
        @BeforeMethod
        public void firstBeforeTest() {
            initNewBrowser();
        }

        @BeforeMethod
        public void secondBeforeTest() {
            initNewBrowser();
        }

        @Test
        public void firstTest() {
            initBrowsers();
        }

        @Test
        public void secondTest() {
            initBrowsers();
        }

        @Test
        public void thirdTest() {
            initBrowsers();
        }

        /**
         * Verify that after test browsers are not closed
         */
        @AfterSuite
        public void afterSuite() {
            Assertions.assertThat(UI.getActiveBrowserCount()).isEqualTo(EXPECTED_ACTIVE_BROWSER_COUNT_BEFORE_CLOSE);
        }
    }

    private static void initBrowsers() {
        for (int i = 0; i < INIT_BROWSER_COUNT_PER_TEST; i++) {
            initNewBrowser();
        }
    }

    private static void initNewBrowser() {
        UI.newBrowser(BrowserType.HEADLESS, BrowserOS.WINDOWS);
    }

    public static class InitBrowserClass2 {

        @Test
        public void firstTest() {
            initBrowsers();
        }
    }
}
