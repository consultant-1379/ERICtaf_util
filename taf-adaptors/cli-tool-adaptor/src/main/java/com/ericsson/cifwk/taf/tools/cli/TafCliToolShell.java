package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.meta.API;
import com.ericsson.de.tools.cli.CliToolShell;

/**
 * This interface provides means for TAF CLI command execution
 * <p>
 */
@API(API.Quality.Experimental)
public interface TafCliToolShell extends CliToolShell {

    /**
     * This method return instance of {@link TafCliHostHopper} which allows do hops
     *
     * @return {@link TafCliHostHopper}
     */
    TafCliHostHopper hopper();

}
