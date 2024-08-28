package com.ericsson.cifwk.taf.scenario.api;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

@API(Stable)
public interface ScenarioExceptionHandler {
    Logger log = LoggerFactory.getLogger(ScenarioExceptionHandler.class);

    enum Outcome {
        /**
         * Stop Flow execution and propagate exception to next level exception handler (i.e. Sub Flow → Flow → Scenario).
         * If there are no more handlers defined, exception will be propagated to main thread and test will fail.
         */
        PROPAGATE_EXCEPTION,

        /**
         * If handler handles exception and returns this constant, Flow execution will continue, and no other handlers will be called.
         */
        CONTINUE_FLOW,
    }

    /**
     * @see Outcome#PROPAGATE_EXCEPTION
     */
    ScenarioExceptionHandler PROPAGATE = new ScenarioExceptionHandler() {
        @Override
        public Outcome onException(Throwable e) {
            return Outcome.PROPAGATE_EXCEPTION;
        }
    };

    /**
     * @see Outcome#CONTINUE_FLOW
     */
    ScenarioExceptionHandler IGNORE = new ScenarioExceptionHandler() {
        @Override
        public Outcome onException(Throwable e) {
            return Outcome.CONTINUE_FLOW;
        }
    };

    /**
     * Catch and log the exception but does not fail the test
     */
    ScenarioExceptionHandler LOGONLY = new ScenarioExceptionHandler() {
            @Override
            public Outcome onException(Throwable e) {
                log.error("Exception caught during scenario execution", e);
                return Outcome.CONTINUE_FLOW;
            }
        };

    Outcome onException(Throwable e);
}
