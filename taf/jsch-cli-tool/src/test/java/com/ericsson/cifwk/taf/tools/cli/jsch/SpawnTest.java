package com.ericsson.cifwk.taf.tools.cli.jsch;

import static org.mockito.Mockito.*;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import org.junit.Test;
import org.slf4j.Logger;
import java.util.regex.Pattern;

public class SpawnTest {


    Spawn spawn = spy(new Spawn());
    TimeoutException timeoutException = mock(TimeoutException.class);

    @Test
    public void startSpawnWithNoStandardTerminalNoEnvCommand() throws Exception {

        Logger logger = mock(Logger.class);
        doReturn("").when(spawn).expect(any(Pattern.class), anyLong());
        doReturn(logger).when(spawn).getLogger();
        doThrow(timeoutException).when(spawn).sendCommand("env");

        spawn.getEnv();

        verify(logger, times(1)).warn(same("Can't retrieve Environment Variables of current shell "), same(timeoutException));
    }
}
