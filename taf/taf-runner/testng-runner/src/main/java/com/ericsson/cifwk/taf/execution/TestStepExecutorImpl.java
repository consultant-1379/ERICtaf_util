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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStepExecutorImpl implements TestStepExecutor{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TestStepExecutorImpl.class);
    
    /* (non-Javadoc)
     * @see com.ericsson.cifwk.taf.execution.TestStepExecutor#runAsTestCase(java.lang.String, java.lang.Object)
     */
    @Override
    public void runAsTestCase(Object instance, String testStep, Object... args){
        Method testStepMethod = AnnotatedMethodFinder.findTestStepAnnotatedMethod(instance, testStep);
        
        try {
            testStepMethod.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.error("Exception {} thrown while executing {}", e, testStepMethod);
        }

    }

    /* (non-Javadoc)
     * @see com.ericsson.cifwk.taf.execution.TestStepExecutor#runAsTestCase(java.lang.String, java.lang.Object[])
     */
    @Override
    public void runAsTestCase(Object instance, List<String> testSteps, Object... args) {
        throw new UnsupportedOperationException();        
    }
}
