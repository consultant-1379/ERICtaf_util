package com.ericsson.cifwk.taf.datasource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class TestDataSourceImpl implements TestDataSource<DataRecord> {

    private final List<DataRecord> data = Lists.newCopyOnWriteArrayList();

    public TestDataSourceImpl(Iterable<Map<String, Object>> data) {
        Iterable<DataRecord> iterable = Iterables.transform(data, new Function<Map<String, Object>, DataRecord>() {
            @Override
            public DataRecord apply(Map<String, Object> input) {
                return TestDataSourceFactory.createDataRecord(input);
            }
        });
        this.data.addAll(Lists.newArrayList(iterable));
    }

    @Override
    public DataRecordModifier addRecord() {
        DataRecord dataRecord = TestDataSourceFactory.createDataRecord();
        data.add(dataRecord);
        return new DataRecordModifierImpl(dataRecord);
    }

    @Override
    public void init(ConfigurationSource reader) {
    }

    @Override
    public Iterator<DataRecord> iterator() {
        return data.iterator();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public TestDataSource getSource() {
        return null;
    }
}
