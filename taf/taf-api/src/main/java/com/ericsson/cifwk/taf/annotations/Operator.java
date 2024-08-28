package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={TYPE})
public @interface Operator {
}
