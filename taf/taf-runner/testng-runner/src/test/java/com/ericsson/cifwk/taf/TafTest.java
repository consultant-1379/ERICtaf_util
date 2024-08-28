package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.taf.testng.TafTestRunnerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TafTest {

    Taf taf;
    private Taf.RelaxedTestNG testNG;

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() throws Exception {
        taf = new Taf() {
            @Override
            Taf.RelaxedTestNG createTestNG() {
                testNG = Mockito.mock(RelaxedTestNG.class);
                return testNG;
            }
        };
    }

    @Test
    public void shouldRegisterDefaults() throws Exception {
        taf.go();
        Mockito.verify(testNG).setTestRunnerFactory(Matchers.any(TafTestRunnerFactory.class));
        Mockito.verify(testNG).setSuiteThreadPoolSize(Matchers.anyInt());

        Mockito.verifyNoMoreInteractions(testNG);
    }

    @org.junit.Test
    public void shouldRegisterOneSuite() throws Exception {
        System.setProperty(Taf.SUITES, "suite.xml");
        taf.go("-dir", "/home/path");
        Mockito.verify(testNG).setTestSuites(Matchers.eq(Arrays.asList("/home/path/suite.xml")));
    }

    @org.junit.Test
    public void shouldRegisterMultipleSuites() throws Exception {
        System.setProperty(Taf.SUITES, "suite1.xml,suite2.xml");
        taf.go("-dir", "/home/path");
        Mockito.verify(testNG).setTestSuites(Matchers.eq(Arrays.asList("/home/path/suite1.xml", "/home/path/suite2.xml")));
    }

    @Test
    public void shouldDiscoverSuitesInDirectory() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("discovery/test1.xml");
        Path path = Paths.get(url.toURI()).getParent();

        taf.go("-dir", path.toString());

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(testNG).setTestSuites(captor.capture());
        Assert.assertEquals(2, captor.getValue().size());
        assertTrue(((String) captor.getValue().get(0)).endsWith("test1.xml"));
        assertTrue(((String)captor.getValue().get(1)).endsWith("test2.xml"));
    }

    @Test
    public void shouldAddGroups() throws Exception {
        System.setProperty(Taf.GROUPS, "group1,group2");
        taf.go();
        Mockito.verify(testNG).setGroups("group1,group2");
    }

    @Test
    public void shouldAddListeners() throws Exception {
        System.setProperty(Taf.LISTENER, TestListener.class.getName());
        taf.go();
        Mockito.verify(testNG).addListener((Object) Matchers.any(TestListener.class));
    }

    public static class TestListener implements ISuiteListener {

        @Override
        public void onStart(ISuite suite) {
        }

        @Override
        public void onFinish(ISuite suite) {
        }
    }

}
