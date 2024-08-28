package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.FlowRunOptions;
import com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.SyncSingleInvocation;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Represents sequential flow of test steps.
 */
@API(Stable)
public interface TestStepFlow {
    AtomicLong idGenerator = new AtomicLong();

    void initialize(ScenarioExecutionContext scenarioExecutionContext);

    Long getId();

    /**
     * Gets name of the flow.
     *
     * @return name
     */
    String getName();

    /**
     * Provides a list of registered test steps.
     *
     * @return immutable list
     */
    List<TestStepInvocation> getTestSteps();

    DataSourceDefinition[] getDataSources();

    FlowRunOptions getRunOptions();

    List<DataRecordTransformer> getDataRecordTransformers();

    void addDataRecordTransformer(DataRecordTransformer dataRecordTransformer);

    List<SyncSingleInvocation> getBeforeSteps();

    List<SyncSingleInvocation> getAfterSteps();

    TestStepFlow bindDataSource(String dataSourceName, TafDataSourceDefinitionBuilder<DataRecord> alias);

    interface State {
        /**
         * @return Time in millis when current {@link #getVUser()} started processing Flow
         */
        Long getStartTime();

        /**
         * @return Which iteration {@link #getVUser()} is currently at
         * @see TestStepFlowBuilder#repeatWhile(Predicate)
         * @see TestStepFlowBuilder#withDuration(long, TimeUnit)
         * @see TestStepFlowBuilder#withIterationsPerVuser(long)
         */
        int getIteration();

        /**
         * @return {@link TestContext}
         */
        TestContext getContext();

        /**
         * @return Current vUser
         */
        int getVUser();
    }
}
