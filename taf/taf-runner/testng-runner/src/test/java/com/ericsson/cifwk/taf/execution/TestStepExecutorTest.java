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


import org.junit.Test;

public class TestStepExecutorTest {
    
    private TestSteps steps = new TestSteps();
    private TestStepExecutor executor = new TestStepExecutorImpl();

    @Test
    public void executeSingleTestStep(){
        executor.runAsTestCase(steps, "step1");
    }
    
    @Test
    public void executeSingleTestStepWithData(){
        executor.runAsTestCase(steps, "step2", "This is text", 0);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void executeNonExistentTestStep(){
        executor.runAsTestCase(steps, "step0");
    }
}
