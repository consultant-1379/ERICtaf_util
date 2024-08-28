package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLITool;
import com.ericsson.cifwk.taf.tools.cli.CLIToolProvider;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

/**
 * This class is used to provide the Jsch implementation of {@link CLITool}
 * {@link CLI} uses this class to create a specific implementation of {@link CLITool}
 *
 * @see CLI#init()
 */
public class JSchCLIToolProvider extends CLIToolProvider {

    public static final String CLITOOL_NAME = "jsch";

    @Override
    public String getName() {
        return CLITOOL_NAME;
    }

    @Override
    public CLITool create() {
        //TODO ->> !!!  setup sessions from system properties
        GenericKeyedObjectPool.Config sessionPoolConfig = new GenericKeyedObjectPool.Config();
        sessionPoolConfig.maxActive = 1; // per KEY
        GenericKeyedObjectPool.Config shellPoolConfig = new GenericKeyedObjectPool.Config();
        shellPoolConfig.maxActive = 1;  // per KEY
        //TODO <<-
        JSchSessionFactory sessionFactory = new JSchSessionFactory();
        JSchSessionPool sessionPool = new JSchSessionPool(sessionFactory, sessionPoolConfig);
        JSchShellFactory shellFactory = new JSchShellFactory();
        JSchShellPool shellPool = new JSchShellPool(shellFactory, shellPoolConfig);
        JSchCLITool tool = new JSchCLITool(sessionPool, shellPool);
        return tool;
    }
}
