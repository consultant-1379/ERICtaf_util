package com.ericsson.cifwk.taf.eventbus;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.testapi.events.TestEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * <p>Annotation for a method that subscribes to a particular test event (extension of {@link TestEvent}).</p>
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 23/08/2016
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API(Internal)
public @interface Subscribe {

}
