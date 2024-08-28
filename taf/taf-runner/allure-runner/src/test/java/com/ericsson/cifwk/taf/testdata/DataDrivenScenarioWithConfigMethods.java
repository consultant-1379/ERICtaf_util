package com.ericsson.cifwk.taf.testdata;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder.TEST_CASE_ID;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 30.11.2015
 */
public class DataDrivenScenarioWithConfigMethods {

    private static final String SIMPLE_STEP = "simpleStep";
    private static final String GLOBAL_DATA_SOURCE1 = "whatever1";
    private static final String GLOBAL_DATA_SOURCE2 = "whatever2";
    private static final String GLOBAL_DATA_SOURCE3 = "whatever3";

    public static final String SUITE_1 = "DataDrivenScenarioWithConfigMethodsSuite 1";
    public static final String SUITE_2 = "DataDrivenScenarioWithConfigMethodsSuite 2";
    public static final String SUITE_3 = "DataDrivenScenarioWithConfigMethodsSuite 3";

    @BeforeSuite
    public void setUpSuite() {
        System.out.println("setUpSuite");
    }

    @BeforeTest
    public void setUpTest() {
        System.out.println("setUpTest");
    }

    @BeforeClass
    public void setUpClass() {
        System.out.println("setUpClass");
    }

    @BeforeMethod
    public void setUpMethod() {
        System.out.println("setUpMethod");
    }

    @Test
    @TestSuite(SUITE_1)
    public void testConfiguration1() {
        prepareGlobalDataSource("TestCase_1.", GLOBAL_DATA_SOURCE1);
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A").addTestStep(annotatedMethod(this, SIMPLE_STEP))
                        .pause(new Random().nextInt(1000), TimeUnit.MILLISECONDS).withVusers(10))
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE1))
                .build();
        runner().build().start(scenario);
    }

    @Test
    @TestSuite(SUITE_2)
    public void testConfiguration2() {
        prepareGlobalDataSource("TestCase_2.", GLOBAL_DATA_SOURCE2);

        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A").addTestStep(annotatedMethod(this, SIMPLE_STEP))
                        .pause(new Random().nextInt(1000), TimeUnit.MILLISECONDS).withVusers(10))
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE2))
                .build();
        runner().build().start(scenario);
    }

    @Test
    @TestSuite(SUITE_3)
    public void testConfiguration3() {
        prepareGlobalDataSource("TestCase_3.", GLOBAL_DATA_SOURCE3);
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A").addTestStep(annotatedMethod(this, SIMPLE_STEP))
                        .pause(new Random().nextInt(1000), TimeUnit.MILLISECONDS).withVusers(10))
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE3))
                .build();
        runner().build().start(scenario);
    }

    public void prepareGlobalDataSource(String prefix, String dataSourceName) {
        TestContext context = TafTestContext.getContext();
        if (!context.doesDataSourceExist(dataSourceName)) {
            TestDataSource<DataRecord> globalDataSource = context.dataSource(dataSourceName);
            globalDataSource.addRecord().setField(TEST_CASE_ID, prefix + 1);
            globalDataSource.addRecord().setField(TEST_CASE_ID, prefix + 2);
            globalDataSource.addRecord().setField(TEST_CASE_ID, prefix + 3);
        }
    }

    @AfterMethod
    public void tearDownMethod() {
        System.out.println("tearDownMethod");
    }

    @AfterClass
    public void tearDownClass() {
        System.out.println("tearDownClass");
    }

    @AfterTest
    public void tearDownTest() {
        System.out.println("tearDownTest");
    }

    @AfterSuite
    public void tearDownSuite() {
        System.out.println("tearDownSuite");
    }

    @TestStep(id = SIMPLE_STEP)
    public void simpleStep() {
        logStep(SIMPLE_STEP, null);
    }

    private void logStep(String step, String param) {
        int vUser = TafTestContext.getContext().getVUser();
        System.out.println(String.format("Inside %s with parameter %s (vUser: %s)", step, param, vUser));
    }

}
