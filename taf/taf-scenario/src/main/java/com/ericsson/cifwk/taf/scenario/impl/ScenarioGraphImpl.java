/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl;


import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

@API(Internal)
public class ScenarioGraphImpl implements ScenarioGraph {
    public static final int DEFAULT_VUSERS_FOR_SUBFLOWS = 1;
    private DirectedGraph<TestStepNode, DefaultEdge> graph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
    private Table<Long, Integer, List<Integer>> vUsersByFlowIdAndParent =
            HashBasedTable.create(); //rows - flowId; column - parent vUser; cell - list of vUsers
    private ListMultimap<Long, TestStepNode> nodesByTestStepId = ArrayListMultimap.create();
    private Map<Long, TestStepFlow> flows = Maps.newHashMap();

    ScenarioGraphImpl() {
    }

    public static ScenarioGraph create(TestStepFlow scenarioFlow, VUserManager vUserManager, int defaultVusers) {
        ScenarioGraphImpl scenarioGraph = new ScenarioGraphImpl();
        scenarioGraph.createGraph(scenarioFlow, vUserManager, defaultVusers);
        return scenarioGraph;
    }

    private void createGraph(TestStepFlow scenarioFlow, VUserManager vUserManager, int defaultVusers) {
        List<Integer> rootVUsers = vUserManager.allocateVUsers(defaultIfNegative(scenarioFlow.getRunOptions().getVUsers(), 1));

        for (Integer rootVUser: rootVUsers) {
            allocateAndBindVUsersForChildFlows(scenarioFlow, rootVUser, vUserManager, defaultVusers);
        }

        bindVUsersToFlow(scenarioFlow, rootVUsers, NO_PARENT_VUSER);
        createFlow(new HashSet<TestStepNodeImpl>(), vUserManager, scenarioFlow, rootVUsers);
    }

    private Set<TestStepNodeImpl> createFlow(Set<TestStepNodeImpl> flowParentNodes,
                                             VUserManager vUserManager,
                                             TestStepFlow flow,
                                             List<Integer> vUsers) {
        Set<TestStepNodeImpl> allNodes = newHashSet();
        for (Integer vUser : vUsers) {
            Set<TestStepNodeImpl> parentNodes = flowParentNodes;
            for (TestStepInvocation testStep : Iterables.concat(flow.getBeforeSteps(), flow.getTestSteps(), flow.getAfterSteps())) {
                TestStepNodeImpl node = addTestStepToGraph(flow, testStep, parentNodes, vUser);

                if (testStep instanceof SubFlowInvocationImpl) {
                    parentNodes = addSubFlowToGraph(node, vUser, vUserManager);
                } else if (testStep instanceof SplitInvocationImpl) {
                    parentNodes = addSplitFlowsToGraph(node, vUser, vUserManager);
                } else if (testStep instanceof ChildFlowInvocationImpl) {
                    parentNodes = addChildFlowsToGraph(node, vUser, vUserManager);
                } else {
                    parentNodes = newHashSet(node);
                }
            }
            allNodes.addAll(parentNodes);
        }
        //vUserManager.freeVUsers(vUsers); its possible to reuse vusers
        return allNodes;
    }

    private TestStepNodeImpl addTestStepToGraph(TestStepFlow flow, TestStepInvocation testStep, Set<TestStepNodeImpl> parentNodes, Integer vUser) {
        TestStepNodeImpl node = new TestStepNodeImpl(flow, testStep, vUser);
        graph.addVertex(node);
        nodesByTestStepId.put(testStep.getId(), node);
        for (TestStepNodeImpl parentNode : parentNodes) {
            graph.addEdge(parentNode, node);
        }
        return node;
    }

    private Set<TestStepNodeImpl> addSubFlowToGraph(TestStepNodeImpl parentNode, Integer parentVUser, VUserManager vUserManager) {
        SubFlowInvocationImpl testStep = (SubFlowInvocationImpl) parentNode.getTestStep();
        TestStepFlow childFlow = testStep.getSubFlow();
        bindVUsersToFlow(childFlow, newArrayList(parentVUser), parentVUser);

        return createFlow(newHashSet(parentNode), vUserManager, childFlow, newArrayList(parentVUser));
    }

    private Set<TestStepNodeImpl> addSplitFlowsToGraph(TestStepNodeImpl parentNode, Integer parentVUser, VUserManager vUserManager) {
        Set<TestStepNodeImpl> allNodes = newHashSet();
        SplitInvocationImpl testStep = (SplitInvocationImpl) parentNode.getTestStep();
        for (TestStepFlow childFlow : testStep.getFlows()) {
            List<Integer> childVUsers = allocateVUsers(vUserManager, DEFAULT_VUSERS_FOR_SUBFLOWS, childFlow, parentVUser);
            Set<TestStepNodeImpl> flowNodes = createFlow(newHashSet(parentNode), vUserManager, childFlow, childVUsers);
            allNodes.addAll(flowNodes);
        }
        return allNodes;
    }

    private Set<TestStepNodeImpl> addChildFlowsToGraph(TestStepNodeImpl parentNode, Integer parentVUser, VUserManager vUserManager) {
        ChildFlowInvocationImpl testStep = (ChildFlowInvocationImpl) parentNode.getTestStep();
        TestStepFlow childFlow = testStep.getChildFlow();

        return createFlow(newHashSet(parentNode), vUserManager, childFlow, getVUsers(childFlow, parentVUser));
    }

    private void allocateAndBindVUsersForChildFlows(TestStepFlow flow, Integer rootVUser, VUserManager vUserManager, int defaultVusers) {
        ArrayList<Integer> vUsers = newArrayList(rootVUser);

        for (TestStepInvocation testStep : flow.getTestSteps()) {
            if (testStep instanceof ChildFlowInvocationImpl) {
                TestStepFlow childFlow = ((ChildFlowInvocationImpl) testStep).getChildFlow();
                int childFlowVUserCnt = defaultIfNegative(childFlow.getRunOptions().getVUsers(), defaultVusers);

                if (childFlowVUserCnt > vUsers.size()) {
                    vUsers.addAll(vUserManager.allocateVUsers(childFlowVUserCnt - vUsers.size()));
                }
                List<Integer> subFlowVUsers = FluentIterable.from(vUsers).limit(childFlowVUserCnt).toList();
                bindVUsersToFlow(childFlow, subFlowVUsers, rootVUser);
            }
        }
    }

    private int defaultIfNegative(int vUsers, int defaultVusers) {
        return vUsers == -1 ? defaultVusers : vUsers;
    }

    private List<Integer> allocateVUsers(VUserManager vUserManager, int defaultVusers, TestStepFlow flow, int parentVUser) {
        int vUserCnt = defaultIfNegative(flow.getRunOptions().getVUsers(), defaultVusers);
        List<Integer> vUsers = vUserManager.allocateVUsers(vUserCnt);
        bindVUsersToFlow(flow, vUsers, parentVUser);
        return vUsers;
    }

    private void bindVUsersToFlow(TestStepFlow flow, List<Integer> vUsers, Integer parentVUser) {
        vUsersByFlowIdAndParent.put(flow.getId(), parentVUser, vUsers);
        flows.put(flow.getId(), flow);
    }

    @Override
    public Collection<TestStepNode> getChildren(TestStepNode testStepNode) {
        Set<DefaultEdge> edges = graph.outgoingEdgesOf(testStepNode);

        return Collections2.transform(edges, new Function<DefaultEdge, TestStepNode>() {
            @Override
            public TestStepNode apply(DefaultEdge input) {
                return graph.getEdgeTarget(input);
            }
        });
    }

    @Override
    public Collection<TestStepNode> getParents(TestStepNode testStepNode) {
        Set<DefaultEdge> edges = graph.incomingEdgesOf(testStepNode);

        return Collections2.transform(edges, new Function<DefaultEdge, TestStepNode>() {
            @Override
            public TestStepNode apply(DefaultEdge input) {
                return graph.getEdgeSource(input);
            }
        });
    }

    @Override
    public List<Integer> getVUsers(TestStepFlow flow, int parentVUser) {
        List<Integer> vUsers = vUsersByFlowIdAndParent.get(flow.getId(), parentVUser);
        Preconditions.checkNotNull(vUsers, "No vUsers were bind to flow %s %s with parent vUser %s",
                flow.getId(), flow.getName(), parentVUser);
        return vUsers;
    }

    @Override
    public int getRunnersCount(TestStepFlow flow) {
        Map<Integer, List<Integer>> vUsersToNodes = vUsersByFlowIdAndParent.row(flow.getId());
        Preconditions.checkNotNull(vUsersToNodes, "Flow %s %s does not exist", flow.getId(), flow.getName());
        int vUsersFromAllParents = 0;
        for (List<Integer> vUsersForOneParent : vUsersToNodes.values()) {
            vUsersFromAllParents += vUsersForOneParent.size();
        }
        return vUsersFromAllParents;
    }

    @Override
    public Set<Long> getAllFlowIds() {
        return flows.keySet();
    }

    @Override
    public TestStepFlow getFlow(Long flowId) {
        return flows.get(flowId);
    }

    @Override
    public List<TestStepNode> getNodes(TestStepInvocation testStep) {
        return nodesByTestStepId.get(testStep.getId());
    }

    @Override
    public TestStepNode getNode(TestStepInvocation testStep, final int vuserId) {
        List<TestStepNode> nodes = getNodes(testStep);

        Collection<TestStepNode> vuserNode = Collections2.filter(nodes, new Predicate<TestStepNode>() {
            @Override
            public boolean apply(TestStepNode node) {
                return node.getVUser().equals(vuserId);
            }
        });
        Preconditions.checkArgument(vuserNode.size() == 1, "Can not have Multiple nodes for one vUser!");

        return vuserNode.iterator().next();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Nodes: ");
        for (TestStepNode testStepNode : graph.vertexSet()) {
            builder.append(testStepNode).append("(").append(testStepNode.getVUser()).append("), ");
        }
        builder.append("Edges: ").append(graph.edgeSet());

        return builder.toString();
    }

    public static class TestStepNodeImpl implements TestStepNode {
        private static AtomicLong idGenerator = new AtomicLong();
        private final long id = idGenerator.incrementAndGet();
        private TestStepFlow flow;
        private TestStepInvocation testStep;
        private int vUser;

        public TestStepNodeImpl(TestStepFlow flow, TestStepInvocation testStep, int vUser) {
            this.flow = flow;
            this.testStep = testStep;
            this.vUser = vUser;
        }

        public long getId() {
            return id;
        }

        @Override
        public TestStepFlow getFlow() {
            return flow;
        }

        @Override
        public TestStepInvocation getTestStep() {
            return testStep;
        }

        @Override
        public String toString() {
            return "" + id;
        }

        @Override
        public Integer getVUser() {
            return vUser;
        }
    }
}
