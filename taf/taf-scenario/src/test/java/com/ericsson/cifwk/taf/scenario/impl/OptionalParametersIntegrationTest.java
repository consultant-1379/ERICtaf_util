package com.ericsson.cifwk.taf.scenario.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationUtils.DATASOURCE_VALIDATION;
import static com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationUtils.STRICT_MODE;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.05.2016
 */
public class OptionalParametersIntegrationTest {

    private static final String DATA_SOURCE = OptionalParametersIntegrationTest.class.getName();

    public static final String TEST_STEP_WITH_MANDATORY_PARAMETER = "tsMandatory";
    public static final String TEST_STEP_WITH_OPTIONAL_PARAMETER = "tsOptional";
    public static final String TEST_STEP_WITH_DEFAULT_VALUE = "tsDefault";
    public static final String NEVER_SET = "neverSet";

    private String stringParameter = "neverSet";
    private int primitiveParameter = -1;

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void setUp() {
        prepareDataSource(null, null);
    }

    @After
    public void tearDown() {
        TafTestContext.getContext().removeDataSource(DATA_SOURCE);
    }

    @Test
    public void mandatoryParameterInStrictMode() {
        switchToStrictMode();
        try {
            run(createScenario(TEST_STEP_WITH_MANDATORY_PARAMETER));
            fail();
        } catch (Exception e) {
             assertThat(e.getMessage(), CoreMatchers.containsString("The value of the input/output parameter 'string' is null"));
        }

        // check that test step was never executed
        assertEquals(NEVER_SET, stringParameter);
        assertEquals(-1, primitiveParameter);
    }

    @Test
    @TestSuite
    public void mandatoryParameterInStrictModeInDataDrivenScenario() {
        switchToStrictMode();
        try {
            run(createDataDrivenScenario(TEST_STEP_WITH_MANDATORY_PARAMETER));
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("The value of the input/output parameter 'string' is null"));
        }

        // check that test step was never executed
        assertEquals(NEVER_SET, stringParameter);
        assertEquals(-1, primitiveParameter);
    }

    @Test
    public void mandatoryParameterInRelaxedMode() {
        switchToRelaxedMode();

        run(createScenario(TEST_STEP_WITH_MANDATORY_PARAMETER));

        // test step was executed
        assertEquals(null, stringParameter);
        assertEquals(0, primitiveParameter);
    }

    @Test
    @TestSuite
    public void mandatoryParameterInRelaxedModeInDataDrivenScenario() {
        switchToRelaxedMode();

        run(createDataDrivenScenario(TEST_STEP_WITH_MANDATORY_PARAMETER));

        // test step was executed
        assertEquals(null, stringParameter);
        assertEquals(0, primitiveParameter);
    }

    @Test
    public void optionalParameterInStrictMode() {
        switchToStrictMode();
        run(createScenario(TEST_STEP_WITH_OPTIONAL_PARAMETER));

        // test step was executed
        assertEquals(null, stringParameter);
        assertEquals(0, primitiveParameter);
    }

    @Test
    @TestSuite
    public void optionalParameterInStrictModeInDataDrivenScenario() {
        switchToStrictMode();
        run(createDataDrivenScenario(TEST_STEP_WITH_OPTIONAL_PARAMETER));

        // test step was executed
        assertEquals(null, stringParameter);
        assertEquals(0, primitiveParameter);
    }

    @Test
    public void optionalParameterInRelaxedMode() {
        switchToRelaxedMode();
        run(createScenario(TEST_STEP_WITH_OPTIONAL_PARAMETER));

        // test step was executed
        assertEquals(null, stringParameter);
        assertEquals(0, primitiveParameter);
    }

    @Test
    @TestSuite
    public void optionalParameterInRelaxedModeInDataDrivenScenario() {
        switchToRelaxedMode();
        run(createDataDrivenScenario(TEST_STEP_WITH_OPTIONAL_PARAMETER));

        // test step was executed
        assertEquals(null, stringParameter);
        assertEquals(0, primitiveParameter);
    }

    @Test
    public void defaultValueInStrictMode() {
        switchToStrictMode();
        run(createScenario(TEST_STEP_WITH_DEFAULT_VALUE));

        // test step was executed with default values
        assertEquals("defaultValue", stringParameter);
        assertEquals(42, primitiveParameter);
    }

    @Test
    @TestSuite
    public void defaultValueInStrictModeInDataDrivenScenario() {
        switchToStrictMode();
        run(createDataDrivenScenario(TEST_STEP_WITH_DEFAULT_VALUE));

        // test step was executed with default values
        assertEquals("defaultValue", stringParameter);
        assertEquals(42, primitiveParameter);
    }

    @Test
    public void defaultValueInRelaxedMode() {
        switchToRelaxedMode();
        run(createScenario(TEST_STEP_WITH_DEFAULT_VALUE));

        // test step was executed with default values
        assertEquals("defaultValue", stringParameter);
        assertEquals(42, primitiveParameter);
    }

    @Test
    @TestSuite
    public void defaultValueInRelaxedModeInDataDrivenScenario() {
        switchToRelaxedMode();
        run(createDataDrivenScenario(TEST_STEP_WITH_DEFAULT_VALUE));

        // test step was executed with default values
        assertEquals("defaultValue", stringParameter);
        assertEquals(42, primitiveParameter);
    }

    @TestStep(id = TEST_STEP_WITH_MANDATORY_PARAMETER)
    public void mandatory(@Input("string") String string,
                          @Input("primitive") int primitive) {
        saveParameters(string, primitive);
    }

    @TestStep(id = TEST_STEP_WITH_OPTIONAL_PARAMETER)
    public void optional(@OptionalValue @Input("string") String string,
                         @OptionalValue @Input("primitive") int primitive) {
        saveParameters(string, primitive);
    }

    @TestStep(id = TEST_STEP_WITH_DEFAULT_VALUE)
    public void defaulValue(@Input("string") @OptionalValue("defaultValue") String string,
                            @Input("primitive") @OptionalValue("42") int primitive) {
        saveParameters(string, primitive);
    }

    private TestScenario createDataDrivenScenario(String testStep) {
        return dataDrivenScenario()
                .addFlow(flow("A")
                        .addTestStep(annotatedMethod(this, testStep))
                )
                .withScenarioDataSources(dataSource(DATA_SOURCE))
                .build();
    }

    private TestScenario createScenario(String testStep) {
        return scenario()
                .addFlow(flow("A")
                        .addTestStep(annotatedMethod(this, testStep))
                        .withDataSources(dataSource(DATA_SOURCE))
                )
                .build();
    }

    private void run(TestScenario scenario) {
        runner().build().start(scenario);
    }

    private void saveParameters(String string, int primitive) {
        stringParameter = string;
        primitiveParameter = primitive;
    }

    private void prepareDataSource(String stringValue, Integer primitiveValue) {
        TestDataSource<DataRecord> dataSource = TafTestContext.getContext().dataSource(DATA_SOURCE);
        dataSource.addRecord()
                .setField("string", stringValue)
                .setField("primitive", primitiveValue);
    }

    private void switchToStrictMode() {
        System.setProperty(DATASOURCE_VALIDATION, STRICT_MODE);
    }

    private void switchToRelaxedMode() {
        System.clearProperty(DATASOURCE_VALIDATION);
    }

}
