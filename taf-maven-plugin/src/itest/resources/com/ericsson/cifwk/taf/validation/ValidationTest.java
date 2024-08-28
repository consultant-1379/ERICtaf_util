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
package com.ericsson.cifwk.taf.validation;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.annotations.TestId;

public class ValidationTest {

    @Test
    public void shouldFail(){
        System.out.println("Should Fail"); 
    }

    @TestId(id="1",title="asdas")
    @Test
    public void shouldNotFail(){
        System.out.println("Should Pass"); 
    }

    @Test
    public void shouldPassUsingSetMehod(){
        System.out.println("Should Pass calls setTestCase");
        setTestCase("asdf","asdfsdf");
    }

    public void setTestCase(String id, String name){

    }
    public void setTestcase(String id, String name){

    }



}
