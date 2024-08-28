package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.assertions.SaveAsserts;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         22/12/2015
 */
@API(Internal)
public class SaveAssertsListener extends AbstractMethodListener {

    @Override
    public void afterInvocation(final IInvokedMethod method,
                                final ITestResult testResult) {
        if (method.isTestMethod()) {
            try {
                SaveAsserts.assertAll();
            } catch (AssertionError ae) {
                testResult.setStatus(TestResult.FAILURE);
                testResult.setThrowable(ae);
            }
        }
    }
}
