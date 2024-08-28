package com.ericsson.de.scenariorx.impl;

import static com.ericsson.cifwk.taf.method.Parameter.parametersFor;
import static com.ericsson.cifwk.taf.method.ParameterTransformer.convertValue;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.ericsson.cifwk.taf.annotations.OptionalValue;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.method.Parameter;
import com.ericsson.de.scenariorx.api.RxDataRecord;
import com.ericsson.de.scenariorx.api.RxDataRecordWrapper;
import com.ericsson.de.scenariorx.api.RxTestStep;
import com.google.common.base.Optional;

@SuppressWarnings("WeakerAccess")
public class TafTestStep {

    private static final String ERROR_NO_PARAMETER_ANNOTATIONS = "Parameter with type '%s' in Test Step '%s' has no @Input/Output annotations";
    private static final String ERROR_PARAMETER_NOT_EXIST = "Parameter '%s' for test step '%s' does not exist";
    private static final String ERROR_PARAMETER_VALUE_NULL = "Parameter '%s' for test step '%s' value is null";

    public static RxTestStep getByName(Object instance, String testStepName) {
        Method method = findTestStepMethod(instance, testStepName);
        return new TafTestStepInvocation(instance, method, getName(method));
    }

    static String getName(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getAnnotation(TestStep.class).id();
    }

    public static TafTestStepInvocation getByMethod(Object instance, Method method, String name) {
        return new TafTestStepInvocation(instance, method, name);
    }

    private static Method findTestStepMethod(Object instance, String testStepName) {
        Method method = findAnnotatedMethod(instance, testStepName);
        for (Parameter parameter : parametersFor(method)) {
            checkArgument(parameter.getName() != null,
                    ERROR_NO_PARAMETER_ANNOTATIONS, parameter.getType().getSimpleName(), testStepName);
        }
        return method;
    }

    private static Method findAnnotatedMethod(Object instance, String testStep) {
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

    /**
     * Please note that {@code StackTraceFilter}
     * from core module depends on this class name
     *
     * @see StackTraceFilter.HasFrameworkClassName#FRAMEWORK_CLASSES
     */
    static class TafTestStepInvocation extends RxTestStep {
        private final TafDataRecordTransformer transformer = new TafDataRecordTransformer("none");
        private final Object instance;
        private final Method method;

        TafTestStepInvocation(Object instance, Method method, String name) {
            super(name);
            this.instance = instance;
            this.method = method;
        }

        @Override
        protected void validateParameter(String key) {
            super.validateParameter(key);
            validateParameterExists(key);
        }

        private void validateParameterExists(String key) {
            List<Parameter> parameters = parametersFor(method);
            for (Parameter parameter : parameters) {
                if (parameterMatches(parameter, key)) {
                    return;
                }
            }
            throw new IllegalArgumentException(format(ERROR_PARAMETER_NOT_EXIST, key, getName()));
        }

        private boolean parameterMatches(Parameter parameter, String key) {
            return parameter.getName().equals(key) || matchesDataSourceName(parameter, key);
        }

        private boolean matchesDataSourceName(Parameter parameter, String key) {
            boolean isDataRecord = RxDataRecord.class.isAssignableFrom(parameter.getType()) ||
                    DataRecord.class.isAssignableFrom(parameter.getType());
            return isDataRecord && key.startsWith(parameter.getName() + ".");
        }

        @Override
        protected Optional<Object> doRun(RxDataRecordWrapper dataRecord) throws Exception {
            TafTestContextBridge oldContext = TafTestContextBridge.initOldContext(dataRecord);

            List<Object> args = parseArguments(dataRecord);
            Object result = invokeMethodWith(args);

            return TafTestContextBridge.mergeOldContextAndReturnValue(oldContext, result, getName());
        }

        @Override
        protected RxTestStep copySelf() {
            return new TafTestStepInvocation(instance, method, name);
        }

        private Object invokeMethodWith(List<Object> args) throws Exception {
            try {
                Object result = method.invoke(instance, args.toArray());

                return transformer.convertDataRecordsToRx(result);
            } catch (InvocationTargetException e) {
                throw propagate(e.getTargetException());
            }
        }

        private List<Object> parseArguments(RxDataRecordWrapper dataRecord) {
            List<Parameter> parameters = parametersFor(method);
            List<Object> arguments = newArrayListWithCapacity(parameters.size());
            for (Parameter parameter : parameters) {
                Optional value = getArgumentValue(parameter, dataRecord);
                arguments.add(value.orNull());
            }
            return arguments;
        }

        private <T> Optional<T> getArgumentValue(Parameter<T> parameter, RxDataRecordWrapper dataRecord) {
            Optional<T> value = dataRecord.getFieldValue(parameter.getName(), parameter.getType());
            return value.isPresent() ? value : fromNullable(getDefaultValue(parameter));
        }

        private <T> T getDefaultValue(Parameter<T> parameter) {
            OptionalValue annotation = parameter.getAnnotation(OptionalValue.class);
            checkNotNull(annotation, ERROR_PARAMETER_VALUE_NULL, parameter.getName(), getName());

            String annotationValue = annotation.value();
            Object defaultValue = OptionalValue.VALUE_NOT_DEFINED.equals(annotationValue) ? null : annotationValue;
            return convertValue(defaultValue, parameter.getType());
        }
    }
}
