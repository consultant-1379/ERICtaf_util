package com.ericsson.cifwk.taf.scenario.impl.exception;

import com.ericsson.cifwk.taf.datasource.InvalidDataSourceException;

public class EmptyDataSourceException extends InvalidDataSourceException {

    public EmptyDataSourceException(String message) {
        super(message);
    }

    public EmptyDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
