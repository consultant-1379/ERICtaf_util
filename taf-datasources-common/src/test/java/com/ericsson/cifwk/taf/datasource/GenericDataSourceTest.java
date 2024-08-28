package com.ericsson.cifwk.taf.datasource;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenericDataSourceTest {

    private TestDataSource<DataRecord> internal;
    private GenericDataSource<Node> genericDataSource;
    private CyclicDataSource<Node> cyclicGenericDataSource;

    @Before
    public void setUp() {
        //noinspection unchecked
        internal = mock(TestDataSource.class);
        genericDataSource = new GenericDataSource<>(internal, Node.class);
        cyclicGenericDataSource = new CyclicDataSource<>(genericDataSource);
    }

    @Test
    public void testIterator_Empty() {
        when(internal.iterator()).thenReturn(Collections.<DataRecord>emptyIterator());
        Iterator<Node> iterator = genericDataSource.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIterator_SimpleGenericDataSource() {
        DataRecord dr1 = initRecord(new DataRecordImpl(null), "nodeId", 1);
        DataRecord dr2 = initRecord(new DataRecordImpl(null), "nodeId", 2);
        DataRecord dr3 = initRecord(new DataRecordImpl(null), "nodeId", 3);

        List<DataRecord> list = Arrays.asList(
                dr1,
                dr2,
                dr3);
        when(internal.iterator()).thenReturn(list.iterator());

        Iterator<Node> iterator = genericDataSource.iterator();
        Integer[] expectedNodeIds = {1, 2, 3};
        for (int exp : expectedNodeIds) {
            assertTrue(iterator.hasNext());
            assertThat(iterator.next().getNodeId(), is(exp));
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIterator_CyclicGenericDataSource() {
        DataRecord dr1 = initRecord(new DataRecordImpl(null), "nodeId", 1);
        DataRecord dr2 = initRecord(new DataRecordImpl(null), "nodeId", 2);
        DataRecord dr3 = initRecord(new DataRecordImpl(null), "nodeId", 3);

        List<DataRecord> list = Arrays.asList(
                dr1,
                dr2,
                dr3);
        when(internal.iterator()).thenReturn(list.iterator());

        Iterator<Node> iterator = cyclicGenericDataSource.iterator();
        Integer[] expectedNodeIds = {1, 2, 3};
        for (int exp : expectedNodeIds) {
            assertTrue(iterator.hasNext());
            assertThat(iterator.next().getNodeId(), is(exp));
        }
        assertTrue(iterator.hasNext());
        cyclicGenericDataSource.stop();
        assertFalse(iterator.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableDataSource() throws Exception {
        TestDataSource<DataRecord> dataSource = TestDataSourceFactory.createDataSource(Maps.<String, Object>newHashMap());
        TestDataSource<DataRecord> unmodifiableDataSource = new MarkedAsSharedTafDataSource<>(dataSource);
        GenericDataSource<Node> genericDataSource = new GenericDataSource<>(unmodifiableDataSource, Node.class);
        genericDataSource.addRecord().setField("nodeId", 1);
    }

    @Test
    public void testModifiableDataSource() throws Exception {
        final int NODE_ID = 1;
        TestDataSource<DataRecord> dataSource = TestDataSourceFactory.createDataSource();
        GenericDataSource<Node> genericDataSource = new GenericDataSource<>(dataSource, Node.class);
        genericDataSource.addRecord().setField("nodeId", NODE_ID);

        Node next = genericDataSource.iterator().next();
        assertThat(next.getNodeId(), equalTo(NODE_ID));
    }

    private DataRecord initRecord(DataRecordImpl dr, String key, Object value) {
        dr.setFieldValue(key, value);
        return dr;
    }

    interface Node extends DataRecord {
        Integer getNodeId();
    }
}
