package com.ericsson.cifwk.taf.handlers.netsim.exceptions;

/**
 * Exception thrown when validation of NetworkMap.json file has failed for Netsim Box
 */
public class InvalidNetworkMapException extends RuntimeException {
    public InvalidNetworkMapException() {
    }

    public InvalidNetworkMapException(String message) {
        super(message);
    }

    public InvalidNetworkMapException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNetworkMapException(Throwable cause) {
        super(cause);
    }
}
