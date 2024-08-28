package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.meta.API;
import com.ericsson.de.tools.cli.BufferedCliTool;
import com.ericsson.de.tools.cli.CliCommandResult;
import com.ericsson.de.tools.cli.CliIntermediateResult;
import com.ericsson.de.tools.cli.CliToolShell;
import com.ericsson.de.tools.cli.WaitCondition;

@API(API.Quality.Experimental)
class TafSshCliTool implements TafCliToolShell {

    private final CliToolShell shell;

    TafSshCliTool(CliToolShell shell) {
        this.shell = shell;
    }

    @Override
    public void close() {
        shell.close();
    }

    @Override
    public CliCommandResult execute(String command) {
        return shell.execute(command);
    }

    @Override
    public CliCommandResult execute(String command, long timeout) {
        return shell.execute(command, timeout);
    }

    @Override
    public CliIntermediateResult writeLine(String command) {
        return shell.writeLine(command);
    }

    @Override
    public CliIntermediateResult writeLine(String command, long timeout) {
        return shell.writeLine(command, timeout);
    }

    @Override
    public CliIntermediateResult writeLine(String command, WaitCondition waitCondition) {
        return shell.writeLine(command, waitCondition);
    }

    @Override
    public TafCliHostHopper hopper() {
        return new TafCliHostHopper(shell);
    }

    @Override
    public String switchUser(String username) {
        return shell.switchUser(username);
    }

    @Override
    public String switchUser(String username, String password) {
        return shell.switchUser(username, password);
    }

    @Override
    public String sudoRootUser() {
        return shell.sudoRootUser();
    }

    @Override
    public String sudoRootUser(String password) {
        return shell.sudoRootUser(password);
    }

    @Override
    public BufferedCliTool getBufferedShell() {
        return shell.getBufferedShell();
    }

    @Override
    public void interrupt() {
        shell.interrupt();
    }
}
