package com.ericsson.de.scenariorx.impl;

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.fromDataRecords;
import static com.ericsson.de.scenariorx.impl.Api.fromIterable;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Stack;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.TafUser;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxBasicDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("DanglingJavadoc")
public class TafTestStepTest {

    private final FlawedTestSteps flawedTestSteps = new FlawedTestSteps();
    private final ValidTestSteps validTestSteps = new ValidTestSteps();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        validTestSteps.stack.clear();
    }

    /*-------- Tests for scenario build time validations --------*/

    @Test
    public void annotatedMethod_shouldThrowIllegalArgumentException_whenTestStepDoesNotExist() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Method annotated with @TestStep(id = \"testStepDoesNotExist\") not found for class FlawedTestSteps!");

        scenario()
                .addFlow(flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(flawedTestSteps, FlawedTestSteps.TEST_STEP_DOES_NOT_EXIST))
                );
    }

    @Test
    public void annotatedMethod_shouldThrowIllegalArgumentException_whenTestStepHasPrivateAccessModifier() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Method annotated with @TestStep(id = \"testStepWithPrivateAccessModifier\") not found for class FlawedTestSteps!");

        scenario()
                .addFlow(flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(flawedTestSteps, FlawedTestSteps.TEST_STEP_PRIVATE_ACCESS))
                );
    }

    @Test
    public void annotatedMethod_shouldThrowIllegalArgumentException_whenTestStepParametersWithoutAnnotations() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Parameter with type 'Integer' in Test Step 'testStepParametersWithoutAnnotations' has no @Input/Output annotations");

        scenario()
                .addFlow(flow()
                        /** {@link FlawedTestSteps#testStepParametersWithoutAnnotations(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(flawedTestSteps, FlawedTestSteps.TEST_STEP_NO_PARAMETER_ANNOTATIONS))
                );
    }

    @Test
    public void withParameter_shouldThrowIllegalStateException_whenParameter_null() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(RxTestStep.ERROR_PARAMETER_NULL);

        scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStepWithoutParameters()} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_NO_PARAMS)
                                .withParameter(null).value(13)
                        )
                );
    }

    @Test
    public void withParameter_shouldThrowIllegalArgumentException_whenParameter_doesNotExist() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Parameter 'nonExistingParameter' for test step 'ValidTestSteps#testStepWithoutParameters' does not exist");

        scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStepWithoutParameters()} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_NO_PARAMS)
                                .withParameter("nonExistingParameter").value(13)
                        )
                );
    }

    @Test
    public void withParameter_shouldThrowIllegalStateException_whenSameParameterSetTwice() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(format(RxTestStep.ERROR_PARAMETER_ALREADY_SET, DataSources.NON_EXISTING));

        scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStepWithOptionalParameter(Object)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_OPTIONAL_PARAM)
                                .withParameter(DataSources.NON_EXISTING).value(42)
                                .withParameter(DataSources.NON_EXISTING).value(13)
                        )
                );
    }

    /*-------- Tests for parameter overriding --------*/

    @Test
    public void withParameter_shouldOverride_iterableDataSource_withParameters() throws Exception {
        List<Integer> digits = newArrayList(1, 2, 3);
        List<String> letters = newArrayList("A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withIterableDataSource_andMultipleParameters(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_ITERABLE_MULTI_PARAMS)
                                .withParameter(DataSources.DIGITS).value(42)
                                .withParameter(DataSources.LETTERS).value("Z")
                        )
                        .withDataSources(
                                fromIterable(DataSources.DIGITS, digits),
                                fromIterable(DataSources.LETTERS, letters)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(42, "Z", 42, "Z", 42, "Z");
    }

    @Test
    public void withParameter_shouldOverride_iterableDataSource_withDataRecords() throws Exception {
        List<Integer> digits = newArrayList(1, 2, 3);
        List<String> letters = newArrayList("A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withIterableDataSource_andMultipleDataRecords(RxDataRecord, RxDataRecord)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_ITERABLE_MULTI_DATA_RECORDS)
                                .withParameter(DataSources.DIGITS + "." + DataSources.DIGITS).value(42)
                                .withParameter(DataSources.LETTERS + "." + DataSources.LETTERS).value("Z")
                        )
                        .withDataSources(
                                fromIterable(DataSources.DIGITS, digits),
                                fromIterable(DataSources.LETTERS, letters)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(42, "Z", 42, "Z", 42, "Z");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withParameters() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andMultipleParameters(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_MULTI_PARAMS)
                                .withParameter(DataRecords.DIGIT).value(42)
                                .withParameter(DataRecords.LETTER).value("Z")
                        )
                        .withDataSources(
                                fromDataRecords(DataSources.DIGITS, digits),
                                fromDataRecords(DataSources.LETTERS, letters)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(42, "Z", 42, "Z", 42, "Z");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withFullNameParameters() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andMultipleFullNameParameters(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_MULTI_FULL_NAME_PARAMS)
                                .withParameter(DataSources.DIGITS + "." + DataRecords.DIGIT).value(42)
                                .withParameter(DataSources.LETTERS + "." + DataRecords.LETTER).value("Z")
                        )
                        .withDataSources(
                                fromDataRecords(DataSources.DIGITS, digits),
                                fromDataRecords(DataSources.LETTERS, letters)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(42, "Z", 42, "Z", 42, "Z");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withDataRecords() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andMultipleDataRecords(RxDataRecord, RxDataRecord)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_MULTI_DATA_RECORDS)
                                .withParameter(DataSources.DIGITS + "." + DataRecords.DIGIT).value(42)
                                .withParameter(DataSources.LETTERS + "." + DataRecords.LETTER).value("Z")
                        )
                        .withDataSources(
                                fromDataRecords(DataSources.DIGITS, digits),
                                fromDataRecords(DataSources.LETTERS, letters)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(42, "Z", 42, "Z", 42, "Z");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withComplexParameter() throws Exception {
        RxDataRecord[] nodes = new RxDataRecord[]{
                RxBasicDataRecord.fromValues(DataRecords.NETWORK_ELEMENT_ID, "SGSN-14B", DataRecords.NODE_TYPE, "SGSN-MME"),
                RxBasicDataRecord.fromValues(DataRecords.NETWORK_ELEMENT_ID, "LTE01ERB", DataRecords.NODE_TYPE, "ERBS"),
                RxBasicDataRecord.fromValues(DataRecords.NETWORK_ELEMENT_ID, "LTE08dg2", DataRecords.NODE_TYPE, "RadioNode")
        };

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andComplexParameter(TafNode)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_COMPLEX_PARAM)
                                .withParameter(DataSources.COMPLEX + "." + DataRecords.NODE_TYPE).value("someType")
                                .withParameter(DataSources.COMPLEX + "." + DataRecords.NETWORK_ELEMENT_ID).value("someId")
                        )
                        .withDataSources(fromDataRecords(DataSources.COMPLEX, nodes))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly("someType", "someId", "someType", "someId", "someType", "someId");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withThreeParameters_andMultipleNestedFlows() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");
        RxDataRecord[] signs = dataRecords(DataRecords.SIGN, "+", "-", "=");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addSubFlow(flow()
                                .addSubFlow(flow()
                                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andThreeParameters(Integer, String, String)} */
                                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_THREE_PARAMS)
                                                .withParameter(DataRecords.DIGIT).value(42)
                                                .withParameter(DataRecords.LETTER).value("Z")
                                                .withParameter(DataRecords.SIGN).value("*")
                                        )
                                        .withDataSources(fromDataRecords(DataSources.SIGNS, signs))
                                )
                                .withDataSources(fromDataRecords(DataSources.LETTERS, letters))
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsOnly(42, "Z", "*");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withThreeParameters_andMixedDataSources_1() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");
        RxDataRecord[] signs = dataRecords(DataRecords.SIGN, "+", "-", "=");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andThreeParameters(Integer, String, String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_THREE_PARAMS)
                                        .withParameter(DataRecords.DIGIT).value(42)
                                        .withParameter(DataRecords.LETTER).value("Z")
                                        .withParameter(DataRecords.SIGN).value("*")
                                )
                                .withDataSources(fromDataRecords(DataSources.SIGNS, signs))
                        )
                        .withDataSources(
                                fromDataRecords(DataSources.DIGITS, digits),
                                fromDataRecords(DataSources.LETTERS, letters)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsOnly(42, "Z", "*");
    }

    @Test
    public void withParameter_shouldOverride_dataRecordsDataSource_withThreeParameters_andMixedDataSources_2() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");
        RxDataRecord[] signs = dataRecords(DataRecords.SIGN, "+", "-", "=");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andThreeParameters(Integer, String, String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_THREE_PARAMS)
                                        .withParameter(DataRecords.DIGIT).value(42)
                                        .withParameter(DataRecords.LETTER).value("Z")
                                        .withParameter(DataRecords.SIGN).value("*")
                                )
                                .withDataSources(
                                        fromDataRecords(DataSources.LETTERS, letters),
                                        fromDataRecords(DataSources.SIGNS, signs)
                                )
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsOnly(42, "Z", "*");
    }

    /*-------- Tests for scenario runtime validations --------*/

    @Test
    public void scenarioRun_shouldThrowNullPointerException_whenTestStepParameterHasNoValue() throws Exception {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Parameter 'nonExisting' for test step 'FlawedTestSteps#testStepParameterHasNoValue' value is null");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link FlawedTestSteps#testStepParameterHasNoValue(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(flawedTestSteps, FlawedTestSteps.TEST_STEP_NO_PARAMETER_VALUE))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void scenarioRun_shouldThrowIllegalArgumentException_whenTestStepParameterHasWrongType() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("digit");
        thrown.expectMessage("Integer");
        thrown.expectMessage("String");

        RxDataRecord[] letters = dataRecords(DataRecords.DIGIT, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_PARAM))
                        .withDataSources(fromDataRecords(DataSources.DIGITS, letters))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void scenarioRun_shouldThrowIllegalArgumentException_whenTestStepFullNameParameterHasWrongType() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("digit");
        thrown.expectMessage("Integer");
        thrown.expectMessage("String");

        RxDataRecord[] letters = dataRecords(DataRecords.DIGIT, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleFullNameParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_FULL_NAME_PARAM))
                        .withDataSources(fromDataRecords(DataSources.DIGITS, letters))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void scenarioRun_shouldThrowIllegalArgumentException_whenTestStepOverridingParameterHasWrongType() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("digit");
        thrown.expectMessage("Integer");
        thrown.expectMessage("String");

        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_PARAM)
                                .withParameter(DataRecords.DIGIT).value("definitely not a digit")
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void scenarioRun_shouldThrowIllegalArgumentException_whenTestStepOverridingFullNameParameterHasWrongType() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("digit");
        thrown.expectMessage("Integer");
        thrown.expectMessage("String");

        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleFullNameParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_FULL_NAME_PARAM)
                                .withParameter(DataSources.DIGITS + "." + DataRecords.DIGIT).value("definitely not a digit")
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void scenarioRun_shouldThrowException_whenTestStepThrowsException() throws Exception {
        thrown.expect(FlawedTestSteps.VerySpecialException.class);
        thrown.expectMessage("This is a very special exception");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link FlawedTestSteps#testStepThrowingException()} */
                        .addTestStep(TafRxScenarios.annotatedMethod(flawedTestSteps, FlawedTestSteps.TEST_STEP_THROWING_EXCEPTION))
                )
                .build();

        RxApi.run(scenario);
    }

    /*-------- Test for Test Steps without Data Sources --------*/

    @Test
    public void verifyValidTestSteps_shouldCallTestStepFromParentClass() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ParentTestSteps#testStepFromParentClass()} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ParentTestSteps.TEST_STEP_FROM_PARENT_CLASS))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly("parent");
    }

    @Test
    public void verifyValidTestSteps_withoutDataSources() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStepWithoutParameters()} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_NO_PARAMS))
                        /** {@link ValidTestSteps#testStepWithOptionalParameter(Object)} ()} (Integer)} (Node)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_OPTIONAL_PARAM))
                        /** {@link ValidTestSteps#testStepWithOptionalDefaultParameter(Object)} ()} (Integer)} (Node)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DEFAULT_PARAM))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly("nop", null, "default");
    }

    /*-------- Tests for Test Steps with Data Sources from Iterable --------*/

    @Test
    public void verifyValidTestSteps_withIterableDataSource_andParameters() throws Exception {
        List<Integer> digits = newArrayList(1, 2, 3);
        List<String> letters = newArrayList("A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withIterableDataSource_andSingleParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_ITERABLE_SINGLE_PARAM))
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withIterableDataSource_andMultipleParameters(Integer, String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_ITERABLE_MULTI_PARAMS))
                                .withDataSources(fromIterable(DataSources.LETTERS, letters))
                        )
                        .withDataSources(fromIterable(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(
                1, 1, "A", 1, "B", 1, "C",
                2, 2, "A", 2, "B", 2, "C",
                3, 3, "A", 3, "B", 3, "C"
        );
    }

    @Test
    public void verifyValidTestSteps_withIterableDataSource_andDataRecords() throws Exception {
        List<Integer> digits = newArrayList(1, 2, 3);
        List<String> letters = newArrayList("A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withIterableDataSource_andSingleDataRecord(RxDataRecord)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_ITERABLE_SINGLE_DATA_RECORD))
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withIterableDataSource_andMultipleDataRecords(RxDataRecord, RxDataRecord)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_ITERABLE_MULTI_DATA_RECORDS))
                                .withDataSources(fromIterable(DataSources.LETTERS, letters))
                        )
                        .withDataSources(fromIterable(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(
                1, 1, "A", 1, "B", 1, "C",
                2, 2, "A", 2, "B", 2, "C",
                3, 3, "A", 3, "B", 3, "C"
        );
    }

    /*-------- Tests for Test Steps with Data Sources from Data Records --------*/

    @Test
    public void verifyValidTestSteps_withDataRecordsDataSource_andParameters() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_PARAM))
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andMultipleParameters(Integer, String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_MULTI_PARAMS))
                                .withDataSources(fromDataRecords(DataSources.LETTERS, letters))
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(
                1, 1, "A", 1, "B", 1, "C",
                2, 2, "A", 2, "B", 2, "C",
                3, 3, "A", 3, "B", 3, "C"
        );
    }

    @Test
    public void verifyValidTestSteps_withDataRecordsDataSource_andFullNameParameters() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleFullNameParameter(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_FULL_NAME_PARAM))
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andMultipleFullNameParameters(Integer, String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_MULTI_FULL_NAME_PARAMS))
                                .withDataSources(fromDataRecords(DataSources.LETTERS, letters))
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(
                1, 1, "A", 1, "B", 1, "C",
                2, 2, "A", 2, "B", 2, "C",
                3, 3, "A", 3, "B", 3, "C"
        );
    }

    @Test
    public void verifyValidTestSteps_withDataRecordsDataSource_andDataRecords() throws Exception {
        RxDataRecord[] digits = dataRecords(DataRecords.DIGIT, 1, 2, 3);
        RxDataRecord[] letters = dataRecords(DataRecords.LETTER, "A", "B", "C");

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andSingleDataRecord(RxDataRecord)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_SINGLE_DATA_RECORD))
                        .addSubFlow(flow()
                                /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andMultipleDataRecords(RxDataRecord, RxDataRecord)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_MULTI_DATA_RECORDS))
                                .withDataSources(fromDataRecords(DataSources.LETTERS, letters))
                        )
                        .withDataSources(fromDataRecords(DataSources.DIGITS, digits))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(
                1, 1, "A", 1, "B", 1, "C",
                2, 2, "A", 2, "B", 2, "C",
                3, 3, "A", 3, "B", 3, "C"
        );
    }

    @Test
    public void verifyValidTestSteps_withDataRecordsDataSource_andComplexParameter() throws Exception {
        RxDataRecord[] nodes = new RxDataRecord[]{
                RxBasicDataRecord.fromValues(DataRecords.NETWORK_ELEMENT_ID, "SGSN-14B", DataRecords.NODE_TYPE, "SGSN-MME"),
                RxBasicDataRecord.fromValues(DataRecords.NETWORK_ELEMENT_ID, "LTE01ERB", DataRecords.NODE_TYPE, "ERBS"),
                RxBasicDataRecord.fromValues(DataRecords.NETWORK_ELEMENT_ID, "LTE08dg2", DataRecords.NODE_TYPE, "RadioNode")
        };

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStep_withDataRecordsDataSource_andComplexParameter(TafNode)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_DATA_RECORDS_COMPLEX_PARAM))
                        .withDataSources(fromDataRecords(DataSources.COMPLEX, nodes))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly(
                "SGSN-MME", "SGSN-14B",
                "ERBS", "LTE01ERB",
                "RadioNode", "LTE08dg2"
        );
    }

    @Test
    public void typeConversion() throws Exception {
        RxDataSource<RxDataRecord> users = fromDataRecords("user",
                RxBasicDataRecord.fromValues(
                        TafUser.ID, 1,
                        TafUser.USERNAME, "objects",
                        TafUser.ENABLED, true
                ),
                RxBasicDataRecord.fromValues(
                        TafUser.ID, "1",
                        TafUser.USERNAME, "strings",
                        TafUser.ENABLED, "true"
                )
        );

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStepConversions} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_CONVERSIONS))
                        .withDataSources(users)
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly("objects", "strings");
    }

    @Test
    public void tafTypeConversion() throws Exception {
        final String dataSourceName = "user";
        TestContext testContext = ServiceRegistry.getTestContextProvider().get();
        testContext.addDataSource(dataSourceName, TafDataSources.fromCsv("data/user.csv"));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link ValidTestSteps#testStepConversions} */
                        .addTestStep(TafRxScenarios.annotatedMethod(validTestSteps, ValidTestSteps.TEST_STEP_CONVERSIONS))
                        .withDataSources(TafRxScenarios.dataSource(dataSourceName, TafUser.class))
                )
                .build();

        RxApi.run(scenario);

        assertThat(validTestSteps.stack).containsExactly("administrator1", "administrator2", "disabledAdmin", "user");
    }

    private RxDataRecord[] dataRecords(String key, Object... values) {
        RxDataRecord[] dataRecords = new RxDataRecord[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            dataRecords[i] = RxBasicDataRecord.fromValues(key, value);
        }
        return dataRecords;
    }

    private static class FlawedTestSteps extends ParentTestSteps {

        private static final String TEST_STEP_DOES_NOT_EXIST = "testStepDoesNotExist";
        private static final String TEST_STEP_PRIVATE_ACCESS = "testStepWithPrivateAccessModifier";
        private static final String TEST_STEP_NO_PARAMETER_ANNOTATIONS = "testStepParametersWithoutAnnotations";
        private static final String TEST_STEP_NO_PARAMETER_VALUE = "testStepParameterHasNoValue";
        private static final String TEST_STEP_THROWING_EXCEPTION = "testStepThrowingException";

        @TestStep(id = TEST_STEP_PRIVATE_ACCESS)
        private void testStepWithPrivateAccessModifier() {
            throw new RuntimeException("This method should never get invoked");
        }

        @TestStep(id = TEST_STEP_NO_PARAMETER_ANNOTATIONS)
        public void testStepParametersWithoutAnnotations(Integer param1, String param2) {
            throw new RuntimeException("This method should never get invoked");
        }

        @TestStep(id = TEST_STEP_NO_PARAMETER_VALUE)
        public void testStepParameterHasNoValue(@Input(DataSources.NON_EXISTING) Integer param) {
            throw new RuntimeException("This method should never get invoked");
        }

        @TestStep(id = TEST_STEP_THROWING_EXCEPTION)
        public void testStepThrowingException() {
            throw new VerySpecialException("This is a very special exception");
        }

        private static class VerySpecialException extends RuntimeException {
            VerySpecialException(String message) {
                super(message);
            }
        }
    }

    private static class ValidTestSteps extends ParentTestSteps {

        private static final String TEST_STEP_NO_PARAMS = "testStepWithoutParameters";
        private static final String TEST_STEP_OPTIONAL_PARAM = "testStepWithOptionalParameter";
        private static final String TEST_STEP_DEFAULT_PARAM = "testStepWithOptionalDefaultParameter";

        private static final String TEST_STEP_ITERABLE_SINGLE_PARAM = "testStep_withIterableDataSource_andSingleParameter";
        private static final String TEST_STEP_ITERABLE_MULTI_PARAMS = "testStep_withIterableDataSource_andMultipleParameters";
        private static final String TEST_STEP_ITERABLE_SINGLE_DATA_RECORD = "testStep_withIterableDataSource_andSingleDataRecord";
        private static final String TEST_STEP_ITERABLE_MULTI_DATA_RECORDS = "testStep_withIterableDataSource_andMultipleDataRecords";

        private static final String TEST_STEP_DATA_RECORDS_SINGLE_PARAM = "testStep_withDataRecordsDataSource_andSingleParameter";
        private static final String TEST_STEP_DATA_RECORDS_MULTI_PARAMS = "testStep_withDataRecordsDataSource_andMultipleParameters";
        private static final String TEST_STEP_DATA_RECORDS_SINGLE_FULL_NAME_PARAM = "testStep_withDataRecordsDataSource_andSingleFullNameParameter";
        private static final String TEST_STEP_DATA_RECORDS_MULTI_FULL_NAME_PARAMS = "testStep_withDataRecordsDataSource_andMultipleFullNameParameters";
        private static final String TEST_STEP_DATA_RECORDS_SINGLE_DATA_RECORD = "testStep_withDataRecordsDataSource_andSingleDataRecord";
        private static final String TEST_STEP_DATA_RECORDS_MULTI_DATA_RECORDS = "testStep_withDataRecordsDataSource_andMultipleDataRecords";
        private static final String TEST_STEP_DATA_RECORDS_COMPLEX_PARAM = "testStep_withDataRecordsDataSource_andComplexParameter";
        private static final String TEST_STEP_DATA_RECORDS_THREE_PARAMS = "testStep_withDataRecordsDataSource_andThreeParameters";
        private static final String TEST_STEP_CONVERSIONS = "testStepConversions";

        /*-------- Test Steps without Data Sources --------*/

        @TestStep(id = TEST_STEP_NO_PARAMS)
        public void testStepWithoutParameters() {
            System.out.println("Test Step without parameters");
            stack.push("nop");
        }

        @TestStep(id = TEST_STEP_OPTIONAL_PARAM)
        public void testStepWithOptionalParameter(@OptionalValue @Input(DataSources.NON_EXISTING) Object param) {
            System.out.printf("Test Step with optional parameter: '%s'%n", param);
            stack.push(param);
        }

        @TestStep(id = TEST_STEP_DEFAULT_PARAM)
        public void testStepWithOptionalDefaultParameter(@OptionalValue("default") @Input(DataSources.NON_EXISTING) Object param) {
            System.out.printf("Test Step with optional parameter with default value: '%s'%n", param);
            stack.push(param);
        }

        /*-------- Test Steps for Data Sources from Iterable --------*/

        @TestStep(id = TEST_STEP_ITERABLE_SINGLE_PARAM)
        public void testStep_withIterableDataSource_andSingleParameter(@Input(DataSources.DIGITS) Integer param) {
            System.out.printf("Test Step with iterable Data Source and single parameter: '%s'%n", param);
            stack.push(param);
        }

        @TestStep(id = TEST_STEP_ITERABLE_MULTI_PARAMS)
        public void testStep_withIterableDataSource_andMultipleParameters(@Input(DataSources.DIGITS) Integer param1,
                                                                          @Output(DataSources.LETTERS) String param2) {
            System.out.printf("Test Step with iterable Data Source and multiple parameters: '%d' and '%s'%n", param1, param2);
            stack.push(param1);
            stack.push(param2);
        }

        @TestStep(id = TEST_STEP_ITERABLE_SINGLE_DATA_RECORD)
        public void testStep_withIterableDataSource_andSingleDataRecord(@Input(DataSources.DIGITS) RxDataRecord dataRecord) {
            System.out.printf("Test Step with iterable Data Source and single Data Record: '%s'%n", dataRecord);
            stack.push(dataRecord.getFieldValue(DataSources.DIGITS));
        }

        @TestStep(id = TEST_STEP_ITERABLE_MULTI_DATA_RECORDS)
        public void testStep_withIterableDataSource_andMultipleDataRecords(@Input(DataSources.DIGITS) RxDataRecord dataRecord1,
                                                                           @Output(DataSources.LETTERS) RxDataRecord dataRecord2) {
            System.out.printf("Test Step with iterable Data Source and multiple Data Records: '%s' and '%s'%n", dataRecord1, dataRecord2);
            stack.push(dataRecord1.getFieldValue(DataSources.DIGITS));
            stack.push(dataRecord2.getFieldValue(DataSources.LETTERS));
        }

        /*-------- Test Steps for Data Sources from Data Records --------*/

        @TestStep(id = TEST_STEP_DATA_RECORDS_SINGLE_PARAM)
        public void testStep_withDataRecordsDataSource_andSingleParameter(@Input(DataRecords.DIGIT) Integer param) {
            System.out.printf("Test Step with data records Data Source and single parameter: '%s'%n", param);
            stack.push(param);
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_MULTI_PARAMS)
        public void testStep_withDataRecordsDataSource_andMultipleParameters(@Input(DataRecords.DIGIT) Integer param1,
                                                                             @Output(DataRecords.LETTER) String param2) {
            System.out.printf("Test Step with data records Data Source and multiple parameters: '%d' and '%s'%n", param1, param2);
            stack.push(param1);
            stack.push(param2);
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_SINGLE_FULL_NAME_PARAM)
        public void testStep_withDataRecordsDataSource_andSingleFullNameParameter(@Input(DataSources.DIGITS + "." + DataRecords.DIGIT) Integer param) {
            System.out.printf("Test Step with data records Data Source and single full name parameter: '%d'%n", param);
            stack.push(param);
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_MULTI_FULL_NAME_PARAMS)
        public void testStep_withDataRecordsDataSource_andMultipleFullNameParameters(@Input(DataSources.DIGITS + "." + DataRecords.DIGIT) Integer param1,
                                                                                     @Output(DataSources.LETTERS + "." + DataRecords.LETTER) String param2) {
            System.out.printf("Test Step with data records Data Source and multiple full name parameters: '%d' and '%s'%n", param1, param2);
            stack.push(param1);
            stack.push(param2);
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_SINGLE_DATA_RECORD)
        public void testStep_withDataRecordsDataSource_andSingleDataRecord(@Input(DataSources.DIGITS) RxDataRecord dataRecord) {
            System.out.printf("Test Step with data records Data Source and single Data Record: '%s'%n", dataRecord);
            stack.push(dataRecord.getFieldValue(DataRecords.DIGIT));
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_MULTI_DATA_RECORDS)
        public void testStep_withDataRecordsDataSource_andMultipleDataRecords(@Input(DataSources.DIGITS) RxDataRecord dataRecord1,
                                                                              @Output(DataSources.LETTERS) RxDataRecord dataRecord2) {
            System.out.printf("Test Step with data records Data Source and multiple Data Records: '%s' and '%s'%n", dataRecord1, dataRecord2);
            stack.push(dataRecord1.getFieldValue(DataRecords.DIGIT));
            stack.push(dataRecord2.getFieldValue(DataRecords.LETTER));
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_COMPLEX_PARAM)
        public void testStep_withDataRecordsDataSource_andComplexParameter(@Input(DataSources.COMPLEX) TafNode param) {
            String nodeType = param.getNodeType();
            String networkElementId = param.getNetworkElementId();
            System.out.printf("Test Step with data records Data Source and complex Data Record parameter: 'Node{type=%s, id=%s}'%n", nodeType, networkElementId);
            stack.push(nodeType);
            stack.push(networkElementId);
        }

        @TestStep(id = TEST_STEP_DATA_RECORDS_THREE_PARAMS)
        public void testStep_withDataRecordsDataSource_andThreeParameters(@Input(DataRecords.DIGIT) Integer param1,
                                                                          @Input(DataRecords.LETTER) String param2,
                                                                          @Output(DataRecords.SIGN) String param3) {
            System.out.printf("Test Step with data records Data Source and three parameters: '%d', '%s' and '%s'%n", param1, param2, param3);
            stack.push(param1);
            stack.push(param2);
            stack.push(param3);
        }

        @TestStep(id = TEST_STEP_CONVERSIONS)
        public void testStepConversions(
                @Input("user") TafUser user,
                @Input("user") PrimitiveTafUser primitiveUser,
                @Input(TafUser.ID) Integer id,
                @Input(TafUser.ENABLED) Boolean enabled,
                @Input(TafUser.USERNAME) String username) {

            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getEnabled()).isNotNull();
            assertThat(user.getUsername()).isNotNull();

            assertThat(primitiveUser).isNotNull();
            assertThat(primitiveUser.getId()).isNotNull();
            assertThat(primitiveUser.getEnabled()).isNotNull();
            assertThat(primitiveUser.getUsername()).isNotNull();

            assertThat(id).isNotNull();
            assertThat(enabled).isNotNull();
            assertThat(username).isNotNull();

            stack.push(user.getUsername());
        }
    }

    private static class ParentTestSteps {

        private static final String TEST_STEP_FROM_PARENT_CLASS = "testStepFromParentClass";

        Stack<Object> stack = new Stack<>();

        @TestStep(id = TEST_STEP_FROM_PARENT_CLASS)
        public void testStepFromParentClass() {
            System.out.println("Test Step from parent class");
            stack.push("parent");
        }
    }

    private interface DataSources {
        String DIGITS = "digits";
        String LETTERS = "letters";
        String SIGNS = "signs";
        String COMPLEX = "complex";
        String NON_EXISTING = "nonExisting";
    }

    private interface DataRecords {
        String DIGIT = "digit";
        String LETTER = "letter";
        String SIGN = "sign";
        String NODE_TYPE = "nodeType";
        String NETWORK_ELEMENT_ID = "networkElementId";
    }

    public interface PrimitiveTafUser extends DataRecord {
        int getId();

        String getUsername();

        boolean getEnabled();
    }
}
