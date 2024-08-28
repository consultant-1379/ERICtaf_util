package com.ericsson.de.scenariorx.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RxCompositeExceptionHandler extends RxExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RxCompositeExceptionHandler.class);

    private final List<RxExceptionHandler> handlers;
    private final RxExceptionHandler finalHandler;

    RxCompositeExceptionHandler(List<RxExceptionHandler> handlers,
                                RxExceptionHandler finalHandler) {
        this.handlers = handlers;
        this.finalHandler = finalHandler;
    }

    @Override
    public Outcome onException(Throwable e) {
        for (RxExceptionHandler handler : handlers) {
            try {
                handler.onException(e);
            } catch (Throwable t) {
                logger.info("{} has thrown {}", handler.getClass().getName(), t);
            }
        }
        return finalHandler.onException(e);
    }
}
