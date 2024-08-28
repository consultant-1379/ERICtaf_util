package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
public class ClassDataSourceTest {

    @Test
    public void shouldLoadViaApi_Class() {
        TestDataSource<DataRecord> classDataSource = fromClass(this.getClass());

        Iterator<DataRecord> iterator = classDataSource.iterator();

        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.hasNext(), is(false));
    }

    @DataSource
    public Iterable<Map<String, Object>> data(ConfigurationSource reader) {
        assertThat(reader, notNullValue());
        ArrayList<Map<String, Object>> list = Lists.newArrayList();
        Map<String, Object> map = Maps.<String, Object> newHashMap();
        map.put("x1", "1");
        map.put("y2", "2");
        map.put("z3", "3");
        list.add(map);
        return list;
    }

}
