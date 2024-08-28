/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.assertions;

import com.ericsson.cifwk.meta.API;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TafAsserts extends SaveAsserts {

    /**
     * Simple utility to execute soft asserts
     */
    public static void softAssert(Object[]... args){
        TafHamcrestAsserts.softAssert(args);
    }

    /**
     * Method to assert that two collections are equal
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     *
     * @param col1
     *            - collection 1
     * @param col2
     *            - collection 2
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(Collection col1, Collection col2){
        TafHamcrestAsserts.assertEquals(col1, col2);
    }

    /**
     * Method to assert that two collections are equal
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param col1
     *            collection 1
     * @param col2
     *            collection 2
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, Collection col1,
            Collection col2){
        TafHamcrestAsserts.assertEquals(reason, col1, col2);
    }

    /**
     * Method to assert that two collections are equal regardless of the order
     * of the elements in each collection
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param col1
     *            - collection 1
     * @param col2
     *            - collection 2
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsOrderNotImportant(Collection col1,
            Collection col2){
        TafHamcrestAsserts.assertEqualsOrderNotImportant(col1, col2);
    }

    /**
     * Method to assert that two collections are equal regardless of the order
     * of the elements in each collection
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param col1
     *            collection 1
     * @param col2
     *            collection 2
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsOrderNotImportant(String reason,
            Collection col1, Collection col2){
        TafHamcrestAsserts.assertEqualsOrderNotImportant(reason, col1, col2);
    }

    /**
     * Asserts that two collections contain the same elements in the same order.
     * If they do not, an AssertionError is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsInOrder(Collection<?> actual,
            Collection<?> expected){
        TafHamcrestAsserts.assertEqualsInOrder(actual, expected);
    }

    /**
     * Asserts that two collections contain the same elements in the same order.
     * If they do not, an AssertionError, with the given reason, is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsInOrder(String reason, Collection<?> actual,
            Collection<?> expected){
        TafHamcrestAsserts.assertEqualsInOrder(reason, actual, expected);
    }

    /**
     * Asserts that two arrays contain the same elements in the same order. If
     * they do not, an AssertionError is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsInOrder(Object[] actual, Object[] expected){
        TafHamcrestAsserts.assertEqualsInOrder(actual, expected);
    }

    /**
     * Asserts that two arrays contain the same elements in the same order. If
     * they do not, an AssertionError, with the given reason, is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsInOrder(String reason, Object[] actual,
            Object[] expected){
        TafHamcrestAsserts.assertEqualsInOrder(reason, actual, expected);
    }

    /**
     * Asserts that two arrays contain the same elements in no particular order.
     * If they do not, an AssertionError is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsNoOrder(Object[] actual, Object[] expected){
        TafHamcrestAsserts.assertEqualsNoOrder(actual, expected);
    }

    /**
     * Asserts that two arrays contain the same elements in no particular order.
     * If they do not, an AssertionError, with the given reason, is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsNoOrder(String reason, Object[] actual,
            Object[] expected){
        TafHamcrestAsserts.assertEqualsNoOrder(reason, actual, expected);
    }

    /**
     * Asserts that two sets are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(Set<?> actual, Set<?> expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two sets are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, Set<?> actual,
            Set<?> expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two maps are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(Map<?, ?> actual, Map<?, ?> expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two maps are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, Map<?, ?> actual,
            Map<?, ?> expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two objects are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(Object actual, Object expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two objects are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, Object actual,
            Object expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two objects are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(Object actual, Object expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two objects are equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, Object actual,
            Object expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that an object isn't null.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param object
     *            Object to check for null
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotNull(Object object){
        TafHamcrestAsserts.assertNotNull(object);
    }

    /**
     * Asserts that an object isn't null.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param object
     *            Object to check for null
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotNull(String reason, Object object){
        TafHamcrestAsserts.assertNotNull(reason, object);
    }

    /**
     * Asserts that an object is null.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param object
     *            Object that is checked to be null
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNull(Object object){
        TafHamcrestAsserts.assertNull(object);
    }

    /**
     * Asserts that an object is null.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param object
     *            Object that is checked to be null
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNull(String reason, Object object){
        TafHamcrestAsserts.assertNull(reason, object);
    }

    /**
     * Asserts that two objects refer to the same object.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertSame(Object actual, Object expected){
        TafHamcrestAsserts.assertSame(actual, expected);
    }

    /**
     * Asserts that two objects refer to the same object.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertSame(String reason, Object actual, Object expected){
        TafHamcrestAsserts.assertSame(reason, actual, expected);
    }

    /**
     * Asserts that two objects don't refer to the same object.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotSame(Object actual, Object expected){
        TafHamcrestAsserts.assertNotSame(actual, expected);
    }

    /**
     * Asserts that two objects don't refer to the same object.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotSame(String reason, Object actual,
            Object expected){
        TafHamcrestAsserts.assertNotSame(reason, actual, expected);
    }

    /**
     * Asserts that a condition is true.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param condition
     *            condition to be evaluated
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertTrue(boolean condition){
        TafHamcrestAsserts.assertTrue(condition);
    }

    /**
     * Asserts that a condition is true.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param condition
     *            condition to be evaluated
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertTrue(String reason, boolean condition){
        TafHamcrestAsserts.assertTrue(reason, condition);
    }

    /**
     * Asserts that a condition is false.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param condition
     *            condition to be evaluated
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertFalse(boolean condition){
        TafHamcrestAsserts.assertFalse(condition);
    }

    /**
     * Asserts that a condition is false.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param condition
     *            condition to be evaluated
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertFalse(String reason, boolean condition){
        TafHamcrestAsserts.assertFalse(reason, condition);
    }

    /**
     * Asserts that two booleans are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(boolean actual, boolean expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two booleans are equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, boolean actual,
            boolean expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two booleans are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(boolean actual, boolean expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two booleans are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, boolean actual,
            boolean expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two bytes are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(byte actual, byte expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two bytes are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, byte actual, byte expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two bytes are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(byte actual, byte expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two bytes are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, byte expected, byte actual){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two byte arrays contain the same elements in the same order.
     * If they do not, an AssertionError is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsInOrder(final byte[] actual,
            final byte[] expected){
        TafHamcrestAsserts.assertEqualsInOrder(actual, expected);
    }

    /**
     * Asserts that two byte arrays contain the same elements in the same order.
     * If they do not, an AssertionError is thrown.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEqualsInOrder(String reason, final byte[] actual,
            final byte[] expected){
        TafHamcrestAsserts.assertEqualsInOrder(reason, actual, expected);
    }

    /**
     * Asserts that two chars are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(char actual, char expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two chars are equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, char actual, char expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two chars are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(char actual, char expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two chars are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, char actual, char expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two double are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(double actual, double expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two doubles are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, double actual,
            double expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two ints are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(int actual, int expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two ints are not equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, int expected, int actual){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two ints are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(int actual, int expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two ints are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, int actual, int expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two longs are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(long actual, long expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two longs are equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, long actual, long expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two longs are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(long actual, long expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two longs are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, long actual, long expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two shorts are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(short actual, short expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two shorts are equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, short actual, short expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two shorts are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(short actual, short expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two shorts are not equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, short actual,
            short expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Asserts that two Strings are equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String actual, String expected){
        TafHamcrestAsserts.assertEquals(actual, expected);
    }

    /**
     * Asserts that two Strings are equal.
     * <p/>
     * Reason is logged on failure.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, String actual,
            String expected){
        TafHamcrestAsserts.assertEquals(reason, actual, expected);
    }

    /**
     * Asserts that two strings are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String actual, String expected){
        TafHamcrestAsserts.assertNotEquals(actual, expected);
    }

    /**
     * Asserts that two strings are not equal.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason
     *            User specified reason for failure, this will be printed if the
     *            assertion fails
     * @param actual
     *            the actual value
     * @param expected
     *            the expected value
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotEquals(String reason, String actual,
            String expected){
        TafHamcrestAsserts.assertNotEquals(reason, actual, expected);
    }

    /**
     * Local implementation of {@link org.hamcrest.MatcherAssert#assertThat(Object, Matcher)}
     * method.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param actual Actual test result
     * @param matcher Matcher object which the actual result will be compared against
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static <T> void assertThat(T actual, Matcher<? super T> matcher){
        TafHamcrestAsserts.assertThat(actual, matcher);
    }

    /**
     * Local implementation of {@link org.hamcrest.MatcherAssert#assertThat(String, Object, Matcher)}
     * method.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason User defined String of message to be outputted as reason for the failure
     * @param actual Actual test result
     * @param matcher Matcher object which the actual result will be compared against
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static <T> void assertThat(String reason, T actual,
            Matcher<? super T> matcher){
        TafHamcrestAsserts.assertThat(reason, actual, matcher);
    }

    /**
     * Local implementation of {@link org.hamcrest.MatcherAssert#assertThat(String, Object, Matcher)}
     * method.
     * @deprecated please see https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html for our recommendations
     * on assertion frameworks.
     * @param reason User defined String of message to be outputted as reason for the failure
     * @param assertion Boolean representation of any conditional or assertion check
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertThat(String reason, boolean assertion){
        TafHamcrestAsserts.assertThat(reason, assertion);    
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
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(double actual, double expected, double delta) {
        TafHamcrestAsserts.assertEquals(actual, expected, delta);
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
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String reason, double actual, double expected, double delta) {
        TafHamcrestAsserts.assertEquals(reason, actual, expected, delta);
    }

    /**
     * Fails a test.
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void fail() {
        throw new AssertionError();
    }

    /**
     * Fails a test with message.
     *
     * @param message
     *        string the will be logged on failure
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void fail(String message) {
        throw new AssertionError(message);
    }

    /**
     * Get the current test case name.
     *
     * @return The name of current test case.
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static String getCurTcName() {
        return "UNKNOWN";
    }

    /**
     * Sleeps the specified number of seconds.
     *
     * @param time
     *        seconds to sleep.
     */
    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public void sleep(long time) {
        try {
            LOG.info("Sleeping for " + time + " seconds...");
            Thread.sleep(time * 1000);
        } catch (java.lang.InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertTrue(String type, String message, boolean condition) {
        assertTrue(message, condition);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertFalse(String type, String message, boolean condition) {
        assertFalse(message, condition);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, boolean expected, boolean actual) {
        assertEquals(message, actual, expected);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, byte expected, byte actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, char expected, char actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, double expected, double actual,
                                    double delta) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, float expected, float actual,
                                    float delta) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, int expected, int actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, long expected, long actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, Object expected, Object actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, short expected, short actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertEquals(String type, String message, String expected, String actual) {
        assertEquals(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotNull(String type, String message, Object object) {
        assertNotNull(message, object);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNotSame(String type, String message, Object expected, Object actual) {
        assertNotSame(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertNull(String type, String message, Object object) {
        assertNull(message, object);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void assertSame(String type, String message, Object expected, Object actual) {
        assertSame(message, expected, actual);
    }

    @Deprecated
    @API(API.Quality.Deprecated)
    @API.Since(2.30)
    public static void fail(String type, String message) {
        fail(message);
    }
}
