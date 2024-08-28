package com.ericsson.cifwk.taf.tools.cli.jsch;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * This class is used for a {@link JSchShellPool} that would create and destroy new {@link JSchShell}
 */
public class JSchShellFactory extends BaseKeyedPoolableObjectFactory<String, JSchShell> {

    @Override
    public JSchShell makeObject(String id) throws Exception {
        JSchShell shell = new JSchShell(id);
        return shell;
    }

    @Override
    public void destroyObject(String id, JSchShell shell) throws Exception {
        shell.disconect();
    }
}
