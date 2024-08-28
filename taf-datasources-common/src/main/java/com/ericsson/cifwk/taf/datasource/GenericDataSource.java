package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.AbstractIterator;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 *  DataSource which wraps original data source.
 *  Creates proxy of specified data record interface over each data record element.
 */
@API(Internal)
public class GenericDataSource<T extends DataRecord> implements TestDataSource<T> {

    private final TestDataSource<DataRecord> original;
    private Class<T> type;

    public GenericDataSource(TestDataSource<DataRecord> original, Class<T> type) {
        this.original = original;
        this.type = type;
    }

    @Override
    public void init(ConfigurationSource reader) {
        //nothing to do
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<DataRecord> iterator = original.iterator();
        if (!iterator.hasNext()) {
            return Collections.emptyIterator();
        }

        return new GenericRecordIterator<>(iterator, type);
    }

    @Override
    public DataRecordModifier addRecord() {
        return original.addRecord();
    }

    @Override
    public void close() throws IOException {
        original.close();
    }

    @Override
    public TestDataSource getSource() {
        return original;
    }

    @Override
    public String toString() {
        return "GenericDataSource{" +
                "original=" + original +
                '}';
    }

    private class GenericRecordIterator<T extends DataRecord> extends AbstractIterator<T> {
        private final Iterator<DataRecord> iterator;
        private final Class<T> type;

        public GenericRecordIterator(Iterator<DataRecord> iterator, Class<T> type) {
            this.iterator = iterator;
            this.type = type;
        }

        @Override
        protected T computeNext() {
            if (iterator.hasNext()) {
                DataRecord next = iterator.next();
                return DataRecordProxyFactory.createProxy(next, type);
            }
            return endOfData();
        }
    }

}
