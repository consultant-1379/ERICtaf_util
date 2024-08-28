package com.ericsson.cifwk.taf.performance.threshold;

/**
 * Extension interface to implement handlers for threshold violations.
 */
public interface ViolationListener {

    /**
     * Is called when threshold is reached.
     *
     * @param threshold violated threshold rule
     * @param timestamp timestamp of the event
     * @param update value at the time of the event
     */
    void onViolate(ThresholdRule threshold, long timestamp, double update);

}
