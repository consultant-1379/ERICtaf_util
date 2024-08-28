package com.ericsson.cifwk.taf.datasource;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.io.IOException;
import java.util.Iterator;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 *
 */
@API(Internal)
public class FilteredDataSource<T extends DataRecord> extends UnmodifiableDataSource<T> {

    private final TestDataSource<T> original;
    private final Predicate<? super T> predicate;

    public FilteredDataSource(TestDataSource<T> original, Predicate<? super T> predicate) {
        this.original = original;
        this.predicate = predicate;
    }

    @Override
    public void init(ConfigurationSource reader) {
        original.init(reader);
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> iterator = original.iterator();
        return filtered(iterator, predicate);
    }

    private Iterator<T> filtered(final Iterator<T> iterator, final Predicate<? super T> filter) {
        if (filter != null) {
            return Iterators.filter(iterator, filter);
        }
        return iterator;
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
        return "FilteredDataSource{" +
                   "original=" + original +
                   '}';
    }
}
