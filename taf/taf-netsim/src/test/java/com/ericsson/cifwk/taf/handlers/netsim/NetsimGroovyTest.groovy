package com.ericsson.cifwk.taf.handlers.netsim

import static org.junit.Assert.assertEquals

import org.junit.Test

import com.ericsson.cifwk.taf.handlers.netsim.commands.AvcburstCommand
import com.ericsson.cifwk.taf.handlers.netsim.commands.DumpmotreeCommand
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands
import com.ericsson.cifwk.taf.handlers.netsim.commands.SetFsCommand
import com.ericsson.cifwk.taf.handlers.netsim.commands.StopburstCommand

/**
 *
 */
class NetsimGroovyTest {

    @Test
    public void shouldWorkWithExpressions() {
        AvcburstCommand command = NetSimCommands.avcburst()
        command.freq = 1.0
        command.numEvents = 2
        command.loop = false
        command.idleTime = 0
        command.avcdata = [
            [managedObject:[["userLabel": "one"]]],
            [managedObject:[["userLabel": "two"]]],
            [managedObject:[["userLabel": "three"]]]
        ]

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertEquals("echo -e ' avcburst:freq=1.0,num_events=2,loop=false,idle_time=0,avcdata=\"" +
                "[{\\\"managedObject\\\",[{\\\"userLabel\\\",\\\"one\\\"}]}," +
                "{\\\"managedObject\\\",[{\\\"userLabel\\\",\\\"two\\\"}]}," +
                "{\\\"managedObject\\\",[{\\\"userLabel\\\",\\\"three\\\"}]}]\";" +
                "\n ' | /netsim/inst/netsim_pipe", line);
    }

    @Test
    public void shouldGenerateDumpMoTreeCommand() {
        DumpmotreeCommand command = NetSimCommands.dumpmotree()
        command.moid = "ManagedElement=1"
        command.scope = 0
        command.printattrs = true
        command.includeattrs  = ["userLabel"]

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertEquals("echo -e ' " +
                "dumpmotree:moid=\"ManagedElement=1\",scope=0,printattrs,includeattrs=\"userLabel\"" +
                ";\n ' | /netsim/inst/netsim_pipe", line);
    }

    @Test
    public void shouldGenerateStopBurstCommand() {
        StopburstCommand command = NetSimCommands.stopburst()
        command.id = "1"

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertEquals("echo -e ' " +
                "stopburst:id=1;\n ' | /netsim/inst/netsim_pipe", line);
    }

    @Test
    public void shouldMindTheSequenceOfTheCommandArguments() {
        SetFsCommand setFsCmd = NetSimCommands.setFs("resource", "/home/mydir")
        assertEquals("resource", setFsCmd.resource);
        assertEquals("/home/mydir", setFsCmd.dir);
    }
}
