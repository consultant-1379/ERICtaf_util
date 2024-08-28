package com.ericsson.cifwk.taf.testng;

import com.google.common.base.Throwables;
import org.testng.ITestResult;
import org.testng.TestNG;

public class MiniTestNG {

    public static void runTestNg(Class<?> testClass) {
        TestNGWrapper testNG = new TestNGWrapper();
        testNG.setTestClasses(new Class[]{testClass});
        testNG.addListener(new FailureListener());
        testNG.run();
    }

    public static class TestNGWrapper extends TestNG {
        public TestNGWrapper() {
            super(false);
        }
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            Throwables.propagate(result.getThrowable());
        }
    }

}
