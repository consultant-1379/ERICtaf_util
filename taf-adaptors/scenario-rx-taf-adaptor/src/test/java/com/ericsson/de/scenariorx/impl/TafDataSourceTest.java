package com.ericsson.de.scenariorx.impl;

import static java.util.Collections.emptyList;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.cyclic;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromTafDataProvider;
import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.runnable;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.ericsson.de.scenariorx.api.TafRxScenarios;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

public class TafDataSourceTest {

    private static final String DATA_SOURCE_NAME = "nodes";
    private static final String NETWORK_ELEMENT_ID_TO_STACK = "networkElementIdToStack";
    private static final String DUMMY = "DUMMY";

    private final Stack<String> networkElementIds = new Stack<>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void notFoundValidation() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TafDataSource.ERROR_NOT_FOUND);

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(this, DUMMY))
                        .withDataSources(TafRxScenarios.dataSource("notExisting", TafNode.class))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void emptyValidation() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(TafDataSource.ERROR_EMPTY);

        String dataSourceName = "empty";
        TestContext testContext = ServiceRegistry.getTestContextProvider().get();
        testContext.addDataSource(dataSourceName, fromClass(EmptyProvider.class));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(this, DUMMY))
                        .withDataSources(TafRxScenarios.dataSource(dataSourceName, TafNode.class))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void implicitCyclingValidation() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(TafDataSource.ERROR_IMPLICIT_CYCLING);

        String dataSourceName = "implicitCycle";
        TestContext testContext = ServiceRegistry.getTestContextProvider().get();
        testContext.addDataSource(dataSourceName, cyclic(fromTafDataProvider(DATA_SOURCE_NAME)));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(throwException())
                        .withDataSources(TafRxScenarios.dataSource(dataSourceName, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
    }

    @Test
    public void implicitCyclingValidationRuntime() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(TafDataSource.ERROR_IMPLICIT_CYCLING);

        final String dataSourceName = "implicitCycle";
        TestContext testContext = ServiceRegistry.getTestContextProvider().get();
        testContext.addDataSource(dataSourceName, fromTafDataProvider(DATA_SOURCE_NAME));

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .beforeFlow(TafDataSources.makeDataSourceCyclic(dataSourceName))
                        .addTestStep(TafRxScenarios.annotatedMethod(this, DUMMY))
                        .withDataSources(TafRxScenarios.dataSource(dataSourceName, DataRecord.class))
                )
                .addFlow(flow()
                        .addTestStep(throwException())
                        .withDataSources(TafRxScenarios.dataSource(dataSourceName, DataRecord.class))
                )
                .build();

        RxApi.run(scenario);
    }

    private RxTestStep throwException() {
        return runnable(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("This method should never get invoked");
            }
        });
    }

    @Test
    public void dataRecordValueTest() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(this, NETWORK_ELEMENT_ID_TO_STACK))
                        .withVUsers(3)
                        .withDataSources(TafRxScenarios.dataSource(DATA_SOURCE_NAME, TafNode.class).shared())
                )
                .build();

        RxApi.run(scenario);
        assertThat(networkElementIds).containsExactlyInAnyOrder(
                "LTE08dg2", "LTE08dg2", "LTE08dg2",
                "LTE01ERB", "LTE01ERB", "LTE01ERB",
                "SGSN-14B", "SGSN-14B", "SGSN-14B");
    }

    @TestStep(id = NETWORK_ELEMENT_ID_TO_STACK)
    public void testStep1(@Input(DATA_SOURCE_NAME) TafNode node, @Input(TafNode.NETWORK_ELEMENT_ID) String networkElementId) {
        networkElementIds.push(node.getNetworkElementId());
        networkElementIds.push(node.getAllFields().get(TafNode.NETWORK_ELEMENT_ID).toString());
        networkElementIds.push(networkElementId);
    }

    @TestStep(id = DUMMY)
    public void dummy() {
    }

    public static class EmptyProvider {
        @DataSource
        public List<Map<String, Object>> records() {
            return emptyList();
        }
    }
}
