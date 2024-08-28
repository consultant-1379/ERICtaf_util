/*
 * COPYRIGHT Ericsson (c) 2014.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.aspects;

import java.text.MessageFormat;
import java.util.Arrays;

public class AllureAspectUtils {

    private AllureAspectUtils() {
    }

    public static String renderTemplate(String templateContent, String methodName, Object instance, Object[] parameters) {
        String finalPattern = templateContent
                .replaceAll("\\{method\\}", String.valueOf(methodName))
                .replaceAll("\\{this\\}", String.valueOf(instance))
                // single quote prevents parameter variable inside (e.g.'{0}') to be rendered
                .replaceAll("'", "\"");

        return MessageFormat.format(finalPattern, normalizeParameters(parameters));
    }

    private static Object[] normalizeParameters(Object[] parameters) {
        int paramsCount = parameters == null ? 0 : parameters.length;
        Object[] results = new Object[paramsCount];
        for (int i = 0; i < paramsCount; i++) {
            results[i] = arrayToString(parameters[i]);
        }
        return results;
    }

    private static Object arrayToString(Object obj) {
        if (obj != null && obj.getClass().isArray()) {
            return Arrays.toString((Object[]) obj);
        }
        return obj;
    }
}
