package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 6/15/2015
 */
@API(Internal)
public class ParametersManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParametersManager.class);

    private ParametersManager() {
        // hiding constructor
    }

    /**
     * Generate name for parameter.
     */
    protected static String getNameForParameter(String valueInAnnotation, List<String> methodParameterNames, int index) {
        if (!"".equals(valueInAnnotation)) {
            return valueInAnnotation;
        }

        if (index < methodParameterNames.size()) {
            return methodParameterNames.get(index);
        }

        return "arg" + index;
    }

    /**
     * Find {@link Input} or {@link Output} annotation value in a given array of annotations.
     */
    protected static String getParameterName(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Input) {
                return ((Input) annotation).value();
            }
            if (annotation instanceof Output) {
                return ((Output) annotation).value();
            }
        }
        return null;
    }

    /**
     * Returns true if can get method parameter names. Since java 8 you can get parameter names via
     * reflection if <code>-parameters</code> compiler argument specified. For more information
     * you can see documentation at
     * https://docs.oracle.com/javase/tutorial/reflect/member/methodparameterreflection.html .
     */
    protected static boolean canGetMethodParameterNames() { //NOSONAR
        try {
            Class.forName("java.lang.reflect.Parameter", false, ParametersManager.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) { // NOSONAR
            return false;
        }
    }

    /**
     * Returns method parameter names if can.
     *
     * @see #canGetMethodParameterNames()
     */
    protected static List<String> getMethodParameterNamesIfAvailable(Method testMethod) {
        List<String> result = Lists.newArrayList();

        if (!canGetMethodParameterNames()) {
            return result;
        }

        try {
            Object[] parameters = (Object[]) testMethod.getClass().getMethod("getParameters").invoke(testMethod);
            for (Object parameter : parameters) {
                result.add((String) parameter.getClass().getMethod("getName").invoke(parameter));
            }
            return result;
        } catch (Exception e) {
            String errorMessage = "Could not access parameter names via reflection. " +
                    "Falling back to default test method parameter names.";
            LOGGER.warn(errorMessage, e);
        }
        return result;
    }

    public static String getParametrizedName(final String testName, Object[] parameters) {
        // filtering parameters from TestCaseId (since it will already be present it test case name)
        Object[] filteredParameters = Collections2.filter(Arrays.asList(parameters), new Predicate<Object>(){
            @Override
            public boolean apply(Object input) {
                return !testName.equals(input);
            }
        }).toArray();

        String parametersString = Arrays.deepToString(filteredParameters);
        parametersString = removeEmptyParameters(parametersString);
        return testName.trim() + truncate(parametersString, 150);
    }

    public static String removeEmptyParameters(String nameWithParameters) {
        return nameWithParameters
                .replaceAll("\\[+[\\s,]*\\]+", "")
                .trim()
                .replace("[[", "[")
                .replace("]]", "]");
    }

    public static String truncate(String string, int maxSize) {
        final String suffix = "...";
        if (string.length() > maxSize) {
            return string.substring(0, maxSize - suffix.length()) + suffix;
        }
        return string;
    }

    protected static String getTestIdFromMethodParameters(Method method, Object[] parameters) {
        String newTestId = null;
        Annotation[][] paramsAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Annotation[] annotations = paramsAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(TestId.class)) {
                    Preconditions.checkNotNull(parameters[i], "Test Id parameter in data source wasn't set");
                    newTestId = parameters[i].toString();
                }
            }
        }
        return newTestId;
    }

}
