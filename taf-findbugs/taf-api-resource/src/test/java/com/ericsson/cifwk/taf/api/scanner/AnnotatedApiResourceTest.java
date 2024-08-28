package com.ericsson.cifwk.taf.api.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 14.07.2016
 */
public class AnnotatedApiResourceTest {

    @Test
    public void processMethod() {
        String classLine = "public void com.ericsson.cifwk.taf.api.scanner.AnnotatedApiSamples.deprecatedMethod()";
        String actual = AnnotatedApiResource.processMethod(classLine);
        String expected = "com.ericsson.cifwk.taf.api.scanner.AnnotatedApiSamples.deprecatedMethod()";
        assertThat(actual).isEqualTo(expected);

        classLine = "public static void com.ericsson.cifwk.taf.handlers.implementation.SshRemoteCommandExecutor.execute(java.lang.String,java"
                + ".lang.String[])";
        actual = AnnotatedApiResource.processMethod(classLine);
        expected = "com.ericsson.cifwk.taf.handlers.implementation.SshRemoteCommandExecutor.execute(String,String[])";
        assertThat(actual).isEqualTo(expected);
    }

}
