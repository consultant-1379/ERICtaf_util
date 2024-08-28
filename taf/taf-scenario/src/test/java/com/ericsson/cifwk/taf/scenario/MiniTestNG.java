/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import com.google.common.base.Throwables;
import org.testng.ITestResult;
import org.testng.TestNG;

public class MiniTestNG {
    public static void runTest(Class... clazz) throws Exception {
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(clazz);
        testNG.addListener(new FailureListener());
        testNG.run();
    }

    public static class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            Throwables.propagate(result.getThrowable());
        }
    }
}