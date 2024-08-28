package com.ericsson.cifwk.taf.performance.threshold;

/**
 * Abstract metric check rule.
 */
public interface ThresholdRule {

    /**
     *
     * @param x timestamp
     * @param y metric value
     * @return true is threshold is reached
     */
    boolean check(double x, double y);

}
