package com.ericsson.cifwk.taf.scenario.impl;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordFieldReplacement;
import com.ericsson.cifwk.taf.datasource.DataRecordTransformer;
import com.ericsson.cifwk.taf.datasource.TestDataSourceFactory;
import com.ericsson.cifwk.taf.method.Parameter;
import com.ericsson.cifwk.taf.method.ParameterTransformer;
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationUtils;
import com.ericsson.cifwk.taf.scenario.impl.exception.ScenarioDataSourceValidationException;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.meta.API.Quality.Internal;
import static com.ericsson.cifwk.taf.scenario.api.TestStepDefinition.checkParametersForAnnotations;
import static com.ericsson.cifwk.taf.scenario.api.TestStepDefinition.findAnnotatedMethod;
import static com.google.common.base.Optional.fromNullable;
import static java.lang.String.format;

/**
 *
 */
@API(Internal)
public abstract class MethodInvocation extends ListenableTestStepInvocation {
    private final long id = idGenerator.incrementAndGet();

    public static final String DEFAULT_INPUT_NAMESPACE = "parameter";

    private static Logger LOGGER = LoggerFactory.getLogger(MethodInvocation.class);

    private final String testStepName;

    private final Map<String, Object> parameters = Maps.newHashMap();

    private boolean alwaysRun = false;

    public MethodInvocation(String testStepName) {
        this.testStepName = testStepName;
    }

    protected abstract Object getInstance();

    protected abstract Method getMethod();

    @Override
    public String getName() {
        return testStepName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void addParameter(String key, Object value) {
        Preconditions.checkArgument(!parameters.containsKey(key), "Parameter is already set : " + key);
        parameters.put(key, value);
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
    public Optional<Object> run(TestStepRunner testStepRunner, ScenarioExecutionContext scenarioExecutionContext,
                                LinkedHashMap<String, DataRecord> dataSourcesRecords, TestContext context,
                                List<DataRecordTransformer> dataRecordTransformers) throws Exception {

        Object instance = getInstance();

        // checking annotated parameters
        Method method = findAnnotatedMethod(instance, testStepName);
        checkParametersForAnnotations(method.getName(), Parameter.parametersFor(method));

        LinkedHashMap<String, DataRecord> actualDataSourcesRecords = prepareDataRecordList(dataSourcesRecords,
                dataRecordTransformers);

        List<Parameter> parameters = Parameter.parametersFor(getMethod());
        Object[] args = ParameterTransformer.forMethod(parameters).apply(actualDataSourcesRecords);
        validateParameters(parameters, args);

        return runWithListener(scenarioExecutionContext.getListener(), args);
    }

    @Override
    protected Optional<Object> execute(Object... args) throws Exception {
        Object instance = getInstance();
        Object result = getMethod().invoke(instance, args);
        return fromNullable(result);
    }

    private void validateParameters(List<Parameter> parameters, Object[] values) {
        for (int i = 0; i < parameters.size(); i++) {
            validateParameter(parameters.get(i), values[i]);
        }
    }

    private void validateParameter(Parameter parameter, Object value) {
        if (value == null && isMandatory(parameter) && isMappedToDataSource(parameter)) {
            String cause = format("The value of the input/output parameter '%s' is null. ", parameter.getName());
            String recommendation = "Please provide not null value in your data source or mark method parameter as @OptionalValue. You can also define default value in the same annotation.";
            if (isStrictMode()) {
                String details = "Parameters validation relaxed mode was deprecated in previous TAF versions, strict mode is on - all test (step) parameters are mandatory by default. ";
                String errorMessage = cause + details + recommendation;
                throw new ScenarioDataSourceValidationException(new IllegalArgumentException(errorMessage));
            } else {
                String details = "In next versions of TAF parameters validation strict mode will be turned on by default and scenario exception will be thrown. ";
                String errorMessage = cause + details + recommendation;
                LOGGER.error(errorMessage);
            }
        }
    }

    private static boolean isMandatory(Parameter parameter) {
        return parameter.getAnnotation(OptionalValue.class) == null;
    }

    private static boolean isMappedToDataSource(Parameter parameter) {
        return parameter.getAnnotation(Input.class) != null || parameter.getAnnotation(Output.class) != null;
    }

    private static boolean isStrictMode() {
        return ScenarioConfigurationUtils.isDataSourceValidationStrict();
    }

    private LinkedHashMap<String, DataRecord> prepareDataRecordList(LinkedHashMap<String, DataRecord> dataSourcesRecords,
                                                                    List<DataRecordTransformer> dataRecordTransformers) {
        LinkedHashMap<String, DataRecord> actualDataSourcesRecords = new LinkedHashMap<>();
        actualDataSourcesRecords.put(DEFAULT_INPUT_NAMESPACE, TestDataSourceFactory.createDataRecord(resolveParameters(parameters, dataSourcesRecords)));
        actualDataSourcesRecords.putAll(dataSourcesRecords);

        DataRecordParameterTransformer dataRecordParameterTransformer = new DataRecordParameterTransformer(parameters);
        actualDataSourcesRecords = dataRecordParameterTransformer.apply(actualDataSourcesRecords, testStepName);
        dataRecordTransformers.removeAll(Collections.singleton(null));
        for (DataRecordTransformer dataRecordTransformer : dataRecordTransformers) {
            actualDataSourcesRecords = dataRecordTransformer.apply(actualDataSourcesRecords, testStepName);
        }
        return actualDataSourcesRecords;
    }

    private Map<String, ?> resolveParameters(Map<String, Object> parameters, final LinkedHashMap<String, DataRecord> datasourcesRecords) {
        return Maps.transformEntries(parameters, new Maps.EntryTransformer<String, Object, Object>() {
            @Override
            public Object transformEntry(String key, Object value) {
                if (value instanceof DataRecordFieldReplacement) {
                    return ((DataRecordFieldReplacement) value).findColumnValue(datasourcesRecords);
                }
                if (value instanceof Supplier) {
                    return ((Supplier) value).get();
                }
                return value;
            }
        });
    }

    @Override
    public String toString() {
        return getInstance().getClass().getSimpleName() + "." + getMethod().getName();
    }
}
