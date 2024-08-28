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
package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import com.ericsson.cifwk.meta.API;

/**
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class CommandResult {

    private final String stdOut;
    private final int exitCode;
    private long execTime;

    /**
     * Constructor which takes instance of CLICommandHelper as parameter
     *
     * @param cmdHelper
     *         CLICommandHelper class
     */
    CommandResult(CLICommandHelper cmdHelper) {
        stdOut = cmdHelper.getStdOut();
        exitCode = cmdHelper.getCommandExitValue();
        execTime = cmdHelper.getCommandExecutionTime();
    }

    /**
     * Get command standard output
     *
     * @return stdOut
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * Get command Execution time
     *
     * @return execTime
     */
    public long getExecutionTime() {
        return execTime;
    }

    /**
     * Get command exit code
     *
     * @return exitCode
     */
    public int getExitCode() {
        return exitCode;
    }
}

