package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.FlowRunOptions;
import com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.InitializableByFlow;
import com.google.common.collect.Iterables;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkArgument;

/**
 *
 */
@API(Internal)
public final class TestStepFlowImpl implements TestStepFlow {
    private final long id = idGenerator.incrementAndGet();
    private final String name;
    private final List<TestStepInvocation> testSteps;
    private final List<SyncSingleInvocation> beforeSteps;
    private final List<SyncSingleInvocation> afterSteps;
    private final Map<String, DataSourceDefinition> dataSources;
    private final List<DataRecordTransformer> dataRecordTransformers;
    private final FlowRunOptions runOptions;

    public TestStepFlowImpl(String name,
                            List<TestStepInvocation> testSteps,
                            List<SyncSingleInvocation> beforeSteps,
                            List<SyncSingleInvocation> afterSteps,
                            Map<String, DataSourceDefinition> dataSources,
                            List<DataRecordTransformer> dataRecordTransformers,
                            FlowRunOptions flowRunOptions) {
        this.name = name;
        this.testSteps = testSteps;
        this.beforeSteps = beforeSteps;
        this.afterSteps = afterSteps;
        this.dataSources = dataSources;
        this.dataRecordTransformers = dataRecordTransformers;
        this.runOptions = flowRunOptions;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TestStepInvocation> getTestSteps() {
        return Collections.unmodifiableList(testSteps);
    }

    @Override
    public DataSourceDefinition[] getDataSources() {
        return dataSources.values().toArray(new DataSourceDefinition[dataSources.size()]);
    }

    @Override
    public List<DataRecordTransformer> getDataRecordTransformers() {
        return Collections.unmodifiableList(dataRecordTransformers);
    }

    @Override
    public void addDataRecordTransformer(DataRecordTransformer dataRecordTransformer) {
        dataRecordTransformers.add(dataRecordTransformer);
    }

    public List<SyncSingleInvocation> getBeforeSteps() {
        return beforeSteps;
    }

    public List<SyncSingleInvocation> getAfterSteps() {
        return afterSteps;
    }

    @Override
    public TestStepFlow bindDataSource(final String oldDataSourceName, TafDataSourceDefinitionBuilder<DataRecord> newDataSource) {
        checkArgument(dataSources.containsKey(oldDataSourceName), "Datasource with name '" + oldDataSourceName + "' not found");

        newDataSource.bindTo(oldDataSourceName);
        dataSources.put(oldDataSourceName, newDataSource.build());
        dataRecordTransformers.addAll(newDataSource.getDataRecordTransformers());

        return this;
    }

    @Override
    public void initialize(ScenarioExecutionContext scenarioExecutionContext) {
        Iterable<TestStepInvocation> allSteps = Iterables.concat(beforeSteps, testSteps, afterSteps);
        for (Object step : allSteps) {
            if (step instanceof InitializableByFlow) {
                InitializableByFlow.class.cast(step).initialize(this, scenarioExecutionContext);
            }
        }
    }

    @Override
    public FlowRunOptions getRunOptions() {
        return runOptions;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    public static TestStepFlow nullFlow(){
        return new TestStepFlowImpl("Null Object", null, null, null, null, null, null);
    }
}
