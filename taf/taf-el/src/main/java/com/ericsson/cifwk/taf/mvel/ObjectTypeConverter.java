package com.ericsson.cifwk.taf.mvel;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.configuration.ConvertUtilsConfiguration;
import com.google.common.base.Optional;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 10/03/2016
 */
public class ObjectTypeConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypeConverter.class);

    private ObjectTypeConverter() {
    }

    public static <T> T convert(Object value, Class<T> type) {
        try {
            ConvertUtilsConfiguration.configure(Optional.of(new char[]{'_', ' ', '/'}));
            return (T) ConvertUtils.convert(value, type);
        } catch (ConversionException e){
            LOGGER.error("Error reading value from CSV. Value has been taken as NULL", e);
            return null;
        }
    }
}
