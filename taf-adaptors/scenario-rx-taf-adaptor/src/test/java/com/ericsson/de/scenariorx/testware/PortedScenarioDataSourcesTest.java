/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.testware;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromTafDataProvider;
import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.runnable;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.GenericDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * Ported from com.ericsson.cifwk.taf.scenario.impl.ScenarioDataSourcesTest
 */
@SuppressWarnings({"DanglingJavadoc", "deprecation"})
public class PortedScenarioDataSourcesTest extends PortedScenarioTest {

    private static final String MY_DATA_SOURCE_1 = "myDS1";
    private static final String MY_DATA_SOURCE_2 = "myDS2";

    private static final String SIMPLE_PROVIDER = "simpleProvider";
    private static final String CLASS_PROVIDER = "classProvider";

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnNonExistingDataSource() {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource("nonExisting", DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void shouldReturnDataSourceName() {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link this#checkDataSourceName(DataRecord)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_CHECK_DATA_SOURCE_NAME))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void shouldResetSharedIterator_betweenScenarioRuns_CIS_5829() {
        registerDataSource(MY_DATA_SOURCE_1, fromCsv(CSV_LOCATION));

        RxScenario scenario = scenario("Scenario 1")
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        scenario = scenario("Scenario 2")
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(6);
    }

    @Test
    public void shouldResetSharedIterator_betweenScenarioRuns_CIS_7434() {
        registerDataSource(MY_DATA_SOURCE_1, fromCsv(CSV_LOCATION));

        RxScenario scenario = scenario("Scenario 1")
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        registerDataSource(MY_DATA_SOURCE_1, getDataSource(MY_DATA_SOURCE_1));

        scenario = scenario("Scenario 2")
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(6);
    }

    @Test
    public void shouldResetSharedIterator_betweenSteps() {
        registerDataSource(MY_DATA_SOURCE_1, fromCsv(CSV_LOCATION));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .addTestStep(runnable(counter))
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(9);
    }

    @Test
    public void shouldResetSharedIterator_betweenFlows() {
        registerDataSource(MY_DATA_SOURCE_1, fromCsv(CSV_LOCATION));

        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .addFlow(flow("B")
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .addFlow(flow("C")
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(9);
    }

    @Test
    public void shouldSupportDataProvider() {
        registerDataSource(MY_DATA_SOURCE_1, fromTafDataProvider(CLASS_PROVIDER));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(3);
    }

    @Test
    public void shouldSupportDataProvider_withSharedDataSources() {
        registerDataSource(MY_DATA_SOURCE_1, fromTafDataProvider(SIMPLE_PROVIDER));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                        .withVUsers(3)
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(3);
    }

    @Test
    public void shouldSupportDatasourceLoadStandardAndProvider() {
        registerDataSource(MY_DATA_SOURCE_1, fromTafDataProvider(CLASS_PROVIDER));
        registerDataSource(MY_DATA_SOURCE_2, fromTafDataProvider(CLASS_PROVIDER));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link this#checkDataRecordFromProviders(DataRecord, DataRecord, DataRecord)}  */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_CHECK_DATA_RECORD_FROM_PROVIDERS))
                        .withDataSources(
                                TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class),
                                TafRxScenarios.dataSource(MY_DATA_SOURCE_2, DataRecord.class),
                                TafRxScenarios.dataSource(CLASS_PROVIDER, DataRecord.class)
                        )
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(3);
    }

    @Test
    public void shouldNotResetSharedIterator_betweenSubFlows_CIS_12108() {
        RxScenario scenario = scenario()
                .addFlow(flow("MainFlow")
                        .addTestStep(runnable(sleepForVUser(2, 3)))
                        .addSubFlow(flow("SubFlow")
                                .addTestStep(runnable(counter))
                                .withDataSources(TafRxScenarios.dataSource(CSV_DATA_SOURCE, DataRecord.class).shared())
                        )
                        .withVUsers(3)
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(3);
    }

    @Test
    public void shouldNotResetSharedIterator_ofMainFlow_CIS_12108() {
        RxScenario scenario = scenario()
                .addFlow(flow("MainFlow")
                        .addTestStep(runnable(sleepForVUser(2)))
                        .addSubFlow(flow("SubFlow")
                                /** {@link this#dummy()} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_DUMMY))
                        )
                        /** {@link this#pushFromCsvToStack(String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_CSV_DS_TO_STACK))
                        .withDataSources(TafRxScenarios.dataSource(CSV_DATA_SOURCE, DataRecord.class).shared())
                        .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("attr1", "attr2", "attr3");
    }

    Runnable sleepForVUser(final int... vUsers) {
        return new Runnable() {
            @Override
            public void run() {
                int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
                if (vUsers.length == 0 || Ints.contains(vUsers, vUser)) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
    }

    @Test
    public void shouldAvoidDataSourceCreationByMultipleVUsers() {
        final TestContext context = ServiceRegistry.getTestContextProvider().get();
        final String newDataSource = "newDataSource";
        int vUsers = 30;

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(runnable(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        context.dataSource(newDataSource).addRecord().setField("field", "value");
                                    }
                                })
                        )
                        .withVUsers(vUsers)
                )
                .build();

        for (int i = 0; i < 50; i++) {
            context.removeDataSource(newDataSource);

            RxApi.run(scenario);

            int recordsCount = Iterables.size(context.dataSource(newDataSource));
            assertThat(recordsCount).isEqualTo(vUsers);
        }
    }

    @Test
    public void shouldShareIfDataSourceIsOnion() throws Exception {
        TestDataSource<DataRecord> dataSource = getDataSource(CSV_DATA_SOURCE);
        TestDataSource<DataRecord> wrapper = new GenericDataSource<>(dataSource, DataRecord.class);
        registerDataSource(MY_DATA_SOURCE_1, wrapper);

        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                        .withVUsers(100)
                )
                .addFlow(flow("B")
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                        .withVUsers(100)
                )
                .addFlow(flow("C")
                        .addTestStep(runnable(counter))
                        .withDataSources(TafRxScenarios.dataSource(MY_DATA_SOURCE_1, DataRecord.class).shared())
                        .withVUsers(100)
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(9);
    }

    /*---------------- TEST STEPS ----------------*/

    private static final String STEP_DUMMY = "dummy";
    private static final String STEP_CHECK_DATA_SOURCE_NAME = "checkDataSourceName";
    private static final String STEP_PUSH_CSV_DS_TO_STACK = "pushFromCsvToStack";
    private static final String STEP_CHECK_DATA_RECORD_FROM_PROVIDERS = "checkDataRecordFromProviders";

    @TestStep(id = STEP_DUMMY)
    public void dummy() {
    }

    @TestStep(id = STEP_CHECK_DATA_SOURCE_NAME)
    public void checkDataSourceName(@Input(GLOBAL_DATA_SOURCE) DataRecord dataRecord) {
        assertThat(dataRecord.getDataSourceName()).isEqualTo(GLOBAL_DATA_SOURCE);
    }

    @TestStep(id = STEP_PUSH_CSV_DS_TO_STACK)
    public void pushFromCsvToStack(@Input("attribute") String attribute) {
        stack.push(attribute);
    }

    @TestStep(id = STEP_CHECK_DATA_RECORD_FROM_PROVIDERS)
    public void checkDataRecordFromProviders(@Input(MY_DATA_SOURCE_1) DataRecord dataRecord1,
                                             @Input(MY_DATA_SOURCE_1) DataRecord dataRecord2,
                                             @Input(CLASS_PROVIDER) DataRecord dataRecord3) {

        assertThat(dataRecord1).isNotNull();
        assertThat(dataRecord2).isNotNull();
        assertThat(dataRecord3).isNotNull();

        counter.increment();
    }

    @SuppressWarnings("unused")
    public static class BasicClassDataSource {

        @DataSource
        public Iterable<Map<String, Integer>> data(ConfigurationSource reader) {
            assertThat(reader.getProperty("parameter")).isEqualTo("value");

            List<Map<String, Integer>> list = Lists.newArrayList();
            list.add(ImmutableMap.of("a1", 1, "a2", 2));
            list.add(ImmutableMap.of("b1", 1, "b2", 2));
            list.add(ImmutableMap.of("c1", 1, "c2", 2));

            return list;
        }
    }
}
