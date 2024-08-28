package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows injecting properties. <br />
 *
 * <p>
 * Example: you want to inject property 'timeoutMillis'<br />
 * <code>
 *     {@literal @}TafProperty("timeoutMillis")<br />
 *     long timeoutMillis;<br />
 * </code>
 * <br />
 *
 * Example: you want to inject property 'timeoutMillis' or specific value if property is not defined<br />
 * <code>
 *     {@literal @}TafProperty(value = "timeoutMillis", defaultValue = "2000")<br />
 *     BigInteger timeoutMillis;<br />
 * </code>
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@API(Quality.Experimental)
public @interface TafProperty {

    String NOT_SET = "319e50b9-f8d4-4785-9899-945e40a60b5b";

    String value();

    String defaultValue() default NOT_SET;

}
