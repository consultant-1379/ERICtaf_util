/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.exception.FatalScenarioException;
import com.ericsson.cifwk.taf.scenario.impl.exception.SkipNextHandlerException;
import com.ericsson.cifwk.taf.scenario.impl.exception.ThrownByHandlerException;
import com.google.common.base.Throwables;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
class ExceptionManager {
    private final ScenarioExceptionHandler handler;
    private boolean skipLevel;

    ExceptionManager(ScenarioExceptionHandler handler, boolean skipLevel) {
        this.handler = handler;
        this.skipLevel = skipLevel;
    }

    void handle(Throwable exceptionToHandle) {
        exceptionToHandle = unwrap(exceptionToHandle);

        if (exceptionToHandle instanceof SkipNextHandlerException) {
            Throwables.propagate(exceptionToHandle.getCause());
        }

        if (exceptionToHandle instanceof FatalScenarioException) {
            Throwables.propagate(exceptionToHandle);
        }

        ScenarioExceptionHandler.Outcome outcome = callHandler(exceptionToHandle);

        if (ScenarioExceptionHandler.Outcome.PROPAGATE_EXCEPTION.equals(outcome)) {
            if (skipLevel) {
                throw new SkipNextHandlerException(exceptionToHandle);
            } else {
                Throwables.propagate(exceptionToHandle);
            }
        }
    }

    private ScenarioExceptionHandler.Outcome callHandler(Throwable exceptionToHandle) {
        ScenarioExceptionHandler.Outcome outcome;
        try {
            outcome = handler.onException(exceptionToHandle);
        } catch (Throwable thrownByHandler) {
            throw new ThrownByHandlerException(thrownByHandler);
        }
        return outcome;
    }

    private Throwable unwrap(Throwable exceptionToHandle) {
        if (exceptionToHandle instanceof ExecutionException || exceptionToHandle instanceof InvocationTargetException) {
            return exceptionToHandle.getCause();
        }

        return exceptionToHandle;
    }

    ScenarioExceptionHandler getScenarioExceptionHandler() {
        return handler;
    }
}
