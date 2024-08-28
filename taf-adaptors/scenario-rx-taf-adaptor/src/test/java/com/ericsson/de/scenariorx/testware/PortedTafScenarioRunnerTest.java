package com.ericsson.de.scenariorx.testware;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.filter;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.runnable;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataSourceControl;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.google.common.base.Predicate;

/**
 * Ported from com.ericsson.cifwk.taf.scenario.impl.TafScenarioRunnerTest
 */
@SuppressWarnings({"DanglingJavadoc", "deprecation"})
public class PortedTafScenarioRunnerTest extends PortedScenarioTest {

    private static final String APPLES = "apples";
    private static final String ORANGES = "oranges";

    private static final String DATA_SOURCE_CYCLIC = "cyclic";
    private static final String DATA_SOURCE_SESSION = "session";

    @Test
    public void shouldWorkWithGlobalDataSource() {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link this#parametrizedTestStep(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_OK))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(2);
        assertThat(stack).containsExactly("1", "A", "2", "B");
    }

    @Test
    public void shouldResetDataSource_betweenScenarios() {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link this#parametrizedTestStep(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_OK))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
        counter.assertEqualTo(2);

        counter.reset();

        RxApi.run(scenario);
        counter.assertEqualTo(2);
    }

    @Test
    public void shouldResetDataSource_betweenFlows() {
        RxScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        /** {@link this#parametrizedTestStep(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_OK))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .addFlow(flow("Flow 2")
                        /** {@link this#parametrizedTestStep(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_OK))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
        counter.assertEqualTo(4);

        counter.reset();

        RxApi.run(scenario);
        counter.assertEqualTo(4);
    }

    @Test
    public void shouldResetDataSource_betweenFlows_csv() {
        RxScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        /** {@link this#parametrizedTestStep(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_OK))
                        .withDataSources(TafRxScenarios.dataSource(CSV_DATA_SOURCE, DataRecord.class))
                )
                .addFlow(flow("Flow 2")
                        /** {@link this#parametrizedTestStep(Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_OK))
                        .withDataSources(TafRxScenarios.dataSource(CSV_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
        counter.assertEqualTo(6);

        counter.reset();

        RxApi.run(scenario);
        counter.assertEqualTo(6);
    }

    @Test
    public void shouldSupportSubFlows_contextDataSource() {
        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        /** {@link this#start(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, START))
                        .addSubFlow(flow("B")
                                /** {@link this#inner(String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, INNER))
                                .withDataSources(TafRxScenarios.dataSource(DATA_SOURCE_SESSION, DataRecord.class))
                        )
                        /** {@link this#end(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, END))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly("1", "A", "B", "C", "1", "2", "A", "B", "C", "2");
    }

    @Test
    public void shouldSupportSubFlows_scenarioDataSource() {
        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        /** {@link this#start(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, START))
                        .addSubFlow(flow("B")
                                /** {@link this#inner(String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, INNER))
                                .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                        )
                        /** {@link this#end(Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, END))
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly("1", null, null, "1", "2", null, null, "2");
    }

    @Test
    @Ignore("flaky need to investigate")
    public void shouldSupportSubFlows_dataRecordsFromParentSubFlow() {
        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        /** {@link this#pushFromGlobalDS(String, Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        .addSubFlow(flow("B")
                                /** {@link this#pushFromGlobalDS(String, Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        )
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly("1", "A", "1", "A", "2", "B", "2", "B");
    }

    @Test
    public void shouldSupportSubFlows_dataRecordsFromParentSubFlowOfSubFlow() {
        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        /** {@link this#pushFromGlobalDS(String, Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        .addSubFlow(flow("B")
                                /** {@link this#pushFromGlobalDS(String, Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                                .addSubFlow(flow("C")
                                        /** {@link this#pushFromGlobalDS(String, Integer)} */
                                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_GLOBAL_DS)))
                        )
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly(
                "1", "A", "1", "A", "1", "A",
                "2", "B", "2", "B", "2", "B"
        );
    }


    @Test
    public void shouldSupportSubFlows_dataRecordsCombinedFromParentSubFlowAndDataSource() {
        RxScenario scenario = scenario()
                .addFlow(flow("A")
                        /** {@link this#pushFromGlobalDS(String, Integer)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_GLOBAL_DS))
                        .addSubFlow(flow("B")
                                /** {@link this#pushFromCsvAndGlobalDS(String, Integer, String)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_CSV_AND_GLOBAL_DS))
                                .withDataSources(TafRxScenarios.dataSource(CSV_DATA_SOURCE, DataRecord.class))
                        )
                        .withDataSources(TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly(
                "1", "A",
                "attr1", "1", "A",
                "attr2", "1", "A",
                "attr3", "1", "A",
                "2", "B",
                "attr1", "2", "B",
                "attr2", "2", "B",
                "attr3", "2", "B"
        );
    }

    @Test
    public void shouldRunWithCyclicSharedDataSources_CIS_7605_2() {
        registerDataSource(DATA_SOURCE_CYCLIC, fromCsv(CSV_LOCATION));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        /** {@link this#pushFromCsvAndGlobalDS(String, Integer, String)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, STEP_PUSH_CSV_AND_GLOBAL_DS))
                        .withVUsers(3)
                        .withDataSources(
                                TafRxScenarios.dataSource(DATA_SOURCE_CYCLIC, DataRecord.class).cyclic().shared(),
                                TafRxScenarios.dataSource(GLOBAL_DATA_SOURCE, DataRecord.class)
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).hasSize(18); // 3 arguments (attribute, integerParam, stringParam) * 2 data records from GLOBAL_DATA_SOURCE * 3 vUsers
        assertThat(stack).contains("attr1"); // we can not guarantee that attr2 and attr3 will be given to vUser
    }

    @Test
    public void checkMultipleDataRecordsOnTestStep() {
        getDataSource(APPLES).addRecord().setField("kind", "red").setField("size", 3).setField("fresh", true);
        getDataSource(ORANGES).addRecord().setField("kind", "tasty").setField("diameter", 2).setField("fresh", false);

        RxScenario scenario = scenario()
                .addFlow(flow("test")
                        /** {@link this#multipleRecords(Apple, Orange)} */
                        .addTestStep(TafRxScenarios.annotatedMethod(this, MULTI_RECORDS))
                        .withDataSources(
                                TafRxScenarios.dataSource(APPLES, DataRecord.class),
                                TafRxScenarios.dataSource(ORANGES, DataRecord.class)
                        )
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void shouldRunWithSharedDataSources() {
        registerDataSource("sharedDs", fromCsv(CSV_LOCATION));

        RxScenario scenario = scenario()
                .addFlow(flow("Test")
                        .addTestStep(runnable(counter))
                        .withVUsers(3)
                        .withDataSources(TafRxScenarios.dataSource("sharedDs", DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(3);
    }

    @Test
    public void shouldRunWithFilteredSharedDataSources() {
        TestDataSource<DataRecord> csvDataSource = fromCsv(CSV_LOCATION);
        registerDataSource("fmNodes", filter(csvDataSource, new NodeIdFilter("1", "2")));
        registerDataSource("pmNodes", filter(csvDataSource, new NodeIdFilter("2", "3")));

        RxScenario scenario = scenario()
                .addFlow(flow("Flow1")
                        .addTestStep(runnable(counter))
                        .withVUsers(2)
                        .withDataSources(TafRxScenarios.dataSource("fmNodes", DataRecord.class).shared())
                )
                .addFlow(flow("Flow2")
                        .addTestStep(runnable(counter))
                        .withVUsers(2)
                        .withDataSources(TafRxScenarios.dataSource("pmNodes", DataRecord.class).shared())
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(4);
    }

    private static class NodeIdFilter implements Predicate<DataRecord> {

        private Set<String> validIds;

        private NodeIdFilter(String... validIds) {
            this.validIds = newHashSet(validIds);
        }

        @Override
        public boolean apply(DataRecord dataRecord) {
            String nodeId = dataRecord.getFieldValue("nodeId");
            return validIds.contains(nodeId);
        }
    }

    @Test
    public void shouldDoParallelExecution() {
        RxScenario scenario = scenario()
                .addFlow(flow("sequential1")
                        .addTestStep(runnable(counter))
                        .withVUsers(2)
                )
                .split(
                        flow("parallel1").addTestStep(runnable(counter)),
                        flow("parallel2").addTestStep(runnable(counter)),
                        flow("parallel3").addTestStep(runnable(counter))
                )
                .addFlow(flow("sequential2")
                        .addTestStep(runnable(counter))
                        .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        counter.assertEqualTo(2 * 2 + 3);
    }

    /*---------------- TEST STEPS ----------------*/

    private static final String STEP_OK = "STEP_OK";

    private static final String INNER = "inner";
    private static final String START = "start";
    private static final String END = "end";

    private static final String MULTI_RECORDS = "multiRecords";

    private static final String STEP_PUSH_GLOBAL_DS = "PUSH_GLOBAL_DS";
    private static final String STEP_PUSH_CSV_AND_GLOBAL_DS = "STEP_PUSH_CSV_AND_GLOBAL_DS";
    private static final String STEP_CYCLIC_DATASOURCE = "STEP_CYCLIC_DATASOURCE";

    @TestStep(id = STEP_OK)
    public void parametrizedTestStep(@Input("integer") @OptionalValue Integer integerParam,
                                     @Input("string") @OptionalValue String stringParam) {
        stack.push(String.valueOf(integerParam));
        stack.push(stringParam);
        counter.increment();
    }

    @SuppressWarnings("Duplicates")
    @TestStep(id = START)
    public void start(@Input("integer") Integer i) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.setAttribute("vuser", context.getVUser());
        context.setAttribute("integer", i);

        if (!context.doesDataSourceExist(DATA_SOURCE_SESSION)) {
            TestDataSource<DataRecord> dataSource = context.dataSource(DATA_SOURCE_SESSION);
            dataSource.addRecord().setField("user", "A");
            dataSource.addRecord().setField("user", "B");
            dataSource.addRecord().setField("user", "C");
        }

        stack.push(i.toString());
    }

    @TestStep(id = INNER)
    public void inner(@Input("user") @OptionalValue String user) {
        stack.push(user);
    }

    @TestStep(id = END)
    public void end(@Input("integer") Integer i) {
        stack.push(i.toString());
    }

    @TestStep(id = MULTI_RECORDS)
    public void multipleRecords(@Input(APPLES) Apple apple,
                                @Input(ORANGES) Orange orange) {
        assertThat(apple.getKind()).isEqualTo("red");
        assertThat(apple.getSize()).isEqualTo(3);

        assertThat(orange.getKind()).isEqualTo("tasty");
        assertThat(orange.getDiameter()).isEqualTo(2);

        assertThat(apple.isFresh()).isTrue();
        assertThat(apple.getFresh()).isTrue();
        assertThat(orange.isFresh()).isFalse();
        assertThat(orange.getFresh()).isFalse();
    }

    @TestStep(id = STEP_PUSH_GLOBAL_DS)
    public void pushFromGlobalDS(@Input("string") String stringParam,
                                 @Input("integer") Integer integerParam) {
        stack.push(String.valueOf(integerParam));
        stack.push(stringParam);
    }

    @TestStep(id = STEP_PUSH_CSV_AND_GLOBAL_DS)
    public void pushFromCsvAndGlobalDS(@Input("attribute") String attribute,
                                       @Input("integer") Integer integerParam,
                                       @Input("string") String stringParam) {
        stack.push(attribute);
        stack.push(String.valueOf(integerParam));
        stack.push(stringParam);
    }

    @TestStep(id = STEP_CYCLIC_DATASOURCE)
    public void stepCyclicDatasource(@Input("attribute") String attribute) {
        stack.push(String.valueOf(attribute));

        if (counter.increment() == 8) {
            DataSourceControl.stopExecution(DATA_SOURCE_CYCLIC);
        }
    }

    public interface Apple extends DataRecord {
        String getKind();

        Integer getSize();

        boolean isFresh();

        boolean getFresh();
    }

    public interface Orange extends DataRecord {
        String getKind();

        Integer getDiameter();

        boolean isFresh();

        boolean getFresh();
    }
}
