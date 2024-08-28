package com.ericsson.cifwk.taf.testapi;


import com.ericsson.cifwk.meta.API;
import org.testng.ISuite;
import org.testng.ITestContext;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 09/07/2016
 */
@API(Internal)
public class TestExecutionContextImpl implements TestExecutionContext {

    private final TestSuite testSuite;

    public TestExecutionContextImpl(ITestContext testContext) {
        ISuite suite = testContext.getSuite();
        this.testSuite = new TestSuite(suite);
    }

    @Override
    public TestGroup getTestGroup() {
        return testSuite;
    }

    public String getTestGroupId() {
        return testSuite.getId();
    }
}
