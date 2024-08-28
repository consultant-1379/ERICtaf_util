package com.ericsson.cifwk.taf.findbugs.sample;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.enm.scenarios.FailingSummaryLogger;
import one.util.huntbugs.registry.anno.AssertWarning;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 31.10.2016
 */
public class TestFailingSummaryLoggerUsed {

    @AssertWarning("FailingSummaryLoggerUsed")
    public void scenario() {

        // forbidden class used
        FailingSummaryLogger logger = new FailingSummaryLogger();

        // usage example
        TestScenario scenario = TestScenarios.scenario("Temp")
//                .withTestStepExceptionHandler(logger.exceptionHandler())
                .build();
        TestScenarioRunner runner = runner()
                .withListener(logger)
                .build();
        runner.start(scenario);
    }

}
