package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.TestExecutionHelper;
import com.ericsson.cifwk.taf.annotations.VUsers;
import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import org.junit.Before;
import org.junit.Test;
import org.testng.ITestResult;
import org.testng.TestNG;

public class ConcurrencyTest {

    private static boolean failed;

    @Before
    public void setUp() {
        failed = false;
    }

    @Test
    public void shouldRunParallel() throws InterruptedException {
        runTestNg(true);
        if (failed) org.junit.Assert.fail();
    }

    @Test
    public void shouldRunSequential() {
        runTestNg(false);
        if (failed) org.junit.Assert.fail();
    }

    private void runTestNg(boolean parallel) {
        TestNG testNG = new TestNG(false);
        if (parallel) {
            testNG.setThreadCount(3);
            testNG.setParallel("true");
        }
        testNG.setTestClasses(new Class[]{ParallelTest.class});
        testNG.setAnnotationTransformer(new TestNGAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.run();
    }


    @org.testng.annotations.Test(enabled = false, groups = {"mock"})
    public static class ParallelTest {

        @VUsers(vusers = {2, 4, 8})
        @org.testng.annotations.Test
        public void a() {
            if (TestExecutionHelper.getCurrentVUsers() % 2 != 0) failed = true;
        }

        @VUsers(vusers = {3, 9, 21})
        @org.testng.annotations.Test
        public void b() {
            if (TestExecutionHelper.getCurrentVUsers() % 3 != 0) failed = true;
        }

        @VUsers(vusers = {5, 25, 125})
        @org.testng.annotations.Test
        public void c() {
            if (TestExecutionHelper.getCurrentVUsers() % 5 != 0) failed = true;
        }
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            failed = true;
        }
    }

}
