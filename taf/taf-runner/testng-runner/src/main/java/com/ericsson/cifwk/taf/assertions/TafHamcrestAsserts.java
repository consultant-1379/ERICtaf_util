package com.ericsson.cifwk.taf.assertions;

import com.ericsson.cifwk.meta.API;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
 * on assertion frameworks.
 */
@Deprecated
@API(API.Quality.Deprecated)
@API.Since(2.30)
public class TafHamcrestAsserts{

    static Logger logger = LoggerFactory.getLogger(TafHamcrestAsserts.class);
    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Simple utility to execute soft asserts
     */
    public static void softAssert(Object[]... args){
        try {
            assertNotNull(args);
        } catch (AssertionError e) {
            logger.error("Assertion failed", e);
        }
    }

    /**
     * Method to assert that two collections are equal
     * 
     * @param col1
     *            - collection 1
     * @param col2
     *            - collection 2
     */
    public static void assertEquals(Collection col1, Collection col2){
        assertThat(col1, is(col2));
    }

    /**
     * Method to assert that two collections are equal
     * 
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param col1
     *            collection 1
     * @param col2
     *            collection 2
     */
    public static void assertEquals(String reason, Collection col1,
            Collection col2){
        assertThat(reason, col1, is(col2));
    }

    /**
     * Method to assert that two collections are equal regardless of the order
     * of the elements in each collection
     * 
     * @param col1
     *            - collection 1
     * @param col2
     *            - collection 2
     */
    public static void assertEqualsOrderNotImportant(Collection col1,
            Collection col2){
        assertEqualsOrderNotImportant("", col1, col2);
    }

    /**
     * Method to assert that two collections are equal regardless of the order
     * of the elements in each collection
     * 
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param col1
     *            collection 1
     * @param col2
     *            collection 2
     */
    public static void assertEqualsOrderNotImportant(String reason,
            Collection col1, Collection col2){
        assertThat(reason, col1.size(), equalTo(col2.size()));
        assertThat(reason, col1.containsAll(col2));
        assertThat(reason, col2.containsAll(col1));
    }

    /**
     * Asserts that two collections contain the same elements in the same order.
     * If they do not, an AssertionError is thrown.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEqualsInOrder(Collection<?> actual,
            Collection<?> expected){
        assertEquals(actual, expected);
    }

    /**
     * Asserts that two collections contain the same elements in the same order.
     * If they do not, an AssertionError, with the given reason, is thrown.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEqualsInOrder(String reason, Collection<?> actual,
            Collection<?> expected){
        assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two arrays contain the same elements in the same order. If
     * they do not, an AssertionError is thrown.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEqualsInOrder(Object[] actual, Object[] expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two arrays contain the same elements in the same order. If
     * they do not, an AssertionError, with the given reason, is thrown.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEqualsInOrder(String reason, Object[] actual,
            Object[] expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two arrays contain the same elements in no particular order.
     * If they do not, an AssertionError is thrown.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEqualsNoOrder(Object[] actual, Object[] expected){
        assertEqualsNoOrder("", actual, expected);
    }

    /**
     * Asserts that two arrays contain the same elements in no particular order.
     * If they do not, an AssertionError, with the given reason, is thrown.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    //
    public static void assertEqualsNoOrder(String reason, Object[] actual,
            Object[] expected){
        assertThat(reason, actual, is(arrayWithSize(expected.length)));
        assertThat(reason, Arrays.asList(actual).containsAll(Arrays.asList(expected)));
        assertThat(reason, Arrays.asList(expected).containsAll(Arrays.asList(actual)));
    }

    /**
     * Asserts that two sets are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(Set<?> actual, Set<?> expected){
        assertEquals("", actual, expected);
    }

    /**
     * Asserts that two sets are equal.
     * 
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, Set<?> actual,
            Set<?> expected){
        assertThat(reason, actual.equals(expected));
    }

    /**
     * Asserts that two maps are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(Map<?, ?> actual, Map<?, ?> expected){
        assertEquals("", actual, expected);
    }

    /**
     * Asserts that two maps are equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, Map<?, ?> actual,
            Map<?, ?> expected){
        assertThat(reason, actual.entrySet().equals(expected.entrySet()));
    }

    /**
     * Asserts that two objects are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(Object actual, Object expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two objects are not equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, Object actual,
            Object expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two objects are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(Object actual, Object expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two objects are equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, Object actual,
            Object expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that an object isn't null.
     *
     * @param object
     *            Object to check for null
     */
    public static void assertNotNull(Object object){
        assertThat(object, notNullValue());
    }

    /**
     * Asserts that an object isn't null.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param object
     *            Object to check for null
     */
    public static void assertNotNull(String reason, Object object){
        assertThat(reason, object, notNullValue());
    }

    /**
     * Asserts that an object is null.
     *
     * @param object
     *            Object that is checked to be null
     */
    public static void assertNull(Object object){
        assertThat(object, nullValue());
    }

    /**
     * Asserts that an object is null.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param object
     *            Object that is checked to be null
     */
    public static void assertNull(String reason, Object object){
        assertThat(reason, object, nullValue());
    }

    /**
     * Asserts that two objects refer to the same object.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertSame(Object actual, Object expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two objects refer to the same object.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertSame(String reason, Object actual, Object expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two objects don't refer to the same object.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotSame(Object actual, Object expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two objects don't refer to the same object.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotSame(String reason, Object actual,
            Object expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that a condition is true.
     *
     * @param condition
     *            condition to be evaluated
     */
    public static void assertTrue(boolean condition){
        assertThat(condition, is(true));
    }

    /**
     * Asserts that a condition is true.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param condition
     *            condition to be evaluated
     */
    public static void assertTrue(String reason, boolean condition){
        assertThat(reason, condition, is(true));
    }

    /**
     * Asserts that a condition is false.
     *
     * @param condition
     *            condition to be evaluated
     */
    public static void assertFalse(boolean condition){
        assertThat(condition, is(false));
    }

    /**
     * Asserts that a condition is false.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param condition
     *            condition to be evaluated
     */
    public static void assertFalse(String reason, boolean condition){
        assertThat(reason, condition, is(false));
    }

    /**
     * Asserts that two booleans are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(boolean actual, boolean expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two booleans are equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, boolean actual,
            boolean expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two booleans are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(boolean actual, boolean expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two booleans are not equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, boolean actual,
            boolean expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two bytes are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(byte actual, byte expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two bytes are not equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, byte actual, byte expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two bytes are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(byte actual, byte expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two bytes are equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, byte expected, byte actual){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two byte arrays contain the same elements in the same order.
     * If they do not, an AssertionError is thrown.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    //
    public static void assertEqualsInOrder(final byte[] actual,
            final byte[] expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two byte arrays contain the same elements in the same order.
     * If they do not, an AssertionError is thrown.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    //
    public static void assertEqualsInOrder(String reason, final byte[] actual,
            final byte[] expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two chars are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(char actual, char expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two chars are equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, char actual, char expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two chars are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(char actual, char expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two chars are not equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, char actual, char expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two double are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(double actual, double expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two doubles are equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, double actual,
            double expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two ints are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(int actual, int expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two ints are not equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, int expected, int actual){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two ints are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(int actual, int expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two ints are equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, int actual, int expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two longs are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(long actual, long expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two longs are equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, long actual, long expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two longs are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(long actual, long expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two longs are not equal.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, long actual, long expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two shorts are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(short actual, short expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two shorts are equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, short actual, short expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two shorts are not equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(short actual, short expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two shorts are not equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, short actual,
            short expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Asserts that two Strings are equal.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String actual, String expected){
        assertThat(actual, is(expected));
    }

    /**
     * Asserts that two Strings are equal.
     * <p/>
     * Reason is logged on failure.
     *
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertEquals(String reason, String actual,
            String expected){
        assertThat(reason, actual, is(expected));
    }

    /**
     * Asserts that two strings are not equal.
     * 
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String actual, String expected){
        assertThat(actual, not(expected));
    }

    /**
     * Asserts that two strings are not equal.
     * 
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    public static void assertNotEquals(String reason, String actual,
            String expected){
        assertThat(reason, actual, not(expected));
    }

    /**
     * Local implementation of {@link org.hamcrest.MatcherAssert#assertThat(Object, Matcher)}
     * method.
     * @param actual Actual test result
     * @param matcher Matcher object which the actual result will be compared against
     */
    public static <T> void assertThat(T actual, Matcher<? super T> matcher){
        assertThat("", actual, matcher);
    }

    /**
     * Local implementation of {@link org.hamcrest.MatcherAssert#assertThat(String, Object, Matcher)}
     * method.
     * @param reason User defined String of message to be outputted as reason for the failure
     * @param actual Actual test result
     * @param matcher Matcher object which the actual result will be compared against
     */
    public static <T> void assertThat(String reason, T actual,
            Matcher<? super T> matcher){
        org.hamcrest.MatcherAssert.assertThat(reason, actual, matcher);
    }

    /**
     * Local implementation of {@link org.hamcrest.MatcherAssert#assertThat(String, Object, Matcher)}
     * method.
     * @param reason User defined String of message to be outputted as reason for the failure
     * @param assertion Boolean representation of any conditional or assertion check
     */
    public static void assertThat(String reason, boolean assertion){
        org.hamcrest.MatcherAssert.assertThat(reason, assertion);
    }

    /**
     * Asserts that two doubles or floats are equal within a positive delta.
     *
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     * @param delta
     *            the maximum delta between expected and actual for which both numbers are still considered equal
     */
    public static void assertEquals(double actual, double expected, double delta) {
        assertThat(actual, closeTo(expected, delta));
    }

    /**
     * Asserts that two doubles or floats are equal within a positive delta.
     *
     * @param reason
     *            user specified reason for failure, this will be printed if the assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     * @param delta
     *            the maximum delta between expected and actual for which both numbers are still considered equal
     */
    public static void assertEquals(String reason, double actual, double expected, double delta) {
        assertThat(reason, actual, closeTo(expected, delta));
    }
}