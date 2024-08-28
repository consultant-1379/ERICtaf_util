package com.ericsson.cifwk;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import org.testng.annotations.Test;

public class SampleTest {

    @Test
    @TestId(id = "OSS-33200_Func_3", title = "Testing TE integration with TMS")
    public void shouldPass() {
        System.out.println("Running a successful TAF test.");
        testStep1();
        testStep2();
    }

    @TestStep(id = "DURACI-2449tc")
    public void testStep1() {
        System.out.println("Inside test step 1.");
        nestedTestStep(1);
    }

    @TestStep(id = "TORF-1107_Func_2")
    public void testStep2() {
        System.out.println("Inside test step 2.");
        nestedTestStep(2);
    }

    @TestStep(id = "NTS '{0}'", description = "nestedTestStep({0})")
    public void nestedTestStep(int arg) {
        System.out.println("Inside nested test step " + arg);
    }


}
