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

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.impl.ChildFlowInvocationImpl;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioGraph;
import com.ericsson.cifwk.taf.scenario.impl.SplitInvocationImpl;
import com.ericsson.cifwk.taf.scenario.impl.SubFlowInvocationImpl;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.collect.Iterables.getLast;

@API(Internal)
public class ScenarioExecutionGraph extends DefaultDirectedGraph<ScenarioExecutionGraph.GraphNode, ScenarioExecutionGraph.LabeledEdge> {
    public ScenarioExecutionGraph() {
        super(LabeledEdge.class);
    }

    public static class LabeledEdge extends DefaultEdge {
        private final String name;

        public LabeledEdge() {
            name = "";
        }

        public LabeledEdge(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class ErrorNode extends GraphNode {
        private TestStepFlow flow;

        public ErrorNode(int vUser, TestStepFlow flow, Throwable testStepError) {
            super(vUser, null);
            this.flow = flow;
            finish(testStepError);
        }

        @Override
        public String getName() {
            return "Error in flow\n" + flow.getName();
        }
    }

    public static class SubFlowNode extends GraphNode {
        public SubFlowNode(int vUser) {
            super(vUser, null);
        }

        @Override
        public String getName() {
            return "Subflow started";
        }
    }

    public static class FlowEndedNode extends GraphNode {
        private final String name;

        public FlowEndedNode(Class<? extends TestStepInvocation> clazz, Collection<ScenarioGraph.TestStepNode> nodes, int vuserId) {
            super(vuserId, null);
            if (clazz == ChildFlowInvocationImpl.class) {
                name = "Flow ended:\n" + getLast(nodes).getFlow().getName();
            } else if (clazz == SubFlowInvocationImpl.class) {
                name = "SubFlow ended:\n" + getLast(nodes).getFlow().getName();
            } else if (clazz == SplitInvocationImpl.class) {
                String flows = Joiner.on(", ").join(Iterables.transform(nodes,
                        new Function<ScenarioGraph.TestStepNode, String>() {
                            @Override
                            public String apply(ScenarioGraph.TestStepNode node) {
                                return node.getFlow().getName();
                            }
                        }));
                name = "Parallel ended:\n" + flows;
            } else {
                throw new IllegalArgumentException("Unknown parallel invocation");
            }
        }

        @Override
        public String getName() {
           return name;
        }
    }

    public static class RootNode extends GraphNode {
        private TestScenario scenario;

        public RootNode(TestScenario scenario) {
            super(1, null);
            this.scenario = scenario;
        }

        @Override
        public String getName() {
            return "Scenario started:\n" + scenario.getName();
        }

        public TestScenario getScenario() {
            return scenario;
        }
    }

    public static class ScenarioFinishedNode extends GraphNode {
        private TestScenario scenario;

        public ScenarioFinishedNode(TestScenario scenario) {
            super(1, null);
            this.scenario = scenario;
        }

        @Override
        public String getName() {
            return "Scenario finished:\n" + scenario.getName();
        }
    }

    public static class FlowNode extends GraphNode {
        private TestStepFlow flow;

        public FlowNode(int vUser, TestStepFlow flow, LinkedHashMap<String, DataRecord> dataSourcesRecords) {
            super(vUser, dataSourcesRecords);
            this.flow = flow;
        }

        @Override
        public String getName() {
            return "Flow started:\n" + flow.getName();
        }
    }

    public static class SyncNode extends GraphNode {
        private TestStepInvocation testStep;

        public SyncNode(TestStepInvocation testStep, int vUser) {
            super(vUser, null);
            this.testStep = testStep;
        }

        @Override
        String getName() {
            return testStep.toString();
        }
    }

    public static class TestStepNode extends GraphNode {
        TestStepInvocation testStep;

        int vUser;
        Throwable throwable;

        public TestStepNode(TestStepInvocation testStep, LinkedHashMap<String, DataRecord> dataRecord, int vUser, Throwable throwable) {
            super(vUser, dataRecord);
            this.testStep = testStep;
            this.vUser = vUser;
            this.throwable = throwable;
        }

        @Override
        public String getName() {
            return testStep.toString();
        }

        public TestStepInvocation getTestStep() {
            return testStep;
        }
    }

    public static abstract class GraphNode {
        public static final AtomicLong idGenerator = new AtomicLong();

        private Long id = idGenerator.incrementAndGet();
        private final Calendar startTime;
        private final LinkedHashMap<String, DataRecord> dataRecord;
        private final int vUser;
        private Throwable error = null;
        private Long duration = null;

        public GraphNode(int vUser, LinkedHashMap<String, DataRecord> dataRecord) {
            this.vUser = vUser;
            this.dataRecord = dataRecord;
            this.startTime = Calendar.getInstance();
        }

        abstract String getName();

        public Long getId() {
            return id;
        }

        public Calendar getStartTime() {
            return startTime;
        }

        public Long getDuration() {
            return duration;
        }

        public int getVUser() {
            return vUser;
        }

        public LinkedHashMap<String, DataRecord> getDataRecords() {
            return dataRecord;
        }

        public Throwable getError() {
            return error;
        }

        public boolean isVisible() {
            return true;
        }

        public void finish() {
            finish(null);
        }

        public void finish(Throwable error) {
            Preconditions.checkState(duration == null);
            this.duration = Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis();
            this.error = error;
        }
    }
}
