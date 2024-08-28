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

import java.util.regex.Pattern;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;
import static com.ericsson.cifwk.meta.API.Quality.Stable;
/**
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public final class CommandBuilder{

    CLICommandHelper cmdHelper;

    /**
     * Constructor creates default CLICommandHelper instance
     */
    CommandBuilder(){
        cmdHelper = new CLICommandHelper();
    }

    /**
     * Constructor which creates CLICommandHelper instance on a particular host
     * @param host
     */
    CommandBuilder(Host host){
        cmdHelper = new CLICommandHelper(host);
    }

    /**
     * Constructor which creates CLICommandHelper instance with specified User on a specific host
     * @param host
     * @param user
     */
    CommandBuilder(Host host, User user){
        cmdHelper = new CLICommandHelper(host, user);
    }

    /**
     * Creates a CommandBuilder instance
     * @return CommandBuilder
     */
    public static CommandBuilder newBuilder(){
        return new CommandBuilder();
    }

    /**
     * Creates CommandBuilder instance on specific host
     * @param host
     * @return CommandBuilder
     */
    public static CommandBuilder newBuilder(Host host){
        return new CommandBuilder(host);
    }

    /**
     * Creates CommandBuilder instance with specific user on specific host
     * @param host
     * @param user
     * @return CommandBuilder
     */
    public static CommandBuilder newBuilder(Host host, User user){
        return new CommandBuilder(host, user);
    }

    /**
     * Creats the CLI instance for CLICommandHelper on a host, should only be used
     * if {@link #CommandBuilder()} constructor is instantiated
     * @param host
     * @return CommandBuilder
     */
    public CommandBuilder createCLIInstance(Host host){
        cmdHelper.createCliInstance(host);
        return this;
    }

    /**
     * Creats the CLI instance for CLICommandHelper on a host with certain user, should only be used
     * if {@link #CommandBuilder()} constructor is instantiated
     * @param host
     * @param user
     * @return CommandBuilder
     */
    public CommandBuilder createCLIInstance(Host host, User user){
        cmdHelper.createCliInstance(host, user);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#openShell()
     * @return CommandBuilder
     */
    public CommandBuilder openShell(){
        cmdHelper.openShell();
        return this;
    }


    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#openShell(term)
     * @return CommandBuilder
     */
    public CommandBuilder openShell(Terminal term){
        cmdHelper.openShell(term);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#write(text)
     * @return CommandBuilder
     */
    public CommandBuilder write(String text){
        cmdHelper.write(text);
        return this;
    }


    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#writeln(text)
     * @return CommandBuilder
     */
    public CommandBuilder writeln(String command){
        cmdHelper.interactWithShell(command + "; ");
        return this;
    }


    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expect(text)
     * @return CommandBuilder
     */
    public CommandBuilder expect(String expected){
        cmdHelper.expect(expected);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expect(text, timeout)
     * @return CommandBuilder
     */
    public CommandBuilder expect(String expected, long timeoutInSeconds){
        cmdHelper.expect(expected, timeoutInSeconds);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expect(Pattern pattern)
     * @return CommandBuilder
     */
    public CommandBuilder expect(Pattern pattern){
        cmdHelper.expect(pattern);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expect(pattern, timeout)
     * @return CommandBuilder
     */
    public CommandBuilder expect(Pattern pattern, long timeoutInSeconds){
        cmdHelper.expect(pattern, timeoutInSeconds);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expectErr(err)
     * @return CommandBuilder
     */
    public CommandBuilder expectErr(String err){
        cmdHelper.expectErr(err);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expectErr(err, timeout)
     * @return CommandBuilder
     */
    public CommandBuilder expectErr(String err, long timeoutInSeconds){
        cmdHelper.expectErr(err, timeoutInSeconds);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#setEnv(value, name)
     * @return CommandBuilder
     */
    public CommandBuilder setEnv(String value, String name){
        cmdHelper.setEnv(name, value);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#getEnv(name)
     * @return CommandBuilder
     */
    public CommandBuilder getEnv(String name){
        cmdHelper.getEnv(name);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#unsetEnv(name)
     * @return CommandBuilder
     */
    public CommandBuilder unsetEn(String name){
        cmdHelper.unsetEnv(name);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#resetEnv()
     * @return CommandBuilder
     */
    public CommandBuilder resetEnv(){
        cmdHelper.resetEnv();
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#getEnv()
     * @return CommandBuilder
     */
    public CommandBuilder getEnv(){
        cmdHelper.getEnv();
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#interactWithShell(text)
     * @return CommandBuilder
     */
    public CommandBuilder interact(String text){
        cmdHelper.interactWithShell(text);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#execute(command)
     * @return CommandBuilder
     */
    public CommandBuilder execute(String command){
        cmdHelper.execute(command);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#simpleExec(String... commands)
     * @return CommandBuilder
     */
    public CommandBuilder simpleExec(String... commands){
        cmdHelper.simpleExec(commands);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expectShellClosure()
     * @return CommandBuilder
     */
    public CommandBuilder expectShellClosure(){
        cmdHelper.expectShellClosure();
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#expectShellClosure(timeout)
     * @return CommandBuilder
     */
    public CommandBuilder expectShellClosure(long timeoutInSeconds){
        cmdHelper.expectShellClosure(timeoutInSeconds);
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#isClosed()
     * @return CommandBuilder
     */
    public CommandBuilder isClosed(){
        cmdHelper.isClosed();
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#closeAndValidateShell()
     * @return CommandBuilder
     */
    public CommandBuilder closeShell(){
        cmdHelper.closeAndValidateShell();
        return this;
    }

    /**
     * @see com.ericsson.cifwk.taf.tools.cli.CLICommandHelper#disconnect()
     * @return CommandBuilder
     */
    public CommandBuilder disconnect(){
        cmdHelper.disconnect();
        return this;
    }

    /**
     * Method which returns CommandResult instance which contains the command results
     * @return CommandResult
     */
    public CommandResult build(){
        return new CommandResult(cmdHelper);
    }
}
