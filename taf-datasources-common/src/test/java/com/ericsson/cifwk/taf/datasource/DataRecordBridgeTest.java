package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordBridge;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 10.11.2016
 */
public class DataRecordBridgeTest {

    @Test
    public void getProperties() {
        Map<String, Object> properties = DataRecordBridge.getProperties(new MyDataRecord(123, "John"));
        assertEquals(2, properties.size());
        assertEquals("John", properties.get("getName"));
        assertEquals(123, properties.get("getId"));
    }

    @Test
    public void getPropertiesNullValuesAreFiltered() {
        Map<String, Object> properties = DataRecordBridge.getProperties(new MyDataRecord(123, null));
        assertEquals(1, properties.size());
        assertEquals(123, properties.get("getId"));
    }

    @Test
    public void getPropertiesKeysAreSorted() {
        MyDataRecord john = new MyDataRecord(123, "John");
        john.setId2(456);
        Map<String, Object> properties = DataRecordBridge.getProperties(john);
        assertEquals(3, properties.size());
        Iterator<String> keys = properties.keySet().iterator();
        assertEquals("getId", keys.next());
        assertEquals("getId2", keys.next());
        assertEquals("getName", keys.next());
    }

    private class MyDataRecord implements DataRecord {

        private int id;

        private String name;

        private Integer id2;

        public MyDataRecord(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId2() {
            return id2;
        }

        public void setId2(int id2) {
            this.id2 = id2;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getFieldValue(String name) {
            return (T) "name";
        }

        @Override
        public Map<String, Object> getAllFields() {
            return new HashMap<>();
        }

        @Override
        public String getDataSourceName() {
            return "source";
        }

        @Override
        public <T> T transformTo(Class<T> beanClass) {
            return null;
        }
    }

}