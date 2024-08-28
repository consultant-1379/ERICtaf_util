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
package com.ericsson.cifwk.taf.exceptions;

import com.ericsson.cifwk.taf.data.Host;


//TODO move to jsch-cli-tool module after remoteFileHandler is removed from taf-deprecated
public class RemoteFileNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RemoteFileNotFoundException(Host host, String filePath) {
        super("File " + filePath + " does not exist on host " + host);
    }

}