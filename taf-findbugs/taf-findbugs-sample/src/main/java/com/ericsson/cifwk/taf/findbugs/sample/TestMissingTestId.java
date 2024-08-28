package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestSuite;

import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 20.10.2016
 */
public class TestMissingTestId {

    private static final String WARNING = "MissingTestId";

    @Test(dependsOnGroups = {"test2"})
    @TestId(id = "1")
    @AssertNoWarning(WARNING)
    public void test1() {
    }

    @Test
    @AssertWarning(WARNING)
    @TestId
    public void test2() throws NoSuchMethodException {
        Method method = getClass().getMethod("test2");
        String testId = method.getAnnotation(TestId.class).id();
        assertThat(testId).isEqualTo(TestId.DEFAULT_ID);
    }

    @Test
    @AssertWarning(WARNING)
    public void test3() {
    }

    @Test
    @TestId(id = " ")
    @AssertWarning(WARNING)
    public void test4() {
    }

    @Test
    @TestSuite
    @AssertNoWarning(WARNING)
    public void test5(){
    }

    @AssertNoWarning(WARNING)
    public void regularMethod() {
    }

}
