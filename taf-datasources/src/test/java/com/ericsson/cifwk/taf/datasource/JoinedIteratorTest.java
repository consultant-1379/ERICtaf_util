package com.ericsson.cifwk.taf.datasource;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JoinedIteratorTest {

    @Test
    public void testTransformedIterator() throws Exception {
        List<Iterator<String>> iterators = Arrays.asList(
                Arrays.asList("1", "4", "7", "10").iterator(),
                Arrays.asList("2", "5", "8").iterator(),
                Arrays.asList("3", "6", "9", "11").iterator()
        );
        JoinedIterator<String> result = JoinedIterator.join(iterators);
        assertThat(result.hasNext(), is(true));
        assertThat(result.next(), allOf(hasItem("1"), hasItem("2"), hasItem("3")));
        assertThat(result.hasNext(), is(true));
        assertThat(result.next(), allOf(hasItem("4"), hasItem("5"), hasItem("6")));
        assertThat(result.hasNext(), is(true));
        assertThat(result.next(), allOf(hasItem("7"), hasItem("8"), hasItem("9")));
        assertThat(result.hasNext(), is(false));

        iterators = Arrays.asList(
                Arrays.asList("1", "4", "6").iterator(),
                Arrays.asList("2", "5").iterator(),
                Arrays.asList("3").iterator()
        );
        result = JoinedIterator.join(iterators);
        assertThat(result.hasNext(), is(true));
        assertThat(result.next(), allOf(hasItem("1"), hasItem("2"), hasItem("3")));
        assertThat(result.hasNext(), is(false));

        iterators = Arrays.asList(
                Collections.<String>emptyIterator(),
                Arrays.asList("1", "4", "6").iterator(),
                Arrays.asList("2", "5").iterator(),
                Arrays.asList("3").iterator()
        );
        result = JoinedIterator.join(iterators);
        assertThat(result.hasNext(), is(false));
    }

}
