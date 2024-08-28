package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.google.common.truth.Truth;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.11.2016
 */
public class TestAssertionInOperator {

    public static final String WARNING = "AssertionInOperator";


    @Operator
    private static class MyOperator {

        @AssertWarning(WARNING)
        public void assertJUsed() {
            Assertions.assertThat("string").contains("str");
        }

        @AssertWarning(WARNING)
        public void googleTruthUsed() {
            Truth.assertThat("string").contains("str");
        }

        @AssertWarning(WARNING)
        public void hamcrestUsed() {
            MatcherAssert.assertThat("string", Matchers.containsString("str"));
        }

    }

    private static class MyTestSteps {

        @TestStep(id = "")
        @AssertNoWarning(WARNING)
        public void assertJUsed() {
            Assertions.assertThat("string").contains("str");
        }

    }

}
