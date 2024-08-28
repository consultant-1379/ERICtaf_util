/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.ext.exporter;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.shared;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.OUTPUT_DIR;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.ScenarioExecutionGraphFinalizer.getGraphFileName;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_ENABLED;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_DEBUG_PORT;
import static com.ericsson.cifwk.taf.scenario.ext.exporter.ScenarioExecutionGraphListenerBuilder.TAF_SCENARIO_SHOW_SYNC_POINTS;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.ScenarioListener;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.TafScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.TestStepRunner;
import com.ericsson.cifwk.taf.scenario.impl.configuration.HashMapConfiguration;
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationProvider;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioConfiguration;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class ScenarioExecutionGraphListenerTest {
    public static final String GLOBAL_DATA_SOURCE = "global";
    public static final String CSV_DATA_SOURCE = "csv";
    public static final String DATARDIVEN_DATA_SOURCE = "dataDriven";
    private TafScenarioRunner runner;

    @Before
    public void setUp() {
        runner = new TafScenarioRunner(null, ScenarioExceptionHandler.PROPAGATE, Collections.EMPTY_LIST);
        prepareDataSource(GLOBAL_DATA_SOURCE, 2, "integer");
        prepareDataSource(CSV_DATA_SOURCE, 3, "integer");
        prepareDataSource(DATARDIVEN_DATA_SOURCE, 3, "testCaseId");
    }

    private void prepareDataSource(String name, int size, String... fieldName) {
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        if (!context.doesDataSourceExist(name)) {
            TestDataSource<DataRecord> dataSource = context.dataSource(name);
            for (int i = 0; i < size; i++) {
                for (String field : fieldName) {
                    dataSource.addRecord().setField(field, Integer.toString(i));
                }
            }
        }
    }

    @Test
    public void shouldGenerateAdvancedGraph() throws Exception {
        config(true, 8787, true);

        final boolean sleep = false;
        final boolean throwException = true;
        final String scenarioName = "scenario_graph_test1_" + UUID.randomUUID();
        TestScenario scenario = scenario(scenarioName).addFlow(flow("flow").addTestStep(createStep("step1")).addTestStep(runnable(new Runnable() {
            @Override
            public void run() {
                if (ServiceRegistry.getTestContextProvider().get().getVUser() == 2 && throwException) {
                    throw new VerySpecialException(
                            "Suggested Solution : This is an unhandled system error, please check the error log for more details, Error 9999 : "
                                    + "Internal Error Transaction rolled back, , secadm credentials create  --rootusername root --rootuserpassword "
                                    + "shroot --secureusername netsim --secureuserpassword netsim --normalusername admin --normaluserpassword "
                                    + "adminpass -n LTE01ERBS00004]");
                }
            }
        })).addTestStep(createStep("step2").alwaysRun())
                .split(flow("parallel1").addTestStep(createStep("parallel1Step1")).addTestStep(createStep("parallel1Step2")),
                        flow("parallel2").addTestStep(createStep("parallel2Step1")).withVusers(2)).addSubFlow(
                        flow("subFlow").addTestStep(createStep("subStep1")).addTestStep(createStep("subStep2"))
                                .withDataSources(dataSource(GLOBAL_DATA_SOURCE))).addTestStep(runnable(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (sleep) {
                                Thread.sleep(30_000L);
                            }
                        } catch (InterruptedException ignored) {
                        }
                    }
                })).withVusers(3).withDataSources(dataSource(CSV_DATA_SOURCE))).build();

        try {
            runner.start(scenario);
        } catch (VerySpecialException ignored) {
        }

        String graph = fileToString(OUTPUT_DIR + "/" + getGraphFileName(scenario, ".graphml"));
        List<Node> nodes = parseGraph(graph);
        assertThat(graph, nodes, hasSize(142));
    }

    @Test
    public void shouldGenerateGraphOnFailure() throws Exception {
        config(null, null, null);

        final String ERROR_FLOW = "error_flow";
        final String scenarioName = "failure_" + UUID.randomUUID().toString();
        TestScenario scenario = scenario(scenarioName).addFlow(flow(ERROR_FLOW).addTestStep(throwException()).build()).build();

        try {
            runner.start(scenario);
        } catch (Exception e) {
            //expected
        }

        String graph = fileToString(OUTPUT_DIR + "/" + getGraphFileName(scenario, ".svg"));
        assertThat(graph, containsString(ERROR_FLOW));
    }

    private TestStepDefinition throwException() {
        return runnable(new Runnable() {
            @Override
            public void run() {
                throw new VerySpecialException("expected");
            }
        });
    }

    private String fileToString(String path) throws IOException {
        return new String(readAllBytes(get(path)));
    }

    public class VerySpecialException extends RuntimeException {
        public VerySpecialException(String s) {
            super(s);
        }
    }

    @Test
    public void shouldGenerateVerySimpleGraph() throws Exception {
        config(true, null, null);
        final String scenarioName = UUID.randomUUID().toString();

        TestScenario scenario = scenario(scenarioName).addFlow(flow("flow").addTestStep(createStep("step")).build()).build();

        runner.start(scenario);

        String result = fileToString(OUTPUT_DIR + "/" + getGraphFileName(scenario, ".svg"));
        int width = SvgExporter.TOOLTIP_WIDTH;
        int height = SvgExporter.TOOLTIP_HEIGHT;
        assertThat(result, containsString("viewBox=\"0 0 " + width + " " + height));
        assertThat(result, containsString("height=\"" + height));
        assertThat(result, containsString("width=\"" + width));
    }

    @Test
    public void shouldGenerateSimpleGraph() throws Exception {
        config(true, 8787, null);

        final String scenarioName = "scenario_graph_test3_" + UUID.randomUUID();
        final int flow1_vUsers = 3;
        final int flow2_vUsers = 2;
        TestScenario scenario = scenario(scenarioName).addFlow(
                flow("flow1").addTestStep(createStep("step11")).addTestStep(createStep("step12")).withVusers(flow1_vUsers)
                        .withDataSources(dataSource(CSV_DATA_SOURCE)).build())
                .addFlow(flow("flow2").addTestStep(createStep("step21")).addTestStep(createStep("step22")).withVusers(flow2_vUsers).build()).build();

        runner.start(scenario);

        String graph = fileToString(OUTPUT_DIR + "/" + getGraphFileName(scenario, ".graphml"));
        assertThat(graph, startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\""));

        List<Node> nodes = parseGraph(graph);
        assertThat(graph, nodes, hasSize(1 /*scenario started*/ + flow1_vUsers * (2 /*test step count*/ + 1 /*flow started*/) * 3 /* csv ds records*/
                + 1 /*flow ended*/ + flow2_vUsers * (2 /*test step count*/ + 1 /*flow started*/) + 1 /*flow ended*/ + 1 /*scenario finished*/));
    }

    @Test
    @TestSuite("Simple DataDriven Scenario")
    public void shouldGenerateSimpleDatadrivenGraph() throws Exception {
        config(true, 8787, null);
        final String scenarioName = "scenario_graph_test4_" + UUID.randomUUID();
        TestContext context = ServiceRegistry.getTestContextProvider().get();
        TestDataSource<DataRecord> dd_DataSource = context.dataSource(DATARDIVEN_DATA_SOURCE);
        context.addDataSource(DATARDIVEN_DATA_SOURCE, shared(dd_DataSource));
        final int datadriven_DataSource_size = Iterables.size(context.dataSource(DATARDIVEN_DATA_SOURCE));
        final int csv_DataSource_tests = Iterables.size(context.dataSource(CSV_DATA_SOURCE));

        TestScenario dataDrivenScenario = dataDrivenScenario(scenarioName).addFlow(
                flow("DataDriven_flow_01").addTestStep(createStep("DataDriven_TestStep_01")).addTestStep(createStep("DataDriven_TestStep_02"))
                        .withDataSources(dataSource(CSV_DATA_SOURCE))).withScenarioDataSources(dataSource(DATARDIVEN_DATA_SOURCE))
                .doParallel(datadriven_DataSource_size).build();

        runner.start(dataDrivenScenario);

        String graph = fileToString(OUTPUT_DIR + "/" + getGraphFileName(dataDrivenScenario, ".graphml"));
        assertThat(graph, startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\""));

        List<Node> nodes = parseGraph(graph);
        assertThat(graph, nodes,
                hasSize(1 /*scenario started*/ +
                        datadriven_DataSource_size *
                                ( csv_DataSource_tests *
                                        (1 /* Flow started */ + 2 /*test step count*/ ) + 1 /*flow ended*/)
                        + 1 /*scenario finished*/));
    }

    @Test
    @Ignore("This test is intended to generate big scenario for yEd performance testing. Should be run manually")
    public void doomsdayScenarioGenerator() {
        config(true, null, null);
        TestScenario scenario = scenario().addFlow(
                getFlowWithTestSteps("flow", 20).addSubFlow(getFlowWithTestSteps("subFlow", 10).addSubFlow(getFlowWithTestSteps("subSubFlow", 5)))
                        .split(getFlowWithTestSteps("parallel1", 15), getFlowWithTestSteps("parallel2", 25)).withVusers(4)
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))).build();

        runner.start(scenario);
    }

    private TestStepFlowBuilder getFlowWithTestSteps(String prefix, int testStepCnt) {
        TestStepFlowBuilder flow = flow(prefix);
        for (int i = 0; i < testStepCnt; i++) {
            flow.addTestStep(createStep(prefix + "Step" + i));
        }
        return flow;
    }

    private TestStepDefinition createStep(final String name) {
        TestStepInvocation invocation = new TestStepInvocation() {
            private final long id = idGenerator.incrementAndGet();
            boolean alwaysRun = false;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Long getId() {
                return id;
            }

            @Override
            public void addParameter(String key, Object value) {

            }

            @Override
            public void alwaysRun() {
                alwaysRun = true;
            }

            @Override
            public boolean isAlwaysRun() {
                return alwaysRun;
            }

            @Override
            public Optional<Object> run(TestStepRunner testStepRunner, ScenarioExecutionContext scenarioExecutionContext,
                    LinkedHashMap<String, DataRecord> dataSourcesRecords, TestContext context, List<DataRecordTransformer> dataRecordTransformers)
            throws Exception {
                ScenarioListener listener = scenarioExecutionContext.getListener();

                listener.onTestStepStarted(this, new Object[] {});
                listener.onTestStepFinished(this);

                return Optional.absent();
            }

            @Override
            public String toString() {
                return name;
            }
        };

        return new TestStepDefinition(invocation);
    }

    private static List<Node> parseGraph(String graph) throws IOException {
        ObjectMapper mapper = new XmlMapper();

        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final List<RawNode> rawNodes = mapper.readValue(graph, Doc.class).nodes;

        return FluentIterable.from(rawNodes).filter(new Predicate<RawNode>() {
            @Override
            public boolean apply(RawNode input) {
                return !input.getProperty("vertex_label").isEmpty();
            }
        }).transform(new Function<RawNode, Node>() {
            @Override
            public Node apply(RawNode rawNode) {
                return new Node(rawNode);
            }
        }).toList();
    }

    public static class Doc {
        @JacksonXmlElementWrapper(localName = "graph")
        @JsonProperty("node")
        List<RawNode> nodes;
    }

    public static class Node {
        int id;
        String vertex_label;
        String vertex_type;
        String vertex_time;
        Long vertex_duration;
        String vertex_tooltip;
        Boolean vertex_failed;

        public Node(RawNode rawNode) {
            this.id = rawNode.id;
            this.vertex_label = rawNode.getProperty("vertex_label");
            this.vertex_type = rawNode.getProperty("vertex_type");
            this.vertex_time = rawNode.getProperty("vertex_time");
            try {
                this.vertex_duration = Long.valueOf(rawNode.getProperty("vertex_duration"));
            } catch (NumberFormatException e) {
                this.vertex_duration = 0L;
            }
            this.vertex_tooltip = rawNode.getProperty("vertex_tooltip");
            this.vertex_failed = Boolean.valueOf(rawNode.getProperty("vertex_failed"));
        }

        String getDataRecord(String name) {
            Pattern p = Pattern.compile("<td>" + name + "</td><td>(.*?)</td>");
            Matcher m = p.matcher(vertex_tooltip);
            m.find();
            return m.group(1);
        }
    }

    public static class RawNode {
        int id;
        @JacksonXmlElementWrapper(useWrapping = false)
        @JsonProperty("data")
        List<Map> data;

        private String getProperty(String property) {
            for (Map map : data) {
                if (map.get("key").equals(property)) {
                    return map.get("").toString();
                }
            }

            return "";
        }
    }

    void config(Object tafScenarioDebugEnabled, Object tafScenarioDebugPort, Object tafScenarioShowSyncPoints) {
        final Map<String, Object> configuration = Maps.newHashMap();

        configuration.put(TAF_SCENARIO_DEBUG_ENABLED, tafScenarioDebugEnabled);
        configuration.put(TAF_SCENARIO_DEBUG_PORT, tafScenarioDebugPort);
        configuration.put(TAF_SCENARIO_SHOW_SYNC_POINTS, tafScenarioShowSyncPoints);

        HashMapConfiguration mockConfiguration = new HashMapConfiguration(configuration);
        MockTafScenarioConfiguration.setMockConfiguration(mockConfiguration);
    }

    static class MockTafScenarioConfiguration extends ScenarioConfigurationProvider {
        public static void setMockConfiguration(ScenarioConfiguration mock) {
            tafScenarioConfiguration.set(mock);
        }
    }
}
