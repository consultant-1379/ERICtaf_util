package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import com.ericsson.cifwk.meta.API;

/**
 * This class Timeout while waiting response for a {@link Shell} .
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class TimeoutException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with an explanatory message.
     *
     * @param message An explanation of what went wrong.
     */
    public TimeoutException(String message) {
        super(message);
    }

    /**
     * Create a new exception with an explanatory message and a reference to an exception
     * that made us throw this one.
     *
     * @param message An explanation of what went wrong.
     * @param cause   Another exception that is the reason to throw this one.
     */
    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new exception with an explanatory message and a reference to an exception
     * that made us throw this one.
     *
     * @param cause Another exception that is the reason to throw this one.
     */
    public TimeoutException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
