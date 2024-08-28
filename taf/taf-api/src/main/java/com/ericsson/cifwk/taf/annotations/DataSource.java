package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * This annotation is used to mark the method to be used by TAF Data Source functionality.
 * Such method should return Iterable<Map<String, Object>> and accept no parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={METHOD})
public @interface DataSource {
}
