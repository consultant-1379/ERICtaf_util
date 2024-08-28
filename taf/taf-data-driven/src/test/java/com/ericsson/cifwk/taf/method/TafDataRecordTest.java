package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TafDataRecordTest {

    DataRecord record;

    @Before
    public void setUp() {
        HashMap<String, Object> data = Maps.newHashMap();
        data.put("a", "1");
        data.put("b", "2");
        data.put("c", "3");
        record = new DataRecordImpl(data);
    }

    @Test
    public void testGetValue() throws Exception {
        assertThat(ParameterTransformer.transform("data", record, annotated("x", String.class)).isPresent(), is(false));
        assertThat(ParameterTransformer.transform("data", record, annotated("a", String.class)), IsEqual.<Object>equalTo(Optional.of("1")));
        assertThat(((DataRecord) ParameterTransformer.transform("data", record, annotated("data", DataRecord.class)).get()).getFieldValue("a"), IsEqual.<Object>equalTo("1"));
        assertThat(((BeanPropertyContainer) ParameterTransformer.transform("data", record, annotated("data", Integer.class)).get()).getProperties().size(), equalTo(3));
    }

    private <T> Parameter<T> annotated(String value, Class<T> type) {
        return new Parameter<>(type, input(value));
    }

    private Input input(final String value) {
        return new Input() {
            @Override
            public String value() {
                return value;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Input.class;
            }
        };
    }

}
