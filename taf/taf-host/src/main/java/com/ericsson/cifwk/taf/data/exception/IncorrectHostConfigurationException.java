package com.ericsson.cifwk.taf.data.exception;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public class IncorrectHostConfigurationException extends RuntimeException {

    public IncorrectHostConfigurationException(String message) {
        super(message);
    }

    public IncorrectHostConfigurationException(Throwable cause) {
        super(cause);
    }

    public IncorrectHostConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectHostConfigurationException(String message, Throwable cause,
                                               boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
