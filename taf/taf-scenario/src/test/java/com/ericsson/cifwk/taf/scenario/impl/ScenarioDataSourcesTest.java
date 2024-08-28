/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataSourceControl;
import com.ericsson.cifwk.taf.datasource.GenericDataSource;
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinitionBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.copyDataSource;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromTafDataProvider;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.makeDataSourceCyclic;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shareDataSource;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.iterable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ScenarioDataSourcesTest extends ScenarioTest {

    private static final String STEP_CHECK_DATARECORD_NAME = "nodePreparation";
    private static final String STEP_CHECK_DATARECORD_FROM_PROVIDER = "stepCheckDatarecordFromProvider";
    private static final String STEP_PUSH_MY_DS = "PUSH_MY_DS";
    private static final String STEP_PUSH_MY_DS_1 = "PUSH_MY_DS_1";
    private static final String STEP_ADD_TO_DATASOURCE = "STEP_ADD_TO_DATASOURCE";
    private static final String EMPTY_DS_ERROR_MESSAGE = " produce any records. Check filtering and sharing";

    private final AtomicInteger incrementCounter = new AtomicInteger();

    @Test
    public void shouldReturnDataRecordName() {
        TestDataSource<DataRecord> myDs = fromCsv("data/node.csv");
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, myDs);

        TestStepFlow myFlow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_CHECK_DATARECORD_NAME))
                .withDataSources(
                        dataSource(MY_DATA_SOURCE_1)
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(myFlow)
                .build();

        runner.start(scenario);
    }

    @TestStep(id = STEP_CHECK_DATARECORD_NAME)
    public void stepCheckDatarecordName(@Input(MY_DATA_SOURCE_1) DataRecord dataRecord) {
        assertThat(dataRecord.getDataSourceName(), equalTo(MY_DATA_SOURCE_1));
    }

    @Test
    public void shouldResetSharedIteratorBetweenScenarioRuns_CIS_5829() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared(csvDataSource));

        final TestStepFlow syncFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(dataSource(MY_DATA_SOURCE_1)).build();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(syncFlow).build();

        runner.start(scenario);

        final TestStepFlow syncConfirmFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(dataSource(MY_DATA_SOURCE_1)).build();

        scenario = scenario("Scenario 2")
                .addFlow(syncConfirmFlow).build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(6));
    }

    @Test
    public void shouldResetSharedIteratorBetweenScenarioRuns_CIS_7434() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared(csvDataSource));

        final TestStepFlow syncFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(dataSource(MY_DATA_SOURCE_1)).build();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(syncFlow).build();

        runner.start(scenario);

        final TestStepFlow syncConfirmFlow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(dataSource(MY_DATA_SOURCE_1)).build();

        final TestDataSource<DataRecord> dataSource = context.dataSource(MY_DATA_SOURCE_1);
        context.addDataSource(MY_DATA_SOURCE_1, shared(dataSource));
        scenario = scenario("Scenario 2")
                .addFlow(syncConfirmFlow).build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(6));
    }

    @Test
    public void shouldResetSharedIteratorBetweenSteps() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared(csvDataSource));

        final TestStepFlow syncFlow = flow("Test")
                .addTestStep(runnable(counter))
                .addTestStep(runnable(counter))
                .addTestStep(runnable(counter))
                .withDataSources(dataSource(MY_DATA_SOURCE_1))
                .build();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(syncFlow).build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(9));
    }

    @Test
    public void shouldResetSharedIteratorBetweenFlows() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared(csvDataSource));

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(
                        flow("A")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .addFlow(
                        flow("B")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .addFlow(
                        flow("C")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(9));
    }

    @Test
    public void shouldSupportDataSourceColumnBinding() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSource(CSV_DATA_SOURCE)
                                        .bindColumn("value", "integer")
                                        .bindColumn("attribute", "string")
                                        .inTestStep(STEP_PUSH_GLOBAL_DS_TO_STACK)
                        )
                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS_TO_STACK))
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "111", "attr1",
                "222", "attr2",
                "333", "attr3"));
    }

    @Test
    public void shouldSupportDataSourceColumnBindingWithIterables() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                iterable("iterable", prepareList(2))
                                        .bindColumn("x", "integer")
                        )
                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS_TO_STACK))
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "0", null,
                "1", null));
    }

    @Test
    public void shouldSupportDataSourceBinding() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSource(GLOBAL_DATA_SOURCE)
                                        .bindTo(MY_DATA_SOURCE_1)
                                        .bindTo(MY_DATA_SOURCE_2)
                                        .inTestStep(STEP_PUSH_MY_DS)
                                        .bindTo(MY_DATA_SOURCE_3)
                                        .inTestStep(STEP_DUMMY)
                        )
                        .addTestStep(annotatedMethod(this, STEP_PUSH_MY_DS))
                        .addTestStep(annotatedMethod(this, STEP_DUMMY))
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "1", "A", "1", "A", "1", "A", "0", null,
                "2", "B", "2", "B", "2", "B", "0", null));
    }

    @Test
    public void shouldSupportDataSourceBindingWithVUsers() {
        int vUsers = 100;
        DataSourceDefinitionBuilder dataSourceDefinitionBuilder = dataSource(GLOBAL_DATA_SOURCE);

        for (int i = 0; i < 100; i++) {
            dataSourceDefinitionBuilder.bindTo("a" + i);
        }

        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSourceDefinitionBuilder
                        )
                        .withVusers(vUsers)
                        .addTestStep(annotatedMethod(this, STEP_PUSH_GLOBAL_DS_TO_STACK))
                ).build();

        runner.start(scenario);
        assertThat(stack, hasSize(2 /*fields*/ * 2 /*records*/ * vUsers));
    }

    @Test
    public void shouldSupportDataSourceBindingPropagatedToSubflows() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSource(GLOBAL_DATA_SOURCE)
                                        .bindTo(MY_DATA_SOURCE_1)
                                        .bindTo(MY_DATA_SOURCE_2)
                        )
                        .addSubFlow(flow("subflow")
                                .addTestStep(annotatedMethod(this, STEP_PUSH_MY_DS))
                        )
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "1", "A", "1", "A", "1", "A", "0", null,
                "2", "B", "2", "B", "2", "B", "0", null));
    }

    @Test
    public void shouldSupportDataSourceBindingInSubflows() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();

        TestDataSource<DataRecord> myDataSource1 = context.dataSource(MY_DATA_SOURCE_1);
        myDataSource1.addRecord()
                .setField("integer", 3)
                .setField("string", "C");
        myDataSource1.addRecord()
                .setField("integer", 4)
                .setField("string", "D");

        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSource(GLOBAL_DATA_SOURCE)
                        )
                        .addSubFlow(
                                flow("subflow")
                                        .addTestStep(annotatedMethod(this, STEP_PUSH_MY_DS))
                                        .withDataSources(dataSource(MY_DATA_SOURCE_1)
                                                .bindTo(MY_DATA_SOURCE_2))
                        )
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "1", "A", "3", "C", "3", "C", "0", null,
                "1", "A", "4", "D", "4", "D", "0", null,
                "2", "B", "3", "C", "3", "C", "0", null,
                "2", "B", "4", "D", "4", "D", "0", null));
    }

    @TestStep(id = STEP_PUSH_MY_DS)
    public void pushFromMyDS(
            @Input(GLOBAL_DATA_SOURCE + ".string") String globalStringParam,
            @Input(GLOBAL_DATA_SOURCE + ".integer") Integer globalIntegerParam,
            @Input(MY_DATA_SOURCE_1 + ".string") String myStringParam1,
            @Input(MY_DATA_SOURCE_1 + ".integer") Integer myIntegerParam1,
            @Input(MY_DATA_SOURCE_2 + ".string") String myStringParam2,
            @Input(MY_DATA_SOURCE_2 + ".integer") Integer myIntegerParam2,
            @Input(MY_DATA_SOURCE_3 + ".string") String myStringParam3,
            @Input(MY_DATA_SOURCE_3 + ".integer") Integer myIntegerParam3) {

        stack.push(String.valueOf(globalIntegerParam));
        stack.push(globalStringParam);

        stack.push(String.valueOf(myIntegerParam1));
        stack.push(myStringParam1);

        stack.push(String.valueOf(myIntegerParam2));
        stack.push(myStringParam2);

        stack.push(String.valueOf(myIntegerParam3));
        stack.push(myStringParam3);
    }

    @Test
    public void shouldSupportDataSourceBindingWithIterables() {
        TestScenario scenario = scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                iterable("iterable", prepareList(2))
                                        .bindTo(MY_DATA_SOURCE_1)
                        )
                        .addTestStep(annotatedMethod(this, STEP_PUSH_MY_DS_1))
                ).build();

        runner.start(scenario);
        assertThat(stack, contains(
                "0", "1"));
    }

    @TestStep(id = STEP_PUSH_MY_DS_1)
    public void pushFromMyDS1(
            @Input(MY_DATA_SOURCE_1 + ".x") Integer myIntegerParam1) {
        stack.push(String.valueOf(myIntegerParam1));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfWrongBuilderStepOrder() {
        scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSource(GLOBAL_DATA_SOURCE)
                                        .inTestStep(STEP_PUSH_MY_DS)
                                        .bindTo(MY_DATA_SOURCE_1)
                        )
                        .addTestStep(annotatedMethod(this, STEP_PUSH_MY_DS))
                ).build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfNotExistingTestStep() {
        scenario()
                .addFlow(flow("A")
                        .withDataSources(
                                dataSource(GLOBAL_DATA_SOURCE)
                                        .bindTo(MY_DATA_SOURCE_1)
                                        .inTestStep("NOT_EXISTING_TEST_STEP")
                        )
                ).build();
    }

    @Test
    public void shouldSupportConcurrentDataSourceModification() {
        final String AVAILABLE_USERS = "availableUsers";
        final TestContext context = ServiceRegistry.getTestContextProvider().get();
        final int vusers = 31;

        context.dataSource(AVAILABLE_USERS);

        TestScenario scenario = scenario()
                .addFlow(
                        flow("Create user")
                                .addTestStep(annotatedMethod(this, STEP_ADD_TO_DATASOURCE))
                                .afterFlow(new Runnable() {
                                    @Override
                                    public void run() {
                                        TestDataSource<DataRecord> dataSource = context.dataSource(AVAILABLE_USERS);
                                        context.addDataSource(AVAILABLE_USERS, shared(dataSource));
                                    }
                                })
                )
                .addFlow(
                        flow("Login")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(AVAILABLE_USERS))
                )
                .withDefaultVusers(vusers)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(vusers));
    }

    @Test
    public void shouldSupportDataSourceConversion() {
        final TestContext context = ServiceRegistry.getTestContextProvider().get();
        final int vusers = 3;

        final String DS_NAME = "availableUsers";
        TestDataSource<DataRecord> dataSource = context.dataSource(DS_NAME);
        context.addDataSource(DS_NAME, shared(dataSource));

        TestScenario scenario = scenario()
                .addFlow(
                        flow("Copy shared dataSource and populate it with data")
                                .beforeFlow(copyDataSource(DS_NAME))
                                .addTestStep(annotatedMethod(this, STEP_ADD_TO_DATASOURCE))
                )
                .addFlow(
                        flow("Run test step with dataSource")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(DS_NAME))
                                .afterFlow(assertAndResetCounter(vusers * vusers))
                )
                .addFlow(
                        flow("Make dataSource shared and run test step with dataSource")
                                .beforeFlow(shareDataSource(DS_NAME))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(DS_NAME))
                                .afterFlow(assertAndResetCounter(vusers))
                )
                .addFlow(
                        flow("Make dataSource cyclic and shared and run test step with dataSource")
                                .beforeFlow(
                                        makeDataSourceCyclic(DS_NAME),
                                        shareDataSource(DS_NAME))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(DS_NAME),
                                        dataSource(CSV_DATA_SOURCE))
                                .afterFlow(assertAndResetCounter(vusers * 3))
                )
                .addFlow(
                        flow("Copy dataSource and make it shared")
                                .beforeFlow(
                                        copyDataSource(DS_NAME),
                                        shareDataSource(DS_NAME))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(DS_NAME))
                                .afterFlow(assertAndResetCounter(vusers))
                )
                .withDefaultVusers(vusers)
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldResetDataSource() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("Login")
                                .beforeFlow(shareDataSource(GLOBAL_DATA_SOURCE))
                                .addSubFlow(
                                        flow("subflow1")
                                                .addTestStep(runnable(counter))
                                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                                .addSubFlow(
                                        flow("subflow2")
                                                .beforeFlow(TestScenarios.resetDataSource(GLOBAL_DATA_SOURCE))
                                                .addTestStep(runnable(counter))
                                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                                                .afterFlow(TestScenarios.resetDataSource(GLOBAL_DATA_SOURCE)))
                                .addSubFlow(
                                        flow("subflow3")
                                                .addTestStep(runnable(counter))
                                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE)))
                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(6));
    }

    @Test(expected = InvalidDataSourceException.class)
    public void shouldThrowExceptionOnNonExistingDataSource() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("Login")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource("nonExisting"))
                )
                .build();

        runner.start(scenario);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNonExistingDataProvider() {
        fromTafDataProvider("nonExisting");
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionOnNonExistingCsv() {
        fromCsv("nonExisting.csv").iterator();
    }

    @Test
    public void shouldSupportDataProvider() {
        TestDataSource<DataRecord> myDs = fromTafDataProvider(CLASS_PROVIDER);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, myDs);

        TestScenario scenario = scenario()
                .addFlow(
                        flow("Login")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1))
                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(3));
    }

    @Test
    public void shouldSupportDatasourceLoadStandardAndProvider() {
        TestDataSource<DataRecord> myDs1 = fromTafDataProvider(CLASS_PROVIDER);
        TestDataSource<DataRecord> myDs2 = fromTafDataProvider(CLASS_PROVIDER);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, myDs1);
        context.addDataSource(MY_DATA_SOURCE_2, myDs2);

        TestScenario scenario = scenario()
                .addFlow(
                        flow("Login")
                                .addTestStep(annotatedMethod(this, STEP_CHECK_DATARECORD_FROM_PROVIDER))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1),
                                        dataSource(MY_DATA_SOURCE_2),
                                        dataSource(CLASS_PROVIDER))
                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(3));
    }

    @TestStep(id = STEP_CHECK_DATARECORD_FROM_PROVIDER)
    public void stepCheckDatarecordFromProvider(@Input(MY_DATA_SOURCE_1) DataRecord dataRecord1,
                                                @Input(MY_DATA_SOURCE_2) DataRecord dataRecord2,
                                                @Input(CLASS_PROVIDER) DataRecord dataRecord3) {

        assertNotNull(dataRecord1);
        assertNotNull(dataRecord2);
        assertNotNull(dataRecord3);

        count.incrementAndGet();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotSupportDataProviderSharedDataSources() {
        fromTafDataProvider(SHARED_PROVIDER);
    }

    @Test
    public void shouldSupportDataProviderSharedDataSources() {
        TestDataSource<DataRecord> myDs = shared(fromTafDataProvider(SIMPLE_PROVIDER));

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, myDs);

        TestScenario scenario = scenario()
                .addFlow(
                        flow("Login")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1))
                                .withVusers(3)
                )
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(3));
    }

    @Test
    public void shouldSupportOperationsOnDsFromDataProvider() {
        final int vUsers = 3;
        TestScenario scenario = scenario()
                .withDefaultVusers(vUsers)
                .addFlow(
                        flow("Simple run to establish default behaviour")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(SIMPLE_PROVIDER))
                                .afterFlow(assertAndResetCounter(vUsers * vUsers)))
                .addFlow(
                        flow("Shared datasource")
                                .beforeFlow(
                                        shareDataSource(SIMPLE_PROVIDER))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(SIMPLE_PROVIDER))
                                .afterFlow(assertAndResetCounter(vUsers)))
                .addFlow(
                        flow("Copy datasource")
                                .beforeFlow(
                                        copyDataSource(SIMPLE_PROVIDER))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(SIMPLE_PROVIDER))
                                .afterFlow(assertAndResetCounter(vUsers * vUsers)))
                .addFlow(
                        flow("Make datasource cyclic and shared")
                                .beforeFlow(
                                        makeDataSourceCyclic(SIMPLE_PROVIDER),
                                        shareDataSource(SIMPLE_PROVIDER))
                                .addTestStep(annotatedMethod(this, "STEP_CYCLIC_DATASOURCE"))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(SIMPLE_PROVIDER))
                                .afterFlow(assertGreaterThanAndResetCounter(vUsers * vUsers)))
                .addFlow(
                        flow("Copy and reset datasource")
                                .beforeFlow(copyDataSource(SIMPLE_PROVIDER))
                                .beforeFlow(TestScenarios.resetDataSource(SIMPLE_PROVIDER))
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(SIMPLE_PROVIDER))
                                .afterFlow(assertAndResetCounter(vUsers * vUsers)))
                .build();

        runner.start(scenario);
    }

    public static class BasicClassDataSource {
        @DataSource
        public Iterable<Map<String, Integer>> data(ConfigurationSource reader) {
            assertThat(reader.getProperty("parameter"), equalTo("value"));

            List<Map<String, Integer>> list = Lists.newArrayList();
            list.add(ImmutableMap.of("a1", 1, "a2", 2));
            list.add(ImmutableMap.of("b1", 1, "b2", 2));
            list.add(ImmutableMap.of("c1", 1, "c2", 2));

            return list;
        }
    }

    private Runnable assertAndResetCounter(final int assertCount) {
        return new Runnable() {
            @Override
            public void run() {
                assertThat(count.get(), equalTo(assertCount));
                count.set(0);
            }
        };
    }

    private Runnable assertGreaterThanAndResetCounter(final int assertCount) {
        return new Runnable() {
            @Override
            public void run() {
                assertThat(count.get(), greaterThanOrEqualTo(assertCount));
                count.set(0);
            }
        };
    }

    @TestStep(id = STEP_ADD_TO_DATASOURCE)
    public void stepAddToDatasource() {
        final Map<String, Object> map = ImmutableMap.<String, Object>builder().
                put("nodeId", "nodeId").
                put("subscription", "subscription").
                put("collection", "collection").
                put("attribute", "attribute").
                put("value", "value").
                build();
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        DataRecordImpl dataRecord = new DataRecordImpl(map);
        context.dataSource("availableUsers").addRecord().setFields(dataRecord);
    }

    @TestStep(id = "STEP_CYCLIC_DATASOURCE")
    public void stepCyclicDatasource() {
        int iteration = incrementCounter.incrementAndGet();
        if (iteration >= 9) {
            DataSourceControl.stopExecution(SIMPLE_PROVIDER);
            incrementCounter.set(0);
        }
    }

    @Test
    public void shouldNotResetSharedIteratorBetweenSubFlows_CIS_12108() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared(csvDataSource));

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(
                        flow("MainFlow")
                                .addTestStep(sleepForVUser(2, 3))
                                .addSubFlow(flow("A")
                                        .addTestStep(runnable(counter))
                                        .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                                )
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(3));
    }

    @Test
    public void shouldNotResetSharedIteratorOfMainFlow_CIS_12108() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(MY_DATA_SOURCE_1, shared(csvDataSource));

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(
                        flow("MainFlow")
                                .addTestStep(sleepForVUser(2))
                                .addSubFlow(flow("A")
                                        .addTestStep(annotatedMethod(this, STEP_DUMMY))
                                )
                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        assertThat(stack, containsInAnyOrder("attr1", "attr2", "attr3"));
    }

    @Test
    public void shouldFailDataSourceIsEmpty() {
        final String FLOW_NAME = "check for empty datasources";
        final String FAILING_FLOW_NAME = "failing flow";

        TestScenario scenario = scenario()
                .addFlow(flow(FLOW_NAME)
                        .withDataSources(dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty())
                        .addTestStep(runnable(counter))
                )
                .addFlow(flow(FAILING_FLOW_NAME)
                        .withDataSources(
                                dataSource(EMPTY_CSV_DATA_SOURCE)
                        )
                        .addTestStep(runnable(counter))
                ).build();
        try {
            runner.start(scenario);
            fail("Empty data source Runtime exception not thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(EMPTY_DS_ERROR_MESSAGE));
            assertThat(e.getMessage(), containsString(FAILING_FLOW_NAME));
            assertThat(e.getMessage(), not(containsString(FLOW_NAME)));
            assertThat(count.get(), equalTo(0));
        }
    }

    @Test
    @TestSuite
    public void shouldFailDataSourceIsEmpty_ScenarioLevel() {
        final String FLOW_NAME = "flow check for empty datasources";

        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow(FLOW_NAME)
                        .withDataSources(dataSource(EMPTY_CSV_DATA_SOURCE))
                        .addTestStep(runnable(counter))
                )
                .withScenarioDataSources(dataSource(EMPTY_CSV_DATA_SOURCE))
                .build();

        try {
            runner.start(scenario);
            fail("Empty data source Runtime exception not thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(EMPTY_DS_ERROR_MESSAGE));
            assertThat(count.get(), equalTo(0));
        }
    }

    @Test
    public void shouldPassDataSourceEmptyIsAllowed() {

        final String FLOW_NAME = "flow check for empty datasources";
        TestScenario scenario = scenario()
                .addFlow(flow(FLOW_NAME)
                        .withDataSources(
                                dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty()
                        )
                        .addTestStep(runnable(counter))
                ).build();
        runner.start(scenario);
        assertThat(count.get(), equalTo(0));
    }

    @Test
    @TestSuite
    public void shouldPassDataSourceIsEmpty_ScenarioLevel() {

        final String FLOW_NAME = "flow check for empty datasources";

        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow(FLOW_NAME)
                        .addTestStep(runnable(counter)))
                .withScenarioDataSources(dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty())
                .build();

        runner.start(scenario);
        assertThat(count.get(), equalTo(0));
    }

    @Test
    public void shouldPassDataSourceEmpty_GlobalProperty() {

        final String FLOW_NAME = "flow check for empty datasources";
        setSystemAllowEmptyProperty(true);

        TestScenario scenario = scenario()
                .addFlow(flow(FLOW_NAME).addTestStep(runnable(counter))
                        .withDataSources(
                                dataSource(SIMPLE_PROVIDER)))
                .addFlow(flow(FLOW_NAME)
                        .withDataSources(dataSource(EMPTY_CSV_DATA_SOURCE))
                        .addTestStep(runnable(counter)))
                .build();
        try {
            runner.start(scenario);
        } finally {
            setSystemAllowEmptyProperty(false);
        }
        assertThat(count.get(), equalTo(3));

    }

    @Test
    @TestSuite
    public void shouldPassDataSourceIsEmpty_ScenarioLevelGlobalProperty() {

        setSystemAllowEmptyProperty(true);
        final String FLOW_NAME = "flow check for empty datasources";

        TestScenario scenario = dataDrivenScenario()
                .addFlow(flow(FLOW_NAME).addTestStep(runnable(counter))
                        .withDataSources(
                                dataSource(SIMPLE_PROVIDER)))
                .withScenarioDataSources(dataSource(EMPTY_CSV_DATA_SOURCE))
                .build();
        try {
            runner.start(scenario);
        } finally {
            setSystemAllowEmptyProperty(false);
        }
    }

    @Test
    public void shouldPassEmptyDataSourceInParallel() {

        TestScenario scenario = scenario()
                .addFlow(
                        flow("sequential1").addTestStep(runnable(counter))
                ).split(
                        flow("parallel1")
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(SIMPLE_PROVIDER)
                                ),
                        flow("parallel2")
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty()
                                ),
                        flow("parallel3")
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty()
                                )
                ).build();
        runner.start(scenario);
        assertThat(count.get(), equalTo(4));
    }

    @Test
    public void shouldFailEmptyDataSourceInParallel() {

        final String FLOW_NAME = "parallel check for empty datasources";
        final String FAILING_FLOW_NAME = "parallel failing flow";

        TestScenario scenario = scenario()
                .addFlow(
                        flow("sequential1").addTestStep(runnable(counter))
                ).split(
                        flow(FLOW_NAME)
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty()
                                ),
                        flow(FLOW_NAME)
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(EMPTY_CSV_DATA_SOURCE).allowEmpty()
                                ),
                        flow(FAILING_FLOW_NAME)
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(EMPTY_CSV_DATA_SOURCE)
                                )
                ).build();
        try {
            runner.start(scenario);
            fail("Empty data source Runtime exception not thrown");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString(EMPTY_DS_ERROR_MESSAGE));
            assertThat(e.getMessage(), containsString(FAILING_FLOW_NAME));
            assertThat(e.getMessage(), not(containsString(FLOW_NAME)));
            assertThat(count.get(), equalTo(1));
        }
    }

    private void setSystemAllowEmptyProperty(boolean property) {
        System.setProperty("allowEmptyDataSources", Boolean.toString(property));
        assertThat(System.getProperties().getProperty("allowEmptyDataSources"), containsString(Boolean.toString(property)));
    }

    @Test
    public void shouldAvoidDataSourceCreationByMultipleVUsers() {
        final TestContext context = ServiceRegistry.getTestContextProvider().get();
        final int VUSERS = 30;
        final String newDataSource = "newDataSource";

        TestScenario scenario = scenario()
                .addFlow(
                        flow("Create user")
                                .addTestStep(runnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        context.dataSource(newDataSource)
                                                .addRecord().setField("field", "value");
                                    }
                                }))
                )
                .withDefaultVusers(VUSERS)
                .build();

        for (int i = 0; i < 50; i++) {
            context.removeDataSource(newDataSource);
            runner.start(scenario);
            int recordsCount = Iterables.size(context.dataSource(newDataSource));
            assertThat(recordsCount, equalTo(VUSERS));
        }
    }

    @Test
    public void shouldFilterSharedDataSources() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("Data Sources Are Not Reset Between Subflows")
                                .beforeFlow(shareDataSource(CSV_DATA_SOURCE))
                                .addSubFlow(
                                        flow("Push attr3")
                                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                                                .withDataSources(dataSource(CSV_DATA_SOURCE).withFilter("nodeId == 3"))
                                )
                                .addSubFlow(
                                        flow("Push attr1")
                                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                                                .withDataSources(dataSource(CSV_DATA_SOURCE).withFilter("nodeId == 1"))
                                )
                                .addSubFlow(
                                        flow("Push attr2")
                                                .addTestStep(annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                                                .withDataSources(dataSource(CSV_DATA_SOURCE).withFilter("nodeId == 2"))
                                )
                )
                .withDefaultVusers(100)
                .build();

        runner.start(scenario);
        printStack();
        assertThat(stack, contains("attr3", "attr1", "attr2"));
    }

    @Test
    public void shouldShareIfDataSourceIsOnion() throws Exception {
        TestContext context = TafTestContext.getContext();

        TestDataSource<DataRecord> dataSource = context.dataSource(CSV_DATA_SOURCE);
        TestDataSource<DataRecord> wrapper1 = shared(dataSource);
        TestDataSource<DataRecord> wrapper2 = shared(wrapper1);
        TestDataSource<DataRecord> wrapper3 = new GenericDataSource<>(wrapper1, DataRecord.class);
        context.addDataSource(MY_DATA_SOURCE_1, wrapper3);

        wrapper1.iterator().next();
        wrapper2.iterator().next();

        TestScenario scenario = scenario("Scenario 1")
                .addFlow(
                        flow("A")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .addFlow(
                        flow("B")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .addFlow(
                        flow("C")
                                .addTestStep(runnable(counter))
                                .withDataSources(dataSource(MY_DATA_SOURCE_1)))
                .withDefaultVusers(100)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(9));
    }

    @Test
    public void shouldFilterByParentRecords() {
        final TestContext context = ServiceRegistry.getTestContextProvider().get();

        TestDataSource<DataRecord> parentFlowDs = context.dataSource("parentFlowDs");
        parentFlowDs.addRecord().setField("a", "1");
        parentFlowDs.addRecord().setField("a", "2");

        TestDataSource<DataRecord> flowDS = context.dataSource("flowDs");
        flowDS.addRecord().setField("a", "1").setField("b", "11");
        flowDS.addRecord().setField("a", "1").setField("b", "12");
        flowDS.addRecord().setField("a", "2").setField("b", "21");
        flowDS.addRecord().setField("a", "2").setField("b", "22");

        TestDataSource<DataRecord> subFlowDs = context.dataSource("subFlowDs");
        subFlowDs.addRecord().setField("b", "11").setField("c", "111");
        subFlowDs.addRecord().setField("b", "11").setField("c", "112");
        subFlowDs.addRecord().setField("b", "12").setField("c", "121");
        subFlowDs.addRecord().setField("b", "12").setField("c", "122");
        subFlowDs.addRecord().setField("b", "21").setField("c", "211");
        subFlowDs.addRecord().setField("b", "21").setField("c", "212");
        subFlowDs.addRecord().setField("b", "22").setField("c", "221");
        subFlowDs.addRecord().setField("b", "22").setField("c", "222");

        TestScenario scenario = scenario()
                .addFlow(
                        flow("parentFlow")
                                .addSubFlow(
                                        flow("flow")
                                                .addSubFlow(
                                                        flow("subFlow")
                                                                .addTestStep(annotatedMethod(this, "putABC"))
                                                                .withDataSources(dataSource("subFlowDs").withFilter("flowDs.b==b"))
                                                )
                                                .withDataSources(dataSource("flowDs").withFilter("parentFlowDs.a==a"))

                                )
                                .withDataSources(dataSource("parentFlowDs"))
                )
                .build();

        runner.start(scenario);

        assertThat(stack, contains(
                "1-11-111",
                "1-11-112",
                "1-12-121",
                "1-12-122",
                "2-21-211",
                "2-21-212",
                "2-22-221",
                "2-22-222"));
    }

    @TestStep(id = "putABC")
    public void testStep1(@Input("a") String a, @Input("b") String b, @Input("c") String c) {
        stack.push(a + '-' + b + '-' + c);
    }

    @Test
    public void shouldWorkWithMultipleSharedDataSourcesWithDifferentLength() {
        TestScenario scenario = scenario()
                .addFlow(
                        flow("flow1")
                                .beforeFlow(
                                        shareDataSource(GLOBAL_DATA_SOURCE),
                                        shareDataSource(CSV_DATA_SOURCE))
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(CSV_DATA_SOURCE),
                                        dataSource(GLOBAL_DATA_SOURCE)
                                )
                                .withVusers(3)
                                .afterFlow(assertAndResetCounter(2))
                )
                .addFlow(
                        flow("flow2")
                                .addTestStep(runnable(counter))
                                .withDataSources(
                                        dataSource(CSV_DATA_SOURCE))
                                .withVusers(3)
                                .afterFlow(assertAndResetCounter(3))
                )
                .build();

        runner.start(scenario);
    }
}
