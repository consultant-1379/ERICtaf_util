package com.ericsson.cifwk.taf.utils;

import com.ericsson.cifwk.meta.API;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class CommandExecutionOutput {
    public CommandExecutionOutput(String stdErr, String stdOut, int exitCode) {
        this.stdErr = stdErr;
        this.stdOut = stdOut;
        this.exitCode = exitCode;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    private String stdErr;
    private String stdOut;
    private int exitCode;
}
