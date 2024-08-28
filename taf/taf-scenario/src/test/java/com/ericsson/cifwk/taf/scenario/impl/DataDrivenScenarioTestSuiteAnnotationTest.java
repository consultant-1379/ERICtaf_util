package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;

/**
 * Created by eendjor on 15/03/2017.
 */
public class DataDrivenScenarioTestSuiteAnnotationTest {

    private static final String GLOBAL_DATA_SOURCE = "Sample Data Source";
    private static final String TEST_SUITE_1 = "Data Driven Scenario with annotation";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    @TestSuite(TEST_SUITE_1)
    public void dataDrivenScenarioWithTestSuiteAnnotation() {
        generateDataSource();
        runDataDrivenScenario();
    }

    @Test
    public void dataDrivenScenarioWithoutTestSuiteAnnotation() {
        expectedException.expect(RuntimeException.class);
        generateDataSource();
        runDataDrivenScenario();
    }

    private void generateDataSource() {
        TestContext context = TafTestContext.getContext();
        TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
        globalDataSource.addRecord()
                .setField("integer", 1)
                .setField("string", "A");
        globalDataSource.addRecord()
                .setField("integer", 2)
                .setField("string", "B");
    }

    private void runDataDrivenScenario() {
        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow("A")
                )
                .withScenarioDataSources(dataSource(GLOBAL_DATA_SOURCE))
                .build();
        runner().build().start(scenario);
    }
}
