package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.UnmodifiableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
@DataSourceOptions(shared = true)
public class SharedTafDataSource<T extends DataRecord> extends UnmodifiableDataSource<T> {
    private static final Logger logger = LoggerFactory.getLogger(SharedTafDataSource.class);

    Iterator<T> iterator;
    TestDataSource<T> dataSource;

    public SharedTafDataSource(TestDataSource<T> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init(ConfigurationSource reader) {
        // nothing to do
    }

    /**
     * Access to the iterator is synchronized as it is potentially accessed from multiple threads.
     *
     * @return iterator
     */
    @Override
    public Iterator<T> iterator() {
        if (this.iterator == null) {
            synchronized (this) {
                if (this.iterator == null) {
                    initIterator();
                }
            }
        }
        return this.iterator;
    }

    @Override
    public void close() throws IOException {
        dataSource.close();
    }

    private void initIterator() {
        synchronized (this) {
            final Iterator<T> recordIterator = dataSource.iterator();
            this.iterator = new UnmodifiableIterator<T>() {
                ThreadLocal<T> cache = new ThreadLocal<>();

                @Override
                synchronized public boolean hasNext() {
                    boolean hasNext = recordIterator.hasNext();
                    if (hasNext && cache.get() == null) {
                        cache.set(recordIterator.next());
                    }
                    return hasNext;
                }

                @Override
                synchronized public T next() {
                    T dataRecord = cache.get();
                    if (dataRecord == null) {
                        return recordIterator.next();
                    } else {
                        cache.remove();
                        return dataRecord;
                    }
                }
            };
        }
    }

    @Override
    public String toString() {
        return "SharedTafDataSource{" +
                "dataSource=" + dataSource +
                '}';
    }

    @Override
    public TestDataSource getSource() {
        return dataSource;
    }
}
