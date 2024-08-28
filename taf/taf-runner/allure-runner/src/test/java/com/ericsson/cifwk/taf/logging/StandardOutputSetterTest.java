/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.logging;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertSame;

public class StandardOutputSetterTest {

    private PrintStream standardOutput;

    private PrintStream standardError;

    @Before
    public void setUp() {
        standardOutput = System.out;
        standardError = System.err;
    }

    @After
    public void tearDown() {
        System.setOut(standardOutput);
        System.setErr(standardError);
    }

//    @Test
//    public void set() {
//        assertSame(standardOutput, System.out);
//        assertSame(standardError, System.err);
//
//        StandardOutputSetter.set();
//        assertNotSame(standardOutput, System.out);
//        assertNotSame(standardError, System.err);
//
//        StandardOutputSetter.reset();
//        assertSame(standardOutput, System.out);
//        assertSame(standardError, System.err);
//    }

    @Test
    public void resetNotSet() {
//        StandardOutputSetter.reset();
        assertSame(standardOutput, System.out);
        assertSame(standardError, System.err);
    }

//    @Test
//    public void thirdPartySetterFriendly() {
//
//        // simulating third party
//        PrintStream legalOut = new PrintStream(new NullOutputStream());
//        PrintStream legalErr = new PrintStream(new NullOutputStream());
//        System.setOut(legalOut);
//        System.setErr(legalErr);
//
//        // our setter lifecycle
//        StandardOutputSetter.set();
//        StandardOutputSetter.reset();
//
//        // checking that we set back legal streams
//        assertSame(legalOut, System.out);
//        assertSame(legalErr, System.err);
//    }

}
