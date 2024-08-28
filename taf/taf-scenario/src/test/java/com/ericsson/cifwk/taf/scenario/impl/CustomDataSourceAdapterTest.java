package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.datasources.ExternalDataSourceAdapter;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.fail;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 09/05/2016
 */
public class CustomDataSourceAdapterTest extends ScenarioTest {

    @Test
    public void shouldRunWithExternalDataSource() {
        TestScenario scenario = getFlowForTestStep("externalDsSum", ExternalDataSourceAdapter.SUPPORTED_DATA_SOURCE_NAME);
        runner.start(scenario);
        assertThat(count.get(), Matchers.equalTo(6));
    }

    @Test(expected = InvalidDataSourceException.class)
    public void shouldFailOnMissingDataSource() {
        TestScenario scenario = getFlowForTestStep("noSuchDS", "missingDS");
        try {
            runner.start(scenario);
        } catch (InvalidDataSourceException e) {
            Throwable cause = e.getCause();
            assertThat(cause.getMessage(), containsString("Failed to find an applicable data source for name 'missingDS'"));
            assertThat(cause.getMessage(), containsString("com.ericsson.cifwk.taf.scenario.datasources.ExternalDataSourceAdapter"));
            assertThat(cause.getMessage(), containsString("com.ericsson.cifwk.taf.scenario.TafDataSourceAdapter"));
            throw e;
        }
    }

    private TestScenario getFlowForTestStep(String stepName, String dataSourceName) {
        TestStepFlow flow = flow("Test")
                .addTestStep(annotatedMethod(this, stepName))
                .withDataSources(dataSource(dataSourceName))   // provided by TdmDataSourceAdapter
                .build();

        return scenario()
                .addFlow(flow)
                .withDefaultVusers(2)
                .build();
    }

    @TestStep(id = "externalDsSum")
    public void testExternalDataSourceAdapter(@Input("var1") Integer var1,
                                              @Input("var2") Integer var2,
                                              @Input("sum") Integer sum) {
        count.incrementAndGet();
        assertThat(var1 + var2, equalTo(sum));
    }

    @TestStep(id = "noSuchDS")
    public void noSuchDataSourceTest(@Input("param1") String param1) {
        fail("This shouldn't have been called");
    }

}
