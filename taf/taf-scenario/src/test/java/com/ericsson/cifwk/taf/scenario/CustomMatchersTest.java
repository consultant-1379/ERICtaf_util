/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class CustomMatchersTest {
    @Test
    public void testContains() throws Exception {
        assertThat(Arrays.asList(1, 2, 3), contains(1, 2, 3));
        assertThat(Arrays.asList(1, null, 3), contains(1, null, 3));
        assertThat(Arrays.asList(1, 2, 3), not(contains(1, 3, 1)));
        assertThat(Arrays.asList(1, 2, 3), not(contains(1, 2, 3, 4)));
        assertThat(Arrays.asList(1, 2, 3, 4), not(contains(1, 2, 3)));
        assertThat(null, not(contains(1, 2, 3)));
        assertThat(new ArrayList<Integer>(), not(contains(1, 2, 3)));

        try {
            assertThat(Arrays.asList(1, 3, 1), contains(1, 2, 3));
            fail("Expected exception");
        } catch (java.lang.AssertionError e) {
            assertThat(e.getMessage(), equalTo("\n" +
                    "Expected: [1, 2, 3]\n" +
                    "     but: [1, 3, 1]"));
        }
    }

}