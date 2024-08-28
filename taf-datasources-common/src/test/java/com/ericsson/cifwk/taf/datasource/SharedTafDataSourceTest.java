package com.ericsson.cifwk.taf.datasource;
/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.SharedTafDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.datasource.TestDataSourceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

public class SharedTafDataSourceTest {
    public static final int ELEMENTS = 100_000;
    public static final int THREADS = 10_000;
    public static final int READS = 1000;
    public static final int POOL_SIZE = 100;
    private TestDataSource<DataRecord> dataSource;

    @Test
    public void testGetValueWithoutChecking() throws Exception {
        List<Map<String, Object>> data = getData(ELEMENTS);
        TestDataSourceImpl ds = new TestDataSourceImpl(data);
        SharedTafDataSource<DataRecord> sharedDs = new SharedTafDataSource<>(ds);

        assertThat(sharedDs.iterator().next().getFieldValue("key").toString(), equalTo("0"));
    }

    @Test
    public void testGetValueWithChecking() throws Exception {
        List<Map<String, Object>> data = getData(ELEMENTS);
        TestDataSourceImpl ds = new TestDataSourceImpl(data);
        SharedTafDataSource<DataRecord> sharedDs = new SharedTafDataSource<>(ds);

        assertThat(sharedDs.iterator().hasNext(), equalTo(Boolean.TRUE));
        assertThat(sharedDs.iterator().next().getFieldValue("key").toString(), equalTo("0"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetValueIfChecking() throws Exception {
        List<Map<String, Object>> data = getData(ELEMENTS);
        TestDataSourceImpl ds = new TestDataSourceImpl(data);
        SharedTafDataSource<DataRecord> sharedDs = new SharedTafDataSource<>(ds);

        Iterator<DataRecord> iterator = sharedDs.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }

        assertThat(iterator.hasNext(), equalTo(Boolean.FALSE));
        assertThat(sharedDs.iterator().hasNext(), equalTo(Boolean.FALSE));
        iterator.next();
    }

    @Test
    public void shouldNotThrowExceptionOnConcurrentRead() throws Exception {
        List<Map<String, Object>> data = getData(ELEMENTS);
        TestDataSourceImpl ds = new TestDataSourceImpl(data);
        SharedTafDataSource sharedDs = new SharedTafDataSource(ds);

        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        List<Future<?>> threads = Lists.newArrayList();
        for (int i = 0; i < THREADS; i++) {
            Runnable worker = new MockTestStepRunner(sharedDs);
            threads.add(executor.submit(worker));
        }
        for (Future<?> thread : threads) {
            thread.get(); //Throws exception if any
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    @Test
    public void shouldIterate() throws Exception {
        List<Map<String, Object>> data = getData(3);
        TestDataSourceImpl ds = new TestDataSourceImpl(data);
        SharedTafDataSource sharedDs = new SharedTafDataSource(ds);

        Iterator<DataRecord> iterator = sharedDs.iterator();
        assertTrue(iterator.hasNext());
        assertThat((Integer) iterator.next().getFieldValue("key"), equalTo(0));
        assertTrue(iterator.hasNext());
        assertThat((Integer) iterator.next().getFieldValue("key"), equalTo(1));
        assertTrue(iterator.hasNext());
        assertThat((Integer) iterator.next().getFieldValue("key"), equalTo(2));
        assertFalse(iterator.hasNext());
    }

    private List<Map<String, Object>> getData(int cnt) {
        List<Map<String, Object>> data = Lists.newArrayList();
        for (int i = 0; i < cnt; i++) {
            Map<String, Object> dataRecord = Maps.newHashMap();
            dataRecord.put("key", i);
            data.add(dataRecord);
        }
        return data;
    }

    public class MockTestStepRunner implements Runnable {
        private SharedTafDataSource ds;

        public MockTestStepRunner(SharedTafDataSource ds) {
            this.ds = ds;
        }

        @Override
        public void run() {
            for (int i = 0; i < READS; i++) {
                for (DataRecord d : (Iterable<DataRecord>) ds) {
                }
            }
        }
    }
}
