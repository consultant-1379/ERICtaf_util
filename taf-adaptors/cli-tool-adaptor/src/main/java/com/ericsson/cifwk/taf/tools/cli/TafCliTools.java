package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.de.tools.cli.LocalCliToolBuilder;
import com.ericsson.de.tools.cli.CliTools;

/**
 * Factory class for the TAF CliTool builder
 * */
@API(API.Quality.Experimental)
public class TafCliTools {

    private TafCliTools() {}

    /**
     * Create {@link LocalCliToolBuilder} instance
     *
     * @return {@link LocalCliToolBuilder}
     */
    public static LocalCliToolBuilder local() {
        return CliTools.local();
    }

    /**
     * Create {@link TafSshShellBuilder} instance with target host using {@link Host}
     *
     * @param targetHost
     * @return {@link TafSshShellBuilder}
     */
    public static TafSshShellBuilder sshShell(Host targetHost) {
        return new TafSshShellBuilder(targetHost);
    }

    /**
     * Create {@link TafSshShellBuilder} instance with target host using host address
     *
     * @param targetHost
     * @return {@link TafSshShellBuilder}
     */
    public static TafSshShellBuilder sshShell(String targetHost) {
        return new TafSshShellBuilder(targetHost);
    }

    /**
     * Create {@link TafSimpleExecutorBuilder} instance with target host using {@link Host}.
     *
     * <br>
     * simpleExecutor is stateless tool for executing standalone commands, but is more performant then SshShell
     * <br/>
     *
     * @param targetHost
     * @return {@link TafSimpleExecutorBuilder}
     */
    public static TafSimpleExecutorBuilder simpleExecutor(Host targetHost) {
        return new TafSimpleExecutorBuilder(targetHost);
    }

    /**
     * Create {@link TafSimpleExecutorBuilder} instance with target host using host address
     *
     * <br>
     * simpleExecutor is stateless tool for executing standalone commands, but is more performant then SshShell
     * <br/>
     *
     * @param targetHost
     * @return {@link TafSimpleExecutorBuilder}
     */
    public static TafSimpleExecutorBuilder simpleExecutor(String targetHost) {
        return new TafSimpleExecutorBuilder(targetHost);
    }
}
