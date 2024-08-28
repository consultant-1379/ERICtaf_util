/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.de.scenariorx.examples;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.examples.CyclicDataSourceExample.TestIds.PUT_USER_TO_STACK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.TafUser;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.ericsson.de.scenariorx.examples.mocks.Operator;
import org.junit.Test;

public class CyclicDataSourceExample {

    final protected Stack<String> stack = new Stack<>();

    static class TestIds {
        static final String PUT_USER_TO_STACK = "PUT_USER_TO_STACK";
    }

    private static final String USERS = "USERS";
    private static final String NODES = "NODES";

    private Operator operator = new Operator();

    @TestStep(id = PUT_USER_TO_STACK)
    public void userToStack(@Input(TafUser.USERNAME) String username, @Input(TafNode.NETWORK_ELEMENT_ID) String nodeId) {
        stack.push(username + "-" + nodeId);
    }

    @Test
    public void cyclicDataSource() throws Exception {
        RxDataSource<TafUser> users = TafRxScenarios.dataSource(USERS, fromCsv("data/user.csv"), TafUser.class);
        RxDataSource<TafNode> nodes = TafRxScenarios.dataSource(NODES, fromCsv("data/node.csv"), TafNode.class);

        RxDataSource<TafUser> admins = users
                .filterField(TafUser.ENABLED).equalTo(true)
                .filterField(TafUser.ROLES).contains("ADMINISTRATOR");

        assertThat(nodes).hasSize(3);
        assertThat(admins).hasSize(2);
        assertThat(admins.getSize()).isLessThan(nodes.getSize());

        RxScenario scenario = scenario()
                .addFlow(flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(this, PUT_USER_TO_STACK))
                        .withVUsers(3)
                        .withDataSources(
                                nodes.shared(),
                                admins.shared().cyclic()
                        )
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactlyInAnyOrder("administrator1-SGSN-14B", "administrator2-LTE01ERB", "administrator1-LTE08dg2");
    }
}
