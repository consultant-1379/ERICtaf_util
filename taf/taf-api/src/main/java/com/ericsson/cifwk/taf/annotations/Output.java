package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Marks test method or test step parameter as test output. It is typically used to provide values
 * for assertion operations after test action invocation.
 * It will be populated during runtime if method is annotated with @DataDriven.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={PARAMETER})
public @interface Output {
    String value();
}
