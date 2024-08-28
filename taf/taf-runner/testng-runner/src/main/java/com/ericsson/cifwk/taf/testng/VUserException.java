package com.ericsson.cifwk.taf.testng;

public class VUserException extends RuntimeException {

    public VUserException(Throwable cause) {
        super(cause);
    }

    public VUserException(String message, Throwable cause) {
        super(message, cause);
    }

}
