package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestStep;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import java.util.UUID;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 24.10.2016
 */
public class TestTestStepDirectCall {

    public static final String WARNING = "TestStepDirectCall";

    @AssertNoWarning(WARNING)
    private void regularDelegation() {
        regularMethod();
    }

    @AssertWarning(WARNING)
    private void scenario() {
        testStep();
    }

    @AssertWarning(WARNING)
    private void scenario2() {
        testStep(regularMethod());
    }

    private String regularMethod(){
        return UUID.randomUUID().toString();
    }

    @TestStep(id = "testStep")
    private void testStep(){}

    @TestStep(id = "testStep")
    private void testStep(String argument){}

}
