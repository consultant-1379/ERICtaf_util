package com.ericsson.cifwk.taf.data.exception;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

}
