package com.ericsson.cifwk.taf.handlers;

/**
 * CommandExecutor is an interface used for running commands on the current operating system
 * 
 */
public interface CommandExecutor {

    /**
     * Method for the simple execution of a command on the operating system through TAF
     * (command and arguments supplied in one string)
     *
     * @param cmdWithArgs
     *            - command to be run plus arguments included as a string
     * @return String representation of the result of executing the command
     */
    String simplExec(String cmdWithArgs);

    /**
     * Method for the simple execution of a command on the operating system through TAF
     * (command and arguments supplied as separate strings)
     *
     * @param cmdWithOutArgs
     *            - command (without arguments) to be run as a string
     * @param arguments
     *            - command arguments supplied as separate strings
     * @return String representation of the result of executing the command
     */
    String simplExec(String cmdWithOutArgs, String... arguments);

    /**
     * Method for the execution of a command on the operating system through TAF
     * (command and arguments supplied as separate strings)
     *
     * @param cmdWithArgs
     *            - command to be run as a string
     * @return True if the command has been executed without exceptions
     */
    boolean execute(String cmdWithArgs);

    /**
     * Method for the execution of a command on the operating system through TAF
     * (command and arguments supplied as separate strings)
     *
     * @param cmdWithOutArgs
     *            - command (without arguments) to be run as a string
     * @param arguments
     *            - command arguments supplied as separate strings
     * @return True if the command has been executed without exceptions
     */
    boolean execute(String cmdWithOutArgs, String... arguments);

    /**
     * Method for the to retrieve the standard output from running the command
     *
     * @return String representation of the response from running the command
     */
    String getStdOut();

    /**
     * Method for the to retrieve the error output from running the command
     *
     * @return String representation of the error from running the command
     */
    String getStdErr();

    /**
     * Method for the to retrieve the exit code response from running the command
     *
     * @return command exit code as an int
     */
    int getExitCode();

}
