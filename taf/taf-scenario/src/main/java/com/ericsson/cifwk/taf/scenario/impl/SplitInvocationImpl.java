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
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Split execution to multiple flows. Each flow will have new vUser.
 */
@API(Internal)
public class SplitInvocationImpl extends ParallelInvocation {
    public SplitInvocationImpl(TestStepFlow... flows) {
        super(flows);
    }

    public List<TestStepFlow> getFlows() {
        return Lists.newArrayList(flows);
    }

    @Override
    public String getName() {
        return "Split invocation";
    }
}
