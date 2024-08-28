package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import org.junit.Before;
import org.junit.Test;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

import java.lang.reflect.Method;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 4/7/2015
 */
public class TafTestNGAnnotationManagerTest {

    private ITestResult iTestResult;

    @Before
    public void setUp() {
        iTestResult = mock(ITestResult.class);
        when(iTestResult.getParameters()).thenReturn(new String[]{"value1", "value2", null});
    }

    @Test
    public void getTestId_withoutTestId() {
        TafAnnotationManager manager = mockTestMethod("withoutTestId");
        assertEquals(TestId.DEFAULT_ID, manager.getTestId());
    }

    @Test
    public void getTestId_testIdAsInput1() {
        TafAnnotationManager manager = mockTestMethod("testIdAsInput1");
        assertEquals("value1", manager.getTestId());
    }

    @Test
    public void getTestId_testIdAsInput2() {
        TafAnnotationManager manager = mockTestMethod("testIdAsInput2");
        assertEquals("value2", manager.getTestId());
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
    public void getTestCaseTitle() {
        TafAnnotationManager manager = mockTestMethod("testIdAsMethodAnnotation2");
        String testId = manager.getTestId();
        assertEquals(testId + "[value1, value2]", manager.getTestCaseTitle());
    }

    @Test
    public void getTitle_fromAnnotation() {
        TafAnnotationManager manager = mockTestMethod("testIdAsMethodAnnotation3");
        assertEquals("testTitleFromAnnotation[value1, value2]", manager.getTestCaseTitle());
    }

    @Test
    public void isSetUpMethod() {
        assertTrue(mockTestMethod("beforeSuite").isSetUpMethod());
        assertTrue(mockTestMethod("beforeTest").isSetUpMethod());
        assertTrue(mockTestMethod("beforeClass").isSetUpMethod());
        assertTrue(mockTestMethod("beforeMethod").isSetUpMethod());
        assertFalse(mockTestMethod("afterMethod").isSetUpMethod());
        assertFalse(mockTestMethod("afterClass").isSetUpMethod());
        assertFalse(mockTestMethod("afterTest").isSetUpMethod());
        assertFalse(mockTestMethod("afterSuite").isSetUpMethod());
        assertFalse(mockTestMethod("test").isSetUpMethod());
    }

    @Test
    public void isTearDownMethod() {
        assertFalse(mockTestMethod("beforeSuite").isTearDownMethod());
        assertFalse(mockTestMethod("beforeTest").isTearDownMethod());
        assertFalse(mockTestMethod("beforeClass").isTearDownMethod());
        assertFalse(mockTestMethod("beforeMethod").isTearDownMethod());
        assertTrue(mockTestMethod("afterMethod").isTearDownMethod());
        assertTrue(mockTestMethod("afterClass").isTearDownMethod());
        assertTrue(mockTestMethod("afterTest").isTearDownMethod());
        assertTrue(mockTestMethod("afterSuite").isTearDownMethod());
        assertFalse(mockTestMethod("test").isTearDownMethod());
    }

    @Test
    public void getFixtureAnnotation() {
        assertEquals(BeforeSuite.class, mockTestMethod("beforeSuite").getFixtureAnnotation().annotationType());
        assertEquals(BeforeTest.class, mockTestMethod("beforeTest").getFixtureAnnotation().annotationType());
        assertEquals(BeforeClass.class, mockTestMethod("beforeClass").getFixtureAnnotation().annotationType());
        assertEquals(BeforeMethod.class, mockTestMethod("beforeMethod").getFixtureAnnotation().annotationType());
        assertEquals(AfterMethod.class, mockTestMethod("afterMethod").getFixtureAnnotation().annotationType());
        assertEquals(AfterClass.class, mockTestMethod("afterClass").getFixtureAnnotation().annotationType());
        assertEquals(AfterTest.class, mockTestMethod("afterTest").getFixtureAnnotation().annotationType());
        assertEquals(AfterSuite.class, mockTestMethod("afterSuite").getFixtureAnnotation().annotationType());
        assertEquals(null, mockTestMethod("test").getFixtureAnnotation());
    }

    @Test
    public void getFixtureName() {
        assertEquals("@BeforeMethod: beforeMethod", mockTestMethod("beforeMethod").getFixtureName());
    }

    private TafAnnotationManager mockTestMethod(String methodName) {
        Method method = getMethod(methodName);
        String[] parameters = {};
        if (method.getParameterTypes().length != 0) {
            parameters = new String[]{"value1", "value2"};
        }
        return new TafTestNGAnnotationManager(method, parameters);
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

        @BeforeSuite
        public void beforeSuite() {
            // do nothing, signature only is used for mock
        }

        @BeforeTest
        public void beforeTest() {
            // do nothing, signature only is used for mock
        }

        @BeforeClass
        public void beforeClass() {
            // do nothing, signature only is used for mock
        }

        @BeforeMethod
        public void beforeMethod() {
            // do nothing, signature only is used for mock
        }

        @org.testng.annotations.Test
        public void test() {
            // do nothing, signature only is used for mock
        }

        @AfterMethod
        public void afterMethod() {
            // do nothing, signature only is used for mock
        }

        @AfterClass
        public void afterClass() {
            // do nothing, signature only is used for mock
        }

        @AfterTest
        public void afterTest() {
            // do nothing, signature only is used for mock
        }

        @AfterSuite
        public void afterSuite() {
            // do nothing, signature only is used for mock
        }

    }

}
