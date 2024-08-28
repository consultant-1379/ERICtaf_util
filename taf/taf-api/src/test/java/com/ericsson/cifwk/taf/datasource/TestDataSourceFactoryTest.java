package com.ericsson.cifwk.taf.datasource;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestDataSourceFactoryTest {

    @Test
    public void createDataSource_empty() {
        TestDataSource dataSource = TestDataSourceFactory.createDataSource();
        assertThat((Iterable<Object>) dataSource, iterableWithSize(0));
    }

    @Test
    public void createDataSource_full() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("a", "b");
        TestDataSource dataSource = TestDataSourceFactory.createDataSource(map);
        assertThat((Iterable<Object>) dataSource, iterableWithSize(1));
    }
}