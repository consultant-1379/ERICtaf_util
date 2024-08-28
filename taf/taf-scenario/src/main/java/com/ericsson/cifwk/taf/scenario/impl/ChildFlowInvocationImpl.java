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

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;

/**
 * Difference from {@link SubFlowInvocationImpl} is that ChildFlow may have multiple vUsers, all ChildFlows of Parent Flow share same vUsers
 */
@API(Internal)
public class ChildFlowInvocationImpl extends ParallelInvocation {
    public ChildFlowInvocationImpl(TestStepFlow flow) {
        super(flow);
    }

    public TestStepFlow getChildFlow() {
        return flows[0];
    }

    @Override
    public String getName() {
        return "Child flow invocation";
    }
}
