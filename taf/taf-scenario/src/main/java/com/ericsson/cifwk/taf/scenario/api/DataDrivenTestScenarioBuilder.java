package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.impl.GenericScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioDataSourceContext;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;
import com.ericsson.cifwk.taf.scenario.impl.SyncSingleInvocation;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

@API(Stable)
public final class DataDrivenTestScenarioBuilder extends GenericScenarioBuilder<DataDrivenTestScenarioBuilder> {
    public static final String TEST_CASE_ID = "testCaseId";
    public static final String TEST_CASE_TITLE = "testCaseTitle";
    private DataSourceDefinition[] dataSources = null;
    private boolean dataSourcesSet = false;
    private Integer vUsers = -1;
    private boolean allowNotSharedFlows = false;

    public DataDrivenTestScenarioBuilder(String name) {
        super(name);
        type = ScenarioType.DATA_DRIVEN;
    }

    /**
     * Adds Data Sources to scenario. Each Data Record will appear as new Test Case in reporting. At least one data
     * source should contain field with name `testCaseId` which will be used as Test Case Id in reporting.
     * @param dataSources array of data sources to add
     * @return builder
     */
    public DataDrivenTestScenarioBuilder withScenarioDataSources(DataSourceDefinitionBuilder... dataSources) {
        checkArgument(!dataSourcesSet, "DataSources can't be set twice");
        scenarioFlow.withDataSources(dataSources);
        dataSourcesSet = true;

        return this;
    }

    /**
     * Split Data Source (set with {@link #withScenarioDataSources(DataSourceDefinitionBuilder...)}) between multiple
     * vUsers (threads) and run them in parallel.
     *
     * Used to speed up Data Source processing.
     *
     * NOTE: Data Source needs to be shared. If there's requirement to use not Shared Data Source use {@link #allowNotSharedFlows()}
     *
     * @param vUsers number of vUsers (threads) running in parallel
     * @return builder
     */
    public DataDrivenTestScenarioBuilder doParallel(int vUsers) {
        checkArgument(vUsers > 0, "vUser value should be greater than zero");
        checkArgument(this.vUsers == -1, "vUsers can't be set twice");
        scenarioFlow.withVusers(vUsers);
        this.vUsers = vUsers;

        return this;
    }

    /**
     * Allows to use not Shared Data Source and {@link #doParallel(int)}. In such case each vUser (thread) will process
     * all Data Source records
     * @return
     */
    public DataDrivenTestScenarioBuilder allowNotSharedFlows() {
        allowNotSharedFlows = true;

        return this;
    }

    @Override
    public TestScenario build() {
        Preconditions.checkState(dataSourcesSet, "Data Driven Scenario Should Have at least one Data Source set");
        if (vUsers > 1 && !allowNotSharedFlows) {
            scenarioFlow.beforeFlow(new AssertDataSourcesAreShared());
        }

        TestScenario scenario = super.build();
        this.dataSources = scenario.getFlow().getDataSources();

        return scenario;
    }

    private class AssertDataSourcesAreShared extends SyncSingleInvocation {
        public AssertDataSourcesAreShared() {
            super("Start Data Driven Scenario");
        }

        @Override
        protected void runOnce(ScenarioExecutionContext scenarioExecutionContext) {
            ScenarioDataSourceContext dataSourceContext = scenarioExecutionContext.getDataSourceContext();

            //init
            dataSourceContext.provideDataSources(dataSources, Maps.<String, DataRecord>newHashMap());

            for (DataSourceDefinition definition : dataSources) {
                checkState(dataSourceContext.getDataSources().get(definition.getName()).isShared(),
                        "Data Source `" + definition.getName() + "` must be shared to run " +
                                "in Parallel Data Driven Scenario");
            }
        }
    }
}
