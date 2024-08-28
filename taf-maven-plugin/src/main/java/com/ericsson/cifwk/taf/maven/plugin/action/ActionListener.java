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

/**
 * 
 * Interface to implement handlers for rule violations.
 *
 */
public interface ActionListener {
    
    /**
     * Called when rule is violated
     * 
     * @param name Fully Qualified Name of method or class that has violation
     * @param rule Name of rule that is being violated
     * @throws MojoExecutionException
     */
    void onViolate(String name, String rule) throws MojoExecutionException;
}
