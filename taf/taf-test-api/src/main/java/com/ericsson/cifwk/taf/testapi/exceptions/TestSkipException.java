package com.ericsson.cifwk.taf.testapi.exceptions;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Experimental;

/**
 * Thrown when test is skipped.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 17/11/2016
 */
@API(Experimental)
public class TestSkipException extends RuntimeException {

    public TestSkipException(String message) {
        super(message);
    }

}
