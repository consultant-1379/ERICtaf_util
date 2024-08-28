/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl.messages;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.ParallelInvocation;

/**
 * Invocation of flow and all it subflows will be cancelled for current vUser and datasource iteration
 */
@API(Internal)
public class FlowExecutionCancelledMessage implements ScenarioMessage {
    private ParallelInvocation parallelInvocation;
    private final int vuserId;

    public FlowExecutionCancelledMessage(ParallelInvocation parallelInvocation, int vuserId) {
        this.parallelInvocation = parallelInvocation;
        this.vuserId = vuserId;
    }

    public ParallelInvocation getInvocation() {
        return parallelInvocation;
    }

    public int getVuserId() {
        return vuserId;
    }

}
