package com.ericsson.cifwk.taf.testapi.events;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestExecutionContext;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Sent when test configuration method (e.g., @BeforeTest, @BeforeSuite, etc.) finishes or gets skipped.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 06/09/2016
 */
@API(Internal)
public class TestConfigurationEvent extends AbstractTestMethodEvent {

    public TestConfigurationEvent(TestMethodExecutionResult testMethodExecutionResult, TestExecutionContext executionContext) {
        super(testMethodExecutionResult, executionContext);
    }

}
