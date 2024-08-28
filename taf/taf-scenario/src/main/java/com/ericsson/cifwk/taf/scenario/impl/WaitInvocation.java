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

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.impl.teststepinvocation.ControlInvocation;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;

@API(Internal)
public class WaitInvocation implements ControlInvocation {
    private final long id = idGenerator.incrementAndGet();
    private String name;
    private long waitTime;
    private final boolean skipTestStep;

    private static final Logger logger = LoggerFactory.getLogger(WaitInvocation.class);

    public WaitInvocation(String name, long milliseconds, boolean skipTestStep) {
        this.name = name;
        this.waitTime = milliseconds;
        this.skipTestStep = skipTestStep;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void addParameter(String key, Object value) {
        throw new UnsupportedOperationException("Wait Invocation does not allow params");
    }

    @Override
    public void alwaysRun() {
        throw new UnsupportedOperationException("Wait Invocation does not support alwaysRun");
    }

    @Override
    public boolean isAlwaysRun() {
        return false;
    }

    @Override
    public Optional<Object> run(TestStepRunner testStepRunner,
                                ScenarioExecutionContext scenarioExecutionContext,
                                LinkedHashMap<String, DataRecord> dataSourcesRecords,
                                TestContext context,
                                List<DataRecordTransformer> dataRecordTransformers) throws Exception {
        try {
            if (!skipTestStep) {
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            logger.error("Wait method during flow execution was interrupted", e);
            Thread.currentThread().interrupt();
        }

        return Optional.absent();
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
