package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.ericsson.cifwk.meta.API;

/**
 * This interface is used to provide the concrete implementation of {@link CLITool}
 * {@link CLI} uses this interface to create a specific implementation of {@lin CLITool}
 *
 * @see CLI#init()
 * * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public abstract class CLIToolProvider {

    public abstract String getName();

    public abstract CLITool create();

    /**
     * Initializes itself suitable CLITool.
     * Uses the <code>system property "taf.cli.clitool"</code> as provider name to find a suitable
     * {@link CLIToolProvider} by calling his method {@link CLIToolProvider#getName()}.
     * Default provider name is <code>"jsch"</code>.
     */
    public static CLITool provide() {
        String name = System.getProperty("taf.cli.clitool", "jsch");
        Iterator<CLIToolProvider> providers = ServiceLoader.load(CLIToolProvider.class, CLIToolProvider.class.getClassLoader()).iterator();
        while (providers.hasNext()) {
            CLIToolProvider provider = providers.next();
            if (provider.getName().equalsIgnoreCase(name)) {
                return provider.create();
            }
        }
        throw new CLIToolException("CLITool name [" + name + "] does not match any CLIToolProvider");
    }
}
