package com.ericsson.cifwk.taf.mvel;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.configuration.Configuration;
import com.ericsson.cifwk.taf.spi.TestContextProvider;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TafMVELProcessor {

    public static final String VUSER = "$VUSER";

    public static final String CONFIGURATION = "configuration";

    private static final Logger LOGGER = LoggerFactory.getLogger(TafMVELProcessor.class);

    private TafMVELProcessor() {
    }

    public static <T> T evalIfExpression(String string, Class<T> clazz) {
        if (isExpression(string)) {
            String expression = string.substring(2, string.length() - 1);
            return eval(expression, new HashMap<String, Object>(), clazz);
        } else {
            return ObjectTypeConverter.convert(string, clazz);
        }
    }

    public static boolean isExpression(String string) {
        return string.startsWith("${") && string.endsWith("}");
    }

    public static <T> T eval(String expression, Map<String, Object> additionalParams, Class<T> clazz) {
        try {
            Map<String, Object> allParams = prepareParams(additionalParams);
            return MVEL.eval(expression, allParams, clazz);
            // MVEL doesn't seem to have an easy way to ignore missing variables in the expression - so we need
            // to catch its exceptions and return false in case of appropriate MVEL exception
        } catch (PropertyAccessException e) {
            LOGGER.error(String.format("Expression '%s' evaluation finished with error: '%s'", expression, e.getLocalizedMessage()));
            throw Throwables.propagate(e);
        }
    }

    private static Map<String, Object> prepareParams(Map<String, Object> additionalParams) {
        Map<String, Object> allParams = Maps.newHashMap(additionalParams);

        TestContextProvider testContextProvider = ServiceRegistry.getTestContextProvider();
        if (testContextProvider.isContextInitialized()) {
            TestContext context = testContextProvider.get();
            allParams.putAll(context.getAllAttributes());
            allParams.put(VUSER, context.getVUser());
        }

        Configuration configuration = ServiceRegistry.getConfigurationProvider().get();
        allParams.put(CONFIGURATION, configuration.getProperties());

        return allParams;
    }
}
