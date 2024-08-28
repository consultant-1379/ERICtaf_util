package com.ericsson.cifwk.taf.management;

public interface TafManager {

    String getName();

    void shutdown();

    boolean isTerminated();

    void kill();

}
