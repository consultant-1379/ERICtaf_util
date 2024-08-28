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

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;

import java.util.LinkedHashMap;

@API(Internal)
public class TestStepStartedMessage implements ScenarioMessage {
    private final TestStepFlow flow;
    private final TestStepInvocation invocation;
    private final LinkedHashMap<String, DataRecord> dataSourcesRecords;
    private final int vuserId;

    public TestStepStartedMessage(TestStepFlow flow, TestStepInvocation invocation, LinkedHashMap<String, DataRecord> dataSourcesRecords, int vuserId) {
        this.flow = flow;
        this.invocation = invocation;
        this.dataSourcesRecords = dataSourcesRecords;
        this.vuserId = vuserId;
    }

    public TestStepInvocation getInvocation() {
        return invocation;
    }

    public LinkedHashMap<String, DataRecord> getDataSourcesRecords() {
        return dataSourcesRecords;
    }

    public TestStepFlow getFlow() {
        return flow;
    }

    public int getVuserId() {
        return vuserId;
    }
}
