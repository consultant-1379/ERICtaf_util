package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Marks parameter of test step or parameter of test method as optional.
 * Default value can be provided via annotation parameter.
 *
 * @see OptionalValue#VALUE_NOT_DEFINED can be used in annotation processor
 * to check if default value was provided.
 *
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 22.04.2016
 *
 * @since 2.22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={FIELD, PARAMETER})
public @interface OptionalValue {

    String VALUE_NOT_DEFINED = "VALUE-NOT-DEFINED-dce0301b-0457-4f98-b955-f1a0078722e5";

    String value() default VALUE_NOT_DEFINED;

}
