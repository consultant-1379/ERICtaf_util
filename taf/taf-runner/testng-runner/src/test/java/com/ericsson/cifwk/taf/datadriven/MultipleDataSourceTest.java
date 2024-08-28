package com.ericsson.cifwk.taf.datadriven;

import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.DataProviders;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.testng.AbstractTestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import sun.net.www.protocol.test.TestConnection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static sun.net.www.protocol.test.CSV.csv;

public class MultipleDataSourceTest {

    private boolean failed;
    private Map<String, AtomicInteger> testIterations = new HashMap<>();

    @org.junit.Before
    public void setUp() {

        failed = false;

        TafConfiguration configuration = TafConfigurationProvider.provide();
        TafConfiguration runtimeConfiguration = configuration.getRuntimeConfiguration();
        runtimeConfiguration.setProperty("dataprovider.users.type", "csv");
        runtimeConfiguration.setProperty("dataprovider.users.uri", "test://users");
        runtimeConfiguration.setProperty("dataprovider.admins.type", "csv");
        runtimeConfiguration.setProperty("dataprovider.admins.uri", "test://users");
        runtimeConfiguration.setProperty("dataprovider.commands.type", "csv");
        runtimeConfiguration.setProperty("dataprovider.commands.uri", "test://commands");

        TestConnection.setValue("users",
                                csv("type", "name", "password")
                                        .add("admin", "u1", "p1")
                                        .add("admin", "u2", "p2")
                                        .add("user", "u3", "p3")
                                        .add("user", "u4", "p4")
                                        .add("user", "u5", "p5").toString()
        );

        TestConnection.setValue("commands",
                                csv("command")
                                        .add("cmd1")
                                        .add("cmd2")
                                        .toString()
        );

    }

    @org.junit.After
    public void tearDown() throws Exception {
        TafConfiguration configuration = TafConfigurationProvider.provide();
        TafConfiguration runtimeConfiguration = configuration.getRuntimeConfiguration();
        runtimeConfiguration.clear();
    }

    @org.junit.Test
    public void shouldUseCustomDataSource() {
        runTestNg();
        assertTrue(!failed);
        assertEquals(5, testIterations.get("oldApproach").intValue());
        assertEquals(3, testIterations.get("t1").intValue());
        assertEquals(3, testIterations.get("t2").intValue());
        assertEquals(2, testIterations.get("t3").intValue());
        assertEquals(2, testIterations.get("t4").intValue());
        //assertEquals(3, testIterations.get("customDataSourceUsage").intValue());
    }

    void runTestNg() {
        TestNG testNG = new TestNG(false);
        testNG.setTestClasses(new Class[]{TestNgTest.class});
        testNG.setPreserveOrder(true);
        testNG.addListener(new DataDrivenAnnotationTransformer());
        testNG.addListener(new FailureListener());
        testNG.addListener(new IterationListener());
        testNG.run();
    }

    public static class TestNgTest {
        // OLD,
        @Test
        @DataDriven(name = "users")
        public void oldApproach(@Input("name") String name,
                                @Output("password") String password) {
            assertThat(Arrays.asList("u1", "u2", "u3", "u4", "u5"), hasItem(name));
            assertThat(Arrays.asList("p1", "p2", "p3", "p4", "p5"), hasItem(password));
        }

        // Single DS with filter, parameter mapping by "It’s the early bird that gets the worm"
        @Test
        @DataDriven(name = "users", filter = "type=='user'")
        public void t1(@Input("name") String name,
                       @Output("password") String password,
                       @Input("users") DataRecord users) {
            assertThat(Arrays.asList("u3", "u4", "u5"), hasItem(name));
            assertThat(Arrays.asList("p3", "p4", "p5"), hasItem(password));
            assertThat((String) users.getFieldValue("name"), is(name));
            assertThat((String) users.getFieldValue("password"), is(password));
        }

        // Single DS with filter, parameter mapping by DataDriven name
        @Test
        @DataDriven(name = "users", filter = "type=='user'")
        public void t2(@Input("users.name") String name,
                       @Output("users.password") String password,
                       @Input("users") DataRecord users) {

            assertThat(Arrays.asList("u3", "u4", "u5"), hasItem(name));
            assertThat(Arrays.asList("p3", "p4", "p5"), hasItem(password));
            assertThat((String) users.getFieldValue("name"), is(name));
            assertThat((String) users.getFieldValue("password"), is(password));

        }

        // Multiple DS,parameter mapping by "It’s the early bird that gets the worm"
        @Test
        @DataProviders({
                               @DataDriven(name = "users", filter = "type=='user'"),
                               @DataDriven(name = "commands")
                       })
        public void t3(@Input("name") String name,
                       @Output("password") String password,
                       @Input("command") String command) {
            assertThat(Arrays.asList("u3", "u4"), hasItem(name));
            assertThat(Arrays.asList("p3", "p4"), hasItem(password));
            assertThat(Arrays.asList("cmd1", "cmd2"), hasItem(command));
        }

        // Multiple DS, parameter mapping by DS name
        @Test
        @DataProviders({
                               @DataDriven(name = "users", filter = "type=='user'"),
                               @DataDriven(name = "admins", filter = "type=='admin'")
                       })
        public void t4(@Input("users.name") String name,
                       @Output("users.password") String password,
                       @Input("users") DataRecord users,
                       @Input("admins.name") String admin,
                       @Input("admins") DataRecord admins) {

            assertThat(Arrays.asList("u3", "u4"), hasItem(name));
            assertThat(Arrays.asList("p3", "p4"), hasItem(password));
            assertThat((String) users.getFieldValue("name"), is(name));
            assertThat((String) users.getFieldValue("password"), is(password));

            assertThat(Arrays.asList("u1", "u2"), hasItem(admin));
            assertThat((String) admins.getFieldValue("name"), is(admin));

        }

        @Test
        @DataDriven(name = "users", filter = "type=='user'")
        public void t5(@Input("users") DataRecord users,
                       @Input("admins") DataRecord admins) {
            assertThat(users, notNullValue());
            assertThat(admins, nullValue());
        }

    }

    public class FailureListener extends AbstractTestListener {
        @Override
        public void onTestFailure(ITestResult result) {
            System.err.println(result);
            result.getThrowable().printStackTrace();
            failed = true;
        }
    }

    public class IterationListener extends AbstractTestListener {
        @Override
        public void onTestSuccess(ITestResult result) {
            String testName = result.getName();
            AtomicInteger iterationCount = testIterations.get(testName);
            if (iterationCount == null) {
                iterationCount = new AtomicInteger(1);
                testIterations.put(testName, iterationCount);
            } else {
                iterationCount.incrementAndGet();
            }
        }
    }

}
