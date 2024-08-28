package com.ericsson.cifwk.taf.datasource;

public class InvalidDataSourceException extends RuntimeException {

    public InvalidDataSourceException(String message) {
        super(message);
    }

    public InvalidDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
