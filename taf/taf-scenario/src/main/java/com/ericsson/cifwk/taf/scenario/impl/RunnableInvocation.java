package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.google.common.base.Optional;

import java.util.LinkedHashMap;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.google.common.base.Optional.absent;

/**
 *
 */
@API(Internal)
public final class RunnableInvocation extends ListenableTestStepInvocation {
    private final long id = idGenerator.incrementAndGet();

    public static final String NAME = "anonymous";

    private final Runnable runnable;
    private boolean alwaysRun = false;

    public RunnableInvocation(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Optional<Object> run(TestStepRunner testStepRunner, ScenarioExecutionContext scenarioExecutionContext,
                                LinkedHashMap<String, DataRecord> dataSourcesRecords, TestContext context,
                                List<DataRecordTransformer> dataRecordTransformers) throws Exception {
        return runWithListener(scenarioExecutionContext.getListener());
    }

    @Override
    protected Optional<Object> execute(Object... args) throws Exception {
        runnable.run();
        return absent();
    }

    @Override
    public void addParameter(String key, Object value) {
        throw new UnsupportedOperationException(
                "Runnable test step does not support parameters.");
    }

    @Override
    public void alwaysRun() {
        this.alwaysRun = true;
    }

    @Override
    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    @Override
    public String toString() {
        String simpleName = runnable.getClass().getSimpleName();
        return simpleName.equals("") ? NAME : simpleName;
    }
}
