package com.ericsson.cifwk.taf.scenario.impl.datasource;

import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.scenario.impl.datasource.ProperlySharedScenarioDataSource.DataRecordSuppler;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class DataRecordSupplerTest {
    final Stack<String> stack = new Stack<>();

    @Before
    public void setUp() throws Exception {
        stack.clear();
    }

    @Test
    public void testDataRecordSupplier() throws Exception {
        final List<DataRecord> source = Arrays.asList(dataRecord("1"), dataRecord("2"), dataRecord("3"), dataRecord("4"), dataRecord("5"), dataRecord("6"));

        final DataRecordSuppler<DataRecord> dataRecordSuppler
                = new DataRecordSuppler<>(source.iterator());


        int nThreads = 1000;
        List<Callable<Object>> tasks = Lists.newArrayList();

        for (int i = 0; i < nThreads; i++) {
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Optional<DataRecord> optional = dataRecordSuppler.get();
                    if (optional.isPresent()) {
                        stack.push(optional.get().getFieldValue("value").toString());
                    }

                    return new Object();
                }
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        executor.invokeAll(tasks);
        executor.shutdown();

        assertThat(stack, hasSize(source.size()));
    }

    private DataRecord dataRecord(String s) {
        return new DataRecordImpl(ImmutableMap.of("value", s));
    }
}
