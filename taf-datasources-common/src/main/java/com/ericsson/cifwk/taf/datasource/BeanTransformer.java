/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.cifwk.meta.API;
import com.google.common.base.Throwables;

@API(Stable)
@SuppressWarnings("ALL")
public class BeanTransformer {

    /**
     * Transform a bean into a DataRecord
     * Takes the properties in the bean object and create a DataRecord of a specific type containing these properties
     *
     * @param dataRecordType
     * @param beanObject
     * @return
     */
    public static <T extends DataRecord> T transformTo(Class<T> dataRecordType, Object beanObject) {
        Method[] methods = beanObject.getClass().getDeclaredMethods();
        String className = beanObject.getClass().getSimpleName();
        Map<String, Object> beanProperties = new HashMap<String, Object>();
        for (Method method : methods) {
            if (method.getName().startsWith("get")) {
                try {
                    beanProperties.put(new BeanProperty(method).getName(), method.invoke(beanObject));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
        DataRecord dr = TestDataSourceFactory.createDataRecord(beanProperties);
        return DataRecordProxyFactory.createProxy(dr, dataRecordType);
    }
}
