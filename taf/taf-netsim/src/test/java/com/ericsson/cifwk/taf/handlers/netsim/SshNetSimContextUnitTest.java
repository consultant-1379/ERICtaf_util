package com.ericsson.cifwk.taf.handlers.netsim;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.*;

import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import org.junit.Assert;
import org.junit.Test;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SshNetSimContextUnitTest {

    @Test
    public void getOpenSessionAmount() {
        SshNetSimContext unit = getUnit();

        Assert.assertEquals(0, unit.getOpenSessionsAmount());
        NetSimSession session1 = unit.openSession();
        Assert.assertEquals(1, unit.getOpenSessionsAmount());
        NetSimSession session2 = unit.openSession();
        Assert.assertEquals(2, unit.getOpenSessionsAmount());
        session1.close();
        Assert.assertEquals(1, unit.getOpenSessionsAmount());
        session2.close();
        Assert.assertEquals(0, unit.getOpenSessionsAmount());
    }

    @Test
    public void shouldCloseAllSessionsOnClose() {
        SshNetSimContext unit = getUnit();

        NetSimSession session1 = unit.openSession();
        NetSimSession session2 = unit.openSession();
        Assert.assertEquals(2, unit.getOpenSessionsAmount());

        unit.close();

        Assert.assertEquals(0, unit.getOpenSessionsAmount());

        Assert.assertTrue(session1.isClosed());
        Assert.assertTrue(session2.isClosed());
    }

    @Test
    public void shouldCloseWhenSomeSessionsAreAlreadyClosed() {
        SshNetSimContext unit = getUnit();

        NetSimSession session1 = unit.openSession();
        unit.openSession();
        Assert.assertEquals(2, unit.getOpenSessionsAmount());
        session1.close();

        unit.close();

        Assert.assertEquals(0, unit.getOpenSessionsAmount());
    }

    @Test
    public void shouldRejectCommandsIfClosed() {
        SshNetSimContext unit = getUnit();
        unit.exec(NetSimCommands.showFs());
        unit.close();
        try {
            unit.exec(NetSimCommands.showFs());
            Assert.fail("Exception expected");
        } catch (NetSimException e) {
        }
    }

    @Test
    public void shouldThrowTimeoutException() {
        SshNetSimContext unit = getContextForTimeout(5);
        try {
            unit.exec(NetSimCommands.showFs());
            Assert.fail("Exception expected");
        } catch (NetSimException e) {
            assertThat(e.getMessage(), notNullValue());
        }
    }

    private SshNetSimContext getUnit() {
        SshNetSimContext unit = new SshNetSimContext(mock(Host.class));
        unit = spy(unit);
        doReturn(mock(Shell.class)).when(unit).openShell();
        doReturn("").when(unit).executeCommandSetAndGetOutput(anyString());
        return unit;
    }

    private SshNetSimContext getContextForTimeout(int timeout) {
        CLI mockCLI = mock(CLI.class);
        SshNetSimContext unit = new SshNetSimContext(mockCLI, mock(Host.class));
        unit.setNetsimCommandTimeoutSeconds(5);
        unit = spy(unit);
        Shell mockShell = mock(Shell.class);
        doReturn(mockShell).when(mockCLI).executeCommand(any(Terminal.class), any(String.class));
        doReturn(false).when(mockShell).isClosed();
        doAnswer(new DelayedAnswer(2000, "")).when(mockShell).read();
        return unit;
    }

    private class DelayedAnswer implements Answer<String> {
        private int delay;
        private String answer;

        public DelayedAnswer(int delay, String answer) {
            this.delay = delay;
            this.answer = answer;
        }

        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
            Thread.sleep(delay);
            return answer;
        }
    }
}
