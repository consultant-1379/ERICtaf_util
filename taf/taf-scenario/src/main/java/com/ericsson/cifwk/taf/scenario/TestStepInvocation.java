package com.ericsson.cifwk.taf.scenario;

import static com.ericsson.cifwk.meta.API.Quality.*;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.TestStepRunner;
import com.google.common.base.Optional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents how test step is executed.
 */
@API(Stable)
public interface TestStepInvocation {
    AtomicLong idGenerator = new AtomicLong();

    /**
     * Returns name of the test step
     *
     * @return name
     */
    String getName();

    /**
     * Returns uniq id of the test step
     * @return
     */
    Long getId();

    /**
     * Adds extra parameter to tes step instance.
     * @param key id of param
     * @param value actual value
     */
    void addParameter(String key, Object value);

    /**
     * Runs Test Step even if previous Test Step threw un-handled exception
     * Should be used for clean up in current flow
     */
    void alwaysRun();

    /**
     * @return Validates if Test Step should be run if previous Test Step threw un-handled exception
     */
    boolean isAlwaysRun();

    Optional<Object> run(TestStepRunner testStepRunner,
                         ScenarioExecutionContext scenarioExecutionContext,
                         LinkedHashMap<String, DataRecord> dataSourcesRecords,
                         TestContext context,
                         List<DataRecordTransformer> dataRecordTransformers) throws Exception;
}
