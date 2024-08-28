package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static java.lang.String.format;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.List;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.datasource.DataRecordModifier;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.method.Parameter;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.TestStepInvocation;
import com.ericsson.cifwk.taf.scenario.impl.InstanceMethodInvocation;
import com.ericsson.cifwk.taf.scenario.impl.MethodInvocation;
import com.ericsson.cifwk.taf.scenario.impl.ProviderMethodInvocation;
import com.ericsson.cifwk.taf.scenario.impl.RunnableInvocation;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

@API(Stable)
public final class TestStepDefinition {

    private final List<TestStepInvocation> callables;

    public TestStepDefinition(TestStepInvocation... callables) {
        this.callables = Lists.newArrayList(callables);
    }

    public List<TestStepInvocation> getInvocations() {
        return callables;
    }

    /**
     * Pass Object as a parameter to Test Step.
     *
     * Allows to add Supplier instead of Object. This means that Test Step will get object from Supplier at the moment of
     * Test Step execution. This is useful when parameter is result of not yet completed operation. For example result of
     * previous Test Step {@link TestScenarios#fromTestStepResult(java.lang.String)}
     *
     * @param key parameter name
     * @param value java.lang.Object value or com.google.common.base.Supplier will be resolved at moment of Test Step execution
     * @return
     */
    public TestStepDefinition withParameter(String key, Object value) {
        for (TestStepInvocation callable : callables) {
            callable.addParameter(key, value);
        }
        return this;
    }

    /**
     * If Test Step is method with return values, collect output to Data Source with defined name. If Test Step will be
     * executed multiple times (via Flow Data Source or multiple vUsers), resulting Data Source will contain all return
     * values.
     *
     * If Test Step return {@link DataRecordImpl#EMPTY}, result will be ignored.
     *
     * @param outputDataSource name of Data Source to collect output to
     * @return
     */
    public TestStepDefinition collectResultToDatasource(final String outputDataSource) {
        Preconditions.checkState(callables.get(0) instanceof MethodInvocation, "Currently its only possible to collect results " +
                " only for method invocation");
        final String stepName = callables.get(0).getName();

        final Supplier<Object> testStepResult = TestScenarios.fromTestStepResult(stepName);

        TestContext context = ServiceRegistry.getTestContextProvider().get();
        final TestDataSource<DataRecord> dataSource = context.dataSource(outputDataSource);

        callables.add(new RunnableInvocation(new Runnable() {
            @Override
            public void run() {
                Object attribute = testStepResult.get();
                if (attribute == DataRecordImpl.EMPTY) {
                    return;
                }
                DataRecordModifier record = dataSource.addRecord();
                if (attribute instanceof DataRecord) {
                    record.setFields((DataRecord) attribute);
                } else {
                    record.setField(stepName, attribute);
                }
            }
        }));

        return this;
    }

    public TestStepDefinition alwaysRun() {
        for (TestStepInvocation callable : callables) {
            callable.alwaysRun();
        }
        return this;
    }

    public static TestStepDefinition createTestStep(Object instance, String testStepName) {
        Method method = findAnnotatedMethod(instance, testStepName);
        TestStepInvocation callable = new InstanceMethodInvocation(instance, method, testStepName);
        checkParametersForAnnotations(method.getName(), Parameter.parametersFor(method));
        return new TestStepDefinition(callable);
    }

    public static TestStepDefinition createTestStep(Provider provider, String testStepName) {
        TestStepInvocation callable = new ProviderMethodInvocation(provider, testStepName);
        return new TestStepDefinition(callable);
    }

    public static Method findAnnotatedMethod(Object instance, String testStep) {
        Class<?> type = instance.getClass();
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(TestStep.class)) {
                TestStep annotation = method.getAnnotation(TestStep.class);
                if (annotation.id().equals(testStep)) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("Method annotated with @TestStep(id = \"" + testStep + "\") not found for class "
                + instance.getClass().getSimpleName() + "!");
    }

    public static void checkParametersForAnnotations(String methodName, List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotation(Input.class) == null && parameter.getAnnotation(Output.class) == null) {
                throw new IllegalArgumentException(format("Parameter with %s in method %s has no @Input/Output annotation", parameter.getType(),
                        methodName));
            }
        }
    }
}
