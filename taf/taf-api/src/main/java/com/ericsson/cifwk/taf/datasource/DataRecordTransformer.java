/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.datasource;

import java.util.LinkedHashMap;

public interface DataRecordTransformer {
    LinkedHashMap<String,DataRecord> apply(LinkedHashMap<String, DataRecord> dataSourcesRecords, String testStepName);

    void setTestStepName(String testStepName);

    String getTestStepName();
}
