package com.ericsson.cifwk.taf.method;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Parameter<T> {

    private final Class<T> type;
    private final Annotation[] annotations;

    public static List<Parameter> parametersFor(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<Parameter> parameters = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];
            Parameter parameter = new Parameter(parameterType, annotations);
            parameters.add(parameter);
        }
        return parameters;
    }

    public Parameter(Class<T> parameterType, Annotation... annotations) {
        this.type = parameterType;
        this.annotations = annotations;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        if (isAnnotationPresent(Input.class)) {
            return getAnnotation(Input.class).value();
        }
        if (isAnnotationPresent(Output.class)) {
            return getAnnotation(Output.class).value();
        }
        return null;
    }

    private boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isInstance(annotation)) {
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }
}
