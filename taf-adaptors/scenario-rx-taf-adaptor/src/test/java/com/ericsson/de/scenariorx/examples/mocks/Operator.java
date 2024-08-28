package com.ericsson.de.scenariorx.examples.mocks;

/*
 * COPYRIGHT Ericsson (c) 2017.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordBuilder;
import com.ericsson.de.scenariorx.TafNode;

@com.ericsson.cifwk.taf.annotations.Operator
public class Operator {
    public HttpTool login(String username, String password) {
        return new HttpTool(username, password);
    }

    public Object getObject1() {
        return null;
    }

    public Object getObject2() {return null;}

    public Object getObject3() {
        return null;
    }

    public DataRecord createNode() {
        return new DataRecordBuilder()
                .setField("nodeType", "nodeType")
                .setField("networkElementId", "networkElementId")
                .build(TafNode.class);
    }
}
