package com.ericsson.cifwk.taf.method;

import java.lang.reflect.Method;

/**
 * Class to extract information about bean properties from method.
 * Should be used only for get/set and is methods.
 *
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/02/2016
 */
class BeanProperty {

    private Method method;

    public BeanProperty(Method method) {
        this.method = method;
    }

    public String getName() {
        String property = method.getName().substring(3);
        return decamelize(property);
    }

    public String getName(boolean isGetOrSetMethod) {
        String property = isGetOrSetMethod ? method.getName().substring(3) : method.getName().substring(2);
        return decamelize(property);
    }

    private String decamelize(String property) {
        return property.substring(0, 1).toLowerCase() + property.substring(1);
    }
}
