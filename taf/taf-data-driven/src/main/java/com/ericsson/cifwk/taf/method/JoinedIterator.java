package com.ericsson.cifwk.taf.method;

import com.google.common.collect.AbstractIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class JoinedIterator<T> extends AbstractIterator<Collection<T>> {

    public static <T> JoinedIterator<T> join(List<Iterator<T>> iterators) {
        return new JoinedIterator<>(iterators);
    }

    List<Iterator<T>> iterators;

    JoinedIterator(List<Iterator<T>> iterators) {
        checkNotNull(iterators);
        this.iterators = iterators;
    }

    public boolean isEmpty() {
        return this.iterators.isEmpty();
    }

    @Override
    protected Collection<T> computeNext() {
        Collection<T> next = new ArrayList<>();
        if (iterators.isEmpty()) {
            return endOfData();
        }

        for (Iterator<T> iterator : iterators) {
            if (!iterator.hasNext()) {
                return endOfData();
            }
            next.add(iterator.next());
        }
        return next;
    }
}
