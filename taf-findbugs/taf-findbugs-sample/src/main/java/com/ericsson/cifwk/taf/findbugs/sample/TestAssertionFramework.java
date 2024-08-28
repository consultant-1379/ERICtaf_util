package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.assertions.SaveAsserts;
import com.ericsson.cifwk.taf.assertions.TafAsserts;
import com.google.common.truth.Truth;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 31.10.2016
 */
public class TestAssertionFramework {

    private static final String WARNING = "AssertionFramework";

    @AssertNoWarning(WARNING)
    private void assertJUsed() {
        Assertions.assertThat("string").contains("str");
    }

    @AssertNoWarning(WARNING)
    private void googleTruthUsed() {
        Truth.assertThat("string").contains("str");
    }

    @AssertNoWarning(WARNING)
    private void hamcrestUsed() {
        MatcherAssert.assertThat("string", Matchers.containsString("str"));
    }

    @AssertWarning(WARNING)
    private void tafAssert1Used() {
        TafAsserts.assertEquals("expected", "actual");
    }

    @AssertWarning(WARNING)
    private void tafAssert2Used() {
        SaveAsserts.assertEquals("type", "message", "expected", "actual");
    }

    @AssertWarning(WARNING)
    private void testNgAssertUsed() {
        org.testng.Assert.assertTrue("string".contains("str"));
    }

    @AssertWarning(WARNING)
    private void jUnitAssertUsed() {
        org.junit.Assert.assertTrue("string".contains("str"));
    }

}
