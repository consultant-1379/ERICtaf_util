package com.ericsson.cifwk.taf.handlers.netsim;

import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SecureportCommand;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class CommandSyntaxTest {

    @Test
    public void secureportCommand() {

        SecureportCommand netsimCommand = NetSimCommands.secureport();
        netsimCommand.setTurn(SecureportCommand.Turn.ON);
        checkCommand(netsimCommand, "secureport:turn=\"on\";");
        netsimCommand = NetSimCommands.secureport();
        netsimCommand.setStatus(true);
        checkCommand(netsimCommand, "secureport:status;");
        netsimCommand = NetSimCommands.secureport();
        netsimCommand.setRemoveconfig(true);
        checkCommand(netsimCommand, "secureport:removeConfig;");
    }

    static void checkCommand(final NetSimCommand netsimCommand, final String expected) {
        String line = NetSimCommandEmitter.buildCommandLine(netsimCommand);
        assertThat(line, equalTo( "echo -e ' " + expected + "\n ' | /netsim/inst/netsim_pipe"));
    }

}
