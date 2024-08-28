package com.ericsson.cifwk.taf.ddc;

import com.ericsson.cifwk.diagmon.util.instr.config.ConfigTreeNode;
import com.ericsson.cifwk.diagmon.util.instr.outputhandlers.taf.Config;

public interface DDCHandler {

    void init(Config config);

    void recordValues(ConfigTreeNode node);

    void shutdown();
}

