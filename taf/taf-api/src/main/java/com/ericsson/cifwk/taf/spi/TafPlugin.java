package com.ericsson.cifwk.taf.spi;

/**
 * This is a generic TAF plugins bootstrap API. If you would implement that and register in SPI all bootstraps
 * will be triggered upon TAF startup.
 *
 * Check JDK ServiceLoader for details how to write and register your plugin.
 */
public interface TafPlugin {

    /**
     * called when TAF starts
     */
    void init();

    /**
     * called before TAF shuts down
     */
    void shutdown();

}
