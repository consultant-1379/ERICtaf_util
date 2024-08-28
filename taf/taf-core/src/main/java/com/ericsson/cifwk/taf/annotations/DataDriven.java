package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Mark your case Data Driven if you want to benefit from declarative test data binding.
 * <p/>
 * Data provider configuration is looked up in .properties file
 * example: dataprovider.myname.type = csv
 *
 * @see Input
 * @see Output
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {METHOD, TYPE, PARAMETER})
public @interface DataDriven {

    String name();
    String filter() default "";
}
