package com.ericsson.cifwk.taf;

import org.junit.Test;

import static com.ericsson.cifwk.taf.ParametersManager.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 6/15/2015
 */
public class ParametersManagerTest {

    @Test
    public void testTruncate() {
        assertEquals("", truncate("", 4));
        assertEquals("a", truncate("a", 4));
        assertEquals("ab", truncate("ab", 4));
        assertEquals("abc", truncate("abc", 4));
        assertEquals("abcd", truncate("abcd", 4));
        assertEquals("a...", truncate("abcdf", 4));
        assertEquals("ab...", truncate("abcdfg", 5));
        assertEquals("abc...", truncate("abcdfgh", 6));
    }

    @Test
    public void testRemoveEmptyParameters() {

        // shouldn't remove
        assertEquals("testName", removeEmptyParameters("testName"));
        assertEquals("testName[null]", removeEmptyParameters("testName[null]"));
        assertEquals("testName[a,]", removeEmptyParameters("testName[a,]"));
        assertEquals("testName[,b]", removeEmptyParameters("testName[,b]"));
        assertEquals("testName[a,b]", removeEmptyParameters("testName[a,b]"));

        // should remove
        assertEquals("testName", removeEmptyParameters("testName "));
        assertEquals("testName", removeEmptyParameters("testName[]"));
        assertEquals("testName", removeEmptyParameters("testName []"));
        assertEquals("testName", removeEmptyParameters("testName [ ] "));
        assertEquals("testName", removeEmptyParameters("testName[[]]"));
        assertEquals("testName", removeEmptyParameters("testName[,]"));
        assertEquals("testName", removeEmptyParameters("testName[,,]"));
        assertEquals("testName", removeEmptyParameters("testName[ , , ]"));

        // edge cases
        assertEquals("testName[withBracket", removeEmptyParameters("testName[withBracket[,]"));
        assertEquals("bracketAsParameter[a,[,b]", removeEmptyParameters("bracketAsParameter[a,[,b]"));
        assertEquals("bracketAsParameter[a,", removeEmptyParameters("bracketAsParameter[a,[,]"));

        // opening nested brackets
        assertEquals("testName[parameter]", removeEmptyParameters("testName[[parameter]]"));
    }

    @Test
    public void testCaseIdShouldBeExcluded() {
        assertEquals("testCaseId[regularParameter]", getParametrizedName("testCaseId", new Object[]{"testCaseId", "regularParameter"}));
    }

    @Test
    public void testCaseTitleShouldBeTrimmed() {
        assertEquals("testName[regularParameter]", getParametrizedName(" testName ", new Object[]{"regularParameter"}));
    }

}
