/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.assertions;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CustomTafAsserts {

    @Test
    public void checkIntegerAssertEquals() {
        int x = 7;
        int y = 7;
        TafHamcrestAsserts.assertEquals(x, y);
    }

    @Test
    public void checkIntegerAssertNotEquals() {
        int x = 7;
        int y = 12;
        TafHamcrestAsserts.assertNotEquals(x, y);
    }

    @Test
    public void checkDoubleAssertEquals() {
        double x = 7.654;
        double y = 7.654;
        TafHamcrestAsserts.assertEquals(x, y);
    }

    @Test
    public void checkDoubleAssertNotEquals() {
        double x = 2.344;
        double y = 2.433;
        TafHamcrestAsserts.assertNotEquals(x, y);
    }

    @Test
    public void checkStringAssertEquals() {
        String a = "java";
        String b = "java";
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkStringAssertNotEquals() {
        String a = "java";
        String b = "python";
        TafHamcrestAsserts.assertNotEquals(a, b);
    }

    @Test
    public void checkCharAssertEquals() {
        char a = 'p';
        char b = 'p';
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkCharAssertNotEquals() {
        char a = 'p';
        char b = 'q';
        TafHamcrestAsserts.assertNotEquals(a, b);
    }

    @Test
    public void checkByteAssertEquals() {
        byte a = -50;
        byte b = -50;
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkByteAssertNotEquals() {
        byte a = -50;
        byte b = 50;
        TafHamcrestAsserts.assertNotEquals(a, b);
    }

    @Test
    public void checkShortAssertEquals() {
        short a = 1;
        short b = 1;
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkShortAssertNotEquals() {
        short a = 1;
        short b = 245;
        TafHamcrestAsserts.assertNotEquals(a, b);
    }

    @Test
    public void checkLongAssertEquals() {
        long a = 2_345_778L;
        long b = 2_345_778L;
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkLongAssertNotEquals() {
        long a = 2_345_778L;
        long b = 1_254_589L;
        TafHamcrestAsserts.assertNotEquals(a, b);
    }

    @Test
    public void checkCollectionAssertEquals() {
        List<String> a = Arrays.asList("e", "h", "c");
        List<String> b = Arrays.asList("e", "h", "c");
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkCollectionInAnyOrderAssertEquals() {
        List<String> a = Arrays.asList("c", "e", "h");
        List<String> b = Arrays.asList("e", "h", "c");
        TafHamcrestAsserts.assertEqualsOrderNotImportant(a, b);
    }

    @Test
    public void checkMapAssertEquals() {
        Map<String, String> a = new HashMap<>();
        Map<String, String> b = new HashMap<>();
        a.put("a", "1");
        a.put("b", "2");
        a.put("c", "3");
        b.put("a", "1");
        b.put("b", "2");
        b.put("c", "3");
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkSetAssertEquals() {
        Set<Integer> a = new TreeSet<>();
        a.add(15);
        a.add(10);
        a.add(11);
        Set<Integer> b = new TreeSet<>();
        b.add(15);
        b.add(10);
        b.add(11);
        TafHamcrestAsserts.assertEquals(a, b);
    }

    @Test
    public void checkByteArrayAssertEqualsInOrder() {
        byte[] a = new byte[] { 10, 22, 15, -5, -24 };
        byte[] b = new byte[] { 10, 22, 15, -5, -24 };
        TafHamcrestAsserts.assertEqualsInOrder(a, b);
    }

    @Test
    public void checkBooleanAssertEquals() {
        boolean a = false;
        boolean b = false;
        boolean c = true;
        boolean d = true;
        TafHamcrestAsserts.assertEquals(a, b);
        TafHamcrestAsserts.assertEquals(c, d);
    }

    @Test
    public void checkBooleanAssertNotEquals() {
        boolean a = false;
        boolean b = false;
        boolean c = true;
        boolean d = true;
        TafHamcrestAsserts.assertNotEquals(a, d);
        TafHamcrestAsserts.assertNotEquals(b, c);
    }

    @Test
    public void checkArrayAssertEqualsInAnyOrder() {
        Object[] a = new Object[] { 5, 78, 36 };
        Object[] b = new Object[] { 78, 36, 5 };
        TafHamcrestAsserts.assertEqualsNoOrder(a, b);
    }

    @Test
    public void checkArrayAssertEqualsInOrder() {
        Object[] a = new Object[] { 5, 78, 36 };
        Object[] b = new Object[] { 5, 78, 36 };
        TafHamcrestAsserts.assertEqualsInOrder(a, b);
    }

}
