package com.ericsson.cifwk.taf.tools.cli.jsch;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * This class implements {@link GenericKeyedObjectPool} and create a pool of {@linke JSchShell}
 */
public class JSchShellPool extends GenericKeyedObjectPool<String, JSchShell> {

    public JSchShellPool(JSchShellFactory shellFactory, GenericKeyedObjectPool.Config config) {
        super(shellFactory, config);
    }

    @Override
    public JSchShell borrowObject(String id) {
        try {
            return super.borrowObject(id);
        } catch (Exception e) {
            throw new JSchCLIToolException(e);
        }
    }

    @Override
    public void returnObject(String id, JSchShell shell) {
        try {
            super.returnObject(id, shell);
        } catch (Exception e) {
            //TODO ->> !!! implement shell invalidating if any Exception
            throw new JSchCLIToolException(e);
        }
    }
}
