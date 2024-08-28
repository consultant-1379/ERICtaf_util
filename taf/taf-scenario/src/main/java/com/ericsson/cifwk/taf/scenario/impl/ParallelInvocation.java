package com.ericsson.cifwk.taf.scenario.impl;


import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ControlInvocation;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.InitializableByFlow;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Test Step where execution may branch to multiple Flows with vUsers
 */
@API(Internal)
public abstract class ParallelInvocation implements ControlInvocation, InitializableByFlow {
    protected final long id = idGenerator.incrementAndGet();
    protected static final Logger logger = LoggerFactory
            .getLogger(ParallelInvocation.class);
    protected TestStepFlow[] flows;
    private boolean alwaysRun = false;

    public ParallelInvocation(TestStepFlow... flows) {
        this.flows = flows;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void initialize(TestStepFlow testStepFlow, ScenarioExecutionContext scenarioExecutionContext) {
        for (TestStepFlow flow : flows) {
            flow.initialize(scenarioExecutionContext);
        }
    }

    @Override
    public void addParameter(String key, Object value) {
        throw new UnsupportedOperationException("Parallel Invocation does not allow params");
    }

    @Override
    public void alwaysRun() {
        alwaysRun = true;
    }

    @Override
    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    @Override
    public Optional<Object> run(TestStepRunner testStepRunner,
                                ScenarioExecutionContext scenarioContext,
                                LinkedHashMap<String, DataRecord> dataSourcesRecords,
                                TestContext context,
                                List<DataRecordTransformer> dataRecordTransformers) throws Exception {
        List<TestStepRunner> subChain = Lists.newArrayList();

        ThreadPoolBasedScenarioRunnerCore core = scenarioContext.getCore();

        for (TestStepFlow flow : flows) {
            for (DataRecordTransformer dataRecordTransformer : dataRecordTransformers) {
                flow.addDataRecordTransformer(dataRecordTransformer);
            }

            ScenarioGraph graph = scenarioContext.getGraph();
            Collection<Integer> vUsers = graph.getVUsers(flow, context.getVUser());

            subChain.addAll(testStepRunner.cloneChainWithFlow(flow, dataSourcesRecords, vUsers));
        }

        core.runTasks(subChain, testStepRunner.getExceptionHandler());

        return Optional.absent();
    }
}
