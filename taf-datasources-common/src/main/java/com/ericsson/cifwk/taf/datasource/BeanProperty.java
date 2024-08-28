package com.ericsson.cifwk.taf.datasource;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import java.lang.reflect.Method;

import com.ericsson.cifwk.meta.API;

/**
 * Class to extract information about bean properties from method.
 * Should be used only for get and is methods.
 *
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/02/2016
 */
@API(Internal)
public class BeanProperty {

    private Method method;

    public BeanProperty(Method method) {
        this.method = method;
    }

    public String getName() {
        String property = method.getName().substring(3);
        return decamelize(property);
    }

    public String getName(boolean isGetMethod) {
        //Remove first 3 letters for getters, first 2 for 'is' methods
        String property = isGetMethod ? method.getName().substring(3) : method.getName().substring(2);
        return decamelize(property);
    }

    private String decamelize(String property) {
        return property.substring(0, 1).toLowerCase() + property.substring(1);
    }
}
