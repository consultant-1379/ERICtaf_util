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

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;

import static org.testng.Assert.*;

public class TestSteps {

    @TestStep(id="step1")
    public void testStep(){
        assertTrue(true);
    }
    
    @TestStep(id="step2")
    public void testStepWithData(@Input("text") String input, @Output("int") int output){
        assertEquals("This is text", input);
        assertEquals(0, output);
    }
}
