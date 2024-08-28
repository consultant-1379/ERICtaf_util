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

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class PrintStreamSplitterTest {

    private ByteArrayOutputStream streamResult1;

    private ByteArrayOutputStream streamResult2;

    private PrintStream stream1;

    private PrintStream stream2;

    private PrintStreamSplitter splitter;

    @Before
    public void setUp() {
        streamResult1 = new ByteArrayOutputStream();
        streamResult2 = new ByteArrayOutputStream();
        stream1 = new PrintStream(streamResult1);
        stream2 = new PrintStream(streamResult2);
        splitter = new PrintStreamSplitter(stream1, stream2);
    }

    @Test
    public void write() throws IOException {
        splitter.write("string".getBytes());
        assertEquals("string", streamResult1.toString());
        assertEquals("string", streamResult2.toString());
    }

}
