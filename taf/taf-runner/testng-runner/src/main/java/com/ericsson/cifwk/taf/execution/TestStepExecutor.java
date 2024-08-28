/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.execution;

import java.util.List;

public interface TestStepExecutor {
    
    /**
     * Execute a single Test Step as a test case with data
     * @param instance - An instance of the class the test step is in.
     * @param testStep - The name of the testStep in the TestStep annotation
     * @param args - the variables from the data source to pass to the test step
     */
    public void runAsTestCase(Object instance, String testStep, Object... args);
    
    /**
     * Execute a series of Test Steps from a single Test Step class as a test case
     * @param instance - An instance of the class the test step is in.
     * @param teststeps - a list of names of teststeps in Test Step annotations
     * @param args - the variables from the data source to pass to the test step
     */
    public void runAsTestCase(Object instance, List<String> testSteps, Object... args);

}
