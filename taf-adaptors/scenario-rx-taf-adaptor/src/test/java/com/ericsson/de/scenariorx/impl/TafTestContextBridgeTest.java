/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.impl;

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
import com.ericsson.de.scenariorx.api.RxBasicDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import org.junit.Test;

public class TafTestContextBridgeTest {

    private RxDataSource<Integer> vUserEmulation = fromIterable("vUser", asList(1, 2)).shared();
    private Stack<String> stack = new Stack<>();

    private static final String TAF_LOGIN_TO_CONTEXT = "TAF_LOGIN_TO_CONTEXT";
    private static final String TAF_ASSERT_CONTEXT_HAVE_SAME_USER = "TAF_ASSERT_CONTEXT_HAVE_SAME_USER";

    private static final String RX_LOGIN_TO_CONTEXT = "RX_LOGIN_TO_CONTEXT";
    private static final String RX_ASSERT_CONTEXT_HAVE_SAME_USER = "RX_ASSERT_CONTEXT_HAVE_SAME_USER";

    private static final String LOGGED_IN_USER = "loggedInUser";

    @Test
    public void tafToRx() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                /** {@link this#tafLoginToContext(Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, TAF_LOGIN_TO_CONTEXT))
                                /** {@link this#rxAssertContextHaveSameUser(Integer, Integer)}*/
                                .addTestStep(TafRxScenarios.annotatedMethod(this, RX_ASSERT_CONTEXT_HAVE_SAME_USER))
                                .addSubFlow(flow("subFlow")
                                        /** {@link this#rxAssertContextHaveSameUser(Integer, Integer)}*/
                                        .addTestStep(TafRxScenarios.annotatedMethod(this, RX_ASSERT_CONTEXT_HAVE_SAME_USER)))
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("1", "2", "1", "2");
    }

    @Test
    public void rxToTaf() throws Exception {
        RxScenario scenario = scenario()
                .addFlow(
                        flow("flow")
                                /** {@link this#rxLoginToContext(Integer)} */
                                .addTestStep(TafRxScenarios.annotatedMethod(this, RX_LOGIN_TO_CONTEXT))
                                /** {@link this#tafAssertContextHaveSameUser(Integer)}*/
                                .addTestStep(TafRxScenarios.annotatedMethod(this, TAF_ASSERT_CONTEXT_HAVE_SAME_USER))
                                .addSubFlow(flow("subFlow")
                                        /** {@link this#tafAssertContextHaveSameUser(Integer)}}*/
                                        .addTestStep(TafRxScenarios.annotatedMethod(this, TAF_ASSERT_CONTEXT_HAVE_SAME_USER)))
                                .withDataSources(vUserEmulation)
                                .withVUsers(2)
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("1", "2", "1", "2");
    }

    @TestStep(id = TAF_LOGIN_TO_CONTEXT)
    public void tafLoginToContext(@Input("vUser") Integer vUser) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.setAttribute(LOGGED_IN_USER, vUser);
    }

    @TestStep(id = TAF_ASSERT_CONTEXT_HAVE_SAME_USER)
    public void tafAssertContextHaveSameUser(@Input("vUser") Integer vUser) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        Integer loggedInUser = context.getAttribute(LOGGED_IN_USER);

        stack.push(String.valueOf(loggedInUser));
        assertThat(loggedInUser).isEqualTo(vUser);
    }

    @TestStep(id = RX_LOGIN_TO_CONTEXT)
    public RxDataRecord rxLoginToContext(@Input("vUser") Integer vUser) {
        return RxBasicDataRecord.builder()
                .setField(LOGGED_IN_USER, vUser)
                .build();
    }

    @TestStep(id = RX_ASSERT_CONTEXT_HAVE_SAME_USER)
    public void rxAssertContextHaveSameUser(@Input("vUser") Integer vUser, @Input(LOGGED_IN_USER) Integer user) {
        stack.push(String.valueOf(user));
        assertThat(user).isEqualTo(vUser);
    }

}