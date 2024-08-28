package com.ericsson.de.scenariorx.examples;

import static com.ericsson.de.scenariorx.api.RxApi.flow;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.api.DebugGraphMode;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxFlow;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.testng.annotations.Test;

public class LargeDataSourceParallelProcessingExample {
    private static final String PROCESS = "PROCESS";

    @Test
    public void largeDataSourceProcessingExample() {
        // START SNIPPET: SPEED_UP

        RxDataSource<TafNode> largeDataSource = TafRxScenarios.dataSource("lotOfNodes", TafDataSources.fromCsv("large.csv"), TafNode.class);

        RxDataSource<TafNode> part1 = largeDataSource.filterField(TafNode.NODE_TYPE).equalTo("ERBS");
        RxDataSource<TafNode> part2 = largeDataSource.filterField(TafNode.NODE_TYPE).equalTo("RadioNode");

        RxScenario processingScenario = TafRxScenarios.scenario()
                .split(
                        processNode(part1),
                        processNode(part2)
                )
                .build();

        TafRxScenarios.runner()
                .withDebugLogEnabled()
                .withGraphExportMode(DebugGraphMode.ALL)
                .build()
                .run(processingScenario);

        // END SNIPPET: SPEED_UP
    }

    @TestStep(id = PROCESS)
    public void process(@Input(TafNode.NETWORK_ELEMENT_ID) String networkElementId, @Input(TafNode.NODE_TYPE) String nodeType) {
        System.out.println(PROCESS + networkElementId);
    }

    private RxFlow processNode(RxDataSource<TafNode> dataSource) {
        return flow("Add node")
                .addTestStep(TafRxScenarios.annotatedMethod(this, PROCESS))
                .withDataSources(dataSource.shared())
                .withVUsers(2)
                .build();

    }
}
