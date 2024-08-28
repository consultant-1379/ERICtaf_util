package com.ericsson.de.scenariorx.testware;
/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.de.scenariorx.impl.ScenarioTest;
import org.junit.Before;

/**
 * Ported from com.ericsson.cifwk.taf.scenario.impl.ScenarioTest
 */
public class PortedScenarioTest extends ScenarioTest {

    static final String GLOBAL_DATA_SOURCE = "global";
    static final String CSV_DATA_SOURCE = "csv";

    static final String CSV_LOCATION = "data/ported_node.csv";

    Stack<String> stack = new Stack<>();
    Counter counter = new Counter();

    @Before
    public void setUp() {
        stack.clear();
        counter.reset();

        clearAllDataSources();
        prepareGlobalDataSource();
        registerDataSource(CSV_DATA_SOURCE, fromCsv(CSV_LOCATION));
    }

    private void clearAllDataSources() {
        TestContext context = testContext();
        for (String dataSource : context.getAllDataSources().keySet()) {
            context.removeDataSource(dataSource);
        }
    }

    private void prepareGlobalDataSource() {
        TestContext context = testContext();
        if (!context.doesDataSourceExist(GLOBAL_DATA_SOURCE)) {
            TestDataSource<DataRecord> globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
            globalDataSource.addRecord().setField("integer", 1).setField("string", "A");
            globalDataSource.addRecord().setField("integer", 2).setField("string", "B");
        }
    }

    void registerDataSource(String name, TestDataSource<DataRecord> dataSource) {
        testContext().addDataSource(name, dataSource);
    }

    TestDataSource<DataRecord> getDataSource(String name) {
        return testContext().dataSource(name);
    }

    private TestContext testContext() {
        return ServiceRegistry.getTestContextProvider().get();
    }

    class Counter implements Runnable {

        AtomicInteger count = new AtomicInteger();

        @Override
        public void run() {
            increment();
        }

        Integer increment() {
            return count.incrementAndGet();
        }

        void assertEqualTo(Integer value) {
            assertThat(count.get()).isEqualTo(value);
        }

        void reset() {
            count.set(0);
        }
    }
}
