package com.ericsson.cifwk.taf.scenario.impl;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ScenarioDataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;

public class ScenarioSyncPointsTest extends ScenarioTest {
    @Rule
    public Timeout globalTimeout = new Timeout(20_000);

    private static final String SHARED_DS = "SHARED_DS";
    private static final Logger logger = LoggerFactory
            .getLogger(ScenarioSyncPointsTest.class);


    @Test(expected = VerySpecialException.class)
    public void shouldHandleExceptionsInSyncpoints() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(throwExceptionForVUser(2))
                        .syncPoint("sync-point")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);
    }

    @Test(expected = VerySpecialException.class)
    public void shouldHandleExceptionsInSyncpoints_throwExceptionBeforeVUser1ReachesSyncPoint() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(sleepForVUser(1))
                        .addTestStep(throwExceptionForVUser(2))
                        .syncPoint("sync-point")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldHandleExceptionsInSyncpoints_continueSyncIfExceptionWasHandled() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .addTestStep(throwExceptionForVUser(2))
                        .syncPoint("wait-for-b")
                        .addTestStep(runnable(pushToStack("b")))
                )
                .withDefaultVusers(10)
                .withExceptionHandler(ScenarioExceptionHandler.IGNORE)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", "a", "a", "a", "a", "a", "a", "a", "a",
                "b", "b", "b", "b", "b", "b", "b", "b", "b", "b"));
    }

    @Test
    public void shouldHandleExceptionsInSyncpoints_ignoreAlwaysRunSteps() {
        final int FAILING_VUSER = 2;
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .addTestStep(sleepForVUser(FAILING_VUSER + 1))
                        .addTestStep(throwExceptionForVUser(FAILING_VUSER))
                        .addSubFlow(flow("subFlow")
                                .syncPoint("wait-for-b")
                                .addTestStep(runnable(pushToStack("b")).alwaysRun()))
                        .syncPoint("wait-for-c")
                        .addTestStep(runnable(pushToStack("c")).alwaysRun())
                )
                .withDefaultVusers(10)
                .build();

        runner().withDefaultExceptionHandler(ScenarioExceptionHandler.IGNORE).build().start(scenario);

        printStack();
        assertThat(stack, contains(
                "a", "a", "a", "a", "a", "a", "a", "a", "a", "a",
                "b", "b", "b", "b", "b", "b", "b", "b", "b", //2nd vUser fails before starting subflow
                "c", "c", "c", "c", "c", "c", "c", "c", "c", "c"));
    }

    @Test(expected = VerySpecialException.class)
    public void shouldHandleExceptionsInSyncpoints_subflows() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(throwExceptionForVUser(2))
                        .addTestStep(runnable(new Runnable() {
                            @Override
                            public void run() {
                                sleep(1000);
                            }
                        }))
                        .split(flow("sub-flow")
                                .syncPoint("sync-point")
                                .withVusers(10))
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);
    }

    @Test(expected = VerySpecialException.class)
    public void shouldHandleExceptionsInSyncpoints_inSubflows() {
        TestScenario scenario = scenario()
                .addFlow(flow("flow")
                        .addSubFlow(flow("subFlow")
                                .addTestStep(throwExceptionForVUser(2))
                        )
                        .withVusers(2)
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .build();
        runner.start(scenario);
    }

    @Test(expected = VerySpecialException.class)
    public void shouldHandleExceptionsInSyncpoints_MultipleSubflows() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .split(flow("sub-flow")
                                .addTestStep(runnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
                                        if (vUser > 4) {
                                            throw new VerySpecialException();
                                        }
                                    }
                                }))
                                .syncPoint("sync-point")
                                .withVusers(2)
                        )
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);
    }

    @Test
    public void shouldRespectSyncPointsInCaseSubFlowExceptionIsHandledByParentFlow() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .addSubFlow(flow("subflow-1")
                                .addTestStep(throwExceptionForVUser(2))
                                .addTestStep(sleepForVUser(1))
                                .syncPoint("do-not-hang")
                                .addTestStep(runnable(pushToStack("b")))
                        )
                        .addTestStep(sleepForVUser(1))
                        .addTestStep(runnable(pushToStack("c")))
                        .syncPoint("wait-for-c")
                        .addTestStep(runnable(pushToStack("d")))
                        .syncPoint("wait-for-d")
                        .addTestStep(runnable(pushToStack("e")))
                        .syncPoint("wait-for-e")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(3)
                .build();

        TestScenarioRunner runner = new TafScenarioRunner(ScenarioExceptionHandler.PROPAGATE, ScenarioExceptionHandler.IGNORE, Collections.EMPTY_LIST);
        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", "a",
                "b", "b",
                "c", "c", "c",
                "d", "d", "d",
                "e", "e", "e",
                "a", "a", "a",
                "b", "b",
                "c", "c", "c",
                "d", "d", "d",
                "e", "e", "e"));
    }

    @Test
    public void shouldRespectSyncPointsInCaseSplitExceptionIsHandledByParentFlow() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .split(flow("subflow-1")
                                .addTestStep(throwExceptionForVUser(5))
                                .addTestStep(sleepForVUser(1))
                                .syncPoint("do-not-hang")
                                .addTestStep(runnable(pushToStack("b")))
                        )
                        .addTestStep(sleepForVUser(1))
                        .addTestStep(runnable(pushToStack("c")))
                        .syncPoint("wait-for-c")
                        .addTestStep(runnable(pushToStack("d")))
                        .syncPoint("wait-for-d")
                        .addTestStep(runnable(pushToStack("e")))
                        .syncPoint("wait-for-e")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(3)
                .build();

        TestScenarioRunner runner = new TafScenarioRunner(ScenarioExceptionHandler.PROPAGATE, ScenarioExceptionHandler.IGNORE, Collections.EMPTY_LIST);
        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", "a",
                "b", "b",
                "c", "c", "c",
                "d", "d", "d",
                "e", "e", "e",
                "a", "a", "a",
                "b", "b",
                "c", "c", "c",
                "d", "d", "d",
                "e", "e", "e"));
    }

    @Test(expected = InvalidDataSourceException.class)
    public void shouldHandleExceptionsInDataSources() {
        TestScenario scenario = scenario("sync")
                .addFlow(flow("flow")
                        .addTestStep(runnable(new Runnable() {
                            @Override
                            public void run() {
                                sleep(2000);
                            }
                        }))
                        .syncPoint("sync-point")
                        .withDataSources(
                                new DataSourceDefinitionBuilder("throwing") {
                                    @Override
                                    public DataSourceDefinition build() {
                                        return new ThrowingDataSourceDefinition();
                                    }
                                }
                        )
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);
    }

    public static class ThrowingDataSourceDefinition implements DataSourceDefinition<DataRecord> {

        @Override
        public Supplier<Optional<DataRecord>> provideSupplier(ScenarioDataSourceContext context, Map<String, DataRecord> parentDataSources) {
            return new ScenarioDataSourceAdapter.IteratorToSupplierAdapter<>(new ThrowingIterator());
        }

        @Override
        public String getName() {
            return "throwing";
        }

        @Override
        public boolean allowsEmpty() {
            return false;
        }

    }

    public static class ThrowingIterator implements Iterator<DataRecord> {
        Iterator<Integer> iterator = newArrayList(1, 2, 3).iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public DataRecord next() {
            int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();

            Integer next = iterator.next();
            if (next == 2 && vUser == 2) {
                logger.info("Exception was thrown");
                throw new VerySpecialException();
            }

            return new DataRecordImpl(ImmutableMap.<String, Object>builder().
                    put("integer", next).build());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    @Test
    public void shouldRespectSyncPoints() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(
                        flow("flow")
                                .addTestStep(runnable(pushToStack("a")))
                                .syncPoint("wait-for-a")
                                .addTestStep(runnable(pushToStack("b")))
                                .syncPoint("wait-for-b")
                                .split(
                                        flow("subflow-1")
                                                .addTestStep(runnable(pushToStack("ba")))
                                                .withVusers(4),
                                        flow("subflow-2")
                                                .addTestStep(runnable(pushToStack("ba")))
                                                .withVusers(2)
                                )
                                .addTestStep(runnable(pushToStack("c")))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", "b", "b",
                "ba", "ba", "ba", "ba",
                "ba", "ba", "ba", "ba",
                "ba", "ba", "ba", "ba",
                "c", "c"));
    }

    @Test
    public void shouldRespectSyncPointsInSubflowsAndSplits() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .addTestStep(runnable(pushToStack("b")))
                        .syncPoint("wait-for-b")
                        .split(flow("subflow-2")
                                .addTestStep(runnable(pushToStack("ba")))
                                .syncPoint("wait-for-ba")
                                .addTestStep(runnable(pushToStack("bb")))
                                .syncPoint("wait-for-bb")
                                .split(flow("subsubflow")
                                        .addTestStep(runnable(pushToStack("bba")))
                                        .syncPoint("wait-for-bba")
                                        .addTestStep(runnable(pushToStack("bbb")))
                                        .withVusers(3)
                                )
                                .withVusers(2)
                        )
                        .addTestStep(runnable(pushToStack("c")))
                        .syncPoint("wait-for-c")
                        .addSubFlow(flow("subflow-2")
                                .addTestStep(runnable(pushToStack("ca")))
                                .syncPoint("wait-for-ca")
                                .addTestStep(runnable(pushToStack("cb")))))
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", // 2
                "b", "b", // 2
                "ba", "ba", "ba", "ba", // 2 * 2
                "bb", "bb", "bb", "bb", // 2 * 2
                "bba", "bba", "bba", "bba", "bba", "bba", "bba", "bba", "bba", "bba", "bba", "bba", // 2 * 2 * 3
                "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", "bbb", // 2 * 2 * 3
                "c", "c", // 2
                "ca", "ca", // 2
                "cb", "cb")); // 2
    }

    @Test
    public void shouldRespectSyncPointsWithDataSourcesComplex() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .addTestStep(runnable(pushToStack("b")))
                        .syncPoint("wait-for-b")
                        .split(
                                flow("subflow-1")
                                        .addTestStep(runnable(pushToStack("ba")))
                                        .withVusers(4),
                                flow("subflow-2")
                                        .addTestStep(runnable(pushToStack("ba")))
                                        .withVusers(2)
                        )
                        .addTestStep(runnable(pushToStack("c")))
                        .syncPoint("wait-for-finnish")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(2)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", "b", "b",
                "ba", "ba", "ba", "ba",
                "ba", "ba", "ba", "ba",
                "ba", "ba", "ba", "ba",
                "c", "c",
                "a", "a", "b", "b",
                "ba", "ba", "ba", "ba",
                "ba", "ba", "ba", "ba",
                "ba", "ba", "ba", "ba",
                "c", "c"));
    }

    @Test
    public void shouldRespectSyncPointsWithDataSources() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .addTestStep(runnable(pushToStack("b")))
                        .syncPoint("wait-for-b")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                )
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains(
                "a", "a", "a",
                "b", "b", "b",
                "a", "a", "a",
                "b", "b", "b"));
    }

    @Test
    public void shouldRespectSyncPointsWithFilteredDataSources() {
        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .addTestStep(runnable(pushToStack("b")))
                        .syncPoint("wait-for-b")
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE).withFilter("$VUSER == 1"))
                )
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains("a", "b", "a", "b"));
    }

    @Test
    public void shouldRespectSyncPointsWithSharedDataSources() {
        TestDataSource fmNodes = shared(globalDataSource);
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(SHARED_DS, fmNodes);

        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(flow("flow")
                        .addTestStep(runnable(pushToStack("a")))
                        .syncPoint("wait-for-a")
                        .addTestStep(runnable(pushToStack("b")))
                        .syncPoint("wait-for-b")
                        .withDataSources(dataSource(SHARED_DS))
                )
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack, contains("a", "a", "b", "b"));
    }

    @Test
    public void shouldRespectSyncPointsWithSubflowSharedDataSources() {
        TestDataSource fmNodes = shared(globalDataSource);
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.addDataSource(SHARED_DS, fmNodes);

        TestScenario scenario = scenario("doParallel-merge")
                .addFlow(flow("main-flow")
                        .addSubFlow(flow("subflow")
                                .addTestStep(runnable(pushToStack("a")))
                                .syncPoint("wait-for-a")
                                .addTestStep(runnable(pushToStack("b")))
                                .syncPoint("wait-for-b")
                                .withDataSources(dataSource(SHARED_DS)))
                        .addTestStep(sleepForVUser(1))
                        .addTestStep(runnable(pushToStack("c")))
                        .syncPoint("wait-for-c")
                        .addTestStep(runnable(pushToStack("d")))
                        .syncPoint("wait-for-d")
                        .withDataSources(dataSource(CSV_DATA_SOURCE)))
                .withDefaultVusers(3)
                .build();

        runner.start(scenario);

        printStack();

        assertThat(stack,
                contains("a", "a",  //limited by shared DS
                        "b", "b", //limited by shared DS
                        "c", "c", "c",
                        "d", "d", "d",
                        "c", "c", "c",
                        "d", "d", "d",
                        "c", "c", "c",
                        "d", "d", "d"
                ));
    }

    @Test
    public void vUsersMoreThanDataRecordsAndSyncPointsInSubFlow() {
        TestContext context = TafTestContext.getContext();

        TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
        context.addDataSource(MY_DATA_SOURCE_1, shared(globalDataSource));

        TestScenario scenario = scenario()
                .addFlow(flow("main")
                        .split(flow("A")
                                .syncPoint("hanger")
                                .addTestStep(runnable(counter)))
                        .withDataSources(dataSource(MY_DATA_SOURCE_1))
                        .withVusers(3))
                .build();
        runner().build().start(scenario);
    }

    @Test
    public void vUsersMoreThanDataRecordsAndSyncPointsInSubFlow2() {
        TestContext context = TafTestContext.getContext();

        TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
        context.addDataSource(MY_DATA_SOURCE_1, shared(globalDataSource));

        TestScenario scenario = scenario()
                .addFlow(flow("main")
                        .split(flow("A")
                                .beforeFlow(counter)
                                .addTestStep(runnable(counter)))
                        .withDataSources(dataSource(MY_DATA_SOURCE_1))
                        .withVusers(3))
                .build();
        runner().build().start(scenario);
    }

    @Test
    public void handledExceptionInSubflow() {
        TestContext context = TafTestContext.getContext();

        TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
        context.addDataSource(MY_DATA_SOURCE_1, shared(globalDataSource));

        TestScenario scenario = scenario()
                .addFlow(flow("main")
                        .addSubFlow(flow("A")
                                .addSubFlow(
                                        flow("subFlow")
                                                .addTestStep(runnable(counter))
                                                .addTestStep(annotatedMethod(this, STEP_EXCEPTION_PRODUCER))
                                                .addTestStep(runnable(counter)))
                                .addTestStep(runnable(counter))
                        )
                        .withDataSources(dataSource(MY_DATA_SOURCE_1).withFilter("string == 'A'"))
                        .withVusers(2)
                )
                .withExceptionHandler(ScenarioExceptionHandler.IGNORE)
                .build();
        runner().build().start(scenario);
    }
}
