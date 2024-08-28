package com.ericsson.cifwk.taf;

import com.google.common.collect.Iterables;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Provides same functionality as {@link org.hamcrest.collection.IsIterableContainingInAnyOrder} plus prints actual
 * collection contents
 * @param <T>
 */
public class ReadableIsIterableContainingInAnyOrder<T> extends IsIterableContainingInAnyOrder<T> {
    public ReadableIsIterableContainingInAnyOrder(Collection<Matcher<? super T>> matchers) {
        super(matchers);
    }

    @Override
    protected boolean matchesSafely(Iterable<? extends T> items, Description mismatchDescription) {
        boolean result = super.matchesSafely(items, mismatchDescription);
        if (!result) {
            mismatchDescription.appendText("\n  Actual: " + Iterables.toString(items));
        }
        return result;
    }

    @Factory
    public static <T> Matcher<Iterable<? extends T>> containsInAnyOrder(T... items) {
        List<Matcher<? super T>> matchers = new ArrayList<>();
        for (T item : items) {
            matchers.add(equalTo(item));
        }

        return new ReadableIsIterableContainingInAnyOrder<>(matchers);
    }
}
