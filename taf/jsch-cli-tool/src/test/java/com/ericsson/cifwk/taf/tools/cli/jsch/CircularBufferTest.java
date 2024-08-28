package com.ericsson.cifwk.taf.tools.cli.jsch;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CircularBufferTest {

    @Test
    public void testAdd() {
        CircularBuffer<String> stringCircularBuffer = new CircularBuffer<>(2);
        stringCircularBuffer.add("Element1");
        stringCircularBuffer.add("Element2");
        stringCircularBuffer.add("Element3");
        stringCircularBuffer.add("Element4");

        assertEquals(stringCircularBuffer.toString(), "[Element4, Element3]");
    }
}
