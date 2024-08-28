package com.ericsson.cifwk.taf.tools.cli.jsch;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * This class implements {@link GenericKeyedObjectPool} and create a pool of {@linke JSchSession}
 */
public class JSchSessionPool extends GenericKeyedObjectPool<String, JSchSession> {

    public JSchSessionPool(JSchSessionFactory sessionFactory, GenericKeyedObjectPool.Config config) {
        super(sessionFactory, config);
    }

    @Override
    public JSchSession borrowObject(String id) {
        try {
            return super.borrowObject(id);
        } catch (Exception e) {
            throw new JSchCLIToolException(e);
        }
    }

    @Override
    public void returnObject(String id, JSchSession session) {
        try {
            super.returnObject(id, session);
        } catch (Exception e) {
            //TODO ->> !!! implement session invalidating
            throw new JSchCLIToolException(e);
        }
    }
}

