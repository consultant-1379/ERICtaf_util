package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestStep;

import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

public class TestTestStepParameters {

    private static final String RULE = "TestStepParameters";

    @AssertWarning(RULE)
    @TestStep(id = "")
    public void myTestStep(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) {

    }

    @AssertNoWarning(RULE)
    @TestStep(id = "")
    public void someTestStep(String var1, String var2, String var3, String var4, String var5, String var6, String var7) {

    }

    @AssertNoWarning(RULE)
    public void methodMultipleParams(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8) {

    }
}
