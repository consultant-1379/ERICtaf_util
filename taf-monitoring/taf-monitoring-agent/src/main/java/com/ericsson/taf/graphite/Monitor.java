package com.ericsson.taf.graphite;

import java.util.Map;

/**
 *  
 *
 */
public interface Monitor {

    /**
     * Deliver a sample of data for reporting service
     *
     * @return
     */
    Map<String, Number> getSample();

    void close();
}
