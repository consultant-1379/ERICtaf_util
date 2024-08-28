package com.ericsson.cifwk.taf.scenario.impl;
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
import com.ericsson.cifwk.taf.api.Nullable;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationProvider;
import com.ericsson.cifwk.taf.scenario.impl.exception.FatalScenarioException;
import com.ericsson.cifwk.taf.scenario.impl.exception.SkipNextHandlerException;
import com.ericsson.cifwk.taf.scenario.impl.exception.ThrownByHandlerException;
import com.ericsson.cifwk.taf.scenario.spi.ScenarioConfiguration;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@API(Internal)
public class ThreadPoolBasedScenarioRunnerCore {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolBasedScenarioRunnerCore.class);
    private static final String EXECUTION_SERVICE_TERMINATION_TIMEOUT = "taf.scenario.exec.service.termination.timeout.seconds";
    private static final int EXECUTION_SERVICE_TERMINATION_DEFAULT_TIMEOUT = 30;

    @Deprecated
    private ScenarioExceptionHandler globalHandler;

    ThreadPoolBasedScenarioRunnerCore(@Deprecated @Nullable ScenarioExceptionHandler handler) {
        this.globalHandler = handler;
    }

    protected void runTasks(List<TestStepRunner> tasks, ScenarioExceptionHandler exceptionHandler) {
        ScheduledExecutorService threadPool = createExecutorService(tasks.size());
        invokeAllTasks(tasks, threadPool, exceptionHandler);
    }

    protected static void shutdownExecutorService(String name, ExecutorService executorService) {
        logger.debug("Shutting down executor service " + name + " " + executorService);
        try {
            executorService.shutdown();

            boolean successful = executorService.awaitTermination(getTimeoutInSeconds(), SECONDS);
            if (!successful) {
                logger.error("Some threads refused to terminate. Resort to force.");
                logger.error("Executor service " + name + " " + executorService);
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Executor service " + name + " " + executorService);
            logger.error("Exception while shutting down event bus executor: ", e);
        }
    }

    private static Integer getTimeoutInSeconds() {
        ScenarioConfiguration configuration = ScenarioConfigurationProvider.provide();
        return configuration.getProperty(EXECUTION_SERVICE_TERMINATION_TIMEOUT, EXECUTION_SERVICE_TERMINATION_DEFAULT_TIMEOUT);
    }

    private ScheduledExecutorService createExecutorService(int size) {
        return Executors.newScheduledThreadPool(size);
    }

    private void invokeAllTasks(List<TestStepRunner> tasks, ScheduledExecutorService threadPool, ScenarioExceptionHandler exceptionHandler) {
        ExceptionManager handler = new ExceptionManager(exceptionHandler, true);
        try {
            List<Future<Object>> futures = Lists.newArrayList();
            for (TestStepRunner task : tasks) {
                futures.add(threadPool.schedule(task, task.getStartDelay(), MILLISECONDS));
            }
            awaitCompletion(futures, handler);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        } catch (ExecutionException | SkipNextHandlerException e) {
            handleOrRethrow(e);
        } finally {
            shutdownExecutorService("TestStepRunner", threadPool);
        }
    }

    private void awaitCompletion(List<Future<Object>> futures, ExceptionManager exceptionHandler) throws InterruptedException, ExecutionException {
        for (Future<Object> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                exceptionHandler.handle(e);
            }
        }
    }

    @API(API.Quality.Deprecated)
    @API.Since(2.29)
    @Deprecated
    //backwards compatibility
    private void handleOrRethrow(Throwable e) {
        logger.debug("Exception caught during scenario execution", e);
        if (e instanceof ExecutionException) {
            e = e.getCause();
        }

        if (e instanceof FatalScenarioException || globalHandler == null) {
            Throwables.propagate(e);
        } else {
            if (e instanceof SkipNextHandlerException) {
                e = e.getCause();
            }

            try {
                ScenarioExceptionHandler.Outcome outcome = globalHandler.onException(e);
                if (ScenarioExceptionHandler.Outcome.PROPAGATE_EXCEPTION.equals(outcome)) {
                    Throwables.propagate(e);
                }
            } catch (Throwable newException) {
                throw new ThrownByHandlerException(newException);
            }
        }
    }
}
