package com.ericsson.de.scenariorx.examples;

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.fromIterable;
import static com.google.common.collect.Lists.newArrayList;

import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.ericsson.de.scenariorx.impl.ScenarioTest;
import org.junit.Test;

public class SyncExamples {
    @Test
    public void testStepNotSync() throws Exception {
        // START SNIPPET: TEST_STEP_NOT_SYNC
        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(step("TS1"))
                        .addTestStep(step("TS2"))
                        .withVUsers(3)
                )
                .build();
        // END SNIPPET: TEST_STEP_NOT_SYNC
    }

    @Test
    public void batchSync() throws Exception {
        // START SNIPPET: BATCH_SYNC
        RxDataSource<String> dataSource =
                fromIterable("dataSource",
                        newArrayList("DR1", "DR2", "DR3",
                                "DR4", "DR5", "DR6",
                                "DR7", "DR8"));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(step("TS1"))
                        .addTestStep(step("TS2"))
                        .withDataSources(dataSource.shared())
                        .withVUsers(3)
                )
                .build();
        // END SNIPPET: BATCH_SYNC
    }

    @Test
    public void subFlowSync() throws Exception {
        // START SNIPPET: SUBFLOW_SYNC
        RxDataSource<String> subFlowDataSource =
                fromIterable("subFlowDataSource",
                        newArrayList("DR1", "DR2", "DR3"));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(step("flow-ts1"))
                        .addTestStep(step("flow-ts2"))
                        .addSubFlow(flow()
                                .addTestStep(step("subFlow-ts1"))
                                .withDataSources(subFlowDataSource.shared())
                        )
                        .addTestStep(step("flow-ts3"))
                        .withVUsers(2)
                )
                .build();
        // END SNIPPET: SUBFLOW_SYNC
    }

    private RxTestStep step(String name) {
        return new ScenarioTest.Counter();
    }
}
