package com.ericsson.cifwk;

import com.ericsson.cifwk.taf.annotations.TestId;
import org.junit.Test;

public class SimpleTest {

    @Test
    @TestId(id = "SimpleTest")
    public void simpleTest() {
        System.out.println("Running a successful TAF test.");
    }

}
