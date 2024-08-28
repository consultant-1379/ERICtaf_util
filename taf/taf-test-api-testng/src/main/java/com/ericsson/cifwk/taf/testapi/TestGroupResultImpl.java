package com.ericsson.cifwk.taf.testapi;

import org.testng.ISuiteResult;
import org.testng.ITestContext;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 08/12/2016
 */
public class TestGroupResultImpl implements TestGroupResult {

    private final ITestContext testContext;

    public TestGroupResultImpl(ISuiteResult suiteResult) {
        this.testContext = suiteResult.getTestContext();
    }

    @Override
    public final int getFailedTestCount() {
        return testContext.getFailedTests().size();
    }

    @Override
    public boolean isSuccess() {
        return getFailedTestCount() == 0;
    }
}
