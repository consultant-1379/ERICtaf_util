package com.ericsson.de.scenariorx.examples;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordBuilder;
import com.ericsson.cifwk.taf.guice.OperatorLookupModuleFactory;
import com.ericsson.de.scenariorx.TafNode;
import com.ericsson.de.scenariorx.api.DebugGraphMode;
import com.ericsson.de.scenariorx.api.RxApi;
import com.ericsson.de.scenariorx.api.RxBasicDataRecord;
import com.ericsson.de.scenariorx.api.RxCompositeExceptionHandler;
import com.ericsson.de.scenariorx.api.RxContextDataSource;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataSource;
import com.ericsson.de.scenariorx.api.RxDataRecordWrapper;
import com.ericsson.de.scenariorx.api.RxExceptionHandler;
import com.ericsson.de.scenariorx.api.RxFlow;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxScenarioListener;
import com.ericsson.de.scenariorx.api.RxScenarioRunner;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.ericsson.de.scenariorx.api.TafRxScenarios;
import com.ericsson.de.scenariorx.api.events.RxFlowEvent;
import com.ericsson.de.scenariorx.examples.mocks.HttpTool;
import com.ericsson.de.scenariorx.examples.mocks.Operator;
import com.ericsson.de.scenariorx.impl.Api;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import static com.ericsson.de.scenariorx.api.RxApi.run;
import static com.ericsson.de.scenariorx.api.RxBasicDataRecord.fromValues;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class ScenarioExamples {

    private static final class Ids {
        /**
         * @see #testStep1()
         */
        static final String TEST_STEP_1 = "TEST_STEP_1";

        /**
         * @see #testStep2()
         */

        static final String TEST_STEP_2 = "TEST_STEP_2";
        /**
         * @see #testStep3()
         */
        static final String TEST_STEP_3 = "TEST_STEP_3";

        /**
         * @see #testStep4()
         */
        static final String TEST_STEP_4 = "TEST_STEP_4";

        /**
         * @see #fakeTestStep()
         */
        static final String FAKE = "FAKE";

        /**
         * @see #login(String, String) ()
         */
        static final String LOGIN = "login";

        /**
         * @see #login2()
         */
        static final String LOGIN2 = "LOGIN2";

        /**
         * @see #createNode()
         */
        static final String CREATE_NODE = "CREATE_NODE";

        /**
         * @see #collectionOfNodes()
         */
        static final String CREATE_3_NODES = "CREATE_3_NODES";

        /**
         * @see #createNodeAlt()
         */
        static final String CREATE_NODE_2 = "CREATE_NODE_2";

        /**
         * @see #consumer(Object, Object, Object)
         */
        static final String CONSUMER = "CONSUMER";

        /**
         * @see #loginUsingUserObject(User)
         */
        static final String LOGIN_USING_USER_OBJECT = "login using user object";

        /**
         * @see #composeEmail()
         */
        static final String COMPOSE_EMAIL = "send email";

        /**
         * @see #sendEmail()
         */
        static final String SEND_EMAIL = "send email";

        /**
         * @see #logout()
         */
        static final String LOGOUT = "logout";

        /**
         * @see #multipleRecords(User)
         */
        static final String DATA_RECORD = "dataRecord";

        /**
         * @see #setup()
         */
        static final String SETUP = "setup";

        /**
         * @see #teardown()
         */
        static final String TEARDOWN = "teardown";

        /**
         * @see #multipleRecords(String, String)
         */
        static final String FIELDS_OF_DATA_RECORD = "fieldsOfDataRecord";

        /**
         * @see #stepCheckDataRecordName()
         */
        static final String STEP_CHECK_DATA_RECORD_NAME = "stepCheckDataRecordName";

        static final String STEP_WITH_EXCEPTION = "stepWithException";
        static final String REPEAT_WHILE = "printDataRecord";
    }

    private static final Stack<String> STACK = new Stack<>();


    @Before
    public void setupMethod() {
        Module module = new OperatorLookupModuleFactory().assembleGuiceModule();
        Guice.createInjector(module).injectMembers(this);
        STACK.clear();
    }

    /**
     * ### Scenario Example
     */

    @Test
    public void simpleScenario() {
        // START SNIPPET: SIMPLEST_SCENARIO_EXAMPLE
        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        RxApi.flow("flow")
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_2))
                )
                .build();
        //  END SNIPPET: SIMPLEST_SCENARIO_EXAMPLE

        TafRxScenarios.run(scenario);

        assertThat(STACK).containsExactly(Ids.TEST_STEP_1, Ids.TEST_STEP_2);
    }


    @TestStep(id = Ids.TEST_STEP_1)
    public void testStep1() {
        STACK.push(Ids.TEST_STEP_1);
    }

    @TestStep(id = Ids.TEST_STEP_2)
    public void testStep2() {
        STACK.push(Ids.TEST_STEP_2);
    }

    /**
     * #### Flow Example
     */
    @Test
    public void flowExample() {
        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        // START SNIPPET: FLOW_EXAMPLE
                        RxApi.flow("send email")
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, Ids.LOGIN)
                                                // ↑ execute method from current class (this) annotated with `@TestStep(id = Ids.LOGIN)`.
                                                .withParameter("username").value("root")
                                                //↑ passing parameters to Test Step defined in previous slide
                                                .withParameter("password").value("shroot")
                                )
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.COMPOSE_EMAIL))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.SEND_EMAIL))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.LOGOUT))
                        // END SNIPPET: FLOW_EXAMPLE
                )
                .build();

        TafRxScenarios.run(scenario);

        assertThat(STACK).containsExactly(Ids.LOGIN, Ids.COMPOSE_EMAIL, Ids.SEND_EMAIL, Ids.LOGOUT);
    }

    @TestStep(id = Ids.LOGIN)
    public void login(@Input("username") String username, @Input("password") String password) {
        STACK.push(Ids.LOGIN);
    }

    @TestStep(id = Ids.LOGIN_USING_USER_OBJECT)
    public void loginUsingUserObject(@Input("user") User user) {
        STACK.push(Ids.LOGIN_USING_USER_OBJECT);
    }

    @TestStep(id = Ids.COMPOSE_EMAIL)
    public void composeEmail() {
        STACK.push(Ids.COMPOSE_EMAIL);
    }

    @TestStep(id = Ids.SEND_EMAIL)
    public void sendEmail() {
        STACK.push(Ids.SEND_EMAIL);
    }

    @TestStep(id = Ids.LOGOUT)
    public void logout() {
        STACK.push(Ids.LOGOUT);
    }

    /**
     * ## Defining & Running Scenario
     */
    @Test
    public void definingAndRunningScenario() {
        RxFlow createUsers = createUsersFlow();
        RxFlow sendEmail = createSendEmailFlow();
        RxFlow cleanupUsers = createCleanupUsersFlow();

        // START SNIPPET: DEFINING_SCENARIO
        RxScenario scenario = RxApi.scenario("email SUT test")
                .addFlow(createUsers)
                .addFlow(sendEmail)
                .addFlow(cleanupUsers).alwaysRun()
                .build();
        // END SNIPPET: DEFINING_SCENARIO

        // START SNIPPET: RUNNING_SCENARIO
        TafRxScenarios.run(scenario);
        // END SNIPPET: RUNNING_SCENARIO

        // START SNIPPET: SCENARIO_RUNNER
        RxScenarioRunner runner = RxApi.runner()
                // ...invoke builder methods...
                .build();
        runner.run(scenario);
        // END SNIPPET: SCENARIO_RUNNER
    }

    private RxFlow createUsersFlow() {
        return RxApi.flow("createUsers").addTestStep(fakeAnnotatedMethod()).build();
    }

    private RxFlow createSendEmailFlow() {
        return RxApi.flow("sendEmail").addTestStep(fakeAnnotatedMethod()).build();
    }

    private RxFlow createCleanupUsersFlow() {
        return RxApi.flow("cleanupUsers").addTestStep(fakeAnnotatedMethod()).build();
    }

    private RxTestStep fakeAnnotatedMethod() {
        return TafRxScenarios.annotatedMethod(this, Ids.FAKE);
    }

    @TestStep(id = Ids.FAKE)
    public void fakeTestStep() {
        STACK.push(Ids.FAKE);
    }

    /**
     * ### Scenario Listeners
     */
    @Test
    public void scenarioListeners() throws Exception {
        RxScenario scenario = RxApi.scenario()
                .addFlow(RxApi.flow()
                        .addTestStep(fakeAnnotatedMethod())
                )
                .build();

        // START SNIPPET: SCENARIO_LISTENER
        RxScenarioListener listener = new RxScenarioListener() {
            @Override
            public void onFlowStarted(RxFlowEvent.RxFlowStartedEvent event) {
                RxFlow flow = event.getFlow();
                // ...log read-only flow information...
            }
        };

        RxScenarioRunner runner = RxApi.runner()
                .addListener(listener)
                .build();
        // END SNIPPET: SCENARIO_LISTENER
    }

    /**
     * #### Adding Exception Handlers
     */
    @Test
    public void addingExceptionHandlers() throws Exception {
        RxExceptionHandler flowHandler = RxExceptionHandler.IGNORE;
        RxExceptionHandler scenarioHandler = RxExceptionHandler.IGNORE;
        RxExceptionHandler defaultRunnerHandler = RxExceptionHandler.IGNORE;

        // START SNIPPET: EXCEPTION_HANDLERS
        RxScenario scenario = RxApi.scenario()
                .addFlow(RxApi.flow()
                        .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                        // Flow level
                        .withExceptionHandler(flowHandler)
                )
                // Scenario level
                .withExceptionHandler(scenarioHandler)
                .build();

        RxScenarioRunner runner = RxApi.runner()
                // Runner level
                .withDefaultExceptionHandler(defaultRunnerHandler)
                .build();
        // END SNIPPET: EXCEPTION_HANDLERS
    }

    @Test
    public void compositeExceptionHandler() throws Exception {
        RxExceptionHandler handler1 = RxExceptionHandler.IGNORE;
        RxExceptionHandler handler2 = RxExceptionHandler.IGNORE;
        RxExceptionHandler handler3 = RxExceptionHandler.IGNORE;
        RxExceptionHandler finalHandler = RxExceptionHandler.IGNORE;

        // START SNIPPET: COMPOSITE_EXCEPTION_HANDLER
        RxCompositeExceptionHandler composite = TafRxScenarios.compositeExceptionHandler()
                .addExceptionHandler(handler1)
                .addExceptionHandler(handler2)
                .addExceptionHandler(handler3)
                .setFinalExceptionHandler(finalHandler)
                .build();

        RxFlow flow = RxApi.flow()
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                .withExceptionHandler(composite)
                .build();
        // END SNIPPET: COMPOSITE_EXCEPTION_HANDLER
    }

    /**
     * #### Exception propagation
     */
    @Test
    public void exceptionPropagation() throws Exception {
        // START SNIPPET: EXCEPTION_PROPAGATION
        RxExceptionHandler subFlowHandlerPropagate = RxExceptionHandler.PROPAGATE;
        RxExceptionHandler flowHandlerPropagate = RxExceptionHandler.PROPAGATE;
        RxExceptionHandler scenarioHandlerIgnore = RxExceptionHandler.IGNORE;

        RxScenario scenario = RxApi.scenario("Scenario")
                .addFlow(RxApi.flow("Flow")
                        .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                        .addSubFlow(RxApi.flow("Subflow")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_2))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.STEP_WITH_EXCEPTION))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                                .withExceptionHandler(subFlowHandlerPropagate)
                        )
                        .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_4))
                        .withExceptionHandler(flowHandlerPropagate)
                )
                .withExceptionHandler(scenarioHandlerIgnore)
                .build();
        // END SNIPPET: EXCEPTION_PROPAGATION
    }

    /**
     * ### Data Records
     */
    // START SNIPPET: USER_WITHOUT_FIELD_NAMES
    public interface User extends RxDataRecord {

        String getId();

        String getUsername();

        String getPassword();

        String getEnabled();

        String getRoles();


    }
    // END SNIPPET: USER_WITHOUT_FIELD_NAMES

    // START SNIPPET: USER_WITH_FIELD_NAMES
    public interface UserWithFieldNames extends RxDataRecord {
        String USERNAME = "username";
        String PASSWORD = "password";

        String getUsername();

        String getPassword();
    }
    // END SNIPPET: USER_WITH_FIELD_NAMES

    /**
     * - Data Record
     */
    // START SNIPPET: DATA_RECORD
    @TestStep(id = Ids.DATA_RECORD)
    public void multipleRecords(@Input("users_datasource") User user) {
        STACK.push(user.getUsername());
    }
    // END SNIPPET: DATA_RECORD

    /**
     * - Fields of Data Records
     */
    // START SNIPPET: FIELDS_OF_RECORD
    @TestStep(id = Ids.FIELDS_OF_DATA_RECORD)
    public void multipleRecords(@Input(UserWithFieldNames.USERNAME) String username,
                                @Input(UserWithFieldNames.PASSWORD) String password) {
        STACK.push(Ids.FIELDS_OF_DATA_RECORD);
    }
    // END SNIPPET: FIELDS_OF_RECORD

    /**
     * ### Data Sources
     */

    @Test
    public void dataSourceExample() {
        // START SNIPPET: DATA_SOURCE
        RxDataSource<Node> myDs = Api.fromCsv("myCsvDataSource", "data/node.csv", Node.class);

        RxFlow flow = RxApi.flow("Test")
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.STEP_CHECK_DATA_RECORD_NAME))
                .withDataSources(myDs)
                .build();
        // END SNIPPET: DATA_SOURCE

        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(flow)
                .build();

        TafRxScenarios.run(scenario);

        assertThat(STACK).hasSize(3).containsOnly(Ids.STEP_CHECK_DATA_RECORD_NAME);
    }

    @TestStep(id = Ids.STEP_CHECK_DATA_RECORD_NAME)
    public void stepCheckDataRecordName() {
        STACK.push(Ids.STEP_CHECK_DATA_RECORD_NAME);
    }


    private interface Node extends RxDataRecord {
        String getNetworkElementId();

        String getNetworkNodeType();
    }

    /**
     * ### Creating Data Source
     */

    @Test
    public void creatingDataSourceExample() {
        // START SNIPPET: CREATING_DATA_SOURCE
        List<String> iterable = newArrayList("1", "2", "3", "4");
        RxDataSource dataSource11 = Api.fromIterable("dataSource1", iterable);
        RxDataSource dataSource12 = Api.fromDataRecords("dataSource12",
                fromValues("key", "value"),
                fromValues("key", "value")
        );
        // END SNIPPET: CREATING_DATA_SOURCE
    }

    /**
     * ### Data Source example
     */
    @Test
    public void annotatedMethodWithParams() {
        // START SNIPPET: ANNOTATED_METHOD_WITH_PARAMS
        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        RxApi.flow("send email")
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, Ids.LOGIN)
                                                .withParameter("username").value("root")
                                                .withParameter("password").value("shroot")
                                )
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.LOGOUT))
                )
                .build();
        // END SNIPPET: ANNOTATED_METHOD_WITH_PARAMS

        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactly(Ids.LOGIN, Ids.LOGOUT);
    }

    @Test
    public void fromCsvDataSource() {
        // START SNIPPET: FROM_CSV_DATASOURCE
        RxDataSource<Node> dataSource = Api.fromCsv("myCsvDataSource", "data/node.csv", Node.class);

        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        RxApi.flow("send email")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_2))
                                .withDataSources(dataSource)
                ).build();
        // END SNIPPET: FROM_CSV_DATASOURCE

        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactly(Ids.TEST_STEP_1, Ids.TEST_STEP_2, Ids.TEST_STEP_1, Ids.TEST_STEP_2, Ids.TEST_STEP_1, Ids.TEST_STEP_2);
    }


    /**
     * ### Filtering Data Sources
     */
    @Test
    public void filteringExample() {
        // START SNIPPET: SIMPLE_FILTERING
        RxDataSource<User> dataSource = Api.fromCsv("myCsvDataSource", "data/user.csv", User.class);
        RxApi.flow()
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.LOGIN))
                .withDataSources(
                        dataSource.filterField("enabled").equalTo("false")
                );
        // END SNIPPET: SIMPLE_FILTERING
    }

    @Test
    public void advancedFilteringExample() {
        // START SNIPPET: ADVANCED_FILTER_EXAMPLE
        RxDataSource<User> allUsers = Api.fromCsv("myDataSource", "data/user.csv", User.class);

        RxDataSource<User> enabledUsers = allUsers.filterField("enabled").equalTo("true");

        RxDataSource<User> adminUsers = enabledUsers
                .filterField("roles").contains("ADMINISTRATOR");

        RxScenario scenario = RxApi.scenario()
                .addFlow(
                        RxApi.flow("onlyAdmins")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.LOGIN))
                                .withDataSources(adminUsers)
                )
                .addFlow(
                        RxApi.flow("enabledUsers")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.LOGIN))
                                .withDataSources(enabledUsers)
                )
                .build();
        // END SNIPPET: ADVANCED_FILTER_EXAMPLE

        TafRxScenarios.run(scenario);

        assertThat(STACK).hasSize(5).containsOnly(Ids.LOGIN);
    }

    /**
     * #### Subflow
     */

    @Test
    public void subflowExample() {
        // START SNIPPET: SUBFLOW_EXAMPLE
        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        RxApi.flow("flow")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                                .addSubFlow(createSubFlow1())
                                .addSubFlow(createSubFlow2())
                )
                .build();
        // END SNIPPET: SUBFLOW_EXAMPLE

        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactly(Ids.TEST_STEP_1, Ids.FAKE, Ids.FAKE);
    }

    private RxFlow createSubFlow1() {
        return RxApi.flow("subFlow1").addTestStep(fakeAnnotatedMethod()).build();
    }

    private RxFlow createSubFlow2() {
        return RxApi.flow("subFlow2").addTestStep(fakeAnnotatedMethod()).build();
    }

    /**
     * #### Split
     */
    @Test
    public void splitExample() {
        // START SNIPPET: SPLIT_EXAMPLE
        RxFlow flow = RxApi.flow("flow")
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                .split(
                        RxApi.flow("flow1")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_2)),
                        RxApi.flow("flow2")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_4))
                ).build();
        // END SNIPPET: SPLIT_EXAMPLE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactlyInAnyOrder(Ids.TEST_STEP_1, Ids.TEST_STEP_1, Ids.TEST_STEP_2, Ids.TEST_STEP_3, Ids.TEST_STEP_4);
    }

    @TestStep(id = Ids.TEST_STEP_3)
    public void testStep3() {
        STACK.push(Ids.TEST_STEP_3);
    }

    @TestStep(id = Ids.TEST_STEP_4)
    public void testStep4() {
        STACK.push(Ids.TEST_STEP_4);
    }

    /**
     * ### Multiple vUsers
     */
    @Test
    public void multipleVusersExample() {
        // START SNIPPET: MULTIPLE_VUSERS
        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        RxApi.flow("flow")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_2))
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                                .withVUsers(2)
                )
                .build();
        // END SNIPPET: MULTIPLE_VUSERS

        TafRxScenarios.run(scenario);

        assertThat(STACK).hasSize(6).containsOnly(Ids.TEST_STEP_1, Ids.TEST_STEP_2, Ids.TEST_STEP_3);
    }

    /**
     * ### Combining vUsers and Data Sources
     */
    @Test
    public void combiningVusersAndDataSource() {
        // START SNIPPET: VUSERS_AND_DATA_SOURCE_EXAMPLE
        RxDataSource<String> dataSource = Api.fromIterable("credentials", newArrayList("credential1", "credential2", "credential3"));

        RxFlow flow = RxApi.flow("send email")
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                .withVUsers(2)
                .withDataSources(Api.fromIterable("credentials", dataSource))
                .build();
        // END SNIPPET: VUSERS_AND_DATA_SOURCE_EXAMPLE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).hasSize(6).containsOnly(Ids.TEST_STEP_3);
    }

    /**
     * ### Automatic vUsers
     */
    @Test
    public void automaticVUsersExample() {
        // START SNIPPET: AUTOMATIC_VUSERS_EXAMPLE
        RxDataSource<String> dataSource1 = Api.fromIterable("ds1", newArrayList("dsRecord1", "dsRecord2", "dsRecord3"));
        RxDataSource<String> dataSource2 = Api.fromIterable("ds2", newArrayList("dsRecord4", "dsRecord5", "dsRecord6"));

        RxFlow flow = RxApi.flow("automatic vUsers")
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                .withVUsersAuto()
                .withDataSources(
                        dataSource1.shared(),
                        dataSource2.shared()
                )
                .build();
        // END SNIPPET: AUTOMATIC_VUSERS_EXAMPLE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).hasSize(3).containsOnly(Ids.TEST_STEP_3);
    }

    /**
     * #### Multiple Data Sources in the Same Flow
     */
    @Test
    public void multipleDataSourcesInTheSameFlowExample() {
        // START SNIPPET: MULTIPLE_DATA_SOURCES_IN_THE_SAME_FLOW_EXAMPLE
        RxDataSource<String> dataSource1 = Api.fromIterable("ds1", newArrayList("dsRecord1", "dsRecord2", "dsRecord3"));
        RxDataSource<String> dataSource2 = Api.fromIterable("ds2", newArrayList("dsRecord4", "dsRecord5", "dsRecord6"));

        RxFlow flow = RxApi.flow("flow1")
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_4))
                .withDataSources(dataSource1, dataSource2).build();
        // END SNIPPET: MULTIPLE_DATA_SOURCES_IN_THE_SAME_FLOW_EXAMPLE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactly(Ids.TEST_STEP_3, Ids.TEST_STEP_4, Ids.TEST_STEP_3, Ids.TEST_STEP_4, Ids.TEST_STEP_3, Ids.TEST_STEP_4);
    }

    /**
     * #### Nested Data Sources
     */

    @Test
    public void nestedDataSourcesExample() {
        // START SNIPPET: NESTED_DATA_SOURCES_EXAMPLE
        RxDataSource<String> dataSource1 = Api.fromIterable("ds1", newArrayList("dsRecord1", "dsRecord2", "dsRecord3"));
        RxDataSource<String> dataSource2 = Api.fromIterable("ds2", newArrayList("dsRecord4", "dsRecord5", "dsRecord6"));

        RxScenario scenario = RxApi.scenario("scenario")
                .addFlow(
                        RxApi.flow("flow")
                                .addSubFlow(
                                        RxApi.flow("subFlow")
                                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3))
                                                .withDataSources(dataSource2)
                                )
                                .withDataSources(dataSource1)
                ).build();
        // END SNIPPET: NESTED_DATA_SOURCES_EXAMPLE

        TafRxScenarios.run(scenario);

        assertThat(STACK).hasSize(9).containsOnly(Ids.TEST_STEP_3);
    }

    /**
     * #### Cyclic Data Sources
     */

    @Test
    public void nonCyclicDataSource() {
        // START SNIPPET: NON_CYCLIC_DATA_SOURCE
        RxDataSource<Node> nodes = Api.fromCsv("nodesDs", "data/node.csv", Node.class);
        RxDataSource<User> admins = Api.fromCsv("adminsDs", "data/node.csv", User.class);

        RxFlow flow = RxApi.flow().addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3)).withDataSources(nodes, admins).build();
        // END SNIPPET: NON_CYCLIC_DATA_SOURCE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).hasSize(3).containsOnly(Ids.TEST_STEP_3);
    }

    @Test
    public void cyclicDataSource() {
        // START SNIPPET: CYCLIC_DATA_SOURCE
        RxDataSource<Node> nodes = Api.fromCsv("nodesDs", "data/node.csv", Node.class);
        RxDataSource<User> admins = Api.fromCsv("adminsDs", "data/node.csv", User.class);

        RxFlow flow = RxApi.flow()
                .addTestStep(
                        TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3)
                )
                .withDataSources(nodes, admins.cyclic())
                .build();
        // END SNIPPET: CYCLIC_DATA_SOURCE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).hasSize(3).containsOnly(Ids.TEST_STEP_3);
    }

    /**
     * ### Pass Data Between Flows
     */

    @Test
    public void passDataBetweenFlowsExample() {
        // START SNIPPET: PASS_DATA_BETWEEN_FLOWS
        RxContextDataSource<Node> dataSource = RxApi.contextDataSource("nodes", Node.class);

        RxScenario scenario = RxApi.scenario()
                .addFlow(
                        RxApi.flow("producer")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.CREATE_NODE)
                                        .collectResultsToDataSource(dataSource)) // reference not String name
                                .withVUsers(4) // will produce 4 Data Records
                )
                .addFlow(
                        RxApi.flow("consumer")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_4))
                                .withVUsers(2) // each vUser will get 2 Data Records
                                .withDataSources(dataSource.shared())
                )
                .build();
        // END SNIPPET: PASS_DATA_BETWEEN_FLOWS

        TafRxScenarios.run(scenario);

        System.out.println(STACK);

        assertThat(STACK).containsExactly(
                Ids.CREATE_NODE, Ids.CREATE_NODE, Ids.CREATE_NODE, Ids.CREATE_NODE,
                Ids.TEST_STEP_4, Ids.TEST_STEP_4, Ids.TEST_STEP_4, Ids.TEST_STEP_4);
    }

    @Test
    public void passCollectionOfDataBetweenFlowsExample() {
        // START SNIPPET: PASS_COLLECTION_OF_DATA_BETWEEN_FLOWS
        RxContextDataSource<Node> dataSource = RxApi.contextDataSource("nodes", Node.class);

        RxScenario scenario = RxApi.scenario()
                .addFlow(
                        RxApi.flow("producer")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.CREATE_NODE)
                                        .collectResultsToDataSource(dataSource)) // reference not String name
                                .withVUsers(4) // will produce 4 Data Records
                )
                .addFlow(
                        RxApi.flow("consumer")
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_4))
                                .withVUsers(2) // each vUser will get 2 Data Records
                                .withDataSources(dataSource)
                )
                .build();
        // END SNIPPET: PASS_COLLECTION_OF_DATA_BETWEEN_FLOWS

        TafRxScenarios.run(scenario);

        assertThat(STACK).containsExactly(
                Ids.CREATE_NODE, Ids.CREATE_NODE, Ids.CREATE_NODE, Ids.CREATE_NODE,
                Ids.TEST_STEP_4, Ids.TEST_STEP_4, Ids.TEST_STEP_4, Ids.TEST_STEP_4,
                Ids.TEST_STEP_4, Ids.TEST_STEP_4, Ids.TEST_STEP_4, Ids.TEST_STEP_4);
    }


    /**
     * #### Always Run
     */
    @Test(expected = ScenarioExamplesException.class)
    public void alwaysRunExample() {
        // START SNIPPET: ALWAYS_RUN_EXAMPLE
        RxFlow flow = RxApi.flow("flow1")
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.STEP_WITH_EXCEPTION)) //Exception!
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_3).alwaysRun())
                .build();
        // END SNIPPET: ALWAYS_RUN_EXAMPLE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
    }

    @TestStep(id = Ids.STEP_WITH_EXCEPTION)
    public void stepWithException() {
        throw new ScenarioExamplesException();
    }

    /**
     * #### Before and After Steps
     */
    @Test
    public void beforeAndAfterSteps() {
        // START SNIPPET: BEFORE_AND_AFTER_STEPS_EXAMPLE
        RxFlow flow = RxApi.flow("flow1")
                .withBefore(TafRxScenarios.annotatedMethod(this, Ids.SETUP))
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))
                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_2))
                .withAfter(TafRxScenarios.annotatedMethod(this, Ids.TEARDOWN)).build();
        // END SNIPPET: BEFORE_AND_AFTER_STEPS_EXAMPLE

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactly(Ids.SETUP, Ids.TEST_STEP_1, Ids.TEST_STEP_2, Ids.TEARDOWN);
    }

    @TestStep(id = Ids.SETUP)
    public void setup() {
        STACK.push(Ids.SETUP);
    }

    @TestStep(id = Ids.TEARDOWN)
    public void teardown() {
        STACK.push(Ids.TEARDOWN);
    }

    /**
     * ## Scenario Context
     */
    @Test
    public void scenarioContextExample() {
        TafConfigurationProvider.provide().getRuntimeConfiguration().setProperty("property", true);

        // START SNIPPET: SCENARIO_CONTEXT_EXAMPLE
        TafConfiguration configuration = TafConfigurationProvider.provide().getRuntimeConfiguration();

        RxApi.scenario()
                .withParameter("name1", "value1")
                .withParameter("name2", configuration.getBoolean("property"));
        // END SNIPPET: SCENARIO_CONTEXT_EXAMPLE
    }

    /**
     * ### Test Step Parameters
     */
    // START SNIPPET: TEST_WITH_PARAMETER
    @Test
    public void testStepParametersExample() {
        RxDataSource<Node> nodes = Api.fromCsv("nodesDs", "data/node.csv", Node.class);

        RxFlow flow = RxApi.flow()
                .addTestStep(
                        TafRxScenarios.annotatedMethod(this, Ids.CONSUMER)
                                .withParameter("object1").value("2")
                                .withParameter("object2").fromDataSource(nodes, "networkElementId")
                                .withParameter("object3").value("3")
                )
                .withDataSources(nodes)
                .build();

        RxScenario scenario = RxApi.scenario().addFlow(flow).build();
        TafRxScenarios.run(scenario);
        assertThat(STACK).containsExactly(Ids.CONSUMER, Ids.CONSUMER, Ids.CONSUMER);
    }

    @TestStep(id = Ids.CONSUMER)
    public void consumer(
            @Input("object1") Object object1,
            @Input("object2") Object object2,
            @Output("object3") Object object3) {
        STACK.push(Ids.CONSUMER);
    }
    // END SNIPPET: TEST_WITH_PARAMETER

    /**
     * ### Passing Data Between Test Steps
     */
    @Inject
    private Operator operator;

    /**
     * ### Reuse object in different steps in scope of flow
     */
    // START SNIPPET: RETURN_ANY_OBJECT_OR_DATA_RECORD
    @TestStep(id = Ids.LOGIN2)
    public HttpTool login2() {
        HttpTool tool = operator.login("username", "password");
        STACK.push(Ids.LOGIN2);
        return tool; // ← could be usable in other steps
    }

    @TestStep(id = Ids.CREATE_NODE)
    public DataRecord createNode() {
        DataRecord node = operator.createNode();
        STACK.push(Ids.CREATE_NODE);
        return node; // ← could be usable in other steps
    }

    // Alternative
    @TestStep(id = Ids.CREATE_NODE_2)
    public DataRecord createNodeAlt() {
        return new DataRecordBuilder()
                .setField("nodeType", "nodeType")
                .setField("networkElementId", "networkElementId")
                .build(TafNode.class);
    }
    // END SNIPPET: RETURN_ANY_OBJECT_OR_DATA_RECORD

    // START SNIPPET: RETURN_COLLECTION_DATA_RECORDS
    @TestStep(id = Ids.CREATE_3_NODES)
    public Collection<DataRecord> collectionOfNodes() {
        ArrayList<DataRecord> result = Lists.newArrayList();

        for (int i = 0; i < 2; i++) {
            result.add(
                    new DataRecordBuilder()
                            .setField("nodeType", "nodeType")
                            .setField("networkElementId", i)
                            .build(TafNode.class));
        }

        return result;
    }
    // END SNIPPET: RETURN_COLLECTION_DATA_RECORDS


    @Test
    public void resultCouldThenBePassedToSubsequentTestStepsOfFlow() {
        // START SNIPPET: REUSE_OBJECT
        //definition
        RxTestStep loginStep = TafRxScenarios.annotatedMethod(this, Ids.LOGIN2);
        RxTestStep createNodeStep = TafRxScenarios.annotatedMethod(this, Ids.CREATE_NODE);
        RxDataSource<Node> dataSource = Api.fromCsv("nodes", "data/node.csv", Node.class);

        RxScenario scenario = RxApi.scenario("PassData")
                .addFlow(
                        RxApi.flow()
                                .addTestStep(loginStep) //use definition
                                .addTestStep(createNodeStep) //use definition
                                .addTestStep(
                                        TafRxScenarios.annotatedMethod(this, Ids.CONSUMER)
                                                .withParameter("object1").fromTestStepResult(loginStep)
                                                .withParameter("object2").fromDataSource(dataSource, "nodeType")
                                                .withParameter("object3").fromTestStepResult(createNodeStep, "networkElementId")
                                )
                                .withDataSources(dataSource)
                ).build();
        // END SNIPPET: REUSE_OBJECT

        TafRxScenarios.run(scenario);

        assertThat(STACK).containsExactly(Ids.LOGIN2, Ids.CREATE_NODE, Ids.CONSUMER, Ids.LOGIN2, Ids.CREATE_NODE, Ids.CONSUMER, Ids.LOGIN2, Ids.CREATE_NODE, Ids.CONSUMER);
    }

    @Test
    public void passingMechanismWorksOnlyInTheScopeOfOneFlowAndVUser() {
        RxTestStep loginStep = TafRxScenarios.annotatedMethod(this, Ids.LOGIN2);
        // START SNIPPET: IN_THE_SCOPE_OF_ONE_FLOW
        RxApi.scenario("PassData")
                //...
                .addFlow(
                        RxApi.flow()
                                .addTestStep( //will throw exception because passing only works in scope of one flow
                                        TafRxScenarios.annotatedMethod(this, Ids.CONSUMER)
                                                .withParameter("object1").fromTestStepResult(loginStep)
                                )
                )
                // END SNIPPET: IN_THE_SCOPE_OF_ONE_FLOW
                .build();
    }

    /**
     * ### Debug logging
     */
    @Test
    public void debugLogging() throws Exception {
        RxScenario scenario = RxApi.scenario().addFlow(RxApi.flow().addTestStep(TafRxScenarios.annotatedMethod(this, Ids.TEST_STEP_1))).build();
        // START SNIPPET: DEBUG_LOGGING
        RxScenarioRunner runner = RxApi.runner()
                .withDebugLogEnabled()
                .build();

        runner.run(scenario);
        // END SNIPPET: DEBUG_LOGGING

        // START SNIPPET: DEBUG_GRAPH
        RxScenarioRunner runner2 = RxApi.runner()
                .withGraphExportMode(DebugGraphMode.SVG)
                .build();

        runner2.run(scenario);
        // END SNIPPET: DEBUG_GRAPH
    }

    @Test
    public void renameDataSource() throws Exception {
        RxDataSource<RxDataRecord> differentNameDataSource = Api.fromDataRecords("differentName",
                RxBasicDataRecord.fromValues(
                        "username", "user1",
                        "password", "pass1"
                ),
                RxBasicDataRecord.fromValues(
                        "username", "user2",
                        "password", "pass2"
                )
        );

        // START SNIPPET: RENAME_DATASOURCE
        RxScenario scenario = RxApi.scenario()
                .addFlow(
                        RxApi.flow()
                                .addTestStep(TafRxScenarios.annotatedMethod(this, Ids.DATA_RECORD))
                                .withDataSources(differentNameDataSource.rename("users_datasource"))
                )
                .build();
        // END SNIPPET: RENAME_DATASOURCE

        run(scenario);
        assertThat(STACK).containsExactly("user1", "user2");
    }

    @Test
    public void repeatWhileFieldValue() throws Exception {
        RxDataSource<Node> dataSource = Api.fromCsv("nodes", "data/node.csv", Node.class);
        RxTestStep createNodeStep = TafRxScenarios.annotatedMethod(this, Ids.CREATE_NODE);

        // START SNIPPET: REPEAT_WHILE_VALUE
        final Predicate<RxDataRecordWrapper> predicate = new Predicate<RxDataRecordWrapper>() {
            @Override
            public boolean apply(RxDataRecordWrapper input) {
                final Optional<String> fieldValue = input.getFieldValue("nodeType", String.class);
                assertThat(fieldValue.isPresent()).isTrue();
                return !fieldValue.get().equals("RadioNode");
            }
        };

        RxScenario scenario = RxApi.scenario("RepeatWhile")
                .addFlow(
                        RxApi.flow()
                                .addTestStep(createNodeStep)
                                .withDataSources(dataSource)
                                .runWhile(predicate)
                )
                .build();
        // END SNIPPET: REPEAT_WHILE_VALUE

        run(scenario);
        assertThat(STACK).containsExactly("CREATE_NODE", "CREATE_NODE");
    }

    @TestStep(id = Ids.REPEAT_WHILE)
    public void repeatTestStep() {
        STACK.push(Ids.REPEAT_WHILE);
    }

    @Test
    public void repeatWhileCount() throws Exception {
        // START SNIPPET: REPEAT_WHILE_COUNT
        List<String> dataSource = newArrayList("u", "v", "x", "y");
        RxDataSource<String> letterDs = Api.fromIterable("letters", dataSource);
        RxTestStep printDataRecord = TafRxScenarios.annotatedMethod(this, Ids.REPEAT_WHILE);

        final Predicate<RxDataRecordWrapper> predicate = new Predicate<RxDataRecordWrapper>() {
            int count = 0;

            @Override
            public boolean apply(RxDataRecordWrapper input) {
                count++;
                return count <= 7;
            }
        };

        RxScenario scenario = RxApi.scenario("RepeatWhileCount")
                .addFlow(
                        RxApi.flow()
                                .addTestStep(printDataRecord)
                                .withDataSources(letterDs)
                                .runWhile(predicate)
                )
                .build();
        // END SNIPPET: REPEAT_WHILE_COUNT

        run(scenario);
        assertThat(STACK).hasSize(7);
    }
}