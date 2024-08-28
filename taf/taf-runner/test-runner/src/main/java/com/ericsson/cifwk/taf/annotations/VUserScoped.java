package com.ericsson.cifwk.taf.annotations;

import com.ericsson.cifwk.meta.API;
import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Since;

/**
 * <p>Beans annotated with VuserScoped will be bound to execution context of one vuser.
 * This is required for multi-threaded tests to isolate vusers from each other,
 * but still retaining state inside beans.</p>
 *
 * @deprecated Operators should be stateless and contain only <code>@Inject</code>ed components as fields.
 * Therefore, such operators will be working in multi-threaded tests without the current annotation.
 * See https://confluence-nam.lmera.ericsson.se/display/TAF/TAF+Test+Ware+Design+Rules.
 */
@API(Deprecated)
@Since(2.29)
@Deprecated
@Target(value={ElementType.TYPE})
@Retention(value= RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface VUserScoped {
}
