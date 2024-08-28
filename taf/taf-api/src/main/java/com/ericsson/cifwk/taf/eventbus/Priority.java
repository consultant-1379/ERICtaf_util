package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.meta.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * <p>Annotation for event subscriber (see {@link Subscribe}) that defines its priority.</p>
 * <p>If this annotation is not set, the subscriber will be notified only after the prioritized ones.</p>
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 23/08/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(Internal)
public @interface Priority {

    /**
     * Priority of this subscription - should be a positive integer.
     */
    int value();

}
