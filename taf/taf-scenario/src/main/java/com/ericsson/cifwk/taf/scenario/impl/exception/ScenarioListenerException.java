package com.ericsson.cifwk.taf.scenario.impl.exception;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */

/**
 * Exception marked as Internal intentionally. Exception shouldn't be handled by client, instead code logic should be fixed
 * so exception is not thrown.
 */
@API(Internal)
public class ScenarioListenerException extends FatalScenarioException {
    public ScenarioListenerException(Throwable cause) {
        super(cause);
    }
}
