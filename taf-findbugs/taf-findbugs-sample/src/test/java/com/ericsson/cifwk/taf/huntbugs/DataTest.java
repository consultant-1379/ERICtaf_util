package com.ericsson.cifwk.taf.huntbugs;

import one.util.huntbugs.spi.DataTests;
import org.junit.Test;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 12.09.2016
 */
public class DataTest {

    @Test
    public void testDetectors() throws Exception {
        DataTests.test("com/ericsson/cifwk/taf/findbugs/sample");
    }

    @Test
    public void testUiDetectors() throws Exception {
        DataTests.test("com/ericsson/cifwk/taf/findbugs/sample/ui");
    }

    @Test
    public void testPackagingByTechnicalComponent() throws Exception {
        DataTests.test("com/ericsson/cifwk/taf/findbugs/sample/flows/fm");
        DataTests.test("com/ericsson/cifwk/taf/findbugs/sample/fm/flows");
    }

}
