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
import java.util.Map;

public class ColumnBindTransformer implements DataRecordTransformer {
    private String dataSourceName;
    private String columnName;
    private String newColumnName;
    private String testStepName;

    public ColumnBindTransformer(String dataSourceName, String columnName, String newColumnName) {
        this.dataSourceName = dataSourceName;
        this.columnName = columnName;
        this.newColumnName = newColumnName;
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

            final DataRecord transformedDataRecord = applyForDataRecord(dataSourceName, dataRecord, testStepName);
            result.put(dataSourceName, transformedDataRecord);
        }

        return result;
    }

    public DataRecord applyForDataRecord(String dataSourceName, DataRecord dataRecord, String testStepName) {
        if (isTransformationApplicable(dataSourceName, testStepName)) {
            DataRecordImpl result = new DataRecordImpl(dataRecord.getAllFields());
            Object value = dataRecord.getAllFields().get(columnName);
            result.setFieldValue(newColumnName, value);
            return result;
        } else {
            return dataRecord;
        }
    }

    private boolean isTransformationApplicable(String dataSourceName, String testStepName) {
        return this.dataSourceName.equals(dataSourceName) &&
                (this.testStepName == null || this.testStepName.equals(testStepName));
    }
}
