package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.GenericScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioType;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public final class TestScenarioBuilder extends GenericScenarioBuilder<TestScenarioBuilder> {
    public TestScenarioBuilder(String name) {
        super(name);
        type = ScenarioType.DEFAULT;
    }


    /**
     * Sets default concurrency level for flows in scenario if not specified in {@link TestStepFlowBuilder#withVusers(int)}
     *
     * @param vUsers number of virtual users to use
     * @return builder
     */
    @Override
    //left here for backwards compatibility, because moving method to parent changes signature in bytecode
    public TestScenarioBuilder withDefaultVusers(int vUsers) {
        return super.withDefaultVusers(vUsers);
    }


    @Override
    //left here for backwards compatibility, because moving method to parent changes signature in bytecode
    public TestScenarioBuilder addFlow(TestStepFlowBuilder builder) {
        return super.addFlow(builder);
    }

    /**
     * Adds test step flow to the current scenario.
     *
     * @param flow
     *            flow to add
     * @return builder
     */
    @Override
    //left here for backwards compatibility, because moving method to parent changes signature in bytecode
    public TestScenarioBuilder addFlow(TestStepFlow flow) {
        return super.addFlow(flow);
    }

    /**
     * Execute flows passed in param in parallel
     * @param flows to execute in parallel
     * @return builder
     */
    @Override
    //left here for backwards compatibility, because moving method to parent changes signature in bytecode
    public TestScenarioBuilder split(TestStepFlow... flows) {
        return super.split(flows);
    }

    /**
     * Build flow builders passed in param and execute flows in parallel
     * @param flowBuilders to execute in parallel
     * @return builder
     */
    @Override
    //left here for backwards compatibility, because moving method to parent changes signature in bytecode
    public TestScenarioBuilder split(TestStepFlowBuilder... flowBuilders) {
        return super.split(flowBuilders);
    }

    /**
     * Returns a constructed instance of scenario.
     *
     * @return scenario
     */
    @Override
    //left here for backwards compatibility, because moving method to parent changes signature in bytecode
    public TestScenario build() {
        return super.build();
    }
}
