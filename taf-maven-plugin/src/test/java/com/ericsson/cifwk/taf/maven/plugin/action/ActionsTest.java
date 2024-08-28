/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.maven.plugin.action;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

public class ActionsTest {

    @Test(expected=MojoExecutionException.class)
    public void shouldThrowExcpetion() throws MojoExecutionException {
        new Critical().onViolate("a", "b");
    }

    @Test
    public void shouldNotThrowException(){
        new Info().onViolate("a", "b");
        new Warning().onViolate("a", "b");
        new Failure().onViolate("a", "b");
    }
}
