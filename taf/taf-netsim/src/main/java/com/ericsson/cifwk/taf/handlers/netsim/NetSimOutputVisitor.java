package com.ericsson.cifwk.taf.handlers.netsim;

/**
 *
 */
interface NetSimOutputVisitor {

    void onStart();

    void onEmptyLine();

    void onLine(String line);

    void onEnd();

}
