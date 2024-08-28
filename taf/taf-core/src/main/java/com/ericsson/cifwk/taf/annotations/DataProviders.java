package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * <p>A group of {@link DataDriven} annotations. Needs to be defined for test method with multiple <code>DataDriven</code>
 * annotations inside when multiple data sources are used.</p>
 * <p>Example:
 * <pre>
 *      @DataProviders ({
 *           @DataDriven(name="a"),
 *           @DataDriven(name="b", filter="command!='install'")
 *       })
 *      @Test
 *      public test(@DataDriven(name="c", filter="y>24") DataRecord cRecord, @Input("fooName") String name, ...) {
 *          ....
 *      }
 * </pre>
 * 3 data sources here: 2 data sources are defined on method level, and one on the method parameters level.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { METHOD })
public @interface DataProviders {

    DataDriven[] value();

}
