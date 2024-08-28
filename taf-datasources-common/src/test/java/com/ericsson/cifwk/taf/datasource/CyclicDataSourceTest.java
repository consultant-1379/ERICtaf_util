package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.taf.datasource.CyclicDataSource;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CyclicDataSourceTest {

    private CyclicDataSource<DataRecord> dataSource;
    private TestDataSource<DataRecord> internal;

    @Before
    public void setUp() throws Exception {
        //noinspection unchecked
        internal = mock(TestDataSource.class);
        dataSource = new CyclicDataSource<>(internal);
    }

    @Test
    public void shouldWorkWithEmptyList() {
        when(internal.iterator()).thenReturn(Collections.<DataRecord>emptyIterator());
        Iterator<DataRecord> iterator = dataSource.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void shouldLoop() {
        DataRecord dr1 = new DataRecordImpl(null);
        DataRecord dr2 = new DataRecordImpl(null);
        DataRecord dr3 = new DataRecordImpl(null);
        List<DataRecord> list = Arrays.asList(
                dr1,
                dr2,
                dr3);
        when(internal.iterator()).thenReturn(list.iterator());
        Iterator<DataRecord> iterator = dataSource.iterator();
        DataRecord[] expected = {dr1, dr2, dr3, dr1, dr2, dr3};
        for (DataRecord exp : expected) {
            assertTrue(iterator.hasNext());
            assertThat(iterator.next(), is(exp));
        }
        assertTrue(iterator.hasNext());
        dataSource.stop();
        assertFalse(iterator.hasNext());
    }

}
