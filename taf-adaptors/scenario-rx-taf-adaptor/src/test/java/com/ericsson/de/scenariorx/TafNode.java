package com.ericsson.de.scenariorx;

import com.ericsson.cifwk.taf.datasource.DataRecord;

public interface TafNode extends DataRecord {
    String NODE_TYPE = "nodeType";
    String NETWORK_ELEMENT_ID = "networkElementId";

    String getNodeType();

    String getNetworkElementId();
}
