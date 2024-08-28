package com.ericsson.cifwk.taf.handlers.implementation;

import com.ericsson.cifwk.taf.handlers.CommandExecutor;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
 * Class used for running commands on the current operating system
 */
public class LocalCommandExecutor implements CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalCommandExecutor.class);

    private String stdOut;
    private String stdErr;
    private int exitCode;

    /**
     * (non-Javadoc)
     *
     * @see CommandExecutor#execute(String)
     */
    @Override
    public boolean execute(String cmdWithArgs) {
        return doExecute(cmdWithArgs);
    }

    /**
     * (non-Javadoc)
     *
     * @see CommandExecutor#execute(String)
     */
    @Override
    public boolean execute(String cmdWithOutArgs, final String... arguments) {
        return execute(cmdWithOutArgs + " " + Joiner.on(" ").join(arguments));
    }

    /**
     * Method to carry out the actual command execution on the operation system and capture the outputs into class variables
     *
     * @param cmdWithArgs - command (with arguments) to be run as a string
     * @return True if the command executed without exceptions
     */
    protected boolean doExecute(String cmdWithArgs) {
        String commandToRun = cmdWithArgs;
        try {
            boolean isWindowsOs = System.getProperty("os.name").startsWith("Windows");
            if (isWindowsOs) commandToRun = "cmd /c " + commandToRun;

            Process myProcess = Runtime.getRuntime().exec(commandToRun);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));
            String s;
            String tempString = "";
            while((s=bufferedReader.readLine())!=null){
                tempString = tempString + s;
            }
            switch (exitCode = myProcess.waitFor()) {
            }
            stdOut = tempString;
            stdErr = new String(ByteStreams.toByteArray(myProcess.getErrorStream()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (exitCode != 0) {
            LOGGER.error("Command failed with exit code " + String.valueOf(exitCode) + " and Error Output: " + stdErr);
        }
        return exitCode == 0;
    }

    /**
     * (non-Javadoc)
     *
     * @see CommandExecutor#execute(String)
     */
    @Override
    public String simplExec(String cmdWithArgs) {
        return doSimpleExecute(cmdWithArgs);
    }

    /**
     * (non-Javadoc)
     *
     * @see CommandExecutor#execute(String)
     */
    @Override
    public String simplExec(String cmdWithOutArgs, final String... arguments) {
        return simplExec(cmdWithOutArgs + " " + Joiner.on(" ").join(arguments));
    }

    /**
     * Method to carry out the simple command execution on the operation system and capture the outputs into class variables,
     * this implementation only cares about the std out
     *
     * @param cmdWithArgs - command (with arguments) to be run as a string
     * @return String representation of the std out from the command execution
     */
    protected String doSimpleExecute(String cmdWithArgs) {
        doExecute(cmdWithArgs);
        return stdOut;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public int getExitCode() {
        return exitCode;
    }
}