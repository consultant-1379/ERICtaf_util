package com.ericsson.de.scenariorx.testware;

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.fromIterable;
import static com.ericsson.de.scenariorx.impl.Api.runnable;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;
import java.util.Stack;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.ericsson.de.scenariorx.impl.ScenarioDebugger;
import org.assertj.core.api.AbstractListAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AlwaysRunTest {

    private TestSteps testSteps;

    private List<String> digits123 = newArrayList("1", "2", "3");
    private List<String> digits456 = newArrayList("4", "5", "6");

    @Before
    public void setUp() throws Exception {
        testSteps = new TestSteps();
    }

    @Test
    public void testStep_shouldNotRun_inFlow_whenPreviousFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("3"))
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .addFlow(flow("Flow 2")
                        .addTestStep(pushToStack().alwaysRun())
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits456).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().isEmpty();
    }

    @Test
    public void testStep_shouldNotRun_inSubFlow_whenMainFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(pushToStack().alwaysRun())
                        )
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1");
    }

    @Test
    public void testStep_shouldNotRun_inSubFlow_whenPreviousSubFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow 1")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        )
                        .addSubFlow(flow("Sub Flow 2")
                                .addTestStep(pushToStack().alwaysRun())
                        )
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1");
    }

    @Test
    public void subFlow_shouldNotRun_inFlow_whenPreviousFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("3"))
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .addFlow(flow("Flow 2")
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(pushToStack())
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits456).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().isEmpty();
    }

    @Test
    public void subFlow_shouldNotRun_whenInnerTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                                .addTestStep(pushToStack())
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1");
    }

    @Test
    public void subSubFlow_shouldNotRun_whenInnerTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                                .addSubFlow(flow("Sub Sub Flow")
                                        .addTestStep(pushToStack())
                                )
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1");
    }

    @Test
    public void split_shouldNotRun_whenInnerTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .split(
                                flow("Split Flow")
                                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                                        .addTestStep(pushToStack())
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1");
    }

    @Test
    public void scenario_shouldNotRun_whenInnerTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        .addTestStep(pushToStack())
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .alwaysRun()
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1");
    }

    @Test
    public void testStep_shouldRun_whenPreviousTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        .addTestStep(pushToStack().alwaysRun())
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1", "2");
    }

    @Test
    public void testStep_shouldRun_whenSiblingStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        .addTestStep(pushToStack().alwaysRun())
                        .withVUsers(digits123.size())
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    public void testStep_shouldRun_whenPreviousSubFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        )
                        .addTestStep(pushToStack().alwaysRun())
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1", "2");
    }

    @Test
    public void subFlow_shouldRun_whenPreviousTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(pushToStack())
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1", "2");
    }

    @Test
    public void subFlow_shouldRun_whenPreviousSubFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow 1")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        )
                        .addSubFlow(flow("Sub Flow 2")
                                .addTestStep(pushToStack())
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1", "2");
    }

    @Test
    public void subSubFlow_shouldRun_whenPreviousTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        .addSubFlow(flow("Sub Flow")
                                .addSubFlow(flow("Sub Sub Flow")
                                        .addTestStep(pushToStack())
                                )
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1", "2");
    }

    @Test
    public void subSubFlow_shouldRun_whenPreviousSubFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow 1")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("2"))
                        )
                        .addSubFlow(flow("Sub Flow 2")
                                .addSubFlow(flow("Sub Sub Flow")
                                        .addTestStep(pushToStack())
                                )
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("1", "2");
    }

    @Test
    public void split_shouldRun_whenPreviousTestStepFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("1"))
                        .split(
                                flow("Split Flow 1")
                                        .addTestStep(pushToStack()),
                                flow("Split Flow 2")
                                        .addTestStep(runnable(pushToStack("Data")))
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactlyInAnyOrder("1", "Data");
    }

    @Test
    public void split_shouldRun_whenPreviousSubFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Main Flow")
                        .addSubFlow(flow("Sub Flow")
                                .addTestStep(throwExceptionWhenDataParameterEqualTo("1"))
                        )
                        .split(
                                flow("Split Flow 1")
                                        .addTestStep(pushToStack()),
                                flow("Split Flow 2")
                                        .addTestStep(runnable(pushToStack("Data")))
                        )
                        .alwaysRun()
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactlyInAnyOrder("1", "Data");
    }

    @Test
    public void scenario_shouldRun_whenPreviousFlowFailed() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(flow("Flow 1")
                        .addTestStep(throwExceptionWhenDataParameterEqualTo("3"))
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits123).shared())
                )
                .addFlow(flow("Flow 2")
                        .addTestStep(pushToStack())
                        .withDataSources(fromIterable(TestSteps.Params.DATA, digits456).shared())
                )
                .alwaysRun()
                .build();

        ScenarioDebugger.debug(scenario);

        assertThatStack().containsExactly("4", "5", "6");
    }

    private RxTestStep throwExceptionWhenDataParameterEqualTo(String value) {
        return TafRxScenarios.annotatedMethod(testSteps, TestSteps.THROW_EXCEPTION)
                .withParameter(TestSteps.Params.THROW_PARAM).value(value);
    }

    private RxTestStep pushToStack() {
        return TafRxScenarios.annotatedMethod(testSteps, TestSteps.PUSH_DATA_TO_STACK);
    }

    private Runnable pushToStack(final String data) {
        return new Runnable() {
            @Override
            public void run() {
                testSteps.pushToStack(data);
            }
        };
    }

    private static class TestSteps {

        private static final String THROW_EXCEPTION = "throwException";
        private static final String PUSH_DATA_TO_STACK = "pushDataToStack";

        private Stack<String> stack = new Stack<>();

        private interface Params {
            String DATA = "data";
            String THROW_PARAM = "throwParam";
        }

        @TestStep(id = THROW_EXCEPTION)
        public void throwException(@Input(Params.DATA) String data,
                                   @Input(Params.THROW_PARAM) String throwParam) {
            if (Objects.equals(data, throwParam)) {
                throw new RuntimeException();
            }
        }

        @TestStep(id = PUSH_DATA_TO_STACK)
        public void pushDataToStack(@Input(Params.DATA) String data) {
            pushToStack(data);
        }

        private void pushToStack(String data) {
            stack.push(data);
        }
    }

    private AbstractListAssert<?, ? extends List<? extends String>, String> assertThatStack() {
        return assertThat(testSteps.stack);
    }
}
