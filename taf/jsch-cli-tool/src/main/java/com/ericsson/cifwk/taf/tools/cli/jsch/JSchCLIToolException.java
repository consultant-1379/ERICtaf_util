package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.taf.tools.cli.CLIToolException;

/**
 * This class define common JSchCLITool Runtime exception
 */
public class JSchCLIToolException extends CLIToolException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with an explanatory message.
     *
     * @param message An explanation of what went wrong.
     */
    public JSchCLIToolException(String message) {
        super(message);
    }

    /**
     * Create a new exception with an explanatory message and a reference to an exception
     * that made us throw this one.
     *
     * @param message An explanation of what went wrong.
     * @param cause   Another exception that is the reason to throw this one.
     */
    public JSchCLIToolException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new exception with an explanatory message and a reference to an exception
     * that made us throw this one.
     *
     * @param cause Another exception that is the reason to throw this one.
     */
    public JSchCLIToolException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
