package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import com.ericsson.cifwk.meta.API;

/**
 * Created by ekeimoo on 1/19/15.
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public enum ShellType {

    BASH("bash"),
    TCSH("tcsh"),
    SH("sh"),
    KSH("ksh"),
    CSH("csh");

    private String shellType;

    ShellType(String shellType) {
        this.shellType = shellType;
    }

    public String getShellType() {
        return shellType;
    }
}
