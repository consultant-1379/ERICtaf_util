package com.ericsson.cifwk;

import com.ericsson.cifwk.taf.annotations.TestId;
import org.testng.annotations.Test;

public class SampleTest2 {

    @Test
    @TestId(id = "OSS-33200_Func_2", title = "Testing TE integration with TMS")
    public void shouldPass() {
        System.out.println("Running a successful TAF test.");
    }

}
