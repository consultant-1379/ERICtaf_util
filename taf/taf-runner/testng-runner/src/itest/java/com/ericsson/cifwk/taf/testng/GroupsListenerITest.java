package com.ericsson.cifwk.taf.testng;

import org.junit.After;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.*;

public class GroupsListenerITest {

    private static Queue<String> sequence = new LinkedList<>();

    private String groups;

    @After
    public void setUp() {
        sequence.clear();
    }

    private void setGroups(String value) {
        CmdLineGroupsHolder.setGroups(value);
    }

    @Test
    public void shouldRunPerformanceTests() {
        setGroups("Performance");
        MiniTestNG.runTestNg(TestNGTestGroups.class);

        assertEquals(2, sequence.size());
        assertArrayEquals(new String[] { "Performance Test 1", "Performance Test 3" }, sequence.toArray());
    }

    @Test
    public void shouldRunAcceptanceTests() {
        setGroups("Acceptance");
        MiniTestNG.runTestNg(TestNGTestGroups.class);

        assertEquals(2, sequence.size());
        assertArrayEquals(new String[] { "Acceptance, Regression Test 2", "Acceptance, Functional Test 5" }, sequence.toArray());
        assertEquals("Acceptance, Regression Test 2", sequence.poll());
    }

    @Test
    public void shouldRunAcceptanceOrFunctionalTests() {
        setGroups("Functional,Acceptance");
        MiniTestNG.runTestNg(TestNGTestGroups.class);

        assertEquals(2, sequence.size());
        assertArrayEquals(new String[] { "Acceptance, Regression Test 2", "Acceptance, Functional Test 5" }, sequence.toArray());
    }

    @Test
    public void shouldRunAcceptanceAndFunctionalTests() {
        setGroups("Functional AND Acceptance");

        MiniTestNG.runTestNg(TestNGTestGroups.class);

        assertEquals(1, sequence.size());
        assertArrayEquals(new String[] { "Acceptance, Functional Test 5" }, sequence.toArray());
    }

    @Test
    public void shouldRunAcceptanceAndFunctionalOrRegressionTests() {
        setGroups("Functional AND Acceptance, Regression");

        MiniTestNG.runTestNg(TestNGTestGroups.class);

        assertEquals(3, sequence.size());
        assertArrayEquals(new String[] { "Acceptance, Regression Test 2", "Regression Test 4", "Acceptance, Functional Test 5" }, sequence.toArray());
    }

    public static class TestNGTestGroups {

        @org.testng.annotations.Test(groups = { "Performance" })
        public void testA() {
            sequence.add("Performance Test 1");
        }

        @org.testng.annotations.Test(groups = { "Acceptance", "Regression" })
        public void testB() {
            sequence.add("Acceptance, Regression Test 2");
        }

        @org.testng.annotations.Test(groups = { "Performance" })
        public void testC() {
            sequence.add("Performance Test 3");
        }

        @org.testng.annotations.Test(groups = { "Regression" })
        public void testD() {
            sequence.add("Regression Test 4");
        }

        @org.testng.annotations.Test(groups = { "Acceptance", "Functional" })
        public void testE() {
            sequence.add("Acceptance, Functional Test 5");
        }

    }

}
