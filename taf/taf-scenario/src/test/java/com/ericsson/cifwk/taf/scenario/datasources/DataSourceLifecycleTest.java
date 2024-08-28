package com.ericsson.cifwk.taf.scenario.datasources;

import com.beust.jcommander.internal.Maps;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.MiniTestNG;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.datasources.TrackingDataSourceAdapter.TrackingDataSource;
import com.google.common.collect.Lists;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.shareDataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.datasources.TrackingDataSourceAdapter.TRACKING_DATA_SOURCE;
import static org.assertj.core.api.Assertions.assertThat;

public class DataSourceLifecycleTest {

    private static final String SCENARIO_DS = TRACKING_DATA_SOURCE + "_Scenario";
    private static final String SCENARIO_DS_2 = TRACKING_DATA_SOURCE + "_Scenario2";
    private static final String SCENARIO_DS_SHARED = TRACKING_DATA_SOURCE + "_Scenario_Shared";
    private static final String DATA_PROVIDER_DS = TRACKING_DATA_SOURCE + "_DataProvider";
    private static final String DATA_DRIVEN_DS = TRACKING_DATA_SOURCE + "_DataDriven";

    private static final List<String> dataSourceNames
            = Lists.newArrayList(SCENARIO_DS, SCENARIO_DS_2, SCENARIO_DS_SHARED, DATA_PROVIDER_DS, DATA_DRIVEN_DS);


    @org.junit.Test
    public void shouldCloseDataSources() throws Exception {
        MiniTestNG.runTest(DataDrivenTest.class);
        
        List<TrackingDataSource> dataSources = TrackingDataSourceAdapter.getDataSources();
        assertThat(dataSources).extracting("name").containsAll(dataSourceNames);
        
        for (TrackingDataSource dataSource : dataSources) {
            assertThat(dataSource.isClosed()).isTrue();
        }
    }

    public static class DataDrivenTest {

        private static final String TEST_STEP = "testStep";
        private Map<String, String> testData;

        @BeforeMethod
        public void setUp() {
            testData = Maps.newHashMap();
            testData.put("name", "value");
        }

        @Test
        public void scenarioDataSource() {
            TestScenario scenario = scenario()
                    .addFlow(
                            flow("flow")
                                    .addTestStep(annotatedMethod(this, "testStep"))
                                    .withDataSources(dataSource(SCENARIO_DS))
                    )
                    .build();

            runner().build().start(scenario);
        }


        @Test
        public void scenarioSharedDataSource() {
            TestScenario scenario = scenario()
                    .addFlow(
                            flow("flow")
                                    .beforeFlow(shareDataSource(SCENARIO_DS_SHARED))
                                    .addTestStep(annotatedMethod(this, "testStep"))
                                    .withDataSources(dataSource(SCENARIO_DS_2))
                                    .withVusers(3)
                    )
                    .build();

            runner().build().start(scenario);
        }

        @TestStep(id = TEST_STEP)
        public void step(@Input("name") String name, @Input("value") String value) {
            assertThat(testData).containsEntry(name, value);
        }

        @Test
        public void dataSourceFromDataProvider() {
            TestDataSource<DataRecord> fromDataProvider = TafDataSources.fromTafDataProvider(DATA_PROVIDER_DS);

            assertThat(fromDataProvider).isNotNull();
            assertThat(fromDataProvider).hasSize(testData.size());
        }

        @Test
        @DataDriven(name = DATA_DRIVEN_DS)
        public void dataDrivenTest(@Input("name") String name, @Input("value") String value) {
            assertThat(testData).containsEntry(name, value);
        }
    }
}
