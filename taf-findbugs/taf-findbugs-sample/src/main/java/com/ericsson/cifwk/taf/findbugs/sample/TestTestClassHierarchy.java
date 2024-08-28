package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.TafTestBase;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;
import org.testng.annotations.Test;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 26.10.2016
 */
public class TestTestClassHierarchy {

    public static final String WARNING = "TestClassHierarchy";

    private static class BasicClass {}

    @Test
    @AssertWarning(WARNING)
    private static class ExtendingTest extends BasicClass {
    }

    @AssertWarning(WARNING)
    private static class ExtendingTest2 extends BasicClass {
        @Test
        public void test() {

        }
    }

    @AssertNoWarning(WARNING)
    private static class RegularClass extends BasicClass {
    }

    @Test
    @AssertNoWarning(WARNING)
    private static class RegularTestNgTest {
    }

    @AssertNoWarning(WARNING)
    private static class TafTestNgTest extends TafTestBase {
        @Test
        public void test() {
        }
    }


}
