package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioException;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated use {@link ScenarioExceptionHandler()} instead.
 */
@API(Deprecated)
@API.Since(2.29)
@Deprecated
public interface ExceptionHandler {

    Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Ignores exception completely as it never happened.
     */
    ExceptionHandler IGNORE = new ExceptionHandler() {
        @Override
        public void onException(Throwable e) {
        }
    };

    /**
     * Unwraps exception root cause, propagates it as RuntimeException and fails the test.
     */
    ExceptionHandler PROPAGATE = new ExceptionHandler() {
        @Override
        public void onException(Throwable e) {
            if (e instanceof ScenarioException) {
                Throwables.propagate(e);
            } else {
                Throwable rootCause = Throwables.getRootCause(e);
                Throwables.propagate(rootCause);
            }
        }
    };

    /**
     * Wraps exception into RuntimeException and fails the test.
     */
    ExceptionHandler PROPAGATE_FULL_STACK_TRACE = new ExceptionHandler() {
        @Override
        public void onException(Throwable e) {
            throw Throwables.propagate(e);
        }
    };

    /**
     * Catches and logs the exception but does not fail the test
     */
    ExceptionHandler LOGONLY = new ExceptionHandler() {
        @Override
        public void onException(Throwable e) {
            log.error("Exception caught during scenario execution", e);
        }
    };

    class ScenarioExceptionHandlerAdapter implements ScenarioExceptionHandler {
        private final ExceptionHandler handler;

        public ScenarioExceptionHandlerAdapter(ExceptionHandler handler) {
            this.handler = handler;
        }

        @Override
        public Outcome onException(Throwable e) {
            try {
                handler.onException(e);
            } catch (Throwable thrownByHandler) {
                if (thrownByHandler == e) {
                    return Outcome.PROPAGATE_EXCEPTION;
                }
                Throwables.propagate(thrownByHandler);
            }
            return Outcome.CONTINUE_FLOW;
        }
    }

    void onException(Throwable e);

}
