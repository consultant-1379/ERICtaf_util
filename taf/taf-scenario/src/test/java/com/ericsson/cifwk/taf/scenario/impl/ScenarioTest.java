package com.ericsson.cifwk.taf.scenario.impl;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordProxyFactory;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.impl.datarecord.Node;
import com.ericsson.cifwk.taf.scenario.impl.exception.ThrownByHandlerException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.CustomMatchers.contains;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_ENABLED;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.fail;

public class ScenarioTest {
    public static final String ATTRIBUTE = "attribute";
    public static final String PUSH_VUSER = "pushVUser";
    public static final String SET_ATTRIBUTE = "setAttribute";
    public static final String CHECK_ATTRIBUTE = "checkAttribute";
    public static final String USERNAME = "username";
    private static final Logger logger = LoggerFactory
            .getLogger(ScenarioTest.class);

    // data sources
    public static final String GLOBAL_DATA_SOURCE = "global";
    public static final String SHARED_DATA_SOURCE = "shared";
    public static final String CSV_DATA_SOURCE = "csv";
    public static final String GENERIC_CSV_DATA_SOURCE = "generic_csv";
    public static final String EMPTY_CSV_DATA_SOURCE = "myEmptyDS";
    public static final String MY_DATA_SOURCE_1 = "my1";
    public static final String MY_DATA_SOURCE_2 = "my2";
    public static final String MY_DATA_SOURCE_3 = "my3";
    public static final String USER = "user";

    // test steps
    public static final String STEP_DUMMY = "DUMMY";
    public static final String STEP_PUSH_GLOBAL_DS_TO_STACK = "pushGlobalDsToStack";
    public static final String STEP_PUSH_CSV_DS_TO_STACK = "STEP_PUSH_CSV_DS_TO_STACK";
    public static final String STEP_PUSH_DATARECORD_DS_TO_STACK = "STEP_PUSH_DATARECORD_DS_TO_STACK";
    public static final String STEP_PUSH_MULTIPLE_CSV_DS_COLUMNS_TO_STACK = "STEP_PUSH_MULTIPLE_CSV_DS_COLUMNS_TO_STACK";
    public static final String STEP_DATA_RECORD_CONSUMER = "STEP_DATA_RECORD_CONSUMER";
    public static final String STEP_DATA_RECORD_PRODUCER = "STEP_DATA_RECORD_PRODUCER";
    public static final String STEP_VALUE_PRODUCER = "STEP_VALUE_PRODUCER";
    public static final String STEP_VOID_PRODUCER = "STEP_VOID_PRODUCER";
    public static final String STEP_EXCEPTION_PRODUCER = "STEP_EXCEPTION_PRODUCER";
    public static final String STEP_SINGLE_EXCEPTION_PRODUCER = "STEP_SINGLE_EXCEPTION_PRODUCER";
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    // providers
    public static final String SIMPLE_PROVIDER = "simpleProvider";
    public static final String CLASS_PROVIDER = "classProvider";
    public static final String SHARED_PROVIDER = "sharedProvider";
    public static final String SIMPLE_PROVIDER2 = "simpleProvider2";
    public static final String SIMPLE_PROVIDER2_REPEAT = "simpleProvider2Repeat";

    final AtomicInteger count = new AtomicInteger();
    final Stack<String> stack = new Stack<>();
    protected TafScenarioRunner runner;
    protected DataRecord dataRecord;

    TestDataSource<DataRecord> csvDataSource;
    TestDataSource<DataRecord> emptyDataSource;
    TestDataSource<Node> genericCsvDataSource;
    TestDataSource<DataRecord> globalDataSource;
    Counter counter;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() {
        System.setProperty(TAF_SCENARIO_DEBUG_ENABLED, "false"); //to not generate svg for each failing test

        stack.clear();
        ScenarioExceptionHandler coreExceptionHandler = ScenarioExceptionHandler.PROPAGATE;
        runner = new TafScenarioRunner(ScenarioExceptionHandler.PROPAGATE, coreExceptionHandler, Collections.EMPTY_LIST);

        counter = new Counter();
        clearAllDataSources();
        prepareGlobalDataSource();
        prepareSharedDataSource();
        prepareCsvDataSource();
        prepareGenericCsvDataSource();
    }

    @After
    public void tearDown() {
        count.set(0);
    }

    private void prepareCsvDataSource() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        csvDataSource = fromCsv("data/node.csv");
        context.addDataSource(CSV_DATA_SOURCE, csvDataSource);
        emptyDataSource = fromCsv("data/emptyDS.csv");
        context.addDataSource(EMPTY_CSV_DATA_SOURCE, emptyDataSource);
    }

    private void prepareGenericCsvDataSource() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        genericCsvDataSource = fromCsv("data/node.csv", Node.class);
        context.addDataSource(GENERIC_CSV_DATA_SOURCE, genericCsvDataSource);
    }

    private void prepareGlobalDataSource() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        if (!context.doesDataSourceExist(GLOBAL_DATA_SOURCE)) {
            globalDataSource = context.dataSource(GLOBAL_DATA_SOURCE);
            globalDataSource.addRecord()
                    .setField("integer", 1)
                    .setField("string", "A");
            globalDataSource.addRecord()
                    .setField("integer", 2)
                    .setField("string", "B");
        }
    }

    private void prepareSharedDataSource() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        if (!context.doesDataSourceExist(SHARED_DATA_SOURCE)) {
            TestDataSource<DataRecord> global = context.dataSource(GLOBAL_DATA_SOURCE);
            context.addDataSource(SHARED_DATA_SOURCE,  shared(global));
        }
    }

    private void clearAllDataSources() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        for (String dataSource : context.getAllDataSources().keySet()) {
            context.removeDataSource(dataSource);
        }
    }

    @TestStep(id = PUSH_VUSER)
    public void pushVUser() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        stack.push("vuser" + context.getVUser());
    }

    @TestStep(id = SET_ATTRIBUTE)
    public void setAttribute() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        context.setAttribute(ATTRIBUTE, new Random().nextInt());
    }

    @TestStep(id = CHECK_ATTRIBUTE)
    public void checkAttribute() {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        Object attribute = context.getAttribute(ATTRIBUTE);
        assertThat(attribute, not(is(nullValue())));

        stack.push(Integer.toHexString(attribute.hashCode()));
    }

    protected class Counter implements Runnable {
        @Override
        public void run() {
            count.incrementAndGet();
        }
    }

    protected List<Map<String, Object>> prepareList(int size) {
        List<Map<String, Object>> list = Lists.newArrayList();
        for (int i = 0; i < size; i++) {
            list.add(dataRow(i));
        }
        return list;
    }

    protected Map<String, Object> dataRow(int value) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("x", value);
        return map;
    }

    protected Runnable pushToStack(final String push) {
        return new Runnable() {
            @Override
            public void run() {
                stack.push(push);
            }
        };
    }

    protected Runnable setAttribute(final String attribute) {
        return new Runnable() {
            @Override
            public void run() {
                ServiceRegistry.getTestContextProvider().get().setAttribute(attribute, "value");
            }
        };
    }

    protected TestStepDefinition assertVUserInContext() {
        return runnable(new Runnable() {
                            @Override
                            public void run() {
                                TestContext context = ServiceRegistry.getTestContextProvider().get();
                                int contextVUser = context.getVUser();
                                Integer vUser = context.getAttribute("vuser");
                                stack.push(String.valueOf(vUser));

                                assertThat(vUser, equalTo(contextVUser));
                            }
                        }
        );
    }

    protected TestStepDefinition putVUserToContext() {
        return runnable(new Runnable() {
                            @Override
                            public void run() {
                                TestContext context = ServiceRegistry.getTestContextProvider().get();
                                int vUser = context.getVUser();
                                context.setAttribute("vuser", vUser);
                            }
                        }
        );
    }

    protected void expectException(TestScenario scenario, String testStep, String cause) {
        try {
            runner.start(scenario);
            fail("Exception expected");
        } catch (IllegalStateException e) {
            String expectedMessage = format("Test step '%s' didn't return value", testStep);
            MatcherAssert.assertThat(e.getMessage(), startsWith(expectedMessage));
            MatcherAssert.assertThat(e.getMessage(), endsWith(cause + ")"));
        }
    }

    public TestStepDefinition throwException() {
        return runnable(new Runnable() {
            @Override
            public void run() {
                throw new VerySpecialException();
            }
        });
    }

    public TestStepDefinition throwExceptionForVUser(final int... vUsers) {
        return runnable(new Runnable() {
            @Override
            public void run() {
                int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
                if (Ints.contains(vUsers, vUser)) {
                    throw new VerySpecialException();
                }
            }
        });
    }

    public static class VerySpecialException extends RuntimeException {
        public VerySpecialException() {
        }

        public VerySpecialException(String message) {
            super(message);
        }
    }

    public TestStepDefinition sleepForVUser(final int... vUsers) {
        return runnable(new Runnable() {
            @Override
            public void run() {
                int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
                if (vUsers.length == 0 ||  Ints.contains(vUsers, vUser)) {
                    sleep(1000L);
                }
            }
        });
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printStack() {
        logger.info(name.getMethodName() + "\nstack: " + stack);
    }

    public void stackContains(Object... items) {
        printStack();
        assertThat(stack, contains(items));
    }

    @TestStep(id = STEP_PUSH_GLOBAL_DS_TO_STACK)
    public void pushGlobalDsToStack(@Input("integer") Integer integer, @Input("string") String string) {
        stack.push(String.valueOf(integer));
        stack.push(string);
    }

    @TestStep(id = STEP_PUSH_CSV_DS_TO_STACK)
    public void pushFromCsvToStack(@Input("attribute") String attribute) {
        stack.push(attribute);
    }

    @TestStep(id = STEP_PUSH_DATARECORD_DS_TO_STACK)
    public void pushFromDataRecordToStack(@Input("simpleProvider") Node node) {
        stack.push(node.getNodeId());
    }

    @TestStep(id = STEP_DUMMY)
    public void dummy() {
    }

    @TestStep(id = STEP_VALUE_PRODUCER)
    public String produceValue() {
        int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
        return "hello from " + vUser;
    }

    @TestStep(id = STEP_DATA_RECORD_PRODUCER)
    public NetworkNode produceDataRecord() {
        int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
        return DataRecordProxyFactory.createProxy(new DataRecordImpl(ImmutableMap.of("name", "nodeName" + vUser)), NetworkNode.class);
    }

    @TestStep(id = STEP_DATA_RECORD_CONSUMER)
    public void consumeDataRecord(@Input("attribute") NetworkNode node) {
        this.dataRecord = node;
    }

    @TestStep(id = STEP_VOID_PRODUCER)
    public void produceVoid() {
        // do nothing
    }

    @TestStep(id = STEP_EXCEPTION_PRODUCER)
    public String produceException() {
        throw new VerySpecialException("Exception by design");
    }

    @TestStep(id = STEP_SINGLE_EXCEPTION_PRODUCER)
    public void produceException(@Input("integer") Integer recordValue, @Input("recordValueToFail") Integer recordValueToFail) {
        if (recordValueToFail.equals(recordValue)) {
            throw new VerySpecialException("Exception by design, recordValue: " + recordValue);
        }
    }

    @TestStep(id = STEP_PUSH_MULTIPLE_CSV_DS_COLUMNS_TO_STACK)
    public void pushFromCsvToStack(@Input("attribute") String attribute, @Input("value") String value) {
        stack.push(attribute);
        stack.push(value);
    }
    @TestStep(id = LOGIN)
    public void login(@Input(USERNAME) String username) {
        System.out.println(username+ " Logged in successfully!!");
    }

    @TestStep(id = LOGOUT)
    public void logout() {
        System.out.println("Logged out successfully!!");
    }

    public interface NetworkNode extends DataRecord {
        String getName();
    }

    protected static class PropagateIllegalStateException implements ScenarioExceptionHandler {

        @Override
        public Outcome onException(Throwable e) {
            if (e instanceof IllegalStateException || e instanceof ThrownByHandlerException) {
                return Outcome.PROPAGATE_EXCEPTION;
            }
            return Outcome.CONTINUE_FLOW;
        }
    }
}
