package com.ericsson.cifwk.taf.huntbugs;

import com.google.common.base.Preconditions;
import com.strobel.assembler.metadata.IAnnotationsProvider;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.ericsson.cifwk.taf.huntbugs.Annotations.hasAnnotation;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 21.10.2016
 */
public class TafDetectors {

    // TAF classes
    private static final String TEST_STEP_ANNOTATION = "com.ericsson.cifwk.taf.annotations.TestStep";
    private static final String OPERATOR_ANNOTATION = "com.ericsson.cifwk.taf.annotations.Operator";
    private static final String TAF_TEST_BASE_CLASS = "com.ericsson.cifwk.taf.TafTestBase";

    // TestNG classes
    private static final String TEST_NG_TEST_ANNOTATION = "org.testng.annotations.Test";

    // Java classes
    private static final String JAVAX_INJECT_ANNOTATION = "javax.inject.Inject";
    private static final String JAVAX_PROVIDER_CLASS = "javax.inject.Provider";
    private static final String JAVA_OBJECT_CLASS = "java.lang.Object";

    public static boolean isTestNgTest(MethodReference md) {
        return hasAnnotation(tryToResolve(md), TEST_NG_TEST_ANNOTATION);
    }

    public static boolean isTestNgTest(TypeReference td) {
        return hasAnnotation(tryToResolve(td), TEST_NG_TEST_ANNOTATION);
    }

    public static boolean isTestStepMethod(MethodReference md) {
        return hasAnnotation(tryToResolve(md), TEST_STEP_ANNOTATION);
    }

    public static boolean isTafTestBase(TypeReference type) {
        return TAF_TEST_BASE_CLASS.equals(type.getFullName());
    }

    public static boolean isInjectable(IAnnotationsProvider annotationsProvider) {
        return hasAnnotation(annotationsProvider, JAVAX_INJECT_ANNOTATION);
    }

    public static boolean isProvider(TypeReference type) {
        return JAVAX_PROVIDER_CLASS.equals(type.getFullName());
    }

    public static boolean isOperator(TypeReference type) {
        return hasAnnotation(tryToResolve(type), OPERATOR_ANNOTATION);
    }

    public static boolean isJavaObjectClass(TypeReference type) {
        return JAVA_OBJECT_CLASS.equals(type.getFullName());
    }

    public static TypeReference getTypeArgument(TypeReference type) {
        List<TypeReference> typeArguments = getTypeArguments(type);
        Preconditions.checkState(!typeArguments.isEmpty());
        return tryToResolve(typeArguments.iterator().next());
    }

    public static List<TypeReference> getTypeArguments(TypeReference type) {
        Preconditions.checkArgument(type.isGenericType());
        Preconditions.checkArgument("UnresolvedGenericType".equals(type.getClass().getSimpleName()));
        try {
            Method method = type.getClass().getMethod("getTypeArguments");
            method.setAccessible(true);
            return (List<TypeReference>) method.invoke(type);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw propagate(e);
        }
    }

    private static RuntimeException propagate(Exception e) {
        throw new RuntimeException("Was unable to get type arguments", e);
    }

    public static TypeReference tryToResolve(TypeReference type) {
        TypeDefinition resolvedType = type.resolve();
        if (resolvedType == null) {
            return type;
        }
        return resolvedType;
    }

    public static MethodDefinition tryToResolve(MethodDefinition method) {
        MethodDefinition resolvedMethod = method.resolve();
        if (resolvedMethod == null) {
            return method;
        }
        return resolvedMethod;
    }

    public static MethodReference tryToResolve(MethodReference method) {
        MethodDefinition resolvedMethod = method.resolve();
        if (resolvedMethod == null) {
            return method;
        }
        return resolvedMethod;
    }

}
