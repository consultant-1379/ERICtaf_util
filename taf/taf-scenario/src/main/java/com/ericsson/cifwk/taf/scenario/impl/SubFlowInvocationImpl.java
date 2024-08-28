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
import com.google.common.base.Preconditions;

/**
 * Flow that runs with same vUser as parent flow. May have only 1 vUser
 */
@API(Internal)
public class SubFlowInvocationImpl extends ParallelInvocation {
    public SubFlowInvocationImpl(TestStepFlow flow) {
        super(checkFlow(flow));
    }

    private static TestStepFlow checkFlow(TestStepFlow flow) {
        Preconditions.checkArgument(flow.getRunOptions().getVUsers() <= 1, "SubFlow can only have one vUser. If you need multiple vUsers" +
                " please use .doParallel(TestStepFlow... flow)");
        return flow;
    }

    public TestStepFlow getSubFlow() {
        return flows[0];
    }

    @Override
    public String getName() {
        return "Sub Flow Invocation";
    }
}
