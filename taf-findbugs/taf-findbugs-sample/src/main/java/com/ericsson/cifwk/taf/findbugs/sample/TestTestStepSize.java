package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestStep;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import java.util.Date;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 27.10.2016
 */
public class TestTestStepSize {

    public static final String WARNING = "TestStepSize";

    @AssertNoWarning(WARNING)
    public void tooSmallMethod() {
        Date date = new Date(System.currentTimeMillis());
    }

    @TestStep(id = "")
    @AssertWarning(WARNING)
    public void tooSmallTestStep() {
        Date date = new Date(System.currentTimeMillis());
    }

    @TestStep(id = "")
    @AssertNoWarning(WARNING)
    public void smallTestStep() {
        new Date();
        System.currentTimeMillis();
    }

    @TestStep(id = "")
    @AssertNoWarning(WARNING)
    public void bigTestStep() {

        // 20 lines of code
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
    }

    @TestStep(id = "")
    @AssertWarning(WARNING)
    public void tooBigTestStep() {

        // 21 lines of code
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
        System.currentTimeMillis();
    }

    @AssertNoWarning(WARNING)
    public void tooBigMethod() {

        // 21 lines of code
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
        new Date(System.currentTimeMillis());
    }

}
