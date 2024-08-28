package com.ericsson.cifwk.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

/**
 * Annotates public types within TAF to indicate their level of stability and how they are intended to be used.
 * <p>
 * If the annotation is present on a type, it is applicable for all public members of the type as well.
 * A member is allowed to declare a different {@link Quality} of lower level.
 *
 * @since 2.22.18
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Internal)
public @interface API {

    Quality value();

    /**
     * Indicates state of API
     */
    enum Quality {

        /**
         * Will not be changed in backwards-incompatible way in current major version.
         */
        Stable,

        /**
         * New API which became available recently and is in evaluation stage.
         * Might be promoted to {@code Stable} or rolled-back without prior notice.
         */
        Experimental,

        /**
         * Must not be used by any code except TAF itself.
         */
        Internal,

        /**
         * Should not be used and will be removed in the upcoming minor release
         */
        Deprecated

    }

    /**
     * Indicates TAF version in which {@link API.Quality} value of marked API has been changed.
     * Is used mostly for deprecating public API
     * (in the beginning of each sprint API of particular {@link Since} value is searched for to be removed).
     * <p>
     * Warning: this annotation purpose is not for declaring when API was made public.
     * Please use standard Java tag @since for that.
     */
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @API(Internal)
    @interface Since {

        double value();

    }
}
