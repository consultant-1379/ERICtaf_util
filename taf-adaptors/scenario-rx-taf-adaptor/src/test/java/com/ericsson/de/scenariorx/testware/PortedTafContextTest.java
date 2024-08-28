/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.testware;

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.impl.Api.fromIterable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Compare with com.ericsson.cifwk.taf.scenario.impl.TafContextTest
 */
public class PortedTafContextTest {

    private RxDataSource<Integer> vUserEmulation = fromIterable("vUser", asList(1, 2)).shared();
    private Stack<String> stack = new Stack<>();

    private static final String LOGIN_TO_CONTEXT = "TAF_LOGIN_TO_CONTEXT";
    private static final String ASSERT_CONTEXT_HAVE_SAME_VUSER = "TAF_ASSERT_CONTEXT_HAVE_SAME_USER";

    @Test
    public void testPassValuesBetweenFlow() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                /** {@link this#loginToContext(Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN_TO_CONTEXT))
                                /** {@link this#assertContextHaveSameVuser(Integer)}*/
                                .addTestStep(TafRxScenarios.annotatedMethod(this, ASSERT_CONTEXT_HAVE_SAME_VUSER))
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    public void testFlowsHaveSameContext() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow1")
                                /** {@link this#loginToContext(Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN_TO_CONTEXT))
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .addFlow(
                        flow("flow2")
                                /** {@link this#assertContextHaveSameVuser(Integer)}*/
                                .addTestStep(TafRxScenarios.annotatedMethod(this, ASSERT_CONTEXT_HAVE_SAME_VUSER))
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("null", "null");
    }

    @Test
    public void testSubFlowsHaveSameContext() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                .addSubFlow(
                                        flow("flow1")
                                                /** {@link this#loginToContext(Integer)} */
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN_TO_CONTEXT))
                                )
                                .addTestStep(TafRxScenarios.annotatedMethod(this, ASSERT_CONTEXT_HAVE_SAME_VUSER))
                                .addSubFlow(
                                        flow("flow2")
                                                /** {@link this#assertContextHaveSameVuser(Integer)}*/
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, ASSERT_CONTEXT_HAVE_SAME_VUSER))
                                )
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("null", "null", "null", "null");
    }

    @Test
    public void testSubFlowInheritParentFlowValue() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                /** {@link this#loginToContext(Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN_TO_CONTEXT))
                                .addSubFlow(
                                        flow("flow2")
                                                /** {@link this#assertContextHaveSameVuser(Integer)}*/
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, ASSERT_CONTEXT_HAVE_SAME_VUSER))
                                )
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    public void testParallelFlowsHaveItsOwnContext() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                .split(
                                        flow("flow1")
                                                /** {@link this#loginToContext(Integer)} */
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN_TO_CONTEXT))
                                                .withDataSources(vUserEmulation)
                                                .withVUsers(2)
                                )
                                .split(
                                        flow("flow2")
                                                /** {@link this#assertContextHaveSameVuser(Integer)}*/
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, ASSERT_CONTEXT_HAVE_SAME_VUSER))
                                                .withDataSources(vUserEmulation)
                                                .withVUsers(2)
                                )
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("null", "null");
    }

    @TestStep(id = LOGIN_TO_CONTEXT)
    public void loginToContext(@Input("vUser") Integer vUser) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.setAttribute("user", vUser);
    }

    @TestStep(id = ASSERT_CONTEXT_HAVE_SAME_VUSER)
    public void assertContextHaveSameVuser(@Input("vUser") Integer vUser) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        Integer user = context.getAttribute("user");

        stack.push(String.valueOf(user));
        if (user != null) {
            Assertions.assertThat(user).isEqualTo(vUser);
        }
    }

}
