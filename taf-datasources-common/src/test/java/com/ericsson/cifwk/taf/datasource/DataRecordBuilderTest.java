package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordBuilder;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

public class DataRecordBuilderTest {

    @Test
    public void shouldCreateDataRecordFromKV(){
        final DataRecordBuilder builder = new DataRecordBuilder();
        final DataRecord dataRecord = builder.setField("col","data").build();
        assertThat(dataRecord.getFieldValue("col").toString()).isEqualTo("data");
    }

    @Test
    public void shouldCreateMyDataRecordFromKV(){
        final DataRecordBuilder builder = new DataRecordBuilder();
        final MyDataRecord dataRecord = builder.setField("col","data").build(MyDataRecord.class);
        assertThat(dataRecord.getCol().toString()).isEqualTo("data");
    }

    @Test
    public void shouldCreateDataRecordFromMap(){
        final DataRecordBuilder builder = new DataRecordBuilder();
        Map fields = createDataRecord();
        final DataRecord dataRecord = builder.setFields(fields).build();
        assertThat(dataRecord.getFieldValue("col2").toString()).isEqualTo("data2");
    }

    private Map createDataRecord() {
        Map fields = Maps.newHashMap();
        fields.put("col1", "data1");
        fields.put("col2", "data2");
        return fields;
    }

    @Test
    public void shouldCreateDataRecordFromDataRecords(){
        final DataRecordBuilder builder = new DataRecordBuilder();
        Map fields = createDataRecord();
        DataRecordImpl dr1 = new DataRecordImpl(fields);
        fields = Maps.newHashMap();
        fields.put("col1", "data4");
        fields.put("col3", "data3");
        DataRecordImpl dr2 = new DataRecordImpl(fields);
        final DataRecord dataRecord = builder.setFields(dr1,dr2).build();
        assertThat(dataRecord.getFieldValue("col1").toString()).isEqualTo("data4");
    }

    public interface MyDataRecord extends DataRecord {
        String getCol();
    }
}
