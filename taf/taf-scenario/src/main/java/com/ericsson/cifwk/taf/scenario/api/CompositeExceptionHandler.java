package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.meta.API;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import static com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler.Outcome.PROPAGATE_EXCEPTION;

/**
 * Created by ethomev on 7/6/15.
 */
@API(Stable)
public class CompositeExceptionHandler implements ScenarioExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeExceptionHandler.class);
    private ScenarioExceptionHandler finalHandler;

    @VisibleForTesting
    protected List<ScenarioExceptionHandler> handlers = Lists.newArrayList();

    public CompositeExceptionHandler(List<ScenarioExceptionHandler> handlers, ScenarioExceptionHandler finalHandler) {
        this.handlers = handlers;
        this.finalHandler = finalHandler;
    }

    @Override
    public Outcome onException(Throwable e) {
        for (ScenarioExceptionHandler handler : handlers) {
            try {
                handler.onException(e);
            } catch (Throwable t) {
                LOG.info("{} threw {}", handler, t);
            }
        }
        if (finalHandler != null) {
            ScenarioExceptionHandler.Outcome outcome = finalHandler.onException(e);
            if (PROPAGATE_EXCEPTION.equals(outcome)) {
                return PROPAGATE_EXCEPTION;
            }
        }
        return Outcome.CONTINUE_FLOW;
    }
}
