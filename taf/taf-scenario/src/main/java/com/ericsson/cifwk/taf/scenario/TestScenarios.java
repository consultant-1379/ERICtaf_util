package com.ericsson.cifwk.taf.scenario;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordFieldReplacement;
import com.ericsson.cifwk.taf.scenario.api.CompositeExceptionHandlerBuilder;
import com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.api.DataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.api.TafDataSourceDefinitionBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestScenarioRunnerBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepDefinition;
import com.ericsson.cifwk.taf.scenario.api.TestStepFlowBuilder;
import com.ericsson.cifwk.taf.scenario.api.TestStepResult;
import com.ericsson.cifwk.taf.scenario.impl.IterableDataSourceDefinition;
import com.ericsson.cifwk.taf.scenario.impl.ParallelInvocation;
import com.ericsson.cifwk.taf.scenario.impl.RunnableInvocation;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioExecutionContext;
import com.ericsson.cifwk.taf.scenario.impl.SyncInvocation;
import com.ericsson.cifwk.taf.scenario.impl.SyncSingleInvocation;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.ericsson.cifwk.taf.scenario.api.TestStepDefinition.createTestStep;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Entry point for building test scenarios.
 */
@API(Stable)
public final class TestScenarios {

    public static final String CAUSE_VOID_RETURN_TYPE = "test step has void return type";
    public static final String CAUSE_STEP_RESULT_NOT_FOUND = "wrong test step name is provided";
    public static final String CAUSE_STEP_THREW_EXCEPTION = "test step threw exception before returning a value";

    private TestScenarios() {
    }

    /**
     * Creates a new scenario runner builder.
     *
     * @return runner buidler object
     */
    public static TestScenarioRunnerBuilder runner() {
        return new TestScenarioRunnerBuilder();
    }

    /**
     * Creates a new test step flow to be further appended to scenario.
     *
     * @param name name of the flow
     * @return flow builder object
     */
    public static TestStepFlowBuilder flow(String name) {
        Preconditions.checkNotNull(name);

        return new TestStepFlowBuilder(name);
    }

    /**
     * Provides value from previously executed Test Step in same Flow if Test Step method has return value.
     *
     * @param testStepName of Test Step
     */
    public static Supplier<Object> fromTestStepResult(final String testStepName) {
        return new Supplier<Object>() {
            @Override
            public Object get() {

                String errorMessage = "Test step '%s' didn't return value (possible causes: %s)";
                TestStepResult testStepResult = ServiceRegistry.getTestContextProvider().get().getAttribute(testStepName);
                checkState(testStepResult != null, errorMessage, testStepName, CAUSE_STEP_RESULT_NOT_FOUND);
                checkState(testStepResult.isSuccessful(), errorMessage, testStepName, CAUSE_STEP_THREW_EXCEPTION);

                Optional<Object> result = testStepResult.getResult();
                checkState(result.isPresent(), errorMessage, testStepName, CAUSE_VOID_RETURN_TYPE);
                return result.get();
            }
        };
    }


    /**
     * Creates new runnable scenario.
     *
     * @return scenario builder
     */
    public static TestScenarioBuilder scenario() {
        return scenario("unnamed");
    }

    public static TestScenarioBuilder scenario(String name) {
        Preconditions.checkNotNull(name);

        return new TestScenarioBuilder(name);
    }


    /**
     * @see com.ericsson.cifwk.taf.scenario.TestScenarios#dataDrivenScenario(java.lang.String)
     */
    public static DataDrivenTestScenarioBuilder dataDrivenScenario() {
        return dataDrivenScenario("unnamed");
    }

    /**
     * Creates new runnable scenario with Data Source, each Data Record will appear in reporting as new Test Case
     *
     * @param name name of Scenario
     * @return cifwk builder
     * @see com.ericsson.cifwk.taf.scenario.api.DataDrivenTestScenarioBuilder#withScenarioDataSources
     */
    public static DataDrivenTestScenarioBuilder dataDrivenScenario(String name) {
        if (isValidationRequired()) {
            validate(name);
        }
        return new DataDrivenTestScenarioBuilder(name);
    }

    private static boolean isValidationRequired() {
        try {
            Class.forName("com.ericsson.cifwk.taf.annotations.TestSuite");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void validate(String name) {
        Preconditions.checkNotNull(name);
        StackTraceElement[] stackTraces = new Exception().getStackTrace();
        Class annotationClassTestSuite = null;

        try {
            annotationClassTestSuite = Class.forName("com.ericsson.cifwk.taf.annotations.TestSuite");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (!isSuiteAnnotationFound(stackTraces, annotationClassTestSuite)) {
            throw new RuntimeException("Test methods containing Data Driven Scenarios should have @TestSuite annotation");
        }
    }

    private static boolean isSuiteAnnotationFound(final StackTraceElement[] stackTraces,
                                                  final Class annotationClassTestSuite) {
        for (StackTraceElement stackTrace : stackTraces) {
            String className = stackTrace.getClassName();
            if (className.startsWith("com.ericsson")) {
                try {
                    Class callerClass = Class.forName(className);
                    if (isTestSuiteAnnotationFound(annotationClassTestSuite, stackTrace, callerClass)) {
                        return true;
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    private static boolean isTestSuiteAnnotationFound(final Class annotationClassTestSuite, final StackTraceElement stackTrace, final Class callerClass) {
        String methodName = stackTrace.getMethodName();
        Method[] declaredMethods = callerClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                if (method.getAnnotation(annotationClassTestSuite) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a {@link com.ericsson.cifwk.taf.scenario.api.CompositeExceptionHandler} which allows to call multiple
     * ExceptionHandlers in case of Exception
     *
     * @return
     */
    public static CompositeExceptionHandlerBuilder compositeExceptionHandler() {
        return new CompositeExceptionHandlerBuilder(ScenarioExceptionHandler.PROPAGATE);
    }

    /**
     * @see {@link com.ericsson.cifwk.taf.scenario.TestScenarios#dataSource(String, Class)} )
     */
    public static TafDataSourceDefinitionBuilder<DataRecord> dataSource(String name) {
        return dataSource(name, DataRecord.class);
    }

    /**
     * References named data source.
     *
     * @param name           unique data source name
     * @param dataRecordType interface to be used for data record
     * @param <T>            class extends DataRecord
     * @return definition
     */
    public static <T extends DataRecord> TafDataSourceDefinitionBuilder<T> dataSource(String name, Class<T> dataRecordType) {
        Preconditions.checkNotNull(name);
        return new TafDataSourceDefinitionBuilder<>(name, dataRecordType);
    }

    /**
     * Constructs data source based on Iterable contents.
     *
     * @param name     name of the data source
     * @param iterable source of data
     * @return definition
     */
    public static DataSourceDefinitionBuilder iterable(final String name, final Iterable<Map<String, Object>> iterable) {
        Preconditions.checkNotNull(iterable);

        return new DataSourceDefinitionBuilder(name) {
            @Override
            public DataSourceDefinition<DataRecord> build() {
                return new IterableDataSourceDefinition(name, iterable);
            }
        };
    }

    /**
     * Creates a test step out of annotated method on provided object instance.
     * Method can contain any number of attributes annotated with @see{@link com.ericsson.cifwk.taf.annotations.Input}
     * so the framework would inject proper values at runtime.
     *
     * @param instance     object to invoke method on
     * @param testStepName test step id to locate
     * @return invocation object
     */
    public static TestStepDefinition annotatedMethod(Object instance, String testStepName) {
        Preconditions.checkNotNull(instance);
        Preconditions.checkNotNull(testStepName);
        return createTestStep(instance, testStepName);
    }

    /**
     * Creates a test step out of annotated method on operator provided by given provider.
     * Method can contain any number of attributes annotated with @see{@link com.ericsson.cifwk.taf.annotations.Input}
     * so the framework would inject proper values at runtime.
     *
     * @param provider     provider for object to invoke method on
     * @param testStepName test step id to locate
     * @return invocation object
     */
    public static TestStepDefinition annotatedMethod(Provider<?> provider, String testStepName) {
        Preconditions.checkNotNull(provider);
        Preconditions.checkNotNull(testStepName);
        return createTestStep(provider, testStepName);
    }


    /**
     * Creates a test step out of @see{@link java.lang.Runnable} instance.
     * You can not pass data driven attributes in this case.
     *
     * @param runnable runnable
     * @return invocation object
     */
    public static TestStepDefinition runnable(final Runnable runnable) {
        Preconditions.checkNotNull(runnable);

        RunnableInvocation invocation = new RunnableInvocation(runnable);
        return new TestStepDefinition(invocation);
    }

    private static TestStepDefinition wrapInSyncIfNecessary(boolean shouldSyncBeforeAfterRun, ParallelInvocation invocation) {
        if (shouldSyncBeforeAfterRun) {
            return new TestStepDefinition(
                    invocation,
                    new SyncInvocation("sync-after-" + invocation.getName())
            );
        } else {
            return new TestStepDefinition(invocation);
        }
    }

    /**
     * Pauses execution
     * May be used in beforeFlow/AfterFlow methods
     *
     * @param pause to wait
     * @param unit  time unit
     * @return runnable
     */
    public static Runnable pause(long pause, TimeUnit unit) {
        long pauseMs = MILLISECONDS.convert(pause, unit);
        return new Pause(pauseMs);
    }

    /**
     * Resets Data Source with given name in context of scenario
     *
     * @param dataSourceName name of Data Source
     * @return
     */
    public static SyncSingleInvocation resetDataSource(final String dataSourceName) {
        return new SyncSingleInvocation("Reset " + dataSourceName) {
            @Override
            protected void runOnce(ScenarioExecutionContext scenarioExecutionContext) {
                scenarioExecutionContext.getDataSourceContext().getDataSources().remove(dataSourceName);
            }
        };
    }

    /**
     * At runtime the datarecord field will be replaced by a value from the same record
     *
     * @param replacementFieldHeader header value of field to use as replacement
     */
    public static DataRecordFieldReplacement fromDataSourceField(String replacementFieldHeader) {
        return new DataRecordFieldReplacement(replacementFieldHeader);
    }

    /**
     * At runtime the datarecord field will be replaced by a field from a different datasource
     *
     * @param replacementDataSourceName the name of the datasource where the field is to be taken from
     * @param replacementFieldHeader    header value of field from alternative datasource to use as replacement
     */
    public static DataRecordFieldReplacement fromDataSourceField(String replacementDataSourceName, String replacementFieldHeader) {
        return new DataRecordFieldReplacement(replacementDataSourceName, replacementFieldHeader);
    }


    public static class Pause implements Runnable {
        private final long millis;

        protected Pause(long millis) {
            this.millis = millis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public long getMillis() {
            return millis;
        }
    }
}
