package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordModifierImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataRecordModifierImplTest {

    @Test
    public void setField() throws Exception {
        DataRecordImpl dataRecord = new DataRecordImpl();
        new DataRecordModifierImpl(dataRecord).setField("name", "myName").setField("id", 1L);
        assertEquals(2, dataRecord.getAllFields().size());

        Long id = dataRecord.getFieldValue("id");
        assertEquals(1L, id.longValue());
        String name = dataRecord.getFieldValue("name");
        assertEquals("myName", name);
    }

}
