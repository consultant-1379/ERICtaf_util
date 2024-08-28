package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 17.10.2016
 */
public class TestFlowParameters {

    private static final String WARNING = "FlowParameters";

    @AssertWarning(WARNING)
    public TestStepFlow withoutDataSourceParameter(String dataSourceName) {
        return flow("flow").build();
    }

    @AssertNoWarning(WARNING)
    public TestStepFlow flowParameterized(String someParameter, TafDataSourceDefinitionBuilder<?> dataSource) {
        return flow("flow").withDataSources(dataSource).build();
    }

    @AssertNoWarning(WARNING)
    public void standAloneMethod(String dataSourceName) {
        flow("flow").build();
    }

}
