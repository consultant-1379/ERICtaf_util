package com.ericsson.cifwk.taf.testapi.utils;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.TestMethodExecutionResult;

import java.lang.reflect.Method;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         01/03/2016
 */
@API(Internal)
public class TestResultHelper {

    private TestResultHelper() {}

    public static Method getMethod(TestMethodExecutionResult testResult) {
        return testResult.getTestMethod().getJavaMethod();
    }

    public static String getSourceUrl(TestMethodExecutionResult testResult) {
        Method method = getMethod(testResult);
        return method.getDeclaringClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }
}
