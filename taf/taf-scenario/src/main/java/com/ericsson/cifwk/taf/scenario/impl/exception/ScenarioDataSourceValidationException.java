package com.ericsson.cifwk.taf.scenario.impl.exception;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;

/**
 * Is thrown during test case or test step method parameters validation.
 *
 * E.g. values for mandatory parameter is not provided by data source.
 *
 * Exception marked as Internal intentionally. Exception shouldn't be handled by client, instead code logic should be fixed
 * so exception is not thrown.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 02.05.2016
 */
@API(Internal)
public class ScenarioDataSourceValidationException extends ScenarioException {
    public ScenarioDataSourceValidationException(Throwable cause) {
        super(cause);
    }
}
