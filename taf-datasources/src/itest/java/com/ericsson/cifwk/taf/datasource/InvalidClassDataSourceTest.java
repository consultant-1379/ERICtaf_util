package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 */
public class InvalidClassDataSourceTest {

    @Test
    public void shouldThrowAnExcpetion_Class() {
        try {
            TestDataSource<DataRecord> classDataSource = fromClass(this.getClass());
            Iterator<DataRecord> iterator = classDataSource.iterator();
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                    is("Parameter Type should be com.ericsson.cifwk.taf.datasource.ConfigurationSource for the Method annotated with @DataSource"));
        }

    }

    @DataSource
    public Iterable<Map<String, Object>> data(String reader) {
        ArrayList<Map<String, Object>> list = Lists.newArrayList();
        Map<String, Object> map = Maps.<String, Object> newHashMap();
        map.put("x1", "1");
        map.put("y2", "2");
        map.put("z3", "3");
        list.add(map);
        return list;
    }
}
