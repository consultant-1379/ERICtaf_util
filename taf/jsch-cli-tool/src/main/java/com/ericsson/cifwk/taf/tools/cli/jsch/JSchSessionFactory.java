package com.ericsson.cifwk.taf.tools.cli.jsch;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * This class is used for a {@link JSchSessionPool} that would create and destroy new {@link JSchSession}
 */
public class JSchSessionFactory extends BaseKeyedPoolableObjectFactory<String, JSchSession> {

    @Override
    public JSchSession makeObject(String id) throws Exception {
        JSchSession session = new JSchSession(id);
        return session;
    }

    @Override
    public void destroyObject(String id, JSchSession session) throws Exception {
        session.session.disconnect();
    }

}
