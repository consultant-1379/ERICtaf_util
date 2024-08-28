package com.ericsson.cifwk.taf.scenario.impl.exception;
/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Exception thrown by exception handler. Will not be handled by other exception handlers and unwrapped in ScenarioRunner
 */
@API(Internal)
public class ThrownByHandlerException extends FatalScenarioException {
    public ThrownByHandlerException(Throwable cause) {
        super(cause);
    }
}
