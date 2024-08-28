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
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;

import java.util.LinkedHashMap;
import java.util.Map;

@API(Internal)
public class DataSourceBindTransformer implements DataRecordTransformer {
    private String dataSourceName;
    private String testStepName;
    private String newDataSourceName;

    public DataSourceBindTransformer(String dataSourceName, String newDataSourceName) {
        this.dataSourceName = dataSourceName;
        this.newDataSourceName = newDataSourceName;
    }

    @Override
    public void setTestStepName(String testStepName) {
        this.testStepName = testStepName;
    }

    @Override
    public String getTestStepName() {
        return testStepName;
    }

    @Override
    public LinkedHashMap<String, DataRecord> apply(LinkedHashMap<String, DataRecord> dataSourcesRecords, String testStepName) {
        final LinkedHashMap<String, DataRecord> result = new LinkedHashMap<>();
        for (Map.Entry<String, DataRecord> dataSourceRecord : dataSourcesRecords.entrySet()) {
            String dataSourceName = dataSourceRecord.getKey();
            DataRecord dataRecord = dataSourceRecord.getValue();
            result.put(dataSourceName, dataRecord);
            if (transformationApplicable(dataSourceName, testStepName)) {
                final DataRecordImpl binding = new DataRecordImpl(dataRecord.getAllFields());
                result.put(newDataSourceName, binding);
            }
        }
        return result;
    }

    private boolean transformationApplicable(String dataSourceName, String testStepName) {
        return this.dataSourceName.equals(dataSourceName) &&
                (this.testStepName == null ||
                        this.testStepName.equals(testStepName));
    }


}
