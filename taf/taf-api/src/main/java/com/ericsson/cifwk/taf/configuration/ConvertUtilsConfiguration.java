package com.ericsson.cifwk.taf.configuration;

import static com.ericsson.cifwk.meta.API.Quality.*;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.StringConverter;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Optional;

@API(Internal)
public final class ConvertUtilsConfiguration {

    public static final char[] ALLOWED_CHARS = { '_', ' ', '/', '=', '.', '*', '(', ')', '\\', ':', ';', '|', '[', ']', '^' };

    private ConvertUtilsConfiguration() {
    }

    /**
     * This method configures conversion of arrays to allow special characters.
     *
     * @param chars
     *         This is an Optional of the characters to set. If it is empty the default is used, @value com.ericsson
     *         .cifwk.taf.configuration.ConvertUtilsConfiguration#ALLOWED_CHARS
     */
    public static void configure(final Optional<char[]> chars) {
        ArrayConverter converter = new ArrayConverter(String[].class, new StringConverter());

        converter.setAllowedChars(chars.isPresent() ? chars.get() : ALLOWED_CHARS);
        ConvertUtils.register(converter, String[].class);
    }
}
