package com.ericsson.cifwk.taf.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * This annotation is used to pass additional options to @TestMethod
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={METHOD})
public @interface TestOptions {
    /**
     * Timeout in milliseconds for test. Accepts String representing numeric value or ${expression}, where expression is
     * <a href="https://en.wikisource.org/wiki/MVEL_Language_Guide">MVEL</a>.
     * Difference from {@link org.testng.annotations.Test#timeOut()} is that you can pass parameter from
     * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/userdocs/Latest/index.html#_taf_configuration">TAF Configuration</a>
     * Example: "${configuration['timeout']}"
     */
    String timeout() default "0";
}
