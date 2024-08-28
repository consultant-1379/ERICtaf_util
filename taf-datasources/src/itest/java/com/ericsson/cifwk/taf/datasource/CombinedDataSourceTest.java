package com.ericsson.cifwk.taf.datasource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromCsv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 */
public class CombinedDataSourceTest {

    @Test
    public void shouldLoadViaApi_CSV() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/simple.csv");

        Iterator<DataRecord> iterator = csvDataSource.iterator();

        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void shouldCombineTwoCsvViaApi_CSV() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/simple.csv","data/simple1.csv");

        Iterator<DataRecord> iterator = csvDataSource.iterator();

        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void shouldLoadViaApi_Class() {
        TestDataSource<DataRecord> classDataSource = fromClass(this.getClass());

        Iterator<DataRecord> iterator = classDataSource.iterator();

        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testMergeDataSources() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/simple.csv");
        TestDataSource<DataRecord> classDataSource = fromClass(this.getClass());

        TestDataSource<DataRecord> merged = TafDataSources.merge(classDataSource, csvDataSource);
        Iterator<DataRecord> iterator = merged.iterator();

        assertThat(iterator.next(), notNullValue());
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testRegisterContextDataSource() {
        TestDataSource<DataRecord> csvDataSource = fromCsv("data/simple.csv");

        TestContext context = TafTestContext.getContext();
        context.addDataSource("name", csvDataSource);

        assertThat(context.dataSource("name"), is(csvDataSource));
    }

    @DataSource
    public Iterable<Map<String, Object>> data() {
        ArrayList<Map<String, Object>> list = Lists.newArrayList();
        Map<String, Object> map = Maps.<String, Object>newHashMap();
        map.put("x1", "1");
        map.put("y2", "2");
        map.put("z3", "3");
        list.add(map);
        return list;
    }

}
