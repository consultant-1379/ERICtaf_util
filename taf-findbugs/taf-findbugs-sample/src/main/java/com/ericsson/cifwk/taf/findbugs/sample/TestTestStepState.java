package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestStep;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 21.10.2016
 */
public class TestTestStepState {

    private static final String RULE = "TestStepState";

    @AssertNoWarning(RULE)
    private static class NotTestSteps {

        private int state;

    }

    @AssertWarning(RULE)
    private static class StatefulTestSteps {

        private int state;

        @TestStep(id = "")
        public void testStep() {}

    }

    @AssertWarning(RULE)
    private static class StatefulTestStepsReordered {

        @TestStep(id = "")
        public void testStep() {}

        private int state;

    }

    @AssertNoWarning(RULE)
    private static class StatelessTestSteps {

        @javax.inject.Inject
        private int state;

        private final int CONST = 0;

        @TestStep(id = "")
        public void testStep() {}

    }

}
