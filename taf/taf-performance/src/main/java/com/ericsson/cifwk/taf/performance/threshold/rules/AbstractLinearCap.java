package com.ericsson.cifwk.taf.performance.threshold.rules;

import com.ericsson.cifwk.taf.performance.threshold.ThresholdRule;

public abstract class AbstractLinearCap implements ThresholdRule {

    private final double originX;
    private final double originY;
    private final double slope;

    protected AbstractLinearCap(double originX, double originY, double slope) {
        this.originX = originX;
        this.originY = originY;
        this.slope = slope;
    }

    protected double valueAt(double x) {
        return (x - originX) * slope + originY;
    }

    protected String equation() {
        return String.format("(x - %.3f) * %.3f + %.3f", originX, slope, originY);
    }

}
