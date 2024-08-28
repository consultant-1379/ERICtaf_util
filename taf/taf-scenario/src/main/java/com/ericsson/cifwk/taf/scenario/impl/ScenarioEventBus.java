/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Preconditions.checkState;

@API(Internal)
public class ScenarioEventBus {
    public static final int EVENT_BUS_THREAD_POOL_SIZE = 1;

    private static final Logger logger = LoggerFactory
            .getLogger(ScenarioEventBus.class);

    private final ExecutorService executorService;
    private final AsyncEventBus asyncEventBus;

    private final EventBus syncEventBus;
    private final Long scenarioId;

    protected ScenarioEventBus(Long scenarioId, EventBus syncEventBus, AsyncEventBus asyncEventBus, ExecutorService executorService) {
        this.executorService = executorService;
        this.asyncEventBus = asyncEventBus;
        this.syncEventBus = syncEventBus;
        this.scenarioId = scenarioId;

    }

    static ScenarioEventBus getInstance(Long scenarioId) {
        ExecutorService executorService = Executors.newFixedThreadPool(EVENT_BUS_THREAD_POOL_SIZE, new ThreadFactoryBuilder()
                .setNameFormat("EventBus-" + scenarioId + "-%d")
                .setDaemon(true)
                .build());
        logger.debug("Created executor service for event bus " + executorService);

        SubscriberExceptionHandler exceptionHandler = new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                logger.error("Exception in Event Bus Listener", exception);
            }
        };
        AsyncEventBus asyncEventBus = new AsyncEventBus(executorService, exceptionHandler);
        EventBus syncEventBus = new EventBus(exceptionHandler);

        return new ScenarioEventBus(scenarioId, syncEventBus, asyncEventBus, executorService);
    }

    public void shutdown() {
        waitUntilCurrentRunningTaskFinishes(executorService);
        ThreadPoolBasedScenarioRunnerCore.shutdownExecutorService("EventBus-" + scenarioId, executorService);
    }

    public void post(Object event) {
        asyncEventBus.post(event);
        syncEventBus.post(event);
    }

    /**
     * Subscriber will be synchroniously called in same thread where event happened.
     *
     * @param subscriber
     */
    public void registerSync(Object subscriber) {
        syncEventBus.register(subscriber);
    }

    /**
     * All subscribers will be asynchroniously called in separate thread
     * This means that during execution of subscriber state of scenario may move forward, context and thread locals are not available
     *
     * @param subscriber
     */
    public void registerAsync(Object subscriber) {
        asyncEventBus.register(subscriber);
    }

    /**
     * Submits empty task to executor and waits for its completion. Because ExecutorService has single thread, if any task
     * is already running, this will wait for this task completion. This is different from ExecutorService#awaitTermination()
     * because ExecutorService still accepting threads in queue therefore avoiding RejectedExecutionException
     *
     * @param executorService
     */
    private void waitUntilCurrentRunningTaskFinishes(ExecutorService executorService) {
        checkState(EVENT_BUS_THREAD_POOL_SIZE == 1, "Waiting will work only with single thread executor");
        try {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                }
            }).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Exception while waiting for ExecutorService current running task to finish", e);
        }
    }
}
