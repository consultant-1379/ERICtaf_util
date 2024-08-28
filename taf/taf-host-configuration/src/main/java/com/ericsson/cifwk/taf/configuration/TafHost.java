package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.meta.API.Quality;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows to specify search criteria for the injected host. <br />
 * In the case more than one criteria is specified, then searching process will be provided for all of them. <br />
 *
 * <p>
 * Example: you want to find host with name 'tafHost'<br />
 * <code>
 *     {@literal @}TafHost(hostname="tafHost")<br />
 *     Host host;<br />
 * </code>
 * In this case host will be injected with hostname 'tafHost'<br />
 * </p>
 *
 * <p>
 * Example: you want to find host with name 'tafHost' and type 'custom type'<br />
 * <code>
 *     {@literal @}TafHost(hostname="tafHost", type='custom type')<br />
 *     Host host;<br />
 * </code>
 * In this case host will be injected with hostname 'tafHost' and type 'custom type'<br />
 * </p>
 *
 * * <p>
 * It is possible to inject multiple hosts into List, Set or Collection fields.<br />
 * If no search criteria is defined then all hosts will be injected.<br />
 * <br />
 * Example: you want to find all hosts with group 'myGroup'<br />
 * <code>
 *     {@literal @}TafHost(group="myGroup")<br />
 *     List&lt;Host&gt; hosts;<br />
 * </code>
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@API(Quality.Experimental)
public @interface TafHost {

    String NOT_SET = "";

    /**
     * Define hostname criteria
     */
    String hostname() default NOT_SET;

    /**
     * Define host group criteria
     */
    String group() default NOT_SET;

    /**
     * Define host type criteria
     */
    String type() default NOT_SET;

}
