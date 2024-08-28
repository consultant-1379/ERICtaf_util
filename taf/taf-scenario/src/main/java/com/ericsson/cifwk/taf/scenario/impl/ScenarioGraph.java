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

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@API(Internal)
public interface ScenarioGraph {
    int NO_PARENT_VUSER = -1;

    List<TestStepNode> getNodes(TestStepInvocation testStep);

    TestStepNode getNode(TestStepInvocation testStep, int vuserId);

    Collection<TestStepNode> getChildren(TestStepNode testStepNode);

    Collection<TestStepNode> getParents(TestStepNode testStepNode);

    List<Integer> getVUsers(TestStepFlow flow, int parentVUser);

    int getRunnersCount(TestStepFlow flow);

    Set<Long> getAllFlowIds();

    TestStepFlow getFlow(Long flowId);

    interface TestStepNode {
        long getId();

        TestStepFlow getFlow();

        TestStepInvocation getTestStep();

        Integer getVUser();
    }
}
