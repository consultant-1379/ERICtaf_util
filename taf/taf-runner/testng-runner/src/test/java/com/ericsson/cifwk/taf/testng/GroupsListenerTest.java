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
package com.ericsson.cifwk.taf.testng;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.testng.IMethodInstance;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlTest;

public class GroupsListenerTest {

    private final GroupsListener groups = new GroupsListener();
    private final List<String> cmdGroups = new ArrayList<>();
    private final List<String> testGroupings = new ArrayList<>();
    private final List<String> suiteGroups = new ArrayList<>();
    private List<IMethodInstance> methods;
    private IMethodInstance methodInstanceMock;
    private ITestNGMethod testNGMethod;
    private ConstructorOrMethod constructorOrMethod;
    private ITestContext iTestContext;
    private XmlTest xmlTest;

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() {
        cmdGroups.add("Acceptance Test 1");
        cmdGroups.add("Functional Test 1");
        cmdGroups.add("Acceptance Test 2");

        testGroupings.add("Regression Test 1");
        testGroupings.add("Functional Test 1");
        testGroupings.add("Performance Test 1");

        methodInstanceMock = mock(IMethodInstance.class);
        testNGMethod = mock(ITestNGMethod.class);
        constructorOrMethod = mock(ConstructorOrMethod.class);
        iTestContext = mock(ITestContext.class);
        xmlTest = mock(XmlTest.class);

        methods = new ArrayList<>();
        methods.add(methodInstanceMock);

        when(methodInstanceMock.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);

        when(iTestContext.getCurrentXmlTest()).thenReturn(xmlTest);
        when(xmlTest.getIncludedGroups()).thenReturn(suiteGroups);
    }

    @Test
    public void checkIsContainedInGroupsMethod() {
        assertEquals(groups.isContainedInGroups(cmdGroups, testGroupings), true);
    }

    @Test
    public void checkIsNotContainedInGroupsMethod() {
        testGroupings.remove(1);
        assertEquals(groups.isContainedInGroups(cmdGroups, testGroupings), false);
    }

    @Test
    public void checkIsMethodKeptInList() throws NoSuchMethodException, SecurityException {

        final Method method = this.getClass().getMethod("methodWithGroups");
        carryOutMocking(method);
        assertThat(groups.getMethodsToExecute(methods, cmdGroups).size(), is(1));
    }

    @Test
    public void checkThereIsNoMethodKeptInList() throws NoSuchMethodException, SecurityException {

        final Method method = this.getClass().getMethod("methodWithoutGroups");
        carryOutMocking(method);
        assertThat(groups.getMethodsToExecute(methods, cmdGroups).size(), is(0));

    }

    @Test
    public void checkCommandLineGroupsAndSuiteGroupsNull() throws NoSuchMethodException, SecurityException {

        final Method method = this.getClass().getMethod("methodWithoutGroups");
        carryOutMocking(method);
        assertThat(groups.intercept(methods, iTestContext).size(), is(1));
    }

    @Test
    public void checkCommandLineNotEmptySuiteNotEmpty() throws NoSuchMethodException, SecurityException {

        suiteGroups.add("Functional Test 1");
        suiteGroups.add("Performance Test 1");
        suiteGroups.add("Performance Test 2");

        System.setProperty("groups", "Functional Test 1");
        when(xmlTest.getIncludedGroups()).thenReturn(suiteGroups);
        methods.add(methodInstanceMock);
        final Method method = this.getClass().getMethod("methodWithGroups");
        carryOutMocking(method);

        assertThat(groups.intercept(methods, iTestContext).size(), is(2));
        suiteGroups.remove(0);
        System.clearProperty("groups");
        assertThat(groups.intercept(methods, iTestContext).size(), is(0));
    }

    @Test
    public void checkCommandLineNotEmptySuiteNull() throws NoSuchMethodException, SecurityException {

        methods.add(methodInstanceMock);
        System.setProperty("groups", "Functional Test 1");
        final Method method = this.getClass().getMethod("methodWithGroups");
        carryOutMocking(method);
        assertThat(groups.intercept(methods, iTestContext).size(), is(2));
    }

    @Test
    public void checkSuiteNotEmptyCommandLineNull() throws NoSuchMethodException, SecurityException {

        suiteGroups.add("Functional Test 1");
        suiteGroups.add("Performance Test 1");
        suiteGroups.add("Performance Test 2");

        when(xmlTest.getIncludedGroups()).thenReturn(suiteGroups);
        methods.add(methodInstanceMock);
        final Method method = this.getClass().getMethod("methodWithGroups");
        carryOutMocking(method);

        assertThat(groups.intercept(methods, iTestContext).size(), is(2));
    }

    @Test
    public void checkSingleMethodIsCapturedWhenCombinationOf_AND_OR_GroupsAreBothSatisfiedBySingleMethod() throws NoSuchMethodException,
    SecurityException {

        System.setProperty("groups", "Functional Test 1, Acceptance Test 2 AND Acceptance Test 3");
        final Method method = this.getClass().getMethod("methodWithThreeGroups");
        carryOutMocking(method);

        assertThat(groups.intercept(methods, iTestContext).size(), is(1));
    }

    @Test
    public void checkIsContainedInGroupsMethodForVariousOperatorCombinations() {
        final List<String> testGroupings = new ArrayList<>();
        testGroupings.add("Smoke");
        testGroupings.add("Load");
        testGroupings.add("Stress");

        final List<String> cmdGroups = new ArrayList<>();
        cmdGroups.add("Smoke AND Load");
        assertEquals(true, groups.isContainedInGroups(cmdGroups, testGroupings));
        cmdGroups.clear();

        cmdGroups.add("Stress AND Unit");
        cmdGroups.add("Smoke AND Load");
        assertEquals(true, groups.isContainedInGroups(cmdGroups, testGroupings));
        cmdGroups.clear();

        cmdGroups.add("Stress");
        cmdGroups.add("Load AND Unit");
        assertEquals(true, groups.isContainedInGroups(cmdGroups, testGroupings));
        cmdGroups.clear();

        cmdGroups.add("Performance AND Unit");
        assertEquals(false, groups.isContainedInGroups(cmdGroups, testGroupings));
        cmdGroups.clear();
    }

    @org.testng.annotations.Test(groups = { "Functional Test 1", "Acceptance Test 2" })
    public void methodWithGroups() {

    }

    @org.testng.annotations.Test(groups = { "Functional Test 1", "Acceptance Test 2", "Acceptance Test 3" })
    public void methodWithThreeGroups() {

    }

    @org.testng.annotations.Test
    public void methodWithoutGroups() {

    }

    public void carryOutMocking(final Method method) {
        when(constructorOrMethod.getMethod()).thenReturn(method);
    }

}
