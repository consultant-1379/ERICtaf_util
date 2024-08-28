package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.Description;

import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 31.01.2017
 */
public class TafJUnitAnnotationManagerTest {

    private Description description;

    @Before
    public void setUp() {
        description = mock(Description.class);
    }

    @Test
    public void getTestId_withoutTestId() {
        TafAnnotationManager manager = mockTestMethod("withoutTestId");
        assertEquals(TestId.DEFAULT_ID, manager.getTestId());
    }

    @Test
    public void getTestId_testIdAsInput1() {
        TafAnnotationManager manager = mockTestMethod("testIdAsInput1");
        assertEquals(TestId.DEFAULT_ID, manager.getTestId());
    }

    @Test
    public void getTestId_testIdAsInput2() {
        TafAnnotationManager manager = mockTestMethod("testIdAsInput2");
        assertEquals(TestId.DEFAULT_ID, manager.getTestId());
    }

    @Test
    public void getTestId_testIdAsMethodAnnotation1() {
        TafAnnotationManager manager = mockTestMethod("testIdAsMethodAnnotation1");
        assertEquals(TestId.DEFAULT_ID, manager.getTestId());
    }

    @Test
    public void getTestId_testIdAsMethodAnnotation2() {
        TafAnnotationManager manager = mockTestMethod("testIdAsMethodAnnotation2");
        assertEquals("testIdFromMethod", manager.getTestId());
    }

    @Test
    public void getTitle_fromAnnotation() {
        TafAnnotationManager manager = mockTestMethod("testIdAsMethodAnnotation3");
        assertEquals("Human readable testIdAsMethodAnnotation3", manager.getTestCaseTitle());
    }

    @Test
    public void isSetUpMethod() {
        assertTrue(mockTestMethod("beforeTest").isSetUpMethod());
        assertTrue(mockTestMethod("beforeClass").isSetUpMethod());
        assertFalse(mockTestMethod("afterClass").isSetUpMethod());
        assertFalse(mockTestMethod("afterTest").isSetUpMethod());
        assertFalse(mockTestMethod("test").isSetUpMethod());
    }

    @Test
    public void isTearDownMethod() {
        assertFalse(mockTestMethod("beforeTest").isTearDownMethod());
        assertFalse(mockTestMethod("beforeClass").isTearDownMethod());
        assertTrue(mockTestMethod("afterClass").isTearDownMethod());
        assertTrue(mockTestMethod("afterTest").isTearDownMethod());
        assertFalse(mockTestMethod("test").isTearDownMethod());
    }

    @Test
    public void getFixtureAnnotation() {
        assertEquals(Before.class, mockTestMethod("beforeTest").getFixtureAnnotation().annotationType());
        assertEquals(BeforeClass.class, mockTestMethod("beforeClass").getFixtureAnnotation().annotationType());
        assertEquals(After.class, mockTestMethod("afterTest").getFixtureAnnotation().annotationType());
        assertEquals(AfterClass.class, mockTestMethod("afterClass").getFixtureAnnotation().annotationType());
        assertEquals(null, mockTestMethod("test").getFixtureAnnotation());
    }

    @Test
    public void getFixtureName() {
        assertEquals("@Before: beforeTest", mockTestMethod("beforeTest").getFixtureName());
    }

    private TafAnnotationManager mockTestMethod(String methodName) {
        Method method = getMethod(methodName);
        when(description.getMethodName()).thenReturn(methodName);
        when(description.getDisplayName()).thenReturn("Human readable " + methodName);
        when(description.getAnnotations()).thenReturn(asList(method.getAnnotations()));
        return new TafJUnitAnnotationManager(description);
    }

    private Method getMethod(String methodName) {
        for (Method method : MethodSignaturesContainer.class.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("No such method " + methodName);
    }

    private static class MethodSignaturesContainer {

        public void withoutTestId(@Input("field1") String parameter1, @Input("field2") String parameter2, @Input("field3") String parameter3) {
            // do nothing, signature only is used for mock
        }

        public void testIdAsInput1(@TestId @Input("field1") String testId, @Input("field2") String parameter, @Input("field3") String parameter3) {
            // do nothing, signature only is used for mock
        }

        public void testIdAsInput2(@Input("field1") String parameter, @Input("field2") @TestId String testId, @Input("field3") String parameter3) {
            // do nothing, signature only is used for mock
        }

        @TestId
        public void testIdAsMethodAnnotation1(@Input("field1") String parameter1, @Input("field2") String parameter2, @Input("field3") String parameter3) {
            // do nothing, signature only is used for mock
        }

        @TestId(id = "testIdFromMethod")
        public void testIdAsMethodAnnotation2(@Input("field1") String parameter1, @Input("field2") String parameter2, @Input("field3") String parameter3) {
            // do nothing, signature only is used for mock
        }

        @TestId(id = "testIdFromMethod", title = "testTitleFromAnnotation")
        public void testIdAsMethodAnnotation3(@Input("field1") String parameter1, @Input("field2") String parameter2, @Input("field3") String parameter3) {
            // do nothing, signature only is used for mock
        }

        @Before
        public void beforeTest() {
            // do nothing, signature only is used for mock
        }

        @BeforeClass
        public void beforeClass() {
            // do nothing, signature only is used for mock
        }

        @Test
        public void test() {
            // do nothing, signature only is used for mock
        }

        @AfterClass
        public void afterClass() {
            // do nothing, signature only is used for mock
        }

        @After
        public void afterTest() {
            // do nothing, signature only is used for mock
        }

    }

}