package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by ekiajen on 20/01/2017.
 */
public class DataRecordParameterTransformerTest {

    private LinkedHashMap firstDataRecordData;
    private Map<String, Object> parameters;
    private LinkedHashMap<String, DataRecord> dataSourcesRecords;
    private String testStepName = "testCreateEditedDataRecord";

    @Before
    public void setUp() {
        firstDataRecordData = new LinkedHashMap<>();
        parameters = Maps.newHashMap();
        dataSourcesRecords = new LinkedHashMap<>();

        firstDataRecordData.put("A", "aaa");
        firstDataRecordData.put("B", "bbb");
        DataRecord firstRecord = new DataRecordImpl(firstDataRecordData);
        dataSourcesRecords.put("firstDataRecord", firstRecord);
    }

    @Test
    public void shouldContainOriginalDatasource() {
        DataRecordParameterTransformer dataRecordParameterTransformer = new DataRecordParameterTransformer(parameters);
        LinkedHashMap<String, DataRecord> newDatasourceRecords = dataRecordParameterTransformer.apply(dataSourcesRecords, testStepName);
        String fieldValue = newDatasourceRecords.get("firstDataRecord").getFieldValue("B");
        assertThat(fieldValue).containsMatch("bbb");
    }

    @Test
    public void shouldNotReplaceParameterData() {
        parameters.put("unknownDataRecord.B", "test");
        DataRecordParameterTransformer dataRecordParameterTransformer = new DataRecordParameterTransformer(parameters);
        DataRecord parametersRecord = new DataRecordImpl("parameter", parameters);
        dataSourcesRecords.put("parameter", parametersRecord);
        LinkedHashMap<String, DataRecord> newDatasourceRecords = dataRecordParameterTransformer.apply(dataSourcesRecords, testStepName);
        String fieldValue = newDatasourceRecords.get("firstDataRecord").getFieldValue("B");
        assertThat(fieldValue).containsMatch("bbb");
    }

    @Test
    public void shouldReplaceParameterData() {
        parameters.put("firstDataRecord.B", "test");
        DataRecordParameterTransformer dataRecordParameterTransformer = new DataRecordParameterTransformer(parameters);
        DataRecord parametersRecord = new DataRecordImpl("parameter", parameters);
        dataSourcesRecords.put("parameter", parametersRecord);
        LinkedHashMap<String, DataRecord> newDatasourceRecords = dataRecordParameterTransformer.apply(dataSourcesRecords, testStepName);
        String fieldValue = newDatasourceRecords.get("firstDataRecord").getFieldValue("B");
        assertThat(fieldValue).containsMatch("test");
    }
}
