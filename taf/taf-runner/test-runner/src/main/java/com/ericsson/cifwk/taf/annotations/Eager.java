package com.ericsson.cifwk.taf.annotations;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.TYPE})
@Retention(value= RetentionPolicy.RUNTIME)
@Scope
public @interface Eager {
}
