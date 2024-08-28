package com.ericsson.cifwk.taf.scenario;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */


import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.abs;

public class CustomMatchers {
    public static  <T> Matcher<Iterable<? extends T>> allElementsAreSame() {
       return new TypeSafeMatcher<Iterable<? extends T>>() {
           @Override
           protected boolean matchesSafely(Iterable<? extends T> iterable) {
               Iterator<? extends T> iterator = iterable.iterator();
               if (!iterator.hasNext()) {
                   return true;
               }

               T first = iterator.next();

               while (iterator.hasNext()) {
                   if (!first.equals(iterator.next())) {
                        return false;
                   }
               }

               return true;
           }

           @Override
          public void describeTo(final Description description) {
             description.appendText("All collection elements should be the same.");
          }
       };
    }

    public static  <T> Matcher<Iterable<? extends T>> allElementsAreDifferent() {
       return new TypeSafeMatcher<Iterable<? extends T>>() {
           @Override
           protected boolean matchesSafely(Iterable<? extends T> iterable) {
               Iterator<? extends T> iterator = iterable.iterator();
               if (!iterator.hasNext()) {
                   return true;
               }

               Set<T> checked = Sets.newHashSet();

               while (iterator.hasNext()) {
                   T next = iterator.next();
                   boolean added = checked.add(next);
                   if (!added) {
                       return false;
                   }
               }

               return true;
           }

           @Override
          public void describeTo(final Description description) {
             description.appendText("All collection elements should be different.");
          }
       };
    }

    public static <T> Matcher<Iterable<? extends Number>> differenceIsGreaterThanOrEqualTo(final Number delta) {
        return new TypeSafeMatcher<Iterable<? extends Number>>() {
            @Override
            protected boolean matchesSafely(Iterable<? extends Number> iterable) {
                Number prevNumber = Long.MAX_VALUE;
                for (Number number : iterable) {
                    if (abs(number.longValue() - prevNumber.longValue()) < delta.longValue()) {
                        return false;
                    }
                    prevNumber = number;
                }

                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Difference between collection elements should be more or equal to " + delta);
            }
        };
    }

    public static <T> Matcher<Iterable<? extends Number>> differenceIsLessThanOrEqualTo(final Number delta) {
        return new TypeSafeMatcher<Iterable<? extends Number>>() {
            @Override
            protected boolean matchesSafely(Iterable<? extends Number> iterable) {
                Iterator<? extends Number> iterator = iterable.iterator();
                if (!iterator.hasNext()) {
                    return true;
                }

                Number prevNumber = iterator.next();
                while (iterator.hasNext()) {
                    Number number = iterator.next();
                    if (abs(number.longValue() - prevNumber.longValue()) > delta.longValue()) {
                        return false;
                    }
                    prevNumber = number;

                }

                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Difference between collection elements should be less or equal to " + delta);
            }
        };
    }

    public static <E> Matcher<Iterable<? extends E>> contains(final E... expected) {
        return new TypeSafeMatcher<Iterable<? extends E>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(Arrays.toString(expected));
            }

            @Override
            protected boolean matchesSafely(Iterable<? extends E> actual) {
                Iterator<? extends E> iterator = actual.iterator();
                for (E e : expected) {
                    if (!iterator.hasNext() || !Objects.equals(e, iterator.next())) {
                        return false;
                    }
                }

                return !iterator.hasNext();
            }

            @Override
            protected void describeMismatchSafely(Iterable<? extends E> actual, Description mismatchDescription) {
                mismatchDescription.appendText(Iterables.toString(actual));
            }
        };
    }
}
