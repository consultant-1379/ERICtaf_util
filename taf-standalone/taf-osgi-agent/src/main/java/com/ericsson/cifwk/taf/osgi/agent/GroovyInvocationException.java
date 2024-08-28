package com.ericsson.cifwk.taf.osgi.agent;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class GroovyInvocationException extends Exception {

    public GroovyInvocationException(String message) {
        super(message);
    }

    public GroovyInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroovyInvocationException(Throwable cause) {
        super(cause);
    }
}
