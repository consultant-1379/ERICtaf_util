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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs an error if priority is failure
 *
 */
public class Failure implements ActionListener{
    private final Logger logger = LoggerFactory.getLogger(Failure.class);
    
    /*
     * (non-Javadoc)
     * @see com.ericsson.cifwk.taf.maven.plugin.action.ActionListener#onViolate(java.lang.String, java.lang.String)
     */
    @Override
    public void onViolate(String name, String rule) {
        logger.error("{} violated by {}", name, rule);
    }
}
