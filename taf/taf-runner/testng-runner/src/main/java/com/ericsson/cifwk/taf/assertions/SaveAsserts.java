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
import com.ericsson.cifwk.taf.legacy.LegacyTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;


@Deprecated
@API(API.Quality.Deprecated)
@API.Since(2.32) // please use according assertion framework, e.g. https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/recommendations/assertion-fwks.html
public class SaveAsserts extends LegacyTestBase implements SaveAssertion {

    static final Logger LOG = LoggerFactory.getLogger(SaveAsserts.class);
    public static final String ASSERTION_FAILED = "Assertion failed {}";

    private static ThreadLocal<ReusableSoftAssert> softAssertion = new InheritableThreadLocal<ReusableSoftAssert>() {
        @Override
        protected ReusableSoftAssert initialValue() {
            return new ReusableSoftAssert();
        }
    };

    /**
     * Method to assert that two collections are equal
     * <p/>
     * Reason is logged on failure and test case will not be aborted instead the
     * result is saved for evaluation on test case end.
     *
     * @param col1 - collection 1
     * @param col2 - collection 2
     */
    public static void saveAssertEquals(Collection col1, Collection col2) {
        try {
            org.hamcrest.MatcherAssert.assertThat(col1, is(col2));
        } catch (AssertionError ae) {
            softAssertion.get().fail(ae.toString());
            LOG.error(ASSERTION_FAILED, ae);
        }
    }

    /**
     * Method to assert that two collections are equal regardless of the order
     * of the elements in each collection
     * <p/>
     * Reason is logged on failure and test case will not be aborted instead the
     * result is saved for evaluation on test case end.
     *
     * @param col1 - collection 1
     * @param col2 - collection 2
     */
    public static void saveAssertEqualsOrderNotImportant(Collection col1,
                                                         Collection col2) {
        try {
            org.hamcrest.MatcherAssert.assertThat("", col1.containsAll(col2));
        } catch (AssertionError ae) {
            softAssertion.get().fail(ae.toString());
            LOG.error(ASSERTION_FAILED, ae);
        }
    }

    /**
     * Method to assert that an object is not null
     * <p/>
     * Reason is logged on failure and test case will not be aborted instead the
     * result is saved for evaluation on test case end.
     */
    public static void saveAssertNotNull(Object obj) {
        try {
            org.hamcrest.MatcherAssert.assertThat(obj, notNullValue());
        } catch (AssertionError ae) {
            softAssertion.get().fail(ae.toString());
            LOG.error(ASSERTION_FAILED, ae);
        }
    }

    /**
     * Method to assert that an object is null
     * <p/>
     * Reason is logged on failure and test case will not be aborted instead the
     * result is saved for evaluation on test case end.
     */
    public static void saveAssertIsNull(Object obj) {
        try {
            org.hamcrest.MatcherAssert.assertThat(obj, nullValue());
        } catch (AssertionError ae) {
            softAssertion.get().fail(ae.toString());
            LOG.error(ASSERTION_FAILED, ae);
        }
    }

    @Override
    public void saveAssertTrue(String message, boolean condition) {
        softAssertion.get().assertTrue(condition, message);
    }

    @Override
    public void saveAssertFalse(String message, boolean condition) {
        softAssertion.get().assertFalse(condition, message);
    }

    @Override
    public void saveAssertEquals(String message, boolean expected, boolean actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, byte expected, byte actual) {
        softAssertion.get().assertEquals( actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, char expected, char actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, double expected, double actual, double delta) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, float expected, float actual, float delta) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, int expected, int actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, long expected, long actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, Object expected, Object actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, short expected, short actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertEquals(String message, String expected, String actual) {
        softAssertion.get().assertEquals(actual, expected, message);
    }

    @Override
    public void saveAssertNotNull(String message, Object object) {
        softAssertion.get().assertNotNull(object, message);
    }

    @Override
    public void saveAssertNotSame(String message, Object expected, Object actual) {
        softAssertion.get().assertNotSame(actual, expected, message);
    }

    @Override
    public void saveAssertNull(String message, Object object) {
        softAssertion.get().assertNull(object, message);
    }

    @Override
    public void saveAssertSame(String message, Object expected, Object actual) {
        softAssertion.get().assertSame(actual, expected, message);
    }

    /**
     * Fails a test.
     * <p/>
     * Message is logged on failure and testcase will not be aborted instead the result is saved for
     * evaluation on testcase end.
     *
     * @param message string the will be logged on failure
     */
    public void saveFail(String message) {
        softAssertion.get().fail(message);
    }

    public void saveEventFail(String message) {
        saveFail(message);
    }

    /**
     * Throws exception if at least one assertion failed.
     */
    public static void assertAll() {
        ReusableSoftAssert softAssert = softAssertion.get();
        softAssert.assertAll();
    }

    @Deprecated
    public void saveAssertTrue(String type, String message, boolean condition) {
        saveAssertTrue(message, condition);
    }

    @Deprecated
    public void saveAssertFalse(String type, String message, boolean condition) {
        saveAssertFalse(message, condition);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, boolean expected, boolean actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, byte expected, byte actual) {
        saveAssertEquals(message, expected, actual);
    }


    @Deprecated
    public void saveAssertEquals(String type, String message, char expected, char actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, double expected, double actual,
                                 double delta) {
        saveAssertEquals(message, expected, actual, delta);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, float expected, float actual,
                                 float delta) {
        saveAssertEquals(message, expected, actual, delta);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, int expected, int actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, long expected, long actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, Object expected, Object actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, short expected, short actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertEquals(String type, String message, String expected, String actual) {
        saveAssertEquals(message, expected, actual);
    }

    @Deprecated
    public void saveAssertNotNull(String type, String message, Object object) {
        saveAssertNotNull(message, object);
    }

    @Deprecated
    public void saveAssertNotSame(String type, String message, Object expected, Object actual) {
        saveAssertNotSame(message, expected, actual);
    }

    @Deprecated
    public void saveAssertNull(String type, String message, Object object) {
        saveAssertNull(message, object);
    }

    @Deprecated
    public void saveAssertSame(String type, String message, Object expected, Object actual) {
        saveAssertSame(message, expected, actual);
    }

    @Deprecated
    public void saveFail(String type, String message) {
        saveFail(message);
    }

}
