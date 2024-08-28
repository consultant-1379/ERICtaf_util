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
import static com.ericsson.de.scenariorx.examples.FilteringDataSourceExample.TestIds.LOGIN;
import static com.ericsson.de.scenariorx.examples.FilteringDataSourceExample.TestIds.PUT_USER_TO_STACK;
import static com.ericsson.de.scenariorx.examples.FilteringDataSourceExample.TestIds.PUT_USER_TO_STACK_2;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordBuilder;
import com.ericsson.de.scenariorx.TafUser;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.ericsson.de.scenariorx.examples.mocks.HttpTool;
import com.ericsson.de.scenariorx.examples.mocks.Operator;
import org.junit.Test;

public class FilteringDataSourceExample {
    final protected Stack<String> stack = new Stack<String>();
    final protected Stack<String> stack2 = new Stack<String>();

    static class TestIds {
        public static final String LOGIN = "LOGIN";
        public static final String PUT_USER_TO_STACK = "PUT_USER_TO_STACK";
        public static final String PUT_USER_TO_STACK_2 = "PUT_USER_TO_STACK_2";
    }

    private static final String USERS = "USERS";

    private Operator operator = new Operator();

    @TestStep(id = LOGIN)
    public DataRecord login(@Input(TafUser.USERNAME) String username, @Input(TafUser.PASSWORD) String password) {
        HttpTool tool = operator.login(username, password);

        return new DataRecordBuilder()
                .setField("session", tool)
                .build(); // ← value will be put to context
    }

    @TestStep(id = PUT_USER_TO_STACK)
    public void userToStack(@Input("session") HttpTool tool) {
        stack.push(tool.getUsername());
    }

    @TestStep(id = PUT_USER_TO_STACK_2)
    public void userToStack2(@Input("session") HttpTool tool) {
        stack2.push(tool.getUsername());
    }

    @Test
    public void enabledUsers() throws Exception {
        RxDataSource<TafUser> users = TafRxScenarios.dataSource(USERS, fromCsv("data/user.csv"), TafUser.class);

        RxScenario allUsers = scenario()
                .addFlow(
                        flow()
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, PUT_USER_TO_STACK))
                                .withDataSources(
                                        users
                                                .filterField(TafUser.ENABLED).equalTo(true)
                                                .filterField(TafUser.ROLES).contains("ADMINISTRATOR")
                                )

                )
                .build();

        RxApi.run(allUsers);

        assertThat(stack).containsExactly("administrator1", "administrator2");
    }

    @Test
    public void dataSourceSlices() throws Exception {
        RxDataSource<TafUser> allUsers = TafRxScenarios.dataSource(USERS, fromCsv("data/user.csv"), TafUser.class);

        RxDataSource<TafUser> enabledUsers = allUsers
                .filterField(TafUser.ENABLED).equalTo(true);

        RxDataSource<TafUser> adminUsers = enabledUsers
                .filterField(TafUser.ROLES).contains("ADMINISTRATOR");

        RxScenario scenario = scenario()
                .addFlow(
                        flow("onlyAdmins")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, PUT_USER_TO_STACK))
                                .withDataSources(
                                        adminUsers
                                )
                )
                .addFlow(
                        flow("enabledUsers")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, LOGIN))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, PUT_USER_TO_STACK_2))
                                .withDataSources(
                                        enabledUsers
                                )
                )
                .build();

        RxApi.run(scenario);

        assertThat(stack).containsExactly("administrator1", "administrator2");
        assertThat(stack2).containsExactly("administrator1", "administrator2", "user");
    }

}
