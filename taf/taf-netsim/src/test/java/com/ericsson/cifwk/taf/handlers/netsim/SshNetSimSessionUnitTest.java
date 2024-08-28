package com.ericsson.cifwk.taf.handlers.netsim;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;

public class SshNetSimSessionUnitTest {

    @Test
    public void sessionShouldRejectCommandsIfClosed() {
        SshNetSimSession unit = getUnit();
        unit.exec(NetSimCommands.showFs());
        unit.close();
        try {
            unit.exec(NetSimCommands.showFs());
            Assert.fail("Exception expected");
        } catch (NetSimException e) {
        }
    }

    @Test
    public void sessionShouldthrowTimeoutExceptionWithDefaultValue() {

        SshNetSimSession unit = getSessionForTimeout(5);
        try {
            unit.exec(NetSimCommands.showFs());
            Assert.fail("Exception expected");
        } catch (NetSimException e) {
            assertThat(e.getMessage(), equalTo("Timeout while waiting for the commands to finish execution,timeout set is 5 Seconds"));
        }
    }

    @SuppressWarnings("unchecked")
    private SshNetSimSession getUnit() {
        SshNetSimSession unit = new SshNetSimSession(mock(Shell.class));
        unit = spy(unit);
        doReturn(mock(NetSimResult.class)).when(unit).processTextResult(any(String.class));
        doNothing().when(unit).verifyResult(any(List.class), anyString(), any(NetSimResult.class));
        return unit;
    }

    @SuppressWarnings("unchecked")
    private SshNetSimSession getSessionForTimeout(int timeout) {
        Shell mockedShell = mock(Shell.class);
        SshNetSimSession unit = new SshNetSimSession(mockedShell);
        unit.setNetsimCommandTimeoutSeconds(5);
        unit = spy(unit);
        doReturn(mock(NetSimResult.class)).when(unit).processTextResult(any(String.class));
        doNothing().when(unit).verifyResult(any(List.class), anyString(), any(NetSimResult.class));
        when(mockedShell.expect(isA(Pattern.class), anyLong())).thenThrow(new TimeoutException("Test"));
        return unit;
    }

}
