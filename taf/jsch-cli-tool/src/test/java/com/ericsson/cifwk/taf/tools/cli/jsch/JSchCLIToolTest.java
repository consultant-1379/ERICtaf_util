package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.jcraft.jsch.JSchException;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class JSchCLIToolTest {

    JSchCLITool jSchCLITool;

    public JSchCLIToolTest() throws JSchException {
    }

    @Before
    public void init() throws Exception {
        GenericKeyedObjectPool.Config sessionPoolConfig = new GenericKeyedObjectPool.Config();
        sessionPoolConfig.maxActive = 1;
        GenericKeyedObjectPool.Config shellPoolConfig = new GenericKeyedObjectPool.Config();
        shellPoolConfig.maxActive = 1;

        JSchSessionFactory sessionFactory = Mockito.mock(JSchSessionFactory.class);
        JSchShellFactory shellFactory = Mockito.mock(JSchShellFactory.class);
        JSchSession jSchSession = Mockito.mock(JSchSession.class);
        JSchShell jSchShell = Mockito.mock(JSchShell.class);
        JSchSessionPool sessionPool = new JSchSessionPool(sessionFactory, sessionPoolConfig);
        JSchShellPool shellPool = new JSchShellPool(shellFactory, shellPoolConfig);
        jSchCLITool = new JSchCLITool(sessionPool, shellPool);

        when(sessionFactory.makeObject(anyString())).thenReturn(jSchSession);
        when(shellFactory.makeObject(anyString())).thenReturn(jSchShell);

    }

    @Test
    public void ensureJSchSessionClosing() {
        assertThat(jSchCLITool.sessionPool.getNumIdle()).isEqualTo(0);
        String shellId = jSchCLITool.openShell("localhost", 22, "taf", "pass", Terminal.VT100);
        assertThat(jSchCLITool.sessionPool.getNumIdle()).isEqualTo(1);
        jSchCLITool.disconnect(shellId);
        assertThat(jSchCLITool.sessionPool.getNumIdle()).isEqualTo(1);
        jSchCLITool.close();
        assertThat(jSchCLITool.sessionPool.getNumIdle()).isEqualTo(0);
    }

}
