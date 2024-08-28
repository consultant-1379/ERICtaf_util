package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.execution.TestNGAnnotationTransformer;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.testng.*;
import org.testng.annotations.*;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AnnotationsTest {

    private static Queue<String> sequence;

    @Test
    public void shouldRun() {
        sequence = new LinkedList<>();
        runTestNg(1);

        assertEquals(7, sequence.size());
        assertArrayEquals(new String[]{
                "BeforeSuite",
                "BeforeClass",
                "BeforeMethod",
                "Test",
                "AfterMethod",
                "AfterClass",
                "AfterSuite"
        }, sequence.toArray());
    }

    @Test
    public void shouldRunConcurrently() {
        sequence = new ConcurrentLinkedQueue<>();
        runTestNg(3);

        assertEquals(17, sequence.size());
        assertEquals(1, Iterables.frequency(sequence, "BeforeSuite"));
        assertEquals(3, Iterables.frequency(sequence, "BeforeClass"));
        assertEquals(3, Iterables.frequency(sequence, "BeforeMethod"));
        assertEquals(3, Iterables.frequency(sequence, "Test"));
        assertEquals(3, Iterables.frequency(sequence, "AfterMethod"));
        assertEquals(3, Iterables.frequency(sequence, "AfterClass"));
        assertEquals(1, Iterables.frequency(sequence, "AfterSuite"));
        assertEquals("BeforeSuite", sequence.poll());
        assertEquals("BeforeClass", sequence.poll());
    }

    private void runTestNg(int vusers) {
        TestNGWrapper testNG = new TestNGWrapper();
        testNG.setTestRunnerFactory(new VUsersTestRunnerFactory(vusers));
        testNG.setTestClasses(new Class[]{TestNGTest.class});
        testNG.setAnnotationTransformer(new TestNGAnnotationTransformer());
        testNG.run();
    }

    public static class TestNGTest {

        @BeforeSuite
        public void beforeSuite() {
            sequence.add("BeforeSuite");
        }

        @BeforeClass
        public void beforeClass() {
            sequence.add("BeforeClass");
        }

        @BeforeMethod
        public void beforeMethod() {
            sequence.add("BeforeMethod");
        }

        @org.testng.annotations.Test
        public void test() {
            sequence.add("Test");
        }

        @AfterMethod
        public void afterMethod() {
            sequence.add("AfterMethod");
        }

        @AfterClass
        public void afterClass() {
            sequence.add("AfterClass");
        }

        @AfterSuite
        public void afterSuite() {
            sequence.add("AfterSuite");
        }

    }

    private static class TestNGWrapper extends TestNG {
        public TestNGWrapper() {
            super(false);
        }

        @Override
        protected void setTestRunnerFactory(ITestRunnerFactory itrf) {
            super.setTestRunnerFactory(itrf);
        }
    }

    private static class VUsersTestRunnerFactory extends TafTestRunnerFactory {
        private final int vusers;

        private VUsersTestRunnerFactory(int vusers) {
            this.vusers = vusers;
        }

        @Override
        public TestRunner newTestRunner(ISuite iSuite, XmlTest xmlTest, Collection<IInvokedMethodListener> iInvokedMethodListeners, List<IClassListener> classListeners) {
            TafTestRunner runner = (TafTestRunner) super.newTestRunner(iSuite, xmlTest, iInvokedMethodListeners, classListeners);
            runner.setSuiteReader(new VUsersSuiteReader(iSuite, vusers));
            return runner;
        }
    }

    private static class VUsersSuiteReader extends SuiteReader {
        private final int vusers;

        public VUsersSuiteReader(ISuite suite, int vusers) {
            super(suite);
            this.vusers = vusers;
        }

        @Override
        public int getVusers() {
            return vusers;
        }
    }

}
