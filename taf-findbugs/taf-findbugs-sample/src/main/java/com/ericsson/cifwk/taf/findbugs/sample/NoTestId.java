package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestSuite;

import org.testng.annotations.Test;

public class NoTestId {

    @Test(dependsOnGroups = {"test2"})
    @TestId(id = "1")
    public void test1() {
    }

    @Test
    @TestId
    public void test2() {
    }

    @Test
    public void test3() {
    }

    @Test
    @TestId(id = " ")
    public void test4() {
    }

    @Test
    @TestSuite
    public void test5(){
    }

}
