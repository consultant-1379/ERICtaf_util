package com.ericsson.cifwk.taf.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.configuration.configurations.TafConfigurationImpl;
import com.google.common.base.Strings;

/**
 * Utility class which replaces keys with the corresponding values in strings.
 * For example:
 * System.setProperty("myName","Tom")
 * "Hello there ${myName}"
 * will return "Hello there Tom"
 */
public final class KeySubstitutor {
    
    private static final Logger LOG = getLogger(KeySubstitutor.class);

    private KeySubstitutor(){}

    /**
     * Search the value for the existence of a key
     * @param value the value to check for a key in.
     * @return whether the value contains a key.
     */
    public static boolean stringValueContainsKeyIdentifier(final Object value) {
        if(value instanceof String) {
            final String stringValue = (String) ConvertUtils.convert(value, String.class);
            return !Strings.isNullOrEmpty(stringValue) && stringValue.matches(".*\\$\\{.+}.*");
        } else {
            return false;
        }
    }

    /**
     * substitute all occurrences of keys with values and return the substituted string
     * @param value the value to do the substituting in.
     * @return the string containing the substituted keys.
     */
    public static String replaceKeyWithValue(final Object value) {
        final String stringValue = (String) ConvertUtils.convert(value, String.class);
        final StrBuilder strBuilder = new StrBuilder(stringValue);
        final Configuration configuration = ServiceRegistry.getConfigurationProvider().get();
        LOG.info("Found key in [{}], searching for value in properties", value);
        final boolean keyReplacedWithValue = ((TafConfigurationImpl)configuration).getSubstitutor().replaceIn(strBuilder);
        final String substitutedValue = strBuilder.toString();
        if(keyReplacedWithValue){
            LOG.info("Key in data [{}] replaced by [{}]", value, substitutedValue);
        } else {
            LOG.error("Key in [{}] not found in properties", value);
        }
        return substitutedValue;
    }
}
