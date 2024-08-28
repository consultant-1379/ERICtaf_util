package com.ericsson.cifwk.taf.handlers.jms;

/**
 *
 */
public class JmsException extends RuntimeException {

    public JmsException(String message, Throwable cause) {
        super(message, cause);
    }

    public JmsException(String message) {
        super(message);
    }
}
