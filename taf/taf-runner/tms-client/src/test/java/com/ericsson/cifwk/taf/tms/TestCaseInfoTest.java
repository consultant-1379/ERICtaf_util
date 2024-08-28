/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.tms;

import com.ericsson.cifwk.taf.tms.dto.TestCaseInfo;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestCaseInfoTest {

    private TestCaseInfo testCase;

    @Before
    public void setUp() {
        testCase = new TestCaseInfo();
    }

    @Test
    public void collectionsAreNeverNull() {
        assertNotNull(testCase.getRequirements());
    }

}
