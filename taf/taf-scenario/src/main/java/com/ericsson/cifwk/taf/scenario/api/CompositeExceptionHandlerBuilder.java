package com.ericsson.cifwk.taf.scenario.api;

import com.ericsson.cifwk.meta.API;
import com.google.common.collect.Lists;

import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Created by ethomev on 7/6/15.
 */
@API(Stable)
public class CompositeExceptionHandlerBuilder {
    private List<ScenarioExceptionHandler> handlers = Lists.newArrayList();
    private ScenarioExceptionHandler finalHandler;

    public CompositeExceptionHandlerBuilder(ScenarioExceptionHandler finalHandler) {
        this.finalHandler = finalHandler;
    }

    /**
     * Add an {@link com.ericsson.cifwk.taf.scenario.api.ExceptionHandler}.
     * The order in which the handlers are added decides the priority
     * <p><b>NOTE:</b> if ExceptionHandler throws or propagates exception it will be logged and ignored
     * @param handler
     * @return
     * @deprecated use {@link #addExceptionHandler(ScenarioExceptionHandler)} instead.
     */
    @Deprecated
    @API(Deprecated)
    @API.Since(2.29)
    public CompositeExceptionHandlerBuilder addExceptionHandler(ExceptionHandler handler) {
        handlers.add(new ExceptionHandler.ScenarioExceptionHandlerAdapter(handler));
        return this;
    }

    /**
     * Add an {@link com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler}.
     * The order in which the handlers are added decides the priority
     * <p><b>NOTE:</b> if ScenarioExceptionHandler throws or propagates exception it will be logged and ignored
     * @param handler
     * @return
     */
    public CompositeExceptionHandlerBuilder addExceptionHandler(ScenarioExceptionHandler handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * Add an {@link com.ericsson.cifwk.taf.scenario.api.ExceptionHandler} as the final ExceptionHandler to be called
     * <p><b>NOTE:</b> if ExceptionHandler throws or propagates exception will be thrown back to the test execution
     * @param handler
     * @return
     * @deprecated use {@link #setFinalExceptionHandler(ScenarioExceptionHandler)} instead.
     */
    @Deprecated
    @API(Deprecated)
    @API.Since(2.29)
    public CompositeExceptionHandlerBuilder setFinalExceptionHandler(ExceptionHandler handler) {
        this.finalHandler = new ExceptionHandler.ScenarioExceptionHandlerAdapter(handler);
        return this;
    }

    /**
     * Add an {@link com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler} as the final ExceptionHandler to be called
     * <p><b>NOTE:</b> if ScenarioExceptionHandler throws or propagates exception will be thrown back to the test execution
     * @param handler
     * @return
     */
    public CompositeExceptionHandlerBuilder setFinalExceptionHandler(ScenarioExceptionHandler handler) {
        this.finalHandler = handler;
        return this;
    }

    /**
     * Build an instance of {@link com.ericsson.cifwk.taf.scenario.api.CompositeExceptionHandler}
     * @return
     */
    public CompositeExceptionHandler build() {
        return new CompositeExceptionHandler(handlers, finalHandler);
    }


}
