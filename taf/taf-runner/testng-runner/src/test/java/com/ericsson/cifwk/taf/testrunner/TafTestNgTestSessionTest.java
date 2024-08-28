package com.ericsson.cifwk.taf.testrunner;

import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.management.TafBootstrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.ITestNGListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 14/12/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class TafTestNgTestSessionTest {

    @Mock
    private TafBootstrap tafBootstrap;

    @Mock
    private TafTestNgTestSession.RelaxedTestNG testNG;

    @Mock
    private TestEventBus testEventBus;

    @Spy
    private TafTestNgTestSession unit;

    @Before
    public void setUp() {
        doReturn(tafBootstrap).when(unit).initTafBootstrap();
        doReturn(testNG).when(unit).createTestNGInstance();
        doReturn(testEventBus).when(unit).getTestEventBus();
    }

    @Test
    public void shouldSetOnlyTestsIfDefined() throws Exception {
        TafTestSessionOptions options = new TafTestSessionOptions();
        options.setTestClasses(Collections.<Class>singletonList(TestClass1.class));
        unit.init(options);

        verifyMandatoryCalls();
        verify(testNG).setTestClasses(new Class[] {TestClass1.class} );
        verify(testNG, never()).setTestSuites(anyListOf(String.class));
        verify(testNG, never()).setGroups(anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfNoTestsToRun() throws Exception {
        TafTestSessionOptions options = new TafTestSessionOptions();
        unit.init(options);
    }

    @Test
    public void initWithListeners() throws Exception {
        TafTestSessionOptions options = new TafTestSessionOptions();
        List<String> testGroupDefinitions = Arrays.asList("suite1.xml", "suite2.xml");
        options.setTestGroupDefinitions(testGroupDefinitions);
        options.setTestTags(Arrays.asList("group1", "group2"));
        TestNGListener testNGListener = new TestNGListener();
        TestApiSubscriber testApiSubscriber = new TestApiSubscriber();
        options.setTestEventListeners(Arrays.asList(testApiSubscriber, testNGListener));
        unit.init(options);

        verifyMandatoryCalls();
        verify(testNG).setTestSuites(testGroupDefinitions);
        verify(testNG).setGroups("group1,group2");
        verify(testNG).addListener(testNGListener);
        verify(testNG, never()).addListener(testApiSubscriber);
        verify(testEventBus).register(testApiSubscriber);
    }

    @Test
    public void lifecycle() {
        TafTestSessionOptions options = new TafTestSessionOptions();
        List<String> testGroupDefinitions = Arrays.asList("suite1.xml", "suite2.xml");
        options.setTestGroupDefinitions(testGroupDefinitions);

        unit.init(options);
        unit.start();
        verify(testNG).run();
        unit.shutdown();
        verify(tafBootstrap).shutdown();
    }

    private void verifyMandatoryCalls() {
        verify(testNG).setSuiteThreadPoolSize(anyInt());
        verify(tafBootstrap).init();
    }

    private static class TestClass1 {

    }

    private static class TestApiSubscriber {

    }

    public static class TestNGListener implements ITestNGListener {

    }

}