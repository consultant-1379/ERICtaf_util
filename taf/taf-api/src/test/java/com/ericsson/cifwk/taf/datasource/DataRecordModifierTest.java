package com.ericsson.cifwk.taf.datasource;


import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
public class DataRecordModifierTest {

    @Test
    public void should_populateAllFields(){
        DataRecordImpl toCopy = new DataRecordImpl();
        toCopy.setFieldValue("field1","value1");
        toCopy.setFieldValue("field2",7);
        toCopy.setFieldValue("field3", Collections.singletonMap("k1","v1"));
        Map<String,Object> initialValues = Maps.newHashMap();
        initialValues.put("field1","toBeLost");
        initialValues.put("saveMe",12);
        DataRecord toBePopulate = new DataRecordImpl(initialValues );
        DataRecordModifier populationEngine = new DataRecordModifierImpl(toBePopulate);
        populationEngine.setFields(toCopy);
        assertThat(toBePopulate.getFieldValue("field1"),equalTo(toCopy.getFieldValue("field1")));
        assertThat(toBePopulate.getFieldValue("field2"),equalTo(toCopy.getFieldValue("field2")));
        assertThat(toBePopulate.getFieldValue("field3"),equalTo(toCopy.getFieldValue("field3")));
        assertThat((int)toBePopulate.getFieldValue("saveMe"),equalTo(12));
    }


}
