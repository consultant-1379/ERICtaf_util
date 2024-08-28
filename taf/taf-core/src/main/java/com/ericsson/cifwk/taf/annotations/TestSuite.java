package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Marks test method as test suite.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={METHOD})
public @interface TestSuite {

    String NULL = "null";

    String value() default NULL;

}
