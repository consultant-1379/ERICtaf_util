package com.ericsson.cifwk.taf.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.inject.Provider;

import org.junit.Test;
import org.testng.annotations.Guice;

import com.ericsson.cifwk.taf.configuration.TafProperty;
import com.ericsson.cifwk.taf.testng.MiniTestNG;

public class TafPropertyInjectionTest {

    @Test
    public void testTafPropertyDefaultInjection() throws Exception {
        MiniTestNG.runTestNg(TestInjectionDefaultValue.class);
    }

    @Test
    public void testTafPropertyAssignedInjection() throws Exception {
        MiniTestNG.runTestNg(TestInjectionAssignedValue.class);
    }

    @Test
    public void testTafPropertyUnAssignedInjection() throws Exception {
        MiniTestNG.runTestNg(TestInjectionUnAssignedValue.class);
    }


    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class TestInjectionDefaultValue {

        private static final String TEST_PROPERTY = "test.property";

        @TafProperty(value = TEST_PROPERTY, defaultValue = "test")
        private String property;

        @org.testng.annotations.Test
        public void testDefaultValue() {
            assertEquals("test", property);
        }
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class TestInjectionAssignedValue {

        private static final String TEST_ASSIGNED_PROPERTY = "test.assigned.property";

        @TafProperty(value = TEST_ASSIGNED_PROPERTY, defaultValue = "test")
        private String property;

        @org.testng.annotations.Test
        public void setTestAssignedPropertyValue() {
            assertEquals("newValue", property);
        }
    }

    @Guice(moduleFactory = OperatorLookupModuleFactory.class)
    public static class TestInjectionUnAssignedValue {

        private static final String TEST_UNASSIGNED_PROPERTY = "test.unassigned.property";

        @TafProperty(TEST_UNASSIGNED_PROPERTY)
        private Provider<String> property;

        @org.testng.annotations.Test
        public void testUnassignedValue() {
            assertNull(property.get());
        }

    }
}
