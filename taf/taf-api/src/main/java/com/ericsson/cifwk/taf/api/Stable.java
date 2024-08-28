package com.ericsson.cifwk.taf.api;

import com.ericsson.cifwk.meta.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Since;

/**
 * Use {@link API} with {@link com.ericsson.cifwk.meta.API.Quality} <code>Stable</code> instead.
 */
@Deprecated
@API(Deprecated)
@Since(2.29)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface Stable {
}
