package com.ericsson.de.scenariorx.api;

import static com.ericsson.de.scenariorx.impl.FlowBuilder.ERROR_EXCEPTION_HANDLER_NULL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public class RxCompositeExceptionHandlerBuilder {

    private List<RxExceptionHandler> handlers = newArrayList();
    private RxExceptionHandler finalHandler = RxExceptionHandler.PROPAGATE;

    RxCompositeExceptionHandlerBuilder() {
    }

    /**
     * Add an {@link RxExceptionHandler} - handlers are executed in the same order in which they are added
     * <p><b>NOTE:</b> if ExceptionHandler throws or propagates exception then it will be logged and ignored
     */
    public RxCompositeExceptionHandlerBuilder addExceptionHandler(RxExceptionHandler handler) {
        checkNotNull(handler, ERROR_EXCEPTION_HANDLER_NULL);
        handlers.add(handler);
        return this;
    }

    /**
     * Add an {@link RxExceptionHandler} as the final ExceptionHandler to be called
     * <p><b>NOTE:</b> if ExceptionHandler throws or propagates exception then it will be re-thrown
     */
    public RxCompositeExceptionHandlerBuilder setFinalExceptionHandler(RxExceptionHandler handler) {
        checkNotNull(handler, ERROR_EXCEPTION_HANDLER_NULL);
        this.finalHandler = handler;
        return this;
    }

    /**
     * Build an instance of {@link RxCompositeExceptionHandler}
     */
    public RxCompositeExceptionHandler build() {
        return new RxCompositeExceptionHandler(handlers, finalHandler);
    }
}
