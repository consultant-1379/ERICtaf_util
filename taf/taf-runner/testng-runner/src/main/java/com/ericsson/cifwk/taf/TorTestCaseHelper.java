package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.assertions.TafAsserts;
import com.ericsson.cifwk.taf.execution.TestStepExecutor;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import org.testng.annotations.Guice;

import javax.inject.Inject;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

/**
 *  This class is a helper to create a test case for TOR Project
 */
@Deprecated
@API(Deprecated)
@API.Since(2.32)
@Guice(moduleFactory = OperatorLookupModuleFactory.class)
public abstract class TorTestCaseHelper extends TafAsserts {

    private static final String UNKNOWN = "UNKNOWN";

    @Inject
    TestStepExecutor testStepExecutor;

    /**
     * This method is a wrapper for setting test case on Test Case level
     */
    @Override
    public void setTestcase(String testCaseId, String testCaseDescription){
        setTestCase(testCaseId, testCaseDescription);
    }

    /**
     * This method is a wrapper for setting test case on Test Case level
     */
    public void setTestCase(String testCaseId, String testCaseDescription){ // NOSONAR
        super.setTestcase(testCaseId, testCaseDescription+(String.format("\nRunning with VUSERS: %s", TestExecutionHelper.getCurrentVUsers())));
    }

    /**
     * Method to retrieve the test id from a test method
     * Test method must have @TestId annotation with "id" parameter
     * @return test id as a String
     */
    public static String getTestId(){
        return UNKNOWN;
    }
    
    /**
     * Convenience method to execute a test step as a test case
     * This method must be called in a test method which has matching data provided.
     */
    public void runAsTestCase(Object instance, String testStep){
        testStepExecutor.runAsTestCase(instance, testStep);
    }
}
