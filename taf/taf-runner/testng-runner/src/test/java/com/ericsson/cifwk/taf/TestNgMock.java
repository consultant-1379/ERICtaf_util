package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.google.common.base.Throwables;
import org.testng.IAnnotationTransformer;
import org.testng.ITestResult;
import org.testng.TestNG;

/**
 *
 */
public class TestNgMock {

    public static void runTestNg(Class<?> testClass,
                                 IAnnotationTransformer transformer,
                                 Object... listeners) {
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(new Class[]{testClass});
        if (transformer != null) {
            testNG.setAnnotationTransformer(transformer);
        }
        for (Object listener : listeners) {
            testNG.addListener(listener);
        }
        testNG.run();
    }

    public static class FailureListener extends AbstractTestListener {

        private boolean failed;
        public Throwable failureCause;

        @Override
        public void onTestFailure(ITestResult result) {
            this.failed = true;
            failureCause = result.getThrowable();
            Throwables.propagate(result.getThrowable());
        }

        public boolean isFailed() {
            return failed;
        }
    }

}
