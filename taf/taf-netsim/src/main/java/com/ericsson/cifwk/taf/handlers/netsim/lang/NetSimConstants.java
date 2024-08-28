package com.ericsson.cifwk.taf.handlers.netsim.lang;

public interface NetSimConstants {

    String NEW_LINE = "\n";

    String COMMAND_EXEC_START = ">>";

    String COMMAND_EXEC_SUCCESS = "OK";

    String COMMAND_OUTPUT_END = "END";

    // CIP-4139: Output with these spaces and command should be bypassed.
    String RUNNING_COMMAND_SPACES = "                                  ";

    public interface NetworkElements {

        String SHOW_NES_NAME_HEADING = "NE Name";

        String SHOW_NES_ADDRESS_HEADING = "In Address";

        String SHOW_NES_SERVER_HEADING = "Server";

        String SHOW_NES_TYPE_HEADING = "Type";

        String SHOW_NES_DEFAULT_DEST_HEADING = "Default dest.";

        String SHOW_ALL_SIM_NES_NAME_HEADING = "NE";

        String SHOW_ALL_SIM_NES_ADDRESS_HEADING = "Address";

        String SHOW_ALL_SIM_NES_SERVER_HEADING = "Server";
    }
}
