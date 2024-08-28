package com.ericsson.cifwk.taf.handlers.netsim;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NetSim command body descriptor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { TYPE, METHOD })
public @interface Cmd {
    int index() default -1;

    String value();

    boolean quoted() default true;
    
    boolean requiresAssignment() default true;
}
