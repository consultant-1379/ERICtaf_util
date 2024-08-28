package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.datasource.TafDataSources.MergeType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TafDataSourcesTest {

    private TestDataSource<DataRecord> one;
    private TestDataSource<DataRecord> two;
    private TestDataSource<DataRecord> three;
    private TestDataSource<DataRecord> four;

    @Before
    public void setUp() throws Exception {
        one = mock(TestDataSource.class);
        two = mock(TestDataSource.class);
        three = mock(TestDataSource.class);
        four = mock(TestDataSource.class);

        Map<String, Object> data1 = Maps.newHashMap();
        data1.put("x", "1");
        data1.put("y", "1");
        DataRecord dataRecord1 = new DataRecordImpl(data1);
        when(one.iterator()).thenReturn(Lists.newArrayList(dataRecord1).iterator());

        Map<String, Object> data2 = Maps.newHashMap();
        data2.put("x", "2");
        data2.put("z", "2");
        DataRecord dataRecord2 = new DataRecordImpl(data2);
        when(two.iterator()).thenReturn(Lists.newArrayList(dataRecord2).iterator());

        Map<String, Object> data3 = Maps.newHashMap();
        data3.put("x", "3");
        data3.put("a", "3");
        DataRecord dataRecord3 = new DataRecordImpl(data3);
        when(three.iterator()).thenReturn(Lists.newArrayList(dataRecord3).iterator());

        Map<String, Object> data4 = Maps.newHashMap();
        data4.put("x", "4");
        data4.put("b", "4");
        DataRecord dataRecord4 = new DataRecordImpl(data4);
        when(four.iterator()).thenReturn(Lists.newArrayList(dataRecord4).iterator());

    }

    @Test
    public void testMultipleCombine() throws Exception {
        TestDataSource combined = TafDataSources.combine(one, two, three, four);

        assertThat(combined.iterator().next(), notNullValue());
        assertThat(combined.iterator().next(), notNullValue());
        assertThat(combined.iterator().next(), notNullValue());
        assertThat(combined.iterator().next(), notNullValue());
        assertThat(combined.iterator().hasNext(), is(false));
    }

    @Test
    public void testCombine() throws Exception {
        final TestDataSource<DataRecord> ds1 = TafDataSources.fromCsv("data/calculator.csv");
        assertThat(Iterators.size(ds1.iterator()), equalTo(3));
        assertThat(Iterators.size(ds1.iterator()), equalTo(3));

        final TestDataSource<DataRecord> ds2 = TafDataSources.fromCsv("data/calculator.csv");
        assertThat(Iterators.size(ds1.iterator()), equalTo(3));
        assertThat(Iterators.size(ds1.iterator()), equalTo(3));

        final TestDataSource<DataRecord> combined = TafDataSources.combine(ds1, ds2);

        assertThat(Iterators.size(combined.iterator()), equalTo(6));
        assertThat(Iterators.size(combined.iterator()), equalTo(6));
    }

    @Test
    public void testMerge() throws Exception {
        TestDataSource<DataRecord> merged = TafDataSources.merge(one, two);

        DataRecord value = merged.iterator().next();
        assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("2"));
        assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo("1"));
        assertThat(value.getFieldValue("z"), IsEqual.<Object>equalTo("2"));
        assertThat(merged.iterator().hasNext(), is(false));
    }

    @Test
    public void testMerge_RepeatDataSource() throws Exception {
        // TODO
    }

    @Test
    public void testFilter() throws Exception {
        TestDataSource filtered = TafDataSources.filter(one, new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord input) {
                return false;
            }
        });

        assertThat(filtered.iterator().hasNext(), is(false));
    }

    @Test
    public void testTransform() throws Exception {
        TestDataSource<DataRecord> transformed = TafDataSources.transform(one, new Function<DataRecord, DataRecord>() {
            @Override
            public DataRecord apply(DataRecord input) {
                Map<String, Object> map = Maps.newHashMap();
                map.putAll(input.getAllFields());
                map.put("a", "0");
                return new DataRecordImpl(map);
            }
        });

        DataRecord value = transformed.iterator().next();
        assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("1"));
        assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo("1"));
        assertThat(value.getFieldValue("a"), IsEqual.<Object>equalTo("0"));
    }

    @Test
    public void testMergeDefaultInner_diffNoOfRecords() {
        TestDataSource<DataRecord> dataSourceOne = mock(TestDataSource.class);
        TestDataSource<DataRecord> dataSourceTwo = mock(TestDataSource.class);
        List<DataRecord> oneList = Lists.newArrayList();
        List<DataRecord> twoList = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {
            Map<String, Object> data1 = Maps.newHashMap();
            data1.put("x", "1" + i);
            data1.put("y", "1" + i);
            oneList.add(new DataRecordImpl(data1));

            Map<String, Object> data2 = Maps.newHashMap();
            data2.put("x", "2" + i);
            data2.put("z", "2" + i);
            twoList.add(new DataRecordImpl(data2));
        }

        for (int i = 3; i < 5; i++) {
            Map<String, Object> data2 = Maps.newHashMap();
            data2.put("x", "2" + i);
            data2.put("z", "2" + i);
            twoList.add(new DataRecordImpl(data2));
        }

        when(dataSourceOne.iterator()).thenReturn(oneList.iterator());
        when(dataSourceTwo.iterator()).thenReturn(twoList.iterator());

        TestDataSource<DataRecord> merged = TafDataSources.merge(dataSourceOne, dataSourceTwo);

        int i = 0;
        int noOfRecords = 0;
        while (merged.iterator().hasNext()) {
            DataRecord value = merged.iterator().next();
            assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("2" + i));
            assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo("1" + i));
            assertThat(value.getFieldValue("z"), IsEqual.<Object>equalTo("2" + i));
            ++noOfRecords;
            ++i;
        }
        assertThat(noOfRecords, is(3));
    }

    @Test
    public void testMergeOuter_diffNoOfRecords_firstMoreRecords() {
        TestDataSource<DataRecord> dataSourceOne = mock(TestDataSource.class);
        TestDataSource<DataRecord> dataSourceTwo = mock(TestDataSource.class);
        List<DataRecord> oneList = Lists.newArrayList();
        List<DataRecord> twoList = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {
            Map<String, Object> data1 = Maps.newHashMap();
            data1.put("x", "1" + i);
            data1.put("y", "1" + i);
            oneList.add(new DataRecordImpl(data1));

            Map<String, Object> data2 = Maps.newHashMap();
            data2.put("x", "2" + i);
            data2.put("z", "2" + i);
            twoList.add(new DataRecordImpl(data2));
        }

        for (int i = 3; i < 5; i++) {
            Map<String, Object> data1 = Maps.newHashMap();
            data1.put("x", "1" + i);
            data1.put("y", "1" + i);
            oneList.add(new DataRecordImpl(data1));
        }

        when(dataSourceOne.iterator()).thenReturn(oneList.iterator());
        when(dataSourceTwo.iterator()).thenReturn(twoList.iterator());

        TestDataSource<DataRecord> merged = TafDataSources.merge(dataSourceOne, dataSourceTwo, MergeType.OUTER);

        int noOfRecords = 0;
        int i = 0;
        while (merged.iterator().hasNext()) {
            DataRecord value = merged.iterator().next();
            if (noOfRecords < 3) {
                assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("2" + i));
                assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo("1" + i));
                assertThat(value.getFieldValue("z"), IsEqual.<Object>equalTo("2" + i));
            } else {
                assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("1" + i));
                assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo("1" + i));
                assertThat(value.getFieldValue("z"), IsEqual.<Object>equalTo(null));
            }
            ++noOfRecords;
            ++i;
        }
        assertTrue(noOfRecords == 5);
    }

    @Test
    public void testMergeOuter_diffNoOfRecords_secondMoreRecords() {
        TestDataSource<DataRecord> dataSourceOne = mock(TestDataSource.class);
        TestDataSource<DataRecord> dataSourceTwo = mock(TestDataSource.class);
        List<DataRecord> oneList = Lists.newArrayList();
        List<DataRecord> twoList = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {
            Map<String, Object> data1 = Maps.newHashMap();
            data1.put("x", "1" + i);
            data1.put("y", "1" + i);
            oneList.add(new DataRecordImpl(data1));

            Map<String, Object> data2 = Maps.newHashMap();
            data2.put("x", "2" + i);
            data2.put("z", "2" + i);
            twoList.add(new DataRecordImpl(data2));
        }

        for (int i = 3; i < 5; i++) {
            Map<String, Object> data2 = Maps.newHashMap();
            data2.put("x", "2" + i);
            data2.put("z", "2" + i);
            twoList.add(new DataRecordImpl(data2));
        }

        when(dataSourceOne.iterator()).thenReturn(oneList.iterator());
        when(dataSourceTwo.iterator()).thenReturn(twoList.iterator());

        TestDataSource<DataRecord> merged = TafDataSources.merge(dataSourceOne, dataSourceTwo, MergeType.OUTER);

        int noOfRecords = 0;
        int i = 0;
        while (merged.iterator().hasNext()) {
            DataRecord value = merged.iterator().next();
            if (noOfRecords < 3) {
                assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("2" + i));
                assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo("1" + i));
                assertThat(value.getFieldValue("z"), IsEqual.<Object>equalTo("2" + i));
            } else {
                assertThat(value.getFieldValue("x"), IsEqual.<Object>equalTo("2" + i));
                assertThat(value.getFieldValue("y"), IsEqual.<Object>equalTo(null));
                assertThat(value.getFieldValue("z"), IsEqual.<Object>equalTo("2" + i));
            }
            ++noOfRecords;
            ++i;
        }
        assertThat(noOfRecords, is(5));
    }

    @Test
    public void testCopy_shouldUnwrapCyclicShared() throws Exception {
        TestDataSource<DataRecord> dataSourceOne = TestDataSourceFactory.createDataSource();
        dataSourceOne.addRecord().setField("a", "1");
        dataSourceOne.addRecord().setField("b", "2");
        dataSourceOne.addRecord().setField("c", "3");

        dataSourceOne = TafDataSources.cyclic(dataSourceOne);
        dataSourceOne = TafDataSources.shared(dataSourceOne);
        dataSourceOne.iterator().next();

        TestDataSource<DataRecord> dataSourceTwo = TafDataSources.copy(dataSourceOne);
        Iterator<DataRecord> iterator = dataSourceTwo.iterator();
        assertThat(String.valueOf(iterator.next().getFieldValue("a")), equalTo("1"));
        assertThat(String.valueOf(iterator.next().getFieldValue("b")), equalTo("2"));
        assertThat(String.valueOf(iterator.next().getFieldValue("c")), equalTo("3"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testCopy_filtered() throws Exception {
        TestDataSource<DataRecord> dataSourceOne = TestDataSourceFactory.createDataSource();
        dataSourceOne.addRecord().setField("a", "1");
        dataSourceOne.addRecord().setField("b", "2");
        dataSourceOne.addRecord().setField("c", "3");

        dataSourceOne = TafDataSources.filter(dataSourceOne, new Predicate<DataRecord>() {
            @Override
            public boolean apply(DataRecord dataRecord) {
                return dataRecord.getFieldValue("a") != null;
            }
        });

        TestDataSource<DataRecord> dataSourceTwo = TafDataSources.copy(dataSourceOne);

        Iterator<DataRecord> iterator = dataSourceTwo.iterator();
        assertThat(String.valueOf(iterator.next().getFieldValue("a")), equalTo("1"));
        assertThat(String.valueOf(iterator.next().getFieldValue("b")), equalTo("2"));
        assertThat(String.valueOf(iterator.next().getFieldValue("c")), equalTo("3"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testReverse() throws Exception {
        TestDataSource<DataRecord> dataSourceOne = TestDataSourceFactory.createDataSource();
        dataSourceOne.addRecord().setField("a", "1");
        dataSourceOne.addRecord().setField("b", "2");
        dataSourceOne.addRecord().setField("c", "3");

        TestDataSource<DataRecord> dataSourceTwo = TafDataSources.reverse(dataSourceOne);

        Iterator<DataRecord> iterator = dataSourceTwo.iterator();
        assertThat(String.valueOf(iterator.next().getFieldValue("c")), equalTo("3"));
        assertThat(String.valueOf(iterator.next().getFieldValue("b")), equalTo("2"));
        assertThat(String.valueOf(iterator.next().getFieldValue("a")), equalTo("1"));
        assertFalse(iterator.hasNext());
    }
}
