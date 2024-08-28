package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import one.util.huntbugs.registry.anno.AssertNoWarning;
import one.util.huntbugs.registry.anno.AssertWarning;
import org.assertj.core.util.Preconditions;

import java.util.Objects;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 01.11.2016
 */
public class TestCollectResultToDatasource {

    public static final String WARNING = "CollectResultToDatasource";

    @AssertNoWarning(WARNING)
    public TestStepFlowBuilder valueFromParameter(final String resultCollectingDataSource,
                                              TafDataSourceDefinitionBuilder<?> dataSource) {
        return TestScenarios.flow("ACTIVATE_CONFIG_FLOW_NAME")
                .addTestStep(TestScenarios.annotatedMethod(this, "ACTIVATE")
                        .collectResultToDatasource(resultCollectingDataSource))
                .withDataSources(dataSource);
    }

    @AssertWarning(WARNING)
    public TestStepFlowBuilder valueFromFilteredParameter(final String resultCollectingDataSource) {
        return TestScenarios.flow("ACTIVATE_CONFIG_FLOW_NAME")
                .addTestStep(TestScenarios.annotatedMethod(this, "ACTIVATE")
                        .collectResultToDatasource(Preconditions.checkNotNull(resultCollectingDataSource, "Oops")));
    }

    @AssertWarning(WARNING)
    public TestStepFlowBuilder hardcodedValue() {

        // collecting data source is hardcoded
        return TestScenarios.flow("ACTIVATE_CONFIG_FLOW_NAME")
                .addTestStep(TestScenarios.annotatedMethod(this, "ACTIVATE")
                        .collectResultToDatasource("resultCollectingDataSource"));
    }

    @AssertWarning(WARNING)
    public TestStepFlowBuilder calculateValue() {

        // collecting data source is hardcoded
        return TestScenarios.flow("ACTIVATE_CONFIG_FLOW_NAME")
                .addTestStep(TestScenarios.annotatedMethod(this, "ACTIVATE")
                        .collectResultToDatasource(Objects.toString("")));
    }

    @AssertWarning(WARNING)
    public TestStepFlowBuilder valueFromLocalVariable(TafDataSourceDefinitionBuilder<?> dataSource) {

        // collecting data source is not provided as method parameter
        final String resultCollectingDataSource = "resultCollectingDataSource";

        return TestScenarios.flow("ACTIVATE_CONFIG_FLOW_NAME")
                .addTestStep(TestScenarios.annotatedMethod(this, "ACTIVATE")
                        .collectResultToDatasource(resultCollectingDataSource))
                .withDataSources(dataSource);
    }

}
