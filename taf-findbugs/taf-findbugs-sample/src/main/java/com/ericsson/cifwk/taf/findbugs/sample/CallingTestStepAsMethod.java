package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestStep;

public class CallingTestStepAsMethod {

    //reported
    public void test1() {
        test2();
    }

    @TestStep(id = "Not Reported ")
    public void test2() {
    }
}
