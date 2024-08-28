package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

@Retention(RetentionPolicy.RUNTIME)
@Target(value={METHOD, PARAMETER})
public @interface TestId {

    String DEFAULT_TITLE = "no description provided";

    String DEFAULT_ID = "no test ID provided";

    String id() default DEFAULT_ID;

    String title() default DEFAULT_TITLE;

}
