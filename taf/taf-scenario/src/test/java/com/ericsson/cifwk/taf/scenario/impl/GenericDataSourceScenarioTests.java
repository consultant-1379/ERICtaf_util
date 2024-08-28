package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.datarecord.Node;
import com.google.common.base.Predicate;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GenericDataSourceScenarioTests extends ScenarioTest {

    private static final String STEP_CHECK_DATA_RECORD_NAME = "nodePreparation";
    private static final String STEP_OK = "step_ok";
    private static final String STEP_OK_NODE = "step_ok_node";

    @Test
    public void shouldReturnDataRecordName_WhenGenericDataSourceUsed() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        TestDataSource<Node> filter = TafDataSources.filter(genericCsvDataSource, new Predicate<Node>() {
            @Override
            public boolean apply(Node node) {
                return true;
            }
        });
        context.addDataSource(MY_DATA_SOURCE_1, filter);

        TestStepFlow myFlow = flow("Test")
                .addTestStep(annotatedMethod(this, STEP_CHECK_DATA_RECORD_NAME))
                .withDataSources(
                        dataSource(MY_DATA_SOURCE_1)
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(myFlow)
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldRunWithPredicateGenericDataSources() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(GENERIC_CSV_DATA_SOURCE).withFilter(new Predicate<DataRecord>() {
                            @Override
                            public boolean apply(DataRecord dataRecord) {
                                return "1".equals(dataRecord.getFieldValue("nodeId"));
                            }
                        })
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
    }

    @Test
    public void shouldRunWithGenericPredicateForDataSource_whenDataSourceIsTyped() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(GENERIC_CSV_DATA_SOURCE, Node.class).withFilter(new Predicate<Node>() {
                            @Override
                            public boolean apply(Node dataRecord) {
                                return "1".equals(dataRecord.getNodeId());
                            }
                        })
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
    }

    @Test
    public void shouldRunWithGenericPredicateForDataSource_WhenDataSourceNotTyped() {
        TestStepFlow flow = flow("Test")
                .addTestStep(runnable(counter))
                .withDataSources(
                        dataSource(CSV_DATA_SOURCE, Node.class).withFilter(new Predicate<Node>() {
                            @Override
                            public boolean apply(Node dataRecord) {
                                return "1".equals(dataRecord.getNodeId());
                            }
                        })
                )
                .build();

        TestScenario scenario = scenario()
                .addFlow(flow)
                .build();

        runner.start(scenario);

        assertThat(count.get(), equalTo(1));
    }

    @Test
    public void shouldResetDataSourceBetweenFlows_genericCsv() {
        TestScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        .addTestStep(annotatedMethod(this, STEP_OK))
                        .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE)))
                .addFlow(flow("Flow 2")
                        .addTestStep(annotatedMethod(this, STEP_OK_NODE))
                        .withDataSources(dataSource(GENERIC_CSV_DATA_SOURCE)))
                .build();

        runner.start(scenario);
        assertThat(count.get(), equalTo(6));
        count.set(0);

        runner.start(scenario);
        assertThat(count.get(), equalTo(6));
    }

    @Test
    public void testTypedDataSource() throws Exception {
        final String NODE_ID = "1";
        TestDataSource<Node> dataSource = ServiceRegistry.getTestContextProvider().get().dataSource("ds", Node.class);
        dataSource.addRecord().setField("nodeId", NODE_ID);
        Node node = dataSource.iterator().next();
        assertThat(node.getNodeId(), equalTo(NODE_ID));

        TestDataSource<DataRecord> untypedDataSource = ServiceRegistry.getTestContextProvider().get().dataSource("ds");
        DataRecord dataRecord = untypedDataSource.iterator().next();
        String nodeId = dataRecord.getFieldValue("nodeId");
        assertThat(nodeId, equalTo(NODE_ID));
    }

    @TestStep(id = STEP_CHECK_DATA_RECORD_NAME)
    public void stepCheckDataRecordName(@Input(MY_DATA_SOURCE_1) DataRecord dataRecord) {
        assertThat(dataRecord.getDataSourceName(), equalTo(MY_DATA_SOURCE_1));
    }

    @TestStep(id = STEP_OK)
    public void parametrizedTestStep(@Input("nodeId") Integer integerParam, @Input("subscription") String stringParam) {
        count.incrementAndGet();
    }
    @TestStep(id = STEP_OK_NODE)
    public void parametrizedTestStep(@Input(GENERIC_CSV_DATA_SOURCE) Node node) {
        count.incrementAndGet();
    }
}
