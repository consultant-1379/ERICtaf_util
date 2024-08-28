package com.ericsson.cifwk.taf.assertions;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Experimental;

@API(Experimental)
public interface SaveAssertion {

    void saveAssertTrue(String message, boolean condition);

    void saveAssertFalse(String message, boolean condition);

    void saveAssertEquals(String message, boolean expected, boolean actual);

    void saveAssertEquals(String message, byte expected, byte actual);

    void saveAssertEquals(String message, char expected, char actual);

    void saveAssertEquals(String message, double expected, double actual, double delta);

    void saveAssertEquals(String message, float expected, float actual, float delta);

    void saveAssertEquals(String message, int expected, int actual);

    void saveAssertEquals(String message, long expected, long actual);

    void saveAssertEquals(String message, Object expected, Object actual);

    void saveAssertEquals(String message, short expected, short actual);

    void saveAssertEquals(String message, String expected, String actual);

    void saveAssertNotNull(String message, Object object);

    void saveAssertNotSame(String message, Object expected, Object actual);

    void saveAssertNull(String message, Object object);

    void saveAssertSame(String message, Object expected, Object actual);

}
