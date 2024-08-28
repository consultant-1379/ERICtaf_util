package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Marks test method or test step parameter as test input.
 * It will be populated during runtime if method is annotated with @DataDriven.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface Input {
    String value();
}
