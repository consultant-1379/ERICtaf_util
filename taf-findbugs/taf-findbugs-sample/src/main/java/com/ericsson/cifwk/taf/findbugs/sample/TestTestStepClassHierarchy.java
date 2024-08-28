package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestStep;

import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

public class TestTestStepClassHierarchy  {

    private static final String RULE = "TestStepClassHierarchy";

    private static class BasicTestSteps {
    }

    @AssertWarning(RULE)
    private static class MyTestSteps extends BasicTestSteps {

        @TestStep(id = "")
        public void testStep() {}

    }

    @AssertNoWarning(RULE)
    private static class SomeClass extends BasicTestSteps {

    }

}
